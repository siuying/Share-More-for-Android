package hk.ignition.share;

import hk.ignition.commons.Instapaper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class ConfigurationActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = "ConfigurationActivity";
	public static final String ENABLE_BITLY = "bitly.enable";
	public static final String ENABLE_INSTA_USERNAME = "instapaper.username";
	public static final String ENABLE_INSTA_PASSWORD = "instapaper.password";

	private EditTextPreference instaUserPref;
	private EditTextPreference instaPassPref;


	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		
		addPreferencesFromResource(R.xml.perference);
		
		instaUserPref = (EditTextPreference) getPreferenceScreen().findPreference(ENABLE_INSTA_USERNAME);
		instaPassPref = (EditTextPreference) getPreferenceScreen().findPreference(ENABLE_INSTA_PASSWORD);
    }
	
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        instaUserPref.setSummary(instaUserPref.getText());
		if (instaPassPref.getText() == null || "".equals(instaPassPref.getText())) {
			instaPassPref.setSummary("");
		} else {
			instaPassPref.setSummary("******");
		}
        
		// Set up a listener whenever a key changes            
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }


	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		String username = instaUserPref.getText();
		String password = instaPassPref.getText();
		
		if (key.equals(ENABLE_INSTA_USERNAME)) {
			instaUserPref.setSummary(username);
		}

		if (key.equals(ENABLE_INSTA_PASSWORD)) {
			if (password == null || "".equals(password)) {
				instaPassPref.setSummary("");
			} else {
				instaPassPref.setSummary("******");
			}			
		}
		
		if (key.equals(ENABLE_INSTA_USERNAME) || key.equals(ENABLE_INSTA_PASSWORD)) {
			if (username == null || "".equals(username) ||
				password == null || "".equals(password)) {
				Toast.makeText(this, 
						getString(R.string.msg_instapapaer_nopass), 
						Toast.LENGTH_LONG);
				return;
			}
			
			Instapaper instapaper = new Instapaper(username, password);
			try {
				if (!instapaper.authenticate()) {
					Log.e(TAG, "failed to authenticate instapaper");
					Toast.makeText(this, 
							getString(R.string.msg_instapapaer_errauth), 
							Toast.LENGTH_LONG);
					return;
				}
			} catch (Exception e) {
				Log.e(TAG, "error connecting instapaper", e);
				Toast.makeText(this, 
						getString(R.string.msg_instapapaer_errconn), 
						Toast.LENGTH_LONG);
				return;
			}
		}

	}
}
