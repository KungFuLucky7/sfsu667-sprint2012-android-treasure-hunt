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
import android.widget.Toast;

public class LoginActivity extends Activity {
	// Preferences file
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// Main login screen buttons
    private Button settings;
    private Button action;
    
    // Login screen buttons and images.
    private ImageView parchment;
    private ImageView settingsFrame;
    private EditText userNameBox;
    private EditText passwordBox;
    private Button cancel;
    
    private String userName;
    private String password;
    private int balance = 1234;
    private boolean loginState = false;
    private boolean isUserLoggedIn = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        
        parchment = (ImageView) findViewById(R.id.parchmentImage);
        settingsFrame = (ImageView) findViewById(R.id.settingsFrame);
        userNameBox = (EditText) findViewById(R.id.userNameBox);
        passwordBox = (EditText) findViewById(R.id.passwordBox);
        cancel = (Button) findViewById(R.id.cancelButton);
        cancel.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			cancel.setBackgroundResource(R.drawable.wooden_frame_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			cancel.setBackgroundResource(R.drawable.wooden_frame);
        				closeLoginScreen();
        		}
        		return false;
         	}
     	});
        
        action = (Button) findViewById(R.id.actionButton);
        action.setText("Login");
        action.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			action.setBackgroundResource(R.drawable.wooden_frame_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			action.setBackgroundResource(R.drawable.wooden_frame);
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
        
        settings = (Button) findViewById(R.id.settingsButton);
        settings.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			settings.setBackgroundResource(R.drawable.settings_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			settings.setBackgroundResource(R.drawable.settings);
        			settingsScreen();
        		}
        		return false;
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
		cancel.setVisibility(View.VISIBLE);
		loginState = true;
    }
    
    /*
     * Closes login screen and associated buttons.
     */
    private void closeLoginScreen() {
    	parchment.setVisibility(View.INVISIBLE);
		userNameBox.setVisibility(View.INVISIBLE);
		passwordBox.setVisibility(View.INVISIBLE);
		cancel.setVisibility(View.INVISIBLE);
		loginState = false;
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
	 * Processes what happens after returning from a network call.
	 */
	protected void onNetworkResult() {
		if(isUserLoggedIn) {
			action.setText("Start");
			Toast.makeText(this, "Log-In Succesful", Toast.LENGTH_SHORT).show();
			Log.d("Login", "Login to the server.");
			closeLoginScreen();
		} else {
			Toast.makeText(this, "Log-In Failed", Toast.LENGTH_SHORT).show();
			Log.d("Login", "Not Logged in to the server.");
		}
	}
    
    /*
     * Bring up settings screen and associated buttons.
     */
    private void settingsScreen() {
    	settingsFrame.setVisibility(View.VISIBLE);
    }
    
	/*
	 * Save preferences.
	 */
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("USERNAME", userName);
        editor.putInt("BALANCE", balance);
        editor.commit();
        Log.d("Login", "Setting USERNAME = " + userName);
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
             * 
             * String stringToJson = "{\"playerID\":\"" + sendingInfo[0] + "\", \"password\":\"" + sendingInfo[1]
             * 	+ "\", \"currentLocation\":\"0,0\", \"option\":\"signIn\"}";
             * 
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