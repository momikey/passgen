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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PasswordViewer extends ListActivity {
	private ArrayList<String> passwordList;
	private ArrayAdapter<String> adapter;
	private boolean listChanged;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		passwordList = new ArrayList<String>();
		listChanged = false;
		TextView header = new TextView(this);
		getListView().addHeaderView(header);

		openFile();
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		for (String s : passwordList) {
			adapter.add(s);
		}
		
		setListAdapter(adapter);
		registerForContextMenu(getListView());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (listChanged) {
			FileOutputStream fos = null;
			try {
				fos = openFileOutput(PasswordGenerator.SAVED_PASSWORDS_FILE, Context.MODE_PRIVATE);
				for (String s : passwordList) {
					fos.write((s + "\n").getBytes());
				}
			} catch (FileNotFoundException e) {
				// log exception
			} catch (IOException e) {
				// log exception
			} finally {
				if (fos != null) {
					try {
						fos.flush();
						fos.close();
					} catch (IOException e) {
						// nothing to do
					}
				}
			}
			listChanged = false;
		}
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.savedlistcontext, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.ctxremove:
			menuRemove(item);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	
	private void openFile() {
		FileInputStream fis = null;
		try {
			fis = openFileInput(PasswordGenerator.SAVED_PASSWORDS_FILE);
			byte[] reader = new byte[fis.available()];
			fis.read(reader);
			String allPasswords = new String(reader);
			if (allPasswords.length() > 0) {
				passwordList.addAll(Arrays.asList(allPasswords.split("\n")));
			}
		} catch (FileNotFoundException e) {
			// Log exception
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void menuRemove(MenuItem item) {
		int id = (int) ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		passwordList.remove(id);
		adapter.remove(adapter.getItem(id));
		listChanged = true;
	}
}
