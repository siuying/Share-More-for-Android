package hk.ignition.share;

import hk.reality.util.WebUtil;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

public class ShareActivity extends Activity {
	private static final String TAG = "ShareActivity";
	private GetTitleTask task;
	private String title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTheme(android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

		if (title == null) {
			Log.d(TAG, "finding title for url");
			task = new GetTitleTask();
			task.execute();
		} else {
			Log.d(TAG, "share with title: " + title);
			shareWithTitle(title);
			finish();
		}
	}
		
	@Override
	protected void onDestroy() {
		if (task != null) task.cancel(true);
		super.onDestroy();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  savedInstanceState.putString("share.title", title);
	  super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  title = savedInstanceState.getString("share.title");
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
	
	private void shareWithTitle(String title) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, title);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, null));	
	}
	
	private void createErrorMessage() {
		Toast.makeText(this, R.string.error_find_title, Toast.LENGTH_LONG).show();
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
	
	class GetTitleTask extends AsyncTask<Void, Void, String> {
		private ProgressDialog dialog;
		private boolean cancelled = false;
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			cancelled = true;
		}

		@Override
		protected String doInBackground(Void... arg0) {
			String text = StringUtils.defaultString(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
			String subject = getIntent().getExtras().getString(Intent.EXTRA_SUBJECT);
			if (StringUtils.isEmpty(subject)) {
				subject = findTitleFromUrl(text);
			}

			title = String.format("%s %s", subject, text);
			Log.i(TAG, "shared text: " + title);
			return title;
		}

		@Override
		protected void onPostExecute(String newText) {
			dialog.dismiss();
			if (!cancelled) {
				try {
					shareWithTitle(newText);
				} catch (RuntimeException re) {
					Log.e(TAG, "error opening dialog", re);
				}
			}
			finish();
		}

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(ShareActivity.this);
			dialog.setTitle("Loading");
			dialog.setMessage("Finding Page Title... Please wait");
			dialog.setCancelable(false);
			dialog.setIndeterminate(true);
			dialog.show();
		}
	}
}
