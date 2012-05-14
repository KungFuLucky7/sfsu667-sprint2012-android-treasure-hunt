package sfsu.treasurehunt;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sfsu.treasurehunt.Tools.NetworkCall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/*
 * Sends a network call to the server to get a list of all active players.  List is converted
 * into a radio group from which the user will be able to select only one name as the target
 * of a tool.  Hitting the submit button will exist activity and return to the tools activity.
 */
public class SelectTargetActivity extends Activity {
	// Preferences file
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// Network related variables.
    private JSONObject responseJSON;
	
	// Layout objects.
	private Button selectTargetButton;
	
	// Global variables.
	private ArrayList<String> targetList = new ArrayList<String>();
	private RadioGroup radioGroup;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.target_main);

	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    
		String networkSend = "{";
    	networkSend += "\"playerID\":\"" + settings.getString("USERNAME", "NONE") + "\"";
    	networkSend += ", \"password\":\"" + settings.getString("PASSWORD", "NONE") + "\"";
    	networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
    	networkSend += ", \"option\":\"getPlayers\"";
    	networkSend += "}";
    	
    	Log.d("SelectTargetActivity", "Requesting active players.");
    	new NetworkCall().execute(networkSend);
    	// For debugging only.  By-passing network call.
    	//generateTargetList();
	}
	
	/*
	 * After pulling the active players list from the server, this method converts the list
	 * into a radio group.
	 */
	private void generateTargetList() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		String userName = settings.getString("USERNAME", "NONE");
	    radioGroup = (RadioGroup) findViewById(R.id.targetRadioGroup);

		// Go through JSON response from server to add all active players to the target list used for radio buttons.
		try {
			JSONArray activePlayersList = responseJSON.getJSONArray("players");
			for (int x = 0; x < activePlayersList.length(); x++) {
				if (!userName.contentEquals(activePlayersList.getString(x))) {
					targetList.add(activePlayersList.getString(x));
				}
			}
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
		
		// Build radio buttons.
		for (int x = 0; x < targetList.size(); x++) {
			RadioButton radioButton = new RadioButton(this);
			radioButton.setText(targetList.get(x));
			radioButton.setTextSize(40);
			radioButton.setTextColor(Color.BLACK);
			radioButton.setPadding(50, 0, 0, 10);
			radioButton.setId(x);
			radioGroup.addView(radioButton, x);
		}
		
		// Sets first player on radio list as checked.
		radioGroup.check(0);
		
		// Confirms that checked player on the radio button is target, ends activity and returns result.
		selectTargetButton = (Button) findViewById(R.id.selectTargetButton);
		selectTargetButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View view) {
     			Log.d("TargetPlayer", "Selected: " + targetList.get(radioGroup.getCheckedRadioButtonId()));
     			
     			Intent intent = new Intent();
     			intent.putExtra("TARGET", targetList.get(radioGroup.getCheckedRadioButtonId()));
     			setResult(RESULT_OK, intent);
     			finish();
     		}
     	});		
	}
	
	/*
	 * Makes a network request to the server.
	 */
    public class NetworkCall extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(SelectTargetActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Updating location....");
            Log.i("AsyncTask", "onPreExecute");
        }

        @Override
        protected String doInBackground(String... sendingInfo) {
          	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
           	String URL = settings.getString("SERVER", "Error");
 
        	/*
             * JSON for sending to server String stringToJson = "{\"playerID\":\"" + sendingInfo[0] + "\", \"latitude\":\"" + sendingInfo[2] + "\", \"longitude\":\"" + sendingInfo[3] + "\"}";
             */

        	String stringToJson = sendingInfo[0];
            JSONObject jsonToSend;
            try {
                jsonToSend = new JSONObject(stringToJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            HttpClient httpClient = new HttpClient();
            responseJSON = httpClient.httpPost(URL, jsonToSend);

            String responseString = responseJSON.toString();

            Log.i("NetworkCall", "doInBackgroup: " + URL);

            return responseString;
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
            generateTargetList();
        }
    }
}