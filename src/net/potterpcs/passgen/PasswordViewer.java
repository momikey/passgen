package net.potterpcs.passgen;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class PasswordViewer extends ListActivity {
	private ArrayList<String> passwordList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		passwordList = new ArrayList<String>();

		openFile();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		for (String s : passwordList) {
			adapter.add(s);
		}
		
		setListAdapter(adapter);
	}
	
	private void openFile() {
		FileInputStream fis = null;
		try {
			fis = openFileInput(PasswordGenerator.SAVED_PASSWORDS_FILE);
			byte[] reader = new byte[fis.available()];
			fis.read(reader);
			String allPasswords = new String(reader);
			passwordList.addAll(Arrays.asList(allPasswords.split("\n")));
		} catch (FileNotFoundException e) {
			// Log exception
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
