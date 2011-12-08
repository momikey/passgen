package net.potterpcs.passgen;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PasswordGenerator extends Activity {
	Resources resources;
	SharedPreferences preferences;
	SpannableString oldPassword;
	String possibleChars;
	String genPassword;
	Random rng;

	// This controls how fast the animation will be, and how long it will take.
	// Longer passwords will usually take longer to animate.
	static final long THREAD_SLEEP_MILLIS = 30;

	// These are the minimum and maximum lengths for passwords. They are only
	// used until the user sets a length in the preferences.
	static final int MIN_PW_LENGTH = 6;
	static final int MAX_PW_LENGTH = 10;

	// This is the name of the file where passwords are saved. The least we can
	// do is make it hidden until we can get some encryption or something.
	static final String SAVED_PASSWORDS_FILE = ".saved.txt";	

	// Bundle tags
	static final String THREAD_PW_KEY = "password";
	static final String THREAD_PW_CHARS_KEY = "numchars";
	static final String PREFS_KEY_MINLENGTH = "minlength";
	static final String PREFS_KEY_MAXLENGTH = "maxlength";
	static final String PREFS_KEY_UPPERCASE = "uppercase";
	static final String PREFS_KEY_NUMBERS = "numbers";
	
	private TextView passwordText;
	private PasswordSpinner passThread;
	private Handler passHandler;
	private Button skipButton;
	private TextView generatingText;
	private boolean saved;
	
	boolean threadAlive;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        resources = getResources();
        
        skipButton = (Button) findViewById(R.id.skipbutton);
        generatingText = (TextView) findViewById(R.id.generatingtext);
        passwordText = (TextView) findViewById(R.id.passwordtext);

        skipButton.setVisibility(View.INVISIBLE);
        generatingText.setVisibility(View.INVISIBLE);
        passwordText.setVisibility(View.INVISIBLE);
        passwordText.setText(passwordText.getText(), TextView.BufferType.SPANNABLE);
        
        rng = new SecureRandom();
        
        // This is our Handler; all it really does is update the password text
        // with the latest "generated" password every few milliseconds.
        // (Spoiler: When a character of the "generated" password matches the
        // same position in the real password, it lights up in red and freezes.
        // This serves no useful purpose, but it looks like something from
        // WarGames, and that's really the whole point of the app.)
        
        passHandler = new Handler() {
        	public void handleMessage(Message msg) {
        		Bundle b = msg.getData();
        		String pw = b.getString(THREAD_PW_KEY);
        		SpannableString span = new SpannableString(pw);
        		for (int i = 0; i < pw.length(); ++i) {
        			span.setSpan(new ForegroundColorSpan(
        					pw.charAt(i) == genPassword.charAt(i) ? Color.RED : Color.GRAY),
        					i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        		}
        		passwordText.setText(span, TextView.BufferType.SPANNABLE);
        		
        		if (passwordText.getVisibility() != View.VISIBLE) {
        			passwordText.setVisibility(View.VISIBLE);
        		}
        		
        		if (pw.contentEquals(genPassword)) {
        			skipButton.setVisibility(View.INVISIBLE);
        			generatingText.setVisibility(View.INVISIBLE);
        		}
        	}
        };
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	oldPassword = (SpannableString) passwordText.getText();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if (oldPassword != null) {
    		passwordText.setText(oldPassword, TextView.BufferType.SPANNABLE);
    	}
    	preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }
    
    public void generatePassword(View v) {
    	// This is where we actually generate a password, not just put on a show.
    	// Basically, we figure out which characters are allowed, based on the
    	// user's preferences. Next, we select a length, again between the min
    	// and max from preferences. Then, we choose one of the allowed characters
    	// for each position, and there you have it: a random password.
    	
    	int max = Integer.parseInt(preferences.getString(PREFS_KEY_MAXLENGTH, Integer.toString(MAX_PW_LENGTH)));
    	int min = Integer.parseInt(preferences.getString(PREFS_KEY_MINLENGTH, Integer.toString(MIN_PW_LENGTH)));
    	int pwLength = min + rng.nextInt(max - min + 1);
    	StringBuilder pcs = new StringBuilder(resources.getString(R.string.lowercase));
    	if (preferences.getBoolean(PREFS_KEY_UPPERCASE, true)) {
    		pcs.append(resources.getString(R.string.uppercase));
    	}
    	if (preferences.getBoolean(PREFS_KEY_NUMBERS, true)) {
    		pcs.append(resources.getString(R.string.numbers));
    	}
    	// TODO: Add in symbols, etc.
    	
    	possibleChars = pcs.toString();
    	char[] newPassword = new char[pwLength];
    	for (int i = 0; i < pwLength; ++i) {
    		char nextChar = possibleChars.charAt(rng.nextInt(possibleChars.length()));
    		newPassword[i] = nextChar;
    	}
    	genPassword = new String(newPassword);
    	saved = false;

    	if (!threadAlive) {
    		// Start up a thread to do the hard work of generating passwords.
    		// We won't use any of these generated passwords for anything
    		// except the movie-style animation.
    		
    		passThread = new PasswordSpinner();
    		passThread.doSetup(passHandler, genPassword);
    		skipButton.setVisibility(View.VISIBLE);
    		generatingText.setVisibility(View.VISIBLE);
    		threadAlive = true;
    		passThread.start();
    	}
    }
    
    public void passwordOptions(View v) {
    	startActivity(new Intent(this, PasswordPreferences.class));
    }
    
    public void skipAnimation(View v) {
    	threadAlive = false;
    }
    
    public void savePassword(View v) {
    	// Save this password to the list.
    	// TODO: The password list needs to be something more secure than a
    	// plain text file.
    	
    	FileOutputStream fos = null;
    	try {
    		if (!saved) {
    			fos = openFileOutput(SAVED_PASSWORDS_FILE, Context.MODE_APPEND);
    			fos.write((genPassword + "\n").getBytes());
    			saved = true;
    		}
    	} catch (FileNotFoundException e) {
    		// Log exception
    	} catch (IOException e) {
    		// Log exception
    	} finally {
    		if (fos != null) {
    			try {
    				fos.flush();
    				fos.close();
    			} catch (IOException e) {
    				// do nothing
    			}
    		}
    	}
    }
    
    public void viewPasswords(View v) {
    	startActivity(new Intent(this, PasswordViewer.class));
    }
    
    private class PasswordSpinner extends Thread {
    	Handler handler;
    	String password;

		public void doSetup(Handler h, String pw) {
			handler = h;
			password = pw;
		}

		public void run() {
			// This is to make a nice animation of "generating" the password,
			// even though it's really already generated. Sure, we *could*
			// simply make the password and show it, but that's not really
			// worthy of an app. (The user can skip the animation if they're
			// in a hurry, though.)

			if (password != null) {
				char[] animatedPassword = new char[password.length()];
				String apString = new String(animatedPassword);
				int charsSoFar = 0;

				while (!apString.equals(password)) {
					if (!threadAlive) {
						Bundle fb = new Bundle();
						fb.putString(THREAD_PW_KEY, password);
						fb.putInt(THREAD_PW_CHARS_KEY, password.length());
						Message fm = handler.obtainMessage();
						fm.setData(fb);
						handler.sendMessage(fm);
						return;
					}

					// Animation Step: Generate a password, just like we did for
					// the real one. We'll give that password to the Handler, and
					// it will do the work of lighting up the correct characters.
					charsSoFar = 0;
					for (int i = 0; i < animatedPassword.length; ++i) {
						if (animatedPassword[i] != password.charAt(i)) {
							char c = possibleChars.charAt(rng.nextInt(possibleChars.length()));
							animatedPassword[i] = c;
							++charsSoFar;
						}
					}
					apString = new String(animatedPassword);

					if (password.contentEquals(apString)) {
						threadAlive = false;
					}

					Bundle b = new Bundle();
					b.putString(THREAD_PW_KEY, apString);
					b.putInt(THREAD_PW_CHARS_KEY, charsSoFar);

					Message m = handler.obtainMessage();
					m.setData(b);
					handler.sendMessage(m);

					try {
						sleep(THREAD_SLEEP_MILLIS);
					} catch (InterruptedException e) {
						threadAlive = false;
						continue;
					}
				}
			}
		}
    }
}