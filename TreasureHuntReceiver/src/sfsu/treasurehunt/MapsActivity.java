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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

/*
 * Treasure Hunt Receiver.
 * Send to server:
 * GET /<USER><CR><LONG>,<LAT><CR><CMD><CR><EXTRA?>
 * 
 * Commands:
 * SIGNUP -> <USER><CR><LONG>,<LAT><CR>SIGNUP<CR>password
 * SIGNIN -> <USER><CR><LONG>,<LAT><CR>SIGNUP<CR>password
 * GETCLUE -> <USER><CR><LONG>,<LAT><CR>GETCLUE
 * SETTOOL -> <USER><CR><LONG>,<LAT><CR>SETTOOL<CR>toolname
 * GETTOPTHREE -> <USER><CR><LONG>,<LAT><CR>GETTOPTHREE
 * 
 * Receive:
 * SIGNUP -> GOOD / BAD
 * SIGNIN -> GOOD / BAD
 * GETCLUE -> <CLUE><CR><(float)DISTANCE><CR><(float)GOAL_LONG,(float)GOAL_LAT><CR><ELAPSED_TIME -> HH:MM:SS><CR><PLAYER_POINTS>
 * SETTOOL -> <TOOL><CR><(float)DISTANCE><CR><(float)GOAL_LONG,(float)GOAL_LAT><CR><ELAPSED_TIME -> HH:MM:SS><CR><PLAYER_POINTS>
 * GETTOPTHREE -> <USER1><CR><HOT/WARM/COLD><CR><USER1_DISTANCE>
 *                <USER2><CR><HOT/WARM/COLD><CR><USER1_DISTANCE>
 *                <USER3><CR><HOT/WARM/COLD><CR><USER1_DISTANCE>
 */

public class MapsActivity extends MapActivity {
	// Preferences file
	public static final String PREFS_NAME = "MyPrefsFile";
	
	// For Google maps.
	private LocationManager myLocationManager;
	private Location myLocation;
	//private TextView textBox;
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
	
	//For testing.
	private int currentColor;

    // For receiving and handling new geolocation.
    private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1; // Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 10000; // Milliseconds
	
	// Screen buttons.
    private Button tools;
    private Button refresh;
    private Button clue;
    
    // Screen Text Boxes for messages, clues, name, account balance.
    private TextView textMessages;
    private TextView userNameText;
    private TextView balanceText;
    
    // User Account Info.
    private String userName = "Dennis";
    private int balance = 100;
    
    // Network passing variables.
    public static String passData;
    private String webAddress = "http://thecity.sfsu.edu:9226";
    private String webAddress2 = "http://thecity.sfsu.edu:9255";
	private String localHost = "http://10.0.2.2:9226";
	private String server;
	private int networkActivity;
	
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
        
        server = webAddress2;

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
		
        setUserAccountInfo();
                
        refresh = (Button) findViewById(R.id.refreshButton);
        refresh.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			refresh.setBackgroundResource(R.drawable.wooden_frame2_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			refresh.setBackgroundResource(R.drawable.wooden_frame2);
        			refreshCall();
        		}
        		return false;
         	}
     	});
        
        clue = (Button) findViewById(R.id.clueButton);
        clue.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			clue.setBackgroundResource(R.drawable.treasure_map_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			clue.setBackgroundResource(R.drawable.treasure_map);
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
        
        tools = (Button) findViewById(R.id.toolsButton);
        tools.setOnTouchListener(new OnTouchListener() {
        	public boolean onTouch(View v, MotionEvent event) {
        		if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //mSoundManager.playSound(1);
        			tools.setBackgroundResource(R.drawable.tools_button_pressed);
        		}
        		else if (event.getAction() == MotionEvent.ACTION_UP) {
        			tools.setBackgroundResource(R.drawable.tools_button);
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
 		myLocationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 60000, 10, listener);
		String locationProvider = LocationManager.NETWORK_PROVIDER;
		myLocation = myLocationManager.getLastKnownLocation(locationProvider);
			
		GeoPoint point = new GeoPoint((int)(myLocation.getLatitude()*1E6), (int)(myLocation.getLongitude()*1E6));
    	
		mapController = mapView.getController();
		mapController.animateTo(point);
		mapController.setZoom(14);
		
		setLocationColor(point, COLD);
		currentColor = COLD;
		//reverseGeoLocation(myLocation);
		
		myLocationManager.requestLocationUpdates(locationProvider, MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE, listener);
        
		goToLoginScreen();
        //mapView.invalidate();
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
	 * Reverse geolocation using given location parameter resulting in actual address.
	 */
	private void reverseGeoLocation(Location location) {
		List<Address> addresses;
		StringBuilder label = new StringBuilder("");
		try {
			Geocoder geoCoder = new Geocoder(this, Locale.ENGLISH);
			addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			
			if(addresses != null) {
				Address currentAddress = addresses.get(0);
				for(int x = 0; x < currentAddress.getMaxAddressLineIndex(); x++) {
					label.append(currentAddress.getAddressLine(x)).append(", ");
				}
				label.deleteCharAt(label.length()-1);
				label.deleteCharAt(label.length()-1);
				label.append("\nLat: " + location.getLatitude() + "\nLng: " + location.getLongitude());
				//textBox.setText(label.toString());
			}
		} catch(IOException e) {
			//textBox.setText("Error in reverse geolocation: " + e.getMessage());
		}
		//return label.toString();
	}
	
	/*
	 * Geolocation using location to get latitude and longitude from an address.
	 */
	private void getGeoLocation(String myAddress) {
		List<Address> addresses;
		StringBuilder label = new StringBuilder("myAddress");
		Geocoder geoCoder = new Geocoder(this);
		try {
			addresses = geoCoder.getFromLocationName(myAddress, 1);
			if(addresses != null) {
				Address x = addresses.get(0);
				label.append("\nlatitude: ").append(x.getLatitude());
				label.append("\nlongitude: ").append(x.getLongitude());
				//textBox.setText(label.toString());
			}
		} catch(IOException e) {
			//textBox.setText("Error getting geolocation: " + e.getMessage());
		}
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
			//textBox.setText("Location changed.");
			//reverseGeoLocation(location);
			//mapView.invalidate();
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
	 * Switches to the Tools activity.
	 */
    public void goToLoginScreen() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_SCREEN);
    }
    
    /*
     * Requests server to check location or get clue if starting the game for the
     * first time.
     */
    private void refreshCall() {
    	//networkActivity = GETCLUE;
		//new NetworkCall().execute();
    	cycleIcons();
    }
    
    /*
     * FOR TESTING ONLY! Cycles through user icons.
     */
    private void cycleIcons() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		
    	myLocationOverlay = new MyLocationOverlay(this, mapView);
        mapView.setBuiltInZoomControls(true);
 		myLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String locationProvider = myLocationManager.getBestProvider(criteria, true);
		myLocation = myLocationManager.getLastKnownLocation(locationProvider);
		
		GeoPoint point = new GeoPoint((int)(myLocation.getLatitude()*1E6), (int)(myLocation.getLongitude()*1E6));
    	
		mapController = mapView.getController();
		mapController.animateTo(point);
		
		switch(currentColor) {
		case HOT:
			setLocationColor(point, COLD);
			currentColor = COLD;
			Log.d("Treasure Hunt", "Set to Cold.");
			break;
		case WARMER:
			setLocationColor(point, HOT);
			currentColor = HOT;
			Log.d("Treasure Hunt", "Set to Hot.");
			break;
		case WARM:
			setLocationColor(point, WARMER);
			currentColor = WARMER;
			Log.d("Treasure Hunt", "Set to Warmer.");
			break;
		case COLD:
			setLocationColor(point, WARM);
			currentColor = WARM;
			Log.d("Treasure Hunt", "Set to Warm.");
			break;
		}
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
    	balanceText.setText(balance + " pts");
    	textMessages.setText("No Clue!");
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
		switch (networkActivity) {
		case GETCLUE:
			//Toast.makeText(getBaseContext(), passData, Toast.LENGTH_LONG).show();
			//textMessages.setVisibility(View.VISIBLE);
			//textMessages.setText("Testing");
			break;
		}
	}

	/*
	 * Makes a network request to the server.
	 */
    public class NetworkCall extends AsyncTask<String, Void, String> {
        private final ProgressDialog dialog = new ProgressDialog(
                MapsActivity.this);

        @Override
        protected void onPreExecute() {
            this.dialog.setMessage("Updating location....");
            Log.i("AsyncTask", "onPreExecute");
        }

        @Override
        protected String doInBackground(String... sendingInfo) {

            //String URL = "http://thecity.sfsu.edu:9255";
        	String URL = server;

            /*
             * JSON for sending to server String stringToJson =
             * "{\"playerID\":\"" + sendingInfo[0] + "\", \"latitude\":\"" +
             * sendingInfo[2] + "\", \"longitude\":\"" + sendingInfo[3] + "\"}";
             */

            // Debugging Hard-coded JSON
            //String stringToJson = "{\"playerID\":\"testID\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"signUp\"}";
            String stringToJson = "{\"playerID\":\"DF\", \"currentLocation\":\"121.235,-23.456\", \"option\":\"signUp\"}";
            
            JSONObject jsonToSend;
            try {
                jsonToSend = new JSONObject(stringToJson);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            HttpClient httpClient = new HttpClient();
            JSONObject responseJSON = httpClient.httpPost(URL, jsonToSend);

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
            passData = result;
            onNetworkResult();
        }
    }
}