package hk.ignition.share;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

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
			task = new MyGetTitleTask();
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

	private void shareWithTitle(String title) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_TEXT, title);
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, null));	
	}
	
	class MyGetTitleTask extends GetTitleTask {
		private boolean cancelled = false;
		
		public MyGetTitleTask() {
			super(ShareActivity.this);
		}
		@Override
		protected void onCancelled() {
			super.onCancelled();
			cancelled = true;
		}

		@Override
		protected void onPostExecute(String newText) {
			super.onPostExecute(newText);
			try {
				if (!cancelled) {
					shareWithTitle(newText);
				}
				finish();
			} catch (RuntimeException re) {
				Log.e(TAG, "error opening dialog", re);
			}
		}

	}
}
