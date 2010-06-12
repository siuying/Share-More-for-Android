package hk.ignition.share;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConfigurationActivity extends PreferenceActivity {
	public static final String ENABLE_BITLY = "bitly.enable";
	
	@Override
	public void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		
		addPreferencesFromResource(R.xml.perference);
    }
}
