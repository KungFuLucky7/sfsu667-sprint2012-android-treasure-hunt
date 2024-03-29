package sfsu.treasurehunt;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/*
 * Tools menu. A list of tools you can purchase is shown on the screen.  When user selects
 * a tool on a particular line, a tool information image pops up with a "buy" button.  User
 * can tap on anywhere on the image to make it and the "buy" button disappear or hit the
 * button to purchase the item.  Addition actions may occur depending on the tool purchased
 * before sending the request to the server. 
 */
public class Tools extends Activity {
	// Preferences file
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// Network related variables.
    private JSONObject responseJSON;
	private int networkActivity;
	private String networkSend = "";
 
    // Network result status codes.
 	private static final int PURCHASE_TAUNT = 0;
 	private static final int PURCHASE_MONKEY = 1;
 	private static final int PURCHASE_SMOKE = 2;
 	private static final int PURCHASE_CLEARSKY = 3;
 	private static final int PURCHASE_COMPASS = 4;
 	private static final int PURCHASE_STEAL = 5;
 	private static final int PURCHASE_LOCKOUT = 6;
 	
 	// Activity Call IDs.
 	private static final int SELECT_TARGET = 0;
 	
	// Layout objects.
	private Button returnToMap;
	private Button buy;
	private Button sendTaunt;
	private ImageView helpScreen;
	private ImageView completePurchaseScreen;
	private EditText tauntScreen;
	private TextView balanceText;
	private static ProgressBar networkProgressBar;
	
	// Static tools guide.
	private static final int Taunt = 0;
	private static final int Monkey = 1;
	private static final int SmokeScreen = 2;
	private static final int ClearSky = 3;
	private static final int Compass = 4;
	private static final int Stealer = 5;
	private static final int Lockout = 6;

    // Tools Activity global variables.
	private ListView listView1;
	private ArrayList<ToolList> toolListData = new ArrayList<ToolList>();
	private ArrayList<Integer> toolPriceList = new ArrayList<Integer>();
	private int purchaseItem;
	final Context context = this;
	private String userName = "";
	private String userPassword = "";
	private int balance = 0;
	private boolean waitStatus = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools_main);
		setTitle(R.string.tools);

		buy = (Button) findViewById(R.id.buyButton);
		tauntScreen = (EditText) findViewById(R.id.tauntText);
		balanceText = (TextView) findViewById(R.id.balanceText);
		helpScreen = (ImageView) findViewById(R.id.helpScreen);
		networkProgressBar = (ProgressBar) findViewById(R.id.networkProgressBar);
		
		getPreferences();

		// Add tools to the tools list.
		toolListData.add(new ToolList(R.drawable.laugh, "Taunt", 10));
		toolListData.add(new ToolList(R.drawable.monkey, "Dizzy Monkey", 100));
		toolListData.add(new ToolList(R.drawable.smoke, "Smoke Screen", 500));
		toolListData.add(new ToolList(R.drawable.clearsky, "Clear Sky", 700));
		toolListData.add(new ToolList(R.drawable.compass, "Compass", 700));
		toolListData.add(new ToolList(R.drawable.thief, "Stealer", 900));
		toolListData.add(new ToolList(R.drawable.lockout, "Lockout", 950));
		
		toolPriceList.add(10);
		toolPriceList.add(100);
		toolPriceList.add(500);
		toolPriceList.add(700);
		toolPriceList.add(700);
		toolPriceList.add(900);
		toolPriceList.add(950);

		ToolListAdapter adapter = new ToolListAdapter(this,	R.layout.listview_item_row, toolListData);

		listView1 = (ListView) findViewById(R.id.list);

		//View header = (View)
		//getLayoutInflater().inflate(R.layout.listview_header_row, null);
		//listView1.addHeaderView(header);

		listView1.setAdapter(adapter);

		// Handles the different tool help screens.
		listView1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case Taunt:
					helpScreen.setImageResource(R.drawable.info_taunt);
					break;
				case Monkey:
					helpScreen.setImageResource(R.drawable.info_monkey);
					break;
				case SmokeScreen:
					helpScreen.setImageResource(R.drawable.info_smokescreen);
					break;
				case ClearSky:
					helpScreen.setImageResource(R.drawable.info_clearsky);
					break;
				case Compass:
					helpScreen.setImageResource(R.drawable.info_compass);
					break;
				case Stealer:
					helpScreen.setImageResource(R.drawable.info_stealer);
					break;
				case Lockout:
					helpScreen.setImageResource(R.drawable.info_lockout);
					break;
				}

				helpScreen.setVisibility(View.VISIBLE);
				helpScreen.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						helpScreen.setVisibility(View.GONE);
						buy.setVisibility(View.GONE);
					}
				});

				purchaseItem = position; // Global variable set for what tool is selected.
				buy.setVisibility(View.VISIBLE);
				buy.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						confirmToolPurchase();
					}
				});
				// Toast.makeText(getApplicationContext(), toolListData.get(position-1).title, Toast.LENGTH_LONG).show();
			}
		});

		returnToMap = (Button) findViewById(R.id.mapButton);
		returnToMap.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			returnToMap.setBackgroundResource(R.drawable.map_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			returnToMap.setBackgroundResource(R.drawable.map);
        			savePreferences();
        			finish();
        		}
        		return false;
         	}
     	});
		
		// Confirms text to be sent to the server for a taunt.
		sendTaunt = (Button) findViewById(R.id.sendTauntButton);
		sendTaunt.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			//returnToMap.setBackgroundResource(R.drawable.map_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			//sendTaunt.setBackgroundResource(R.drawable.map);
        			String tauntText = tauntScreen.getText().toString();
        			
        			Log.d("Tools", "Taunt to send: " + tauntText);
        			tauntScreen.setText("");
        			sendTaunt.setVisibility(View.INVISIBLE);
        			tauntScreen.setVisibility(View.INVISIBLE);
        			
        	    	networkSend += ", \"message\":\"" + tauntText + "\"";
        			goToSelectTargetScreen();
        		}
        		return false;
         	}
     	});
		
		// Image shows if purchase complete or failed.  Touch image to make it disappear.
		completePurchaseScreen = (ImageView) findViewById(R.id.completePurchaseImage);
		completePurchaseScreen.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!waitStatus) {
					completePurchaseScreen.setVisibility(View.GONE);
				}
			}
		});
	}
	
	@Override
	public void onBackPressed() {
	}
	
	 /*
	  * Get preferences.
	  */
	private void getPreferences() {
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    userName = settings.getString("USERNAME", "NONE");
	    userPassword = settings.getString("PASSWORD", "NONE");
	    balance = settings.getInt("BALANCE", -1);
	    //balance = 0;
	    Log.d("Treasure Hunt", "Tools: balance = " + balance);
	    balanceText.setText(Integer.toString(balance));
	}
	
	/*
	 * Confirm box for each individual tool to ensure user wishes to purchase tool.
	 */
	public void confirmToolPurchase() {
		switch (purchaseItem) {
		case Taunt:
			callConfirmBox("Taunt");
			break;
		case Monkey:
			callConfirmBox("Monkey");
			break;
		case SmokeScreen:
			callConfirmBox("Smoke Bomb");
			break;
		case ClearSky:
			callConfirmBox("ClearSky");
			break;
		case Compass:
			callConfirmBox("Compass");
			break;
		case Stealer:
			callConfirmBox("Stealer");
			break;
		case Lockout:
			callConfirmBox("Lockout");
			break;
		}

		helpScreen.setVisibility(View.GONE);
		buy.setVisibility(View.GONE);
	}

	/*
	 * Alert box pops up asking the user to confirm or cancel the purchase.
	 */
	public void callConfirmBox(String item) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set title
		alertDialogBuilder.setTitle("Do you wish to buy: " + item + "?");

		// set dialog message
		alertDialogBuilder
				// .setMessage("Click yes to exit!")
				.setCancelable(false)
				.setPositiveButton("Confirm",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close current activity
								// AlertBoxActivity.this.finish();
								makeToolPurchase();
								dialog.cancel();
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}
	
	/*
	 * After the user has confirmed to purchase a particular tool, this method handles
	 * all actions related to executing that request. JSON string is built and sent
	 * to networkCall() for each specific tool.
	 */
	private void makeToolPurchase() {
		// For debugging purposes		
		//userName = "DF";
		//userPassword = "testpass";
		networkSend = "{";
		if ((balance - toolPriceList.get(purchaseItem)) < 0) {
			helpScreen.setImageResource(R.drawable.not_enough_funds);
			helpScreen.setVisibility(View.VISIBLE);
			Log.d("Tools", "Not enough funds!");
		} else {
			switch (purchaseItem) {
			case Taunt:
				/*
				 *  Make taunt text box and send button visible.  Return to onCreate() where sendTaunt button
				 *  is listening for user to confirm to send the taunt text to the server.
				 */
				tauntScreen.setVisibility(View.VISIBLE);
				sendTaunt.setVisibility(View.VISIBLE);
			
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"taunt\"";
				
				networkActivity = PURCHASE_TAUNT;
				Log.d("Tools", "Purchasing Taunt.");
				break;
			case Monkey:
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"dizzyMonkey\"";
				goToSelectTargetScreen();
				
				networkActivity = PURCHASE_MONKEY;
				Log.d("Tools", "Purchasing Dizzy Monkey.");
				break;
			case SmokeScreen:
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"smokeBomb\"";
				goToSelectTargetScreen();
				
				networkActivity = PURCHASE_SMOKE;
				Log.d("Tools", "Purchasing SmokeScreen.");
				break;
			case ClearSky:
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"clearSky\""; 
				networkSend += "}";
				
				networkActivity = PURCHASE_CLEARSKY;
				Log.d("Tools", "Purchasing Clear Sky.");
				new NetworkCall().execute(networkSend);
				break;
			case Compass:
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"compass\""; 
				networkSend += "}";
				
				networkActivity = PURCHASE_COMPASS;
				Log.d("Tools", "Purchasing Compass.");
				new NetworkCall().execute(networkSend);
				break;
			case Stealer:
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"stealer\""; 
				networkSend += "}";
				
				networkActivity = PURCHASE_STEAL;
				Log.d("Tools", "Purchasing Stealer.");
				new NetworkCall().execute(networkSend);
				break;
			case Lockout:
				networkSend += "\"playerID\":\"" + userName + "\"";
				networkSend += ", \"password\":\"" + userPassword + "\"";
				networkSend += ", \"currentLocation\":\"" + 0.0 + "," + 0.0 + "\"";
				networkSend += ", \"option\":\"setTool\"";
				networkSend += ", \"tool\":\"lockout\""; 
				networkSend += "}";
				
				networkActivity = PURCHASE_LOCKOUT;
				Log.d("Tools", "Purchasing Lockout.");
				new NetworkCall().execute(networkSend);
				break;
			}
		}
	}
	
	/*
	 * Sends you to the Select Target Activity to choose a target for the tool being
	 * purchased.
	 */
	private void goToSelectTargetScreen() {
        Intent intent = new Intent(this, SelectTargetActivity.class);
        startActivityForResult(intent, SELECT_TARGET);
	}
	
	/*
	 * Save preferences.
	 */
	private void savePreferences() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("BALANCE", balance);
        editor.commit();
        Log.d("Tools", "Saving balance of " + balance);
	}
	
	/*
     * Handles what happens immediately after returning from another activity that was
     * called from this one.
     */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case SELECT_TARGET:
			String targetPlayer = intent.getExtras().getString("TARGET");
			//targetPlayer = "DF";
			networkSend += ", \"targetPlayer\":\"" + targetPlayer + "\"";
	    	networkSend += "}";
	    
	    	new NetworkCall().execute(networkSend);
			break;
		}
	}
	
    /*
	 * Processes what happens after returning from a network call.
	 */
	protected void onNetworkResult() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    
	    try {
			String status = responseJSON.getString("status").toUpperCase();
			
			if (status.contentEquals("OK")) {
				balance = Integer.valueOf(responseJSON.getString("playerPoints"));
				balanceText.setText(responseJSON.getString("playerPoints"));
			
				editor.putInt("BALANCE", balance);
		    	editor.commit();
		    	Log.d("Tools", "Purchase Complete. New balance = " + balance);
			 
		    	switch (networkActivity) {
		    		case PURCHASE_TAUNT:
		    			completePurchaseScreen.setImageResource(R.drawable.purchase_good_taunt);
		    			break;
		    		case PURCHASE_MONKEY:
						completePurchaseScreen.setImageResource(R.drawable.purchase_good_monkey);
					break;
		    		case PURCHASE_SMOKE:
		    			completePurchaseScreen.setImageResource(R.drawable.purchase_good_smoke);
					break;
		    		case PURCHASE_CLEARSKY:
						completePurchaseScreen.setImageResource(R.drawable.purchase_good_clearsky);
					break;
		    		case PURCHASE_COMPASS:
		    			editor.putBoolean("SHOWGOAL", true);
			    		editor.commit();
			    		completePurchaseScreen.setImageResource(R.drawable.purchase_good_compass);
		    			break;
		    		case PURCHASE_STEAL:
						completePurchaseScreen.setImageResource(R.drawable.purchase_good_steal);
					break;
		    		case PURCHASE_LOCKOUT:
						completePurchaseScreen.setImageResource(R.drawable.purchase_good_lockout);
					break;
		    	}
			} else {
				switch (networkActivity) {
				case PURCHASE_TAUNT:
	    			completePurchaseScreen.setImageResource(R.drawable.purchase_good_taunt);
	    			break;
	    		case PURCHASE_MONKEY:
					completePurchaseScreen.setImageResource(R.drawable.purchase_bad_monkey);
				break;
	    		case PURCHASE_SMOKE:
	    			completePurchaseScreen.setImageResource(R.drawable.purchase_bad_smoke);
				break;
	    		case PURCHASE_CLEARSKY:
					completePurchaseScreen.setImageResource(R.drawable.purchase_bad_clearsky);
				break;
	    		case PURCHASE_COMPASS:
		    		completePurchaseScreen.setImageResource(R.drawable.purchase_bad_compass);
	    			break;
	    		case PURCHASE_STEAL:
					completePurchaseScreen.setImageResource(R.drawable.purchase_bad_steal);
				break;
	    		case PURCHASE_LOCKOUT:
					completePurchaseScreen.setImageResource(R.drawable.purchase_bad_lockout);
				break;
		    	}
				
				Log.d("Tools", "Purchase Failed.");
			}
			
			completePurchaseScreen.setVisibility(View.VISIBLE);
	    } catch (JSONException e) {
			Log.e("Treasure Hunt", "Tools -> PURCHASE JSON error: " + e);
		}
	}

	/*
	 * Makes a network request to the server.
	 */
    public class NetworkCall extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(Tools.this);

        @Override
        protected void onPreExecute() {
        	waitScreen(true);
        	
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
            
            waitScreen(false);
        	Log.d("Networking", "result = " + result);
            onNetworkResult();
        }
    }
    
    private void waitScreen(boolean waiting) {
    	if (waiting) {
    		completePurchaseScreen.setImageResource(R.drawable.purchase_wait);
        	completePurchaseScreen.setVisibility(View.VISIBLE);
        	waitStatus = true;
            networkProgressBar.setVisibility(View.VISIBLE);
    	} else {
    		completePurchaseScreen.setVisibility(View.INVISIBLE);
        	completePurchaseScreen.setClickable(true);
        	waitStatus = false;
        	networkProgressBar.setVisibility(View.INVISIBLE);
    	}
    }
}