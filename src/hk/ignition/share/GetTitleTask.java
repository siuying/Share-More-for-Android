package hk.ignition.share;

import hk.ignition.commons.Instapaper;
import hk.ignition.commons.InstapaperException;
import hk.reality.util.WebUtil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.finnjohnsen.bitlyandroid.test.BitlyAndroid;

public abstract class GetTitleTask extends AsyncTask<Void, String, String> {
	private static final String TAG = "GetTitleTask";
	private ProgressDialog dialog;
	
	private String title;
	private Activity context;
	private BitlyAndroid bitly;
	private SharedPreferences pref;

	public GetTitleTask(Activity context) {
		this.context = context;
		this.bitly = new BitlyAndroid(context.getString(R.string.bitly_api_user), 
				context.getString(R.string.bitly_api_key));
		this.pref = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	protected String doInBackground(Void... arg0) {
		String url = StringUtils.defaultString(context.getIntent().getExtras().getString(Intent.EXTRA_TEXT));
		
		// send to instapaper
		if (isEnabledInstapaper() && !"".equals(getInstaUsername())) {
			Instapaper instapaper = new Instapaper(getInstaUsername(), getInstaPassword());
			try {
				publishProgress(context.getString(R.string.msg_save_instapaper));
				instapaper.add(url);
			} catch (InstapaperException e) {
				Log.e(TAG, 
						"failed adding to instapaper, code=" + e.getCode(), 
						e);
			} catch (ClientProtocolException e) {
				Log.e(TAG, 
						"error contact instapaper", 
						e);
			} catch (IOException e) {
				Log.e(TAG, 
						"error contact instapaper", 
						e);
			}
		}

		// shorten URL
		if (isEnabledBitly()) {
			Log.i(TAG, "use bitly to shorten url");
			try {
				publishProgress(context.getString(R.string.msg_shorten_url));
				url = shortenAllUrl(url);
			} catch (Exception e) {
				Log.e(TAG, "Failed to shorten url using Bitly", e);
			}
		}
		
		// find subject
		Log.i(TAG, "find subject from URL");		
		publishProgress(context.getString(R.string.msg_find_subject));
		String subject = context.getIntent().getExtras().getString(Intent.EXTRA_SUBJECT);
		if (StringUtils.isEmpty(subject)) {
			subject = findTitleFromUrl(url);
		}
		title = String.format("%s %s", subject, url);
		
		Log.i(TAG, "shared text: " + title);
		return title;
	}
	
	@Override
	protected void onProgressUpdate(String... values) {
		dialog.setMessage(values[0]);
	}

	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(context.getString(R.string.msg_loading_title));
		dialog.setCancelable(false);
		dialog.setIndeterminate(true);
		dialog.show();
	}

	@Override
	protected void onCancelled() {
		dialog.dismiss();
	}

	@Override
	protected void onPostExecute(String newText) {
		dialog.dismiss();
	}

	/**
	 * Replace all url from text with shorten URL
	 * 
	 * @param text
	 * @return
	 * @throws Exception 
	 */
	private String shortenAllUrl(String text) throws Exception {
		Pattern p = Pattern.compile("(https?://[^ ]+)");
		Matcher m = p.matcher(text);

		StringBuffer buffer = new StringBuffer();
		while(m.find()) {
			 String url = m.group(1);
			 String shortenUrl = bitly.getShortUrl(url);
			 
			 Log.d(TAG, "url = " + url + ", shortenUrl = " + shortenUrl);
			 m.appendReplacement(buffer, shortenUrl);
		}

		return buffer.toString();
	}
	
	private String findTitleFromUrl(String url) {
		if (url.startsWith("http")) {			
			try {
				String html = WebUtil.fetch(url);
				if (StringUtils.isEmpty(html)) {
					Log.d(TAG, "cannot fetch web page");
					createErrorMessage();
					return "";
				}
				
				HtmlCleaner cleaner = getHtmlCleaner();
				TagNode document = cleaner.clean(html);
				
		        String xpath = "//title";
		        Object[] xpathResult = document.evaluateXPath(xpath);
		        for(Object o : xpathResult) {
		            if (o instanceof TagNode) {
		                TagNode contentNode = (TagNode) o;
		                if (StringUtils.equals("title", contentNode.getName())) {
		                    String label = StringUtils.trim(contentNode.getText().toString());
		                    return label;
		                }
		            }
		        }				
			} catch (ClientProtocolException e) {
				Log.e(TAG, "protocol error", e);
				createErrorMessage();
			} catch (IOException e) {
				Log.e(TAG, "error download link", e);
				createErrorMessage();
			} catch (XPatherException e) {
				Log.e(TAG, "xpath error", e);
				createErrorMessage();
			}
			return "";
		} else {
			return "";
		}
	}

	private HtmlCleaner getHtmlCleaner() {
		HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties prop = cleaner.getProperties();
        prop.setOmitDoctypeDeclaration(true);
        prop.setOmitUnknownTags(false);
        prop.setOmitComments(false);
        prop.setIgnoreQuestAndExclam(false);
        prop.setOmitDeprecatedTags(false);
        prop.setOmitXmlDeclaration(true); 
        prop.setAdvancedXmlEscape(true);
        prop.setRecognizeUnicodeChars(true);
        prop.setOmitHtmlEnvelope(false);        
        prop.setUseCdataForScriptAndStyle(true);
        return cleaner;
	}

	private void createErrorMessage() {
		Toast.makeText(context, R.string.error_find_title, Toast.LENGTH_LONG).show();
	}
	
	private boolean isEnabledBitly() {
		return pref.getBoolean(ConfigurationActivity.ENABLE_BITLY, true);
	}

	private boolean isEnabledInstapaper() {
		return pref.getBoolean(ConfigurationActivity.ENABLE_INSTA, true);
	}
	
	private String getInstaUsername() {
		return pref.getString(ConfigurationActivity.ENABLE_INSTA_USERNAME, "");
	}
	
	private String getInstaPassword() {
		return pref.getString(ConfigurationActivity.ENABLE_INSTA_PASSWORD, "");
	}
}
