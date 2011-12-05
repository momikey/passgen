package net.potterpcs.passgen;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.InputType;
import android.widget.EditText;

public class PasswordPreferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.options);
		
		EditText minText = ((EditTextPreference) findPreference(PasswordGenerator.PREFS_KEY_MINLENGTH)).getEditText();
		minText.setInputType(InputType.TYPE_CLASS_NUMBER);
		EditText maxText = ((EditTextPreference) findPreference(PasswordGenerator.PREFS_KEY_MAXLENGTH)).getEditText();
		maxText.setInputType(InputType.TYPE_CLASS_NUMBER);
	}
}
