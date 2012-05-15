package sfsu.treasurehunt;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	// Preferences file
	public static final String PREFS_NAME = "MyPrefsFile";
	
    // Network related variables.
    private JSONObject responseJSON;
	private int networkActivity;
	
	// Network result status codes.
	private static final int LOGIN = 0;
	private static final int LOGOUT = 1;
	private static final int RANKINGS = 2;
	private static final int STATS = 3;
	
	// Main login screen buttons
    private Button settingsButton;
    private Button actionButton;
    
    // Login screen buttons and images.
    private ImageView parchment;
    private EditText userNameBox;
    private EditText passwordBox;
    private Button cancelButton;
    private ProgressBar networkProgressBar;
    
    // Settings background and buttons.
    private ImageView settingsBackground;
    private TextView settingsTextBox;
    private Button logoutButton;
    private Button rankingsButton;
    private Button tutorialButton;
    
    // Global variables.
    private String userName = "";
    private String userPassword = "";
    private int balance = 0;
    private boolean loginState = false;
    private boolean isUserLoggedIn = false;
    private boolean isSettingsScreenOn = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        
        parchment = (ImageView) findViewById(R.id.parchmentImage);
        settingsBackground = (ImageView) findViewById(R.id.settingsBackgroundImage);
        userNameBox = (EditText) findViewById(R.id.userNameBox);
        passwordBox = (EditText) findViewById(R.id.passwordBox);
        networkProgressBar = (ProgressBar) findViewById(R.id.networkProgressBar);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (!settings.getString("USERNAME", "").contentEquals("")) {
        	userName = settings.getString("USERNAME", "");
        	balance = settings.getInt("BALANCE", -1);
        	loginState = true;
        	isUserLoggedIn = true;
        }
        
        // Cancels users attempt to login to server.
        cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			cancelButton.setBackgroundResource(R.drawable.wooden_frame_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			cancelButton.setBackgroundResource(R.drawable.wooden_frame);
        				closeLoginScreen();
        		}
        		return false;
         	}
     	});
        
        // Action performs login and start game functions.
        actionButton = (Button) findViewById(R.id.actionButton);
        if (loginState) {
        	actionButton.setText("Start");
        }
        else {
        	actionButton.setText("Login");
        }
        actionButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			actionButton.setBackgroundResource(R.drawable.wooden_frame_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			actionButton.setBackgroundResource(R.drawable.wooden_frame);
        			closeSettingsScreen();
        			if (isUserLoggedIn) {
        				savePreferences();
        				finish();
        			} else {
        				if (loginState) {
        					requestLogin();
        				} else {
        					openLoginScreen();
        				}
        			}
        		}
        		return false;
         	}
     	});
        
        // Open Settings Menu
        settingsButton = (Button) findViewById(R.id.settingsButton);
        settingsButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			settingsButton.setBackgroundResource(R.drawable.settings_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			settingsButton.setBackgroundResource(R.drawable.settings);
        			if (isSettingsScreenOn) {
        				isSettingsScreenOn = false;
        				closeSettingsScreen();
        			} else {
        				isSettingsScreenOn = true;
        				openSettingsScreen();
        			}
        		}
        		return false;
         	}
     	});
        
        // Logout from server.
        logoutButton = (Button) findViewById(R.id.logoutButton);
     	logoutButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View view) {
     			logoutFromServer();
     		}
     	});
     	
     	// Get rankings from the server and display them to the user.
        rankingsButton = (Button) findViewById(R.id.rankingsButton);
     	rankingsButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View view) {
     			getRankingsFromServer();
     		}
     	});
     	  
     	// Run tutorial screenshots.
        tutorialButton = (Button) findViewById(R.id.tutorialButton);
     	tutorialButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View view) {
     			runTutorial();
     		}
     	});
     	
     	/* Displays settings text box that shows settings messages like
         * Logout message, rankings, and any future messages for add-on.
         */
        settingsTextBox = (TextView) findViewById(R.id.settingsTextBox);
        settingsTextBox.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				settingsTextBox.setVisibility(View.INVISIBLE);
				openSettingsScreen();
			}
		});
    }
    
	@Override
	public void onBackPressed() {
		finish();
	}
	
    /*
     * Bring up login screen and associated buttons.
     */
    private void openLoginScreen() {
    	parchment.setVisibility(View.VISIBLE);
    	userNameBox.setVisibility(View.VISIBLE);
		passwordBox.setVisibility(View.VISIBLE);
		cancelButton.setVisibility(View.VISIBLE);
		loginState = true;
    }
    
    /*
     * Closes login screen and associated buttons.
     */
    private void closeLoginScreen() {
    	parchment.setVisibility(View.INVISIBLE);
		userNameBox.setVisibility(View.INVISIBLE);
		passwordBox.setVisibility(View.INVISIBLE);
		cancelButton.setVisibility(View.INVISIBLE);
		loginState = false;
    }
    
    /*
     * Bring up settings screen and associated buttons.
     */
    private void openSettingsScreen() {
    	settingsBackground.setVisibility(View.VISIBLE);
    	logoutButton.setVisibility(View.VISIBLE);
    	rankingsButton.setVisibility(View.VISIBLE);
    	tutorialButton.setVisibility(View.VISIBLE);
    }
    
    /*
     * Close settings screen and associated buttons.
     */
    private void closeSettingsScreen() {
    	settingsBackground.setVisibility(View.INVISIBLE);
    	logoutButton.setVisibility(View.INVISIBLE);
    	rankingsButton.setVisibility(View.INVISIBLE);
    	tutorialButton.setVisibility(View.INVISIBLE);
    }
    
    /*
     *   Takes user name and password, then sends a network request to the server
     *   for verification.
     */
    private void requestLogin() {
    	if(userNameBox.getText().toString().contentEquals("")) {
    		Toast.makeText(getBaseContext(), "Enter a user name.", Toast.LENGTH_LONG).show();
    		Log.d("Login", "Blank user name field. ");
    	} else if (passwordBox.getText().toString().contentEquals("")) {
    		Toast.makeText(this, "Enter a password.", Toast.LENGTH_LONG).show();
    		Log.d("Login", "Blank password field.");
    	} else {
    		userName = userNameBox.getText().toString();
    		userPassword = passwordBox.getText().toString();
    		Log.d("Login", "requestLogin: " + userName + " / " + userPassword);
    		
    		//String[] loginInfo = {userName, password};

    		// Debugging Hard-coded JSON
            //String stringToJson = "{\"playerID\":\"DF\", \"password\":\"testpass\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"signIn\"}";
    		networkActivity = LOGIN;
        	String networkSend = "{";
        	networkSend += "\"playerID\":\"" + userName + "\"";
        	networkSend += ", \"password\":\"" + userPassword + "\"";
        	networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
        	networkSend += ", \"option\":\"signIn\""; 
        	networkSend += "}";
    		new NetworkCall().execute(networkSend);
    	}
    }
    
    /*
	 * Save preferences.
	 */
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("USERNAME", userName);
        editor.putString("PASSWORD", userPassword);
        editor.putInt("BALANCE", balance);
        editor.commit();
        Log.d("Login", "Saving " + userName + " / " + userPassword + " with a balance of " + balance);
	}
	
	/*
	 * User logout from server.
	 */
	private void logoutFromServer() {
		closeSettingsScreen();
		settingsTextBox.setText("Logged Out.\n\n\nTouch to continue.");
		settingsTextBox.setVisibility(View.VISIBLE);
		
		userName = "";
		userPassword = "";
		balance = 0;
		savePreferences();
		
		//Below resetting of text should occur after network confirmation of logged procedure.
		actionButton.setText("Login");
		isUserLoggedIn = false;
		loginState = false;
	}
	
	/*
	 * Get rankings from server.
	 */
	private void getRankingsFromServer() {
		closeSettingsScreen();
		settingsTextBox.setText("Top 3.\n\n\nTouch to continue.");
		settingsTextBox.setVisibility(View.VISIBLE);
	}
	
    /*
     * Run the tutorial screen shots for the user.
     */
    private void runTutorial() {
    	closeSettingsScreen();
    	settingsTextBox.setText("Tutorial currently unavailable.\n\n\nTouch to continue.");
		settingsTextBox.setVisibility(View.VISIBLE);
    }
    
    /*
	 * Processes what happens after returning from a network call.
	 */
	protected void onNetworkResult() {
		try {
			switch (networkActivity) {
			case LOGIN:
	            isUserLoggedIn = responseJSON.getString("signIn").equalsIgnoreCase("Good");
	            
				if(isUserLoggedIn) {
					actionButton.setText("Start");
					Toast.makeText(this, "Log-In Succesful", Toast.LENGTH_SHORT).show();
					Log.d("Login", "Login to the server.");
			    	balance = Integer.valueOf(responseJSON.getString("playerPoints"));
					savePreferences();
					closeLoginScreen();
				} else {
					Toast.makeText(this, "Log-In Failed", Toast.LENGTH_SHORT).show();
					Log.d("Login", "Not Logged in to the server.");
				}
				break;
			}
		} catch (JSONException e) {
			Log.e("Login", "onNetworkResult -> JSON error: " + e);
		}
	}
	
	/*
	 * Makes a network request to the server.
	 */
    public class NetworkCall extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
        	networkProgressBar.setVisibility(View.VISIBLE);
            this.dialog.setMessage("Logging into game....");
            Log.i("AsyncTask", "onPreExecute");
        }

        @Override
        protected String doInBackground(String... sendingInfo) {
          	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
           	String URL = settings.getString("SERVER", "Error");
 
        	/*
             * JSON for sending to server
             * String stringToJson = "{\"playerID\":\"" + sendingInfo[0] + "\", \"password\":\"" + sendingInfo[1] + "\", \"currentLocation\":\"0,0\", \"option\":\"signIn\"}";
             */
           	
           	String stringToJson = sendingInfo[0];
            
            JSONObject jsonToSend;

            try {
                jsonToSend = new JSONObject(stringToJson);
                HttpClient httpClient = new HttpClient();

                responseJSON = httpClient.httpPost(URL, jsonToSend);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            Log.i("NetworkCall", "doInBackgroup: " + URL);

            return responseJSON.toString();
        }

        /*
         * On completion of Async task, the string pulled from the server is
         * saved in passData.
         */
        @Override
        protected void onPostExecute(String result) {
            if (this.dialog.isShowing()) {
                this.dialog.dismiss();
            }
            networkProgressBar.setVisibility(View.INVISIBLE);
            Log.d("Networking", "result = " + result);
            onNetworkResult();
        }
    }
}