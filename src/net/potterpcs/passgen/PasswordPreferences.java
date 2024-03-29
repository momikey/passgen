/*   Copyright 2011 Michael Potter

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

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
