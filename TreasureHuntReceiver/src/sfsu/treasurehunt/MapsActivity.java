package sfsu.treasurehunt;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.util.Locale;

public class MapsActivity extends MapActivity {
	// For Google maps.
    MapView mapView;
    MapController mapControl;
    GeoPoint geoPoint;

    // For receiving and handling new geolocation.
    private LocationManager locationManager;
    private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1; // Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; // Milliseconds
	final Context context = this;
	
	// Screen buttons.
    Button tools;
    Button refresh;
    
    // Network passing variables.
    public static String passData;
    private String webAddress = "http://thecity.sfsu.edu:9226/";
	private String localHost = "http://10.0.2.2:9226/";
	private String server;
    private int activity;
    private static final int GETCLUE = 1;
	

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        server = webAddress;
        tools = (Button) findViewById(R.id.toolsButton);
        refresh = (Button) findViewById(R.id.refreshButton);

        tools.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				purchaseTools();
			}
		});
        
        refresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getBaseContext(), "Refresh Call", Toast.LENGTH_LONG).show();
				refreshCall();
			}
		});
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				MINIMUM_TIME_BETWEEN_UPDATE, MINIMUM_DISTANCECHANGE_FOR_UPDATE,
				new MyLocationListener());
        
        mapView = (MapView) findViewById(R.id.mapView);
        //Sets Zoom controls that appear at the bottom of the screen.
        mapView.setBuiltInZoomControls(true);

        //Sets starting location when app starts.
        mapControl = mapView.getController();
        String coordinates[] = {"1.352566007", "103.78921587"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);

        //Establish geolocation.
        geoPoint = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));

        //Change map location.
        mapControl.animateTo(geoPoint);
        mapControl.setZoom(17);

        //---Add a location marker---
        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);

        mapView.invalidate();
    }
    
    public void moveLocation() {
    	mapControl = mapView.getController();
        String coordinates[] = {"100.0", "103.78921587"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);

        //Establish geolocation.
        geoPoint = new GeoPoint(
                (int) (lat * 1E6),
                (int) (lng * 1E6));

        //Change map location.
        mapControl.animateTo(geoPoint);
        mapControl.setZoom(17);

        //---Add a location marker---
        MapOverlay mapOverlay = new MapOverlay();
        List<Overlay> listOfOverlays = mapView.getOverlays();
        listOfOverlays.clear();
        listOfOverlays.add(mapOverlay);

        mapView.invalidate();
    }
    
    public void purchaseTools() {
        Intent intent = new Intent(this, Tools.class);
        startActivityForResult(intent, 0);
    }
    
    public class MyLocationListener implements LocationListener {
		public void onLocationChanged(Location location) {
			Toast.makeText(getBaseContext(), "Location changed.", Toast.LENGTH_LONG).show();
			placePin(location);
		}

		public void onStatusChanged(String s, int i, Bundle b) {
		}

		public void onProviderDisabled(String s) {
		}

		public void onProviderEnabled(String s) {
		}

		private void placePin(Location location) {
			GeoPoint newLocation = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));

            mapControl.animateTo(newLocation);
            mapView.invalidate();
		}
	}

    /*
     * Map Functions.
     */
    
    //Set up to allow pushpins to be placed on map.
    class MapOverlay extends com.google.android.maps.Overlay {

        @Override
        //Override original draw to place pushpin onto screen.
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
            super.draw(canvas, mapView, shadow);

            //---translate the GeoPoint to screen pixels---
            Point screenPts = new Point();
            mapView.getProjection().toPixels(geoPoint, screenPts);

            //---add the marker---
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pushpin);
            canvas.drawBitmap(bmp, screenPts.x, screenPts.y - 50, null);

            return true;
        }

        @Override
        //Sense when user touches the screen and captures the location touched.
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
            //---when user lifts his finger---
            if (event.getAction() == 1) {
                GeoPoint p = mapView.getProjection().fromPixels(
                        (int) event.getX(),
                        (int) event.getY());

                Geocoder geoCoder = new Geocoder(
                        getBaseContext(), Locale.getDefault());
                try {
                    //List<Address> addresses = geoCoder.getFromLocation(
                    //    p.getLatitudeE6()  / 1E6, 
                    //    p.getLongitudeE6() / 1E6, 1);
                    //Getting Latitude and Longitude from a name.
                    List<Address> addresses = geoCoder.getFromLocationName("empire state building", 5);

                    String add = "";
                    if (addresses.size() > 0) {
                    //    for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                    //        add += addresses.get(0).getAddressLine(i) + "\n";
                    //    }
                        //Using address grabbed from the name and relocates map to that location.
                        p = new GeoPoint(
                                (int) (addresses.get(0).getLatitude() * 1E6),
                                (int) (addresses.get(0).getLongitude() * 1E6));

                        mapControl.animateTo(p);
                        mapView.invalidate();
                    }

                    Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }
    
    private void refreshCall() {
    	activity = GETCLUE;
		new NetworkCall().execute(server + "CLUE<CMD>");
    }
    
    /*
	 * Processes what happens after returning from a network call.
	 */
	protected void onNetworkResult() {
		switch (activity) {
		case GETCLUE:
			Toast.makeText(getBaseContext(), passData, Toast.LENGTH_LONG).show();
			break;
		}
	}
    
    public class NetworkCall extends AsyncTask<String, Void, String> {
		 @Override
	        protected void onPreExecute() {
	            Log.i("AsyncTask", "onPreExecute");
		}

		/*
		 * Opens a Http connection to a server.
		 */
		private InputStream OpenHttpConnection(String urlString) throws IOException {
			InputStream in = null;
			int response = -1;

			try {
				URL url = new URL(urlString);
				URLConnection conn = url.openConnection();

				if (!(conn instanceof HttpURLConnection))
					throw new IOException("Not an HTTP connection");
				try {
					HttpURLConnection httpConn = (HttpURLConnection) conn;
					httpConn.setAllowUserInteraction(false);
					httpConn.setInstanceFollowRedirects(true);
					httpConn.setRequestMethod("GET");
					httpConn.connect();
					response = httpConn.getResponseCode();
					if (response == HttpURLConnection.HTTP_OK) {
						in = httpConn.getInputStream();
					}
				} catch (Exception ex) {
					Log.d("Networking", ex.getLocalizedMessage());
					throw new IOException("Error connecting");
				}
			} catch (MalformedURLException e) {
				Log.d("Networking", "Error opening URL: " + e);
			}
			return in;
		}

		/*
		 * Pulls string from server.
		 */
		private String DownloadText(String URL) {
			int BUFFER_SIZE = 2000;
			InputStream in = null;
			try {
				in = OpenHttpConnection(URL);
			} catch (IOException e) {
				Log.d("NetworkingActivity", e.getLocalizedMessage());
				return "";
			}

			InputStreamReader isr = new InputStreamReader(in);
			int charRead;
			String str = "";
			char[] inputBuffer = new char[BUFFER_SIZE];
			try {
				while ((charRead = isr.read(inputBuffer)) > 0) {
					// ---convert the chars to a String---
					String readString = String
							.copyValueOf(inputBuffer, 0, charRead);
					str += readString;
					inputBuffer = new char[BUFFER_SIZE];
				}
				in.close();
			} catch (IOException e) {
				Log.d("NetworkingActivity", e.getLocalizedMessage());
				return "";
			}
			return str;
		}

		// Set up DownloadText as occurs in the background.
		@Override
		protected String doInBackground(String... urls) {
			Log.i("NetworkCall", "doInBackgroup: " + urls);
			return DownloadText(urls[0]);
		}
		
		/*
		 * On completion of Async task, the string pulled from the server is
		 * saved in passData.
		 */
		@Override
		protected void onPostExecute(String result) {
			passData = result;
			onNetworkResult();
		}
	}
}