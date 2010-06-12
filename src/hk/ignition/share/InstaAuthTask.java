package hk.ignition.share;

import hk.ignition.commons.Instapaper;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class InstaAuthTask extends AsyncTask<Void, Void, Boolean> {
	private String username;
	private String password;
	private Context context;
	
	private ProgressDialog dialog;
	
	public InstaAuthTask(Context context, String username, String password) {
		this.context = context;
		this.username = username;
		this.password = password;
	}
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		Instapaper instapaper = new Instapaper(username, password);
		try {
			return instapaper.authenticate();			
		} catch (Exception e) {
			Log.e("InstaAuthTask", "error connecting instapaper", e);
			return Boolean.FALSE;
		}
	}
	
	@Override
	protected void onPreExecute() {
		dialog = new ProgressDialog(context);
		dialog.setTitle(context.getString(R.string.msg_loading_title));
		dialog.setMessage(context.getString(R.string.msg_instapapaer_auth));
		dialog.setCancelable(false);
		dialog.setIndeterminate(true);
		dialog.show();
	}

	@Override
	protected void onCancelled() {
		dialog.dismiss();
	}

	@Override
	protected void onPostExecute(Boolean result) {
		dialog.dismiss();
	}
}
