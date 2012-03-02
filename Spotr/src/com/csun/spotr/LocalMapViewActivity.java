package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.GooglePlaceHelper;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.FineLocation.LocationResult;
import com.csun.spotr.core.Place;
import com.csun.spotr.custom_gui.CustomItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * Description:
 * Display map view of places
 */
public class LocalMapViewActivity 
	extends MapActivity 
		implements IActivityProgressUpdate<Place> {
	
	private static final 	String 						TAG = "(LocalMapViewActivity)";
	private static final 	String 						GET_SPOTS_URL = "http://107.22.209.62/android/get_spots.php";
	private static final 	String 						UPDATE_GOOGLE_PLACES_URL = "http://107.22.209.62/android/update_google_places.php";

	private 				MapView 					mapView = null;
	private 				List<Overlay> 				mapOverlays = null;
	private 				MapController 				mapController = null;
	private 				FineLocation 				fineLocation = new FineLocation();
	public 					Location 					lastKnownLocation = null;
	public 					CustomItemizedOverlay 		itemizedOverlay = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mapview);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_map);
		
		setupMapGraphics();
		findLocation();	// locate and places buttons activated once location found		
	}
	
	/**
	 * Initialize the map view, along with associated icons and overlays.
	 */
	private void setupMapGraphics() {
		mapView = (MapView) findViewById(R.id.mapview_xml_map); 					// get map view
		mapController = mapView.getController(); 									// get map controller
		mapView.setBuiltInZoomControls(true); 										// set zoom button
		mapOverlays = mapView.getOverlays(); 										// get overlays
		Drawable drawable = getResources().getDrawable(R.drawable.map_maker_green); // get the display icon on map
		itemizedOverlay = new CustomItemizedOverlay(drawable, mapView); 			// initialize overlay item
		mapOverlays.add(itemizedOverlay); 											// add them to the map
	}
	
	/**
	 * Retrieve current location. Upon finding the current location, set up
	 * the locate and places buttons.
	 */
	private void findLocation() {
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;
				activateLocateButton();
				activatePlacesButton();
			}
		});
		fineLocation.getLocation(this, locationResult);
	}
	
	/**
	 * Set up the locate button to be clickable, to have a new image, and
	 * to handle its click event.
	 */
	private void activateLocateButton() {
		ImageButton locateButton = (ImageButton) findViewById(R.id.title_bar_map_btn_locate);
		locateButton.setClickable(true);
		locateButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_locate_enabled));
		locateButton.setScaleType(ScaleType.FIT_XY);
		locateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				OverlayItem ovl = new OverlayItem(new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6), (int) (lastKnownLocation.getLongitude() * 1E6)), "My location", "Hello");
				Drawable icon = getResources().getDrawable(R.drawable.map_circle_marker_red);
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
				ovl.setMarker(icon);
				Place place = new Place.Builder(lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude(), -1).build();
				itemizedOverlay.addOverlay(ovl, place);

				mapController.animateTo(new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6), (int) (lastKnownLocation.getLongitude() * 1E6)));
				mapController.setZoom(19);				
			}	
		});
	}
	
	/**
	 * Set up the places button to be clickable, to have a new image, and
	 * to handle its click event.
	 */
	private void activatePlacesButton() {
		ImageButton placesButton = (ImageButton) findViewById(R.id.title_bar_map_btn_places);
		placesButton.setClickable(true);
		placesButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_places_enabled));
		placesButton.setScaleType(ScaleType.FIT_XY);
		placesButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new GetSpotsTask(LocalMapViewActivity.this).execute(lastKnownLocation);
			}
		});
	}
	
	/**
	 * Open the Main Menu activity (dashboard). If that activity is already
	 * running, a new instance of that activity will not be launched--instead,
	 * all activities on top of the old instance are removed as the old 
	 * instance is brought to the top.
	 * @param button the button clicked
	 */
	public void goToMainMenu(View button) {
	    final Intent intent = new Intent(this, MainMenuActivity.class);
	    intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    startActivity (intent);
	}
	
	/**
	 * A pop-up dialog for changing the map view type. 
	 */
	private void displayMapViewDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Map View Option");
		myAlertDialog.setMessage("Pick a map view");
		myAlertDialog.setPositiveButton("Street", new DialogInterface.OnClickListener() {
			// do something when the button is clicked
			public void onClick(DialogInterface arg0, int arg1) {
				mapView.setSatellite(false);
				mapView.setTraffic(false);
				mapView.invalidate();
			}
		});

		myAlertDialog.setNeutralButton("Satellite", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				mapView.setSatellite(true);
				mapView.setTraffic(false);
				mapView.invalidate();
			}
		});

		myAlertDialog.setNegativeButton("Traffic", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				mapView.setSatellite(false);
				mapView.setTraffic(true);
				mapView.invalidate();
			}
		});
		myAlertDialog.show();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private static class GetSpotsTask 
		extends AsyncTask<Location, Place, Boolean> 
			implements IAsyncTask<LocalMapViewActivity> {
		
		private List<NameValuePair> placeData = new ArrayList<NameValuePair>();
		private WeakReference<LocalMapViewActivity> ref;
		
		public GetSpotsTask(LocalMapViewActivity a) {
			attach(a);
		}

		private List<NameValuePair> constructGooglePlace(Location loc) {
			// this is data we will send to our server
			List<NameValuePair> sentData = new ArrayList<NameValuePair>();
			// we reformat the original data to include only what we need
			JSONArray reformattedData = new JSONArray();
			JSONObject json = JsonHelper.getJsonFromUrl(GooglePlaceHelper.buildGooglePlacesUrl(loc, GooglePlaceHelper.GOOGLE_RADIUS_IN_METER));
			JSONObject temp = null;
			
			try {
				JSONArray originalGoogleDataArray = json.getJSONArray("results");
				for (int i = 0; i < originalGoogleDataArray.length(); i++) {
					// id: is used to verify place existence 
					JSONObject e = new JSONObject();
					e.put("id", originalGoogleDataArray.getJSONObject(i).getString("id"));
					e.put("name", originalGoogleDataArray.getJSONObject(i).getString("name"));
					e.put("lat", originalGoogleDataArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
					e.put("lon", originalGoogleDataArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
					temp = JsonHelper.getJsonFromUrl(GooglePlaceHelper.buildGooglePlaceDetailsUrl(originalGoogleDataArray.getJSONObject(i).getString("reference")));
					
					if (temp.getJSONObject("result").has("formatted_address")) {
						e.put("addr", temp.getJSONObject("result").getString("formatted_address"));
					}
					else {
						e.put("addr", "default address");
					}
					
					if (temp.getJSONObject("result").has("formatted_phone_number")) {
						e.put("phone", temp.getJSONObject("result").getString("formatted_phone_number"));
					}
					else {
						e.put("phone", "(888) 888-8888");
					}
					
					if (temp.getJSONObject("result").has("url")) {
						e.put("url", temp.getJSONObject("result").getString("url"));
					}
					else {
						e.put("url", "https://www.google.com/");
					}
					
					// put e
					reformattedData.put(e);
				}
			}
			catch (JSONException e) {
				Log.e(TAG + "GetSpotsTask.constructGooglePlace() : ", "JSON error parsing data" + e.toString());
			}
			// send data to our server
			sentData.add(new BasicNameValuePair("google_array", reformattedData.toString()));
			return sentData;
		}
		
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Place... p) {
			ref.get().updateAsyncTaskProgress(p[0]);
		}
		
		@Override
		protected Boolean doInBackground(Location... locations) {
			// send Google data to our server to update 'spots' table
			JsonHelper.getJsonObjectFromUrlWithData(UPDATE_GOOGLE_PLACES_URL, constructGooglePlace(locations[0]));
			
			// now sending latitude, longitude and radius to retrieve places
			placeData.add(new BasicNameValuePair("latitude", Double.toString(locations[0].getLatitude())));
			placeData.add(new BasicNameValuePair("longitude", Double.toString(locations[0].getLongitude())));
			placeData.add(new BasicNameValuePair("radius", GooglePlaceHelper.RADIUS_IN_KM));
			
			// get places as JSON format from our database
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_SPOTS_URL, placeData);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
								new Place.Builder(
									// require parameters
									array.getJSONObject(i).getDouble("spots_tbl_longitude"), 
									array.getJSONObject(i).getDouble("spots_tbl_latitude"), 
									array.getJSONObject(i).getInt("spots_tbl_id"))
										// optional parameters
										.name(array.getJSONObject(i).getString("spots_tbl_name"))
										.address(array.getJSONObject(i).getString("spots_tbl_description")).build());
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetSpotsTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(LocalMapViewActivity a) {
			ref = new WeakReference<LocalMapViewActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	@Override
	public void onRestart() {
		Log.v(TAG, "I'm restarted!");
		super.onRestart();
	}

	@Override
	public void onStop() {
		Log.v(TAG, "I'm stopped!");
		super.onStop();
	}

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.map_menu_xml_item_setting_icon:
			intent = new Intent("com.csun.spotr.SettingsActivity");
			startActivity(intent);
			break;
		case R.id.map_menu_xml_item_logout_icon:
			SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
			editor.clear();
			editor.commit();
			intent = new Intent("com.csun.spotr.LoginActivity");
			startActivity(intent);
			break;
		case R.id.map_menu_xml_item_mainmenu_icon:
			intent = new Intent("com.csun.spotr.MainMenuActivity");
			startActivity(intent);
			break;
		case R.id.map_menu_xml_item_mapview:
			displayMapViewDialog();
			break;
		}
		return true;
	}

	public void updateAsyncTaskProgress(Place p) {
		OverlayItem overlay = new OverlayItem(new GeoPoint((int) (p.getLatitude() * 1E6), (int) (p.getLongitude() * 1E6)), p.getName(), p.getAddress());
		itemizedOverlay.addOverlay(overlay, p);
		mapView.invalidate();
	}
}
