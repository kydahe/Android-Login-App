package com.android.loginapp;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import com.android.database.DatabaseAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import okhttp3.OkHttpClient;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;



import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class UserProfileActivity extends Activity {

    private TextView userProfileName;
    private TextView userProfileEmail;
    private Button logoutButton;
    private Button updateProfileButton;
    private DatabaseAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile); // Assume this is your layout for user profile

        dbHelper = new DatabaseAdapter(this);
        dbHelper.open();

        initControls();
    }

    private void initControls() {
        userProfileName = (TextView) findViewById(R.id.userProfileName);
        userProfileEmail = (TextView) findViewById(R.id.userProfileEmail);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        updateProfileButton = (Button) findViewById(R.id.updateProfileButton);

        loadUserProfile();

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to update profile activity
                Intent intent = new Intent(UserProfileActivity.this, UpdateProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserProfile() {
        SharedPreferences prefs = getSharedPreferences(login.MY_PREFS, MODE_PRIVATE);
        long userId = prefs.getLong("uid", 0);
        if (userId > 0) {
            Cursor cursor = dbHelper.fetchUserById(userId);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex(DatabaseAdapter.COL_NAME));
                String email = cursor.getString(cursor.getColumnIndex(DatabaseAdapter.COL_EMAIL));
                userProfileName.setText(name);
                userProfileEmail.setText(email);
                cursor.close();
            } else {
                Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences(login.MY_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(UserProfileActivity.this, login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}

public class login extends Activity {
	
	public static final String MY_PREFS = "SharedPreferences";
	private DatabaseAdapter dbHelper;
	private EditText theUsername;
	private EditText thePassword;
	private Button loginButton;
	private Button registerButton;
	private Button clearButton;
	private Button exitButton;
	private	CheckBox rememberDetails;
    private AdView mAdView;

    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private Button googleSignInButton;
	
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        SharedPreferences mySharedPreferences = getSharedPreferences(MY_PREFS, 0);
        Editor editor = mySharedPreferences.edit();
        editor.putLong("uid", 0);
        editor.commit();
        
        dbHelper = new DatabaseAdapter(this);
        dbHelper.open();
        
        setContentView(R.layout.main);
        initControls();
        MobileAds.initialize(this, initializationStatus -> {});

        loadAd();

		configureGoogleSignIn();
    }
    
    private void initControls() {
    	//Set the activity layout.
    	theUsername = (EditText) findViewById(R.id.Username);
    	thePassword = (EditText) findViewById(R.id.Password);
    	loginButton = (Button) findViewById(R.id.Login);
    	registerButton = (Button) findViewById(R.id.Register);
    	clearButton = (Button) findViewById(R.id.Clear);
    	exitButton = (Button) findViewById(R.id.Exit);
    	rememberDetails = (CheckBox) findViewById(R.id.RememberMe);
		googleSignInButton = findViewById(R.id.googleSignInButton);
    	
    	//Create touch listeners for all buttons.
    	loginButton.setOnClickListener(new Button.OnClickListener(){
    		public void onClick (View v){
    			LogMeIn(v);
    		}
    	});
    	
    	registerButton.setOnClickListener(new Button.OnClickListener(){
    		public void onClick (View v){
    			Register(v);
    		}
    	});
    	
    	clearButton.setOnClickListener(new Button.OnClickListener(){
    		public void onClick (View v){
    			ClearForm();
    		}
    	});
    	
    	exitButton.setOnClickListener(new Button.OnClickListener(){
    		public void onClick (View v){
    			Exit();
    		}
    	});
    	//Create remember password check box listener.
    	rememberDetails.setOnClickListener(new CheckBox.OnClickListener(){
    		public void onClick (View v){
    			RememberMe();
    		}
    	});

		googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    	
    	//Handle remember password preferences.
    	SharedPreferences prefs = getSharedPreferences(MY_PREFS, 0);
    	String thisUsername = prefs.getString("username", "");
    	String thisPassword = prefs.getString("password", "");
    	boolean thisRemember = prefs.getBoolean("remember", false);
    	if(thisRemember) {
    		theUsername.setText(thisUsername);
    		thePassword.setText(thisPassword);
    		rememberDetails.setChecked(thisRemember);
    	}
    	
    }
    
    /**
     * Deals with Exit option - exits the application.
     */
    private void Exit()
    {
    	finish();
    }
    
	
    /**
     * Clears the login form.
     */
    private void ClearForm() {
    	saveLoggedInUId(0,"","");
    	theUsername.setText("");
    	thePassword.setText("");
    }
    
    /**
     * Handles the remember password option.
     */
    private void RememberMe() {
    	boolean thisRemember = rememberDetails.isChecked();
    	SharedPreferences prefs = getSharedPreferences(MY_PREFS, 0);
    	Editor editor = prefs.edit();
    	editor.putBoolean("remember", thisRemember);
    	editor.commit();
    }
    
	private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

	private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            Log.w("Google Sign In Error", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(login.this, "Failed to sign in with Google.", Toast.LENGTH_LONG).show();
            updateUI(null);
        }
    }

	private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            String googleUsername = account.getDisplayName(); // Or getEmail(), getId(), etc.
            String googleEmail = account.getEmail();
            // You can store the googleUsername or googleEmail in SharedPreferences or proceed with your app's login logic.

            Intent i = new Intent(login.this, Helloworld.class);
            startActivity(i);
            finish();
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(this, "Please sign in to continue.", Toast.LENGTH_SHORT).show();
        }
    }

	private void loadAd() {
        // Find the Ad Container
        LinearLayout adContainer = findViewById(R.id.adContainer);
        if (adContainer != null) {
            mAdView = new AdView(this);
            mAdView.setAdSize(AdSize.BANNER);
            mAdView.setAdUnitId("your-ad-unit-id-here"); // Use your real Ad unit ID here

            // Add the AdView to the view hierarchy
            adContainer.addView(mAdView);

            // Create an ad request
            AdRequest adRequest = new AdRequest.Builder().build();

            // Start loading the ad in the background
            mAdView.loadAd(adRequest);
        }
    }

    /**
     * This method handles the user login process.  
     * @param v
     */
    private void LogMeIn(View v) {
    	//Get the username and password
		boolean loginsuccess = false;
    	String thisUsername = theUsername.getText().toString();
    	String thisPassword = thePassword.getText().toString();
    	
    	//Assign the hash to the password
    	thisPassword = md5(thisPassword);
    	
    	// Check the existing user name and password database
    	Cursor theUser = dbHelper.fetchUser(thisUsername, thisPassword);
    	if (theUser != null) {
    		startManagingCursor(theUser);
    		if (theUser.getCount() > 0) {
    			saveLoggedInUId(theUser.getLong(theUser.getColumnIndex(DatabaseAdapter.COL_ID)), thisUsername, thePassword.getText().toString());
    		    stopManagingCursor(theUser);
    		    theUser.close();
    		    Intent i = new Intent(v.getContext(), Helloworld.class);
    		    startActivity(i);
				loginsuccess = true;
    		}
    		
    		//Returns appropriate message if no match is made
    		else {
    			Toast.makeText(getApplicationContext(), 
    					"You have entered an incorrect username or password.", 
    					Toast.LENGTH_SHORT).show();
    			saveLoggedInUId(0, "", "");
    		}
    		stopManagingCursor(theUser);
    		theUser.close();
    	}
    	
    	else {
    		Toast.makeText(getApplicationContext(), 
    				"Database query error", 
    				Toast.LENGTH_SHORT).show();
    	}

		if (loginsuccess == false){
			performNetworkLogin(thisUsername, thisPassword);
		}
    }


	private void performNetworkLogin(String username, String password) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... voids) {
				try {
					OkHttpClient client = HttpsClient.getHttpsClient(getApplicationContext());
					RequestBody formBody = new FormBody.Builder()
							.add("username", username)
							.add("password", password)
							.build();
					Request request = new Request.Builder()
							.url("https://run.mocky.io/v3/e7b8927b-eafc-42a1-a75f-f4f5b7650463")
							.post(formBody)
							.build();
					
					Response response = client.newCall(request).execute();
					// Assuming the server response includes a boolean indicating success
					return response.isSuccessful() && Boolean.parseBoolean(response.body().string());
				} catch (Exception e) {
					Log.e("LogMeIn", "Error verifying credentials over network", e);
					Log.e("LogMeIn", "Response message" + response.body().string());
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				if (success) {
					// If network verification successful, proceed with login
					saveLoggedInUId(1, username, password); // Assuming a temporary user ID of 1
					Intent i = new Intent(getApplicationContext(), Helloworld.class);
					startActivity(i);
				} else {
					// If network verification fails, show error message
					Toast.makeText(getApplicationContext(), "You have entered an incorrect username or password.", Toast.LENGTH_SHORT).show();
				}
			}
		}.execute();
	}


	@Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }


    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
    

    /**
     * Open the Registration activity.
     * @param v
     */
    private void Register(View v)
    {
    	Intent i = new Intent(v.getContext(), Register.class);
    	startActivity(i);
    }
    
    private void saveLoggedInUId(long id, String username, String password) {
    	SharedPreferences settings = getSharedPreferences(MY_PREFS, 0);
    	Editor myEditor = settings.edit();
    	myEditor.putLong("uid", id);
    	myEditor.putString("username", username);
    	myEditor.putString("password", password);
    	boolean rememberThis = rememberDetails.isChecked();
    	myEditor.putBoolean("rememberThis", rememberThis);
    	myEditor.commit();
    }
    
    /**
	 * Deals with the password encryption. 
	 * @param s The password.
	 * @return
	 */
    private String md5(String s) {
    	try {
    		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
    		digest.update(s.getBytes());
    		byte messageDigest[] = digest.digest();
    		
    		StringBuffer hexString = new StringBuffer();
    		for (int i=0; i<messageDigest.length; i++)
    			hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
    		
    		return hexString.toString();
    	} 
    	
    	catch (NoSuchAlgorithmException e) {
    		return s;
    	}
    }
}


