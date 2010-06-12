package hk.ignition.share;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

public class ConfigurationActivity extends PreferenceActivity {
	public static final String ENABLE_BITLY = "bitly.enable";
	
	@Override
	public void onCreate(Bundle saveInstanceState) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(saveInstanceState);
		
		addPreferencesFromResource(R.xml.perference);
    }
}
