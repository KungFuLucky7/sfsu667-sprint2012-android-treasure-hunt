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
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	// Preferences file
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// Main login screen buttons
    private Button settingsButton;
    private Button actionButton;
    
    // Login screen buttons and images.
    private ImageView parchment;
    private EditText userNameBox;
    private EditText passwordBox;
    private Button cancelButton;
    
    // Settings background and buttons.
    private ImageView settingsBackground;
    private TextView settingsTextBox;
    private Button logoutButton;
    private Button rankingsButton;
    private Button tutorialButton;
    
    // Global variables.
    private String userName;
    private String password;
    private int balance = 1234;
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
        actionButton.setText("Login");
        actionButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			actionButton.setBackgroundResource(R.drawable.wooden_frame_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			actionButton.setBackgroundResource(R.drawable.wooden_frame);
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
    		password = passwordBox.getText().toString();
    		String[] loginInfo = {userName, password};
    		new NetworkCall().execute(loginInfo);
    	}
    }
    
    /*
	 * Save preferences.
	 */
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("USERNAME", userName);
        editor.putString("PASSWORD", password);
        editor.putInt("BALANCE", balance);
        editor.commit();
        Log.d("Login", "Setting USERNAME = " + userName);
	}
	
	/*
	 * User logout from server.
	 */
	private void logoutFromServer() {
		Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_SHORT);

		//Below reseting of text should occur after network confirmation of logged procedure.
		actionButton.setText("Login");
	}
	
	/*
	 * Get rankings from server.
	 */
	private void getRankingsFromServer() {
		Toast.makeText(getBaseContext(), "Top 3.", Toast.LENGTH_SHORT);
	}
	
    /*
     * Run the tutorial screen shots for the user.
     */
    private void runTutorial() {
    	Toast.makeText(this, "Tutorial currently unavailable.", Toast.LENGTH_LONG);
    }
    
    /*
	 * Processes what happens after returning from a network call.
	 */
	protected void onNetworkResult() {
		if(isUserLoggedIn) {
			actionButton.setText("Start");
			Toast.makeText(this, "Log-In Succesful", Toast.LENGTH_SHORT).show();
			Log.d("Login", "Login to the server.");
			closeLoginScreen();
		} else {
			Toast.makeText(this, "Log-In Failed", Toast.LENGTH_SHORT).show();
			Log.d("Login", "Not Logged in to the server.");
		}
	}
	
	/*
	 * Makes a network request to the server.
	 */
    public class NetworkCall extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {
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

            // Debugging Hard-coded JSON
            String stringToJson = "{\"playerID\":\"DF\", \"password\":\"testpass\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"signIn\"}";
            
            JSONObject jsonToSend;
            JSONObject responseJSON;
            String signInStatus;
            try {
                jsonToSend = new JSONObject(stringToJson);
                HttpClient httpClient = new HttpClient();

                responseJSON = httpClient.httpPost(URL, jsonToSend);
                signInStatus = responseJSON.getString("signIn");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            Log.i("NetworkCall", "doInBackgroup: " + URL);

            return signInStatus;
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
            Log.d("Networking", "result = " + result);
            isUserLoggedIn = result.equalsIgnoreCase("Good");
            onNetworkResult();
        }
    }
}