package sfsu.treasurehunt;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Treasure Hunt Receiver.
 */

public class MapsActivity extends MapActivity {
	// For Saved Preferences 
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// For Google maps.
	private LocationManager myLocationManager;
	private Location myLocation;
	private MapView mapView;
	
	// For Map Overlay.
	public static Context myContext;
	private List<Overlay> mapOverlays;
	private MyMarkerLayer markerlayer;
	private MapController mapController;
	private MyLocationOverlay myLocationOverlay;
	
	// Static value for color coding.
	private static final int COLD = 0;
	private static final int WARM = 1;
	private static final int WARMER = 2;
	private static final int HOT = 3;
	private static final int SMOKE = 4;
	
	// Menu Id Numbers.
	private static final int LOGIN_MENU = Menu.FIRST;
	private static final int TOOLS_MENU = Menu.FIRST + 1;
		
	//For testing.
	private int currentColor;

    // For receiving and handling new geolocation.
    private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1; // Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 10000; // Milliseconds
	
	// Screen buttons.
    private Button toolsButton;
    private Button getClueButton;
    private Button clueButton;
    
    // Screen Text Boxes for messages, clues, name, account balance.
    private TextView textMessages;
    private TextView userNameText;
    private TextView balanceText;
    
    // User Account Info.
    private String userName = "Dennis";
    private int balance = 100;
    
    // Network related variables.
    private JSONObject responseJSON;
	private int networkActivity;
    private String server = "http://thecity.sfsu.edu:9226";
    //private String server = "http://thecity.sfsu.edu:9255";
	//private String server = "http://10.0.2.2:9226";
	
	// Network result status codes.
	private static final int GETCLUE = 1;
	
	// Activities
	private static final int LOGIN_SCREEN = 0;
	private static final int TOOLS_SCREEN = 1;
	
	// Screen access variables.
	private boolean clueOn;
	
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        myContext = this;
        
        // Save server web address as a shared preference.
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("SERVER", server);
        editor.commit();

		mapView = (MapView) findViewById(R.id.mapView);
		
        userNameText = (TextView) findViewById(R.id.nameText);
        balanceText = (TextView) findViewById(R.id.balanceText);
        textMessages = (TextView) findViewById(R.id.textMessages);
        textMessages.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				textMessages.setVisibility(View.GONE);
				clueOn = false;
			}
		});
                
        getClueButton = (Button) findViewById(R.id.refreshButton);
        getClueButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			getClueButton.setBackgroundResource(R.drawable.wooden_frame2_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			getClueButton.setBackgroundResource(R.drawable.wooden_frame2);
        			getClueFromServer();
        		}
        		return false;
         	}
     	});
        
        clueButton = (Button) findViewById(R.id.clueButton);
        clueButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			clueButton.setBackgroundResource(R.drawable.treasure_map_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			clueButton.setBackgroundResource(R.drawable.treasure_map);
        			if (clueOn) {
        				textMessages.setVisibility(View.GONE);
        				clueOn = false;
        			} else {
        				textMessages.setVisibility(View.VISIBLE);
        				clueOn = true;
        			}
        		}
        		return false;
         	}
     	});
        
        toolsButton = (Button) findViewById(R.id.toolsButton);
        toolsButton.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
        			//mSoundManager.playSound(1);
        			toolsButton.setBackgroundResource(R.drawable.tools_button_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			toolsButton.setBackgroundResource(R.drawable.tools_button);
        			goToPurchaseToolsScreen();
        		}
        		return false;
         	}
     	});
        
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.setBuiltInZoomControls(true);
 		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
 		myLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 10, listener);
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		myLocation = myLocationManager.getLastKnownLocation(locationProvider);
			
		GeoPoint point = new GeoPoint((int)(myLocation.getLatitude()*1E6), (int)(myLocation.getLongitude()*1E6));
    	
		mapController = mapView.getController();
		mapController.animateTo(point);
		mapController.setZoom(14);
		
		setLocationColor(point, SMOKE);
		
		myLocationManager.requestLocationUpdates(locationProvider, MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, listener);
        
		goToLoginScreen();
    }
    
    protected boolean isRouteDisplayed() {
		return false;
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    // when our activity resumes, we want to register for location updates
	    myLocationOverlay.enableMyLocation();
	}
	 
	@Override
	protected void onPause() {
	    super.onPause();
	    // when our activity pauses, we want to remove listening for location updates
	    myLocationOverlay.disableMyLocation();
	}

    /*
	 * Utilize MyMarkerLayer for map overlays. Changes the icon of the current location
	 * along with it's message based on the conditions: HOT/WARMER/WARM/COLD
	 */
    private void setLocationColor(GeoPoint point, int condition) {
		mapOverlays = mapView.getOverlays();
		mapOverlays.clear();
		Drawable drawable;
		OverlayItem overlayItem;
		
		currentColor = condition;
		
		switch (condition) {
			case HOT:
				drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_flame);
				overlayItem = new OverlayItem(point, "Here", "You're Hot!!!");
				markerlayer = new MyMarkerLayer(drawable);
				markerlayer.addOverlayItem(overlayItem);
				break;
			case WARMER:
				drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_red);
				overlayItem = new OverlayItem(point, "Here", "You're getting Warmer!!");
				markerlayer = new MyMarkerLayer(drawable);
		    	markerlayer.addOverlayItem(overlayItem);
				break;
			case WARM:
				drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_yellow);
				overlayItem = new OverlayItem(point, "Here", "You're getting Warm!");
				markerlayer = new MyMarkerLayer(drawable);
		    	markerlayer.addOverlayItem(overlayItem);
				break;
			case COLD:
				drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_blue);
				overlayItem = new OverlayItem(point, "Here", "You're Cold!");
				markerlayer = new MyMarkerLayer(drawable);
		    	markerlayer.addOverlayItem(overlayItem);
				break;
			case SMOKE:
				drawable = this.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_smoke);
				overlayItem = new OverlayItem(point, "Here", "You've Been Smoke-Screened!");
				markerlayer = new MyMarkerLayer(drawable);
		    	markerlayer.addOverlayItem(overlayItem);
				break;
		}
		
		mapOverlays.add(markerlayer);
    }

    /*
     * Listens to location changes and centers the map to user's new location.
     */
	private final LocationListener listener = new LocationListener() {
		public void onLocationChanged(Location location) {
			myLocation = location;
			GeoPoint newGeoPoint = new GeoPoint((int)(myLocation.getLatitude()*1E6), (int)(myLocation.getLongitude()*1E6));
			mapController.animateTo(newGeoPoint);
		}

		public void onStatusChanged(String s, int i, Bundle b) {}
		public void onProviderDisabled(String s) {}
		public void onProviderEnabled(String s) {}
	};
	
	/*
	 * Switches to the Tools activity.
	 */
    public void goToPurchaseToolsScreen() {
        Intent intent = new Intent(this, Tools.class);
        startActivityForResult(intent, TOOLS_SCREEN);
    }
    
    /*
	 * Switches to the Login activity.
	 */
    public void goToLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_SCREEN);
    }
    
    /*
     * Requests server to check location or get clue if starting the game for the
     * first time.
     */
    private void getClueFromServer() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		
    	myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.setBuiltInZoomControls(true);
 		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String locationProvider = myLocationManager.getBestProvider(criteria, true);
		myLocation = myLocationManager.getLastKnownLocation(locationProvider);
		
    	networkActivity = GETCLUE;
		new NetworkCall().execute("{\"playerID\":\"DF\", \"password\":\"testpass\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"getClue\"}");
    }
    
    /*
	 * Get preferences.
	 */
	private void getPreferences() {
	    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    userName = settings.getString("USERNAME", "NONE");
	    balance = settings.getInt("BALANCE", 0);
	    Log.d("Treasure Hunt", "User: " + userName + " signed on.");
	    Log.d("Treasure Hunt", "Balance: " + balance);
	}
    
    /*
     * Refreshes the text for User name and the user's account balance.
     */
    private void setUserAccountInfo() {
    	userNameText.setText(userName);
    	balanceText.setText(Integer.toString(balance));
    	textMessages.setText("No Clue!");
    }
 
 // Menu created when you push the menu button.
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, LOGIN_MENU, 0, R.string.login);
 		menu.add(0, TOOLS_MENU, 1, R.string.tools);
 		return true;
 	}

 	// Actions for when a menu item is selected.
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case LOGIN_MENU: // Switch to login menu.
 			goToLoginScreen();
 			return true;
 		case TOOLS_MENU: // Switch to tools menu.
 			goToPurchaseToolsScreen();
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
    
    /*
     * Handles what happens immediately after returning from another activity that was
     * called from this one.
     */
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case LOGIN_SCREEN:
			getPreferences();
			setUserAccountInfo();
			break;
		case TOOLS_SCREEN:
			break;
		}
	}
    
    /*
	 * Processes what happens after returning from a network call.
	 */
	protected void onNetworkResult() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
		switch (networkActivity) {
		case GETCLUE:
			try {
				textMessages.setText(responseJSON.getString("clue"));
				balance = Integer.valueOf(responseJSON.getString("playerPoints"));
				balanceText.setText(responseJSON.getString("playerPoints"));
				
				editor.putInt("BALANCE", balance);
			    editor.commit();
			    
			    Log.d("Treasure Hunt", "GETCLUE: Balance = " + balance);
			    Log.d("Treasure Hunt", "GETCLUE: Clue = " + textMessages.getText().toString());
			} catch (JSONException e) {
				Log.e("Treasure Hunt", "MapsActivity -> GETCLUE JSON error: " + e);
			}
			
			textMessages.setVisibility(View.VISIBLE);
			break;
		}
	}

	/*
	 * Makes a network request to the server.
	 */
    public class NetworkCall extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(MapsActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Updating location....");
            Log.i("AsyncTask", "onPreExecute");
        }

        @Override
        protected String doInBackground(String... sendingInfo) {
        	String URL = server;

            /*
             * JSON for sending to server String stringToJson = "{\"playerID\":\"" + sendingInfo[0] + "\", \"latitude\":\"" + sendingInfo[2] + "\", \"longitude\":\"" + sendingInfo[3] + "\"}";
             */

            // Debugging Hard-coded JSON
            //String stringToJson = "{\"playerID\":\"testID\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"signUp\"}";
            //String stringToJson = "{\"playerID\":\"DF\", \"password\":\"testpass\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"signIn\"}";
        	//String stringToJson = "{\"playerID\":\"DF\", \"password\":\"testpass\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"getClue\"}";
        	
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
            onNetworkResult();
        }
    }
}