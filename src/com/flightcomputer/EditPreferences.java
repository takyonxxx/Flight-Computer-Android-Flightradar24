package com.flightcomputer;
import android.os.Bundle;
import android.preference.PreferenceActivity;
public class EditPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}	
}
