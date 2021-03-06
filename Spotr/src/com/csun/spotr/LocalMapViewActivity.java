package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.PlaceIconUtil;
import com.csun.spotr.util.FineLocation.LocationResult;
import com.csun.spotr.asynctask.DownloadImageTask;
import com.csun.spotr.asynctask.GetFriendLocationsTask;
import com.csun.spotr.asynctask.GetMapSpotsTask;
import com.csun.spotr.asynctask.PingMeTask;
import com.csun.spotr.core.FriendAndLocation;
import com.csun.spotr.core.Place;
import com.csun.spotr.custom_gui.BalloonItemizedOverlay;
import com.csun.spotr.custom_gui.PlaceCustomItemizedOverlay;
import com.csun.spotr.custom_gui.ImpactOverlay;
import com.csun.spotr.custom_gui.UserCustomItemizedOverlay;
import com.csun.spotr.custom_gui.UserWithRadiusItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;


/**
 * Description: Display map view of places
 */
public class LocalMapViewActivity extends MapActivity {
	/**
	 * Tag for debug
	 */
	private static final String TAG = "(LocalMapViewActivity)";
	
	/**
	 * Ping constants
	 */
	private static final int PING_DURATION_ONE_DAY = 86400;
	private static final int PING_DURATION_THREE_DAY = 259200;
	private static final int PING_DURATION_SEVEN_DAY = 604800;

	/**
	 * Dialog constant
	 */
	private static final int ID_DIALOG_PING = 1;

	/**
	 * The map
	 */
	private MapView mapView = null;
	
	/**
	 * The list of all overalys
	 */
	private List<Overlay> mapOverlays = null;
	
	/**
	 * The map controller
	 */
	private MapController mapController = null;
	
	/**
	 * The location listener used to retrieve
	 * newest location
	 */
	private FineLocation fineLocation = new FineLocation();
	
	/**
	 * The last known location
	 */
	public Location lastKnownLocation = null;
	
	/**
	 * Overlay for places
	 */
	public PlaceCustomItemizedOverlay placeOverlay = null;
	
	/**
	 * Overlay for friends 
	 */
	public UserCustomItemizedOverlay userOverlay = null;
	
	/**
	 * Overlay for current user
	 */
	public UserWithRadiusItemizedOverlay currentUserOverlay = null;
	
	/**
	 * List of friend data
	 */
	private List<FriendAndLocation> friendLocations = new ArrayList<FriendAndLocation>();
	
	/**
	 * List of place data
	 */
	private List<Place> places = new ArrayList<Place>();

	/**
	 * Indexes to navigate between friends
	 * on map
	 */
	private int indexFriend = 0;
	
	/**
	 * Indexes to navigate between places 
	 * on map
	 */
	private int indexPlace = 0;
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.mapview);
		setupTitleBar();        // 1. set up title bar
		setupFriendButton();    // 2. set up friend button 
		setupMapGraphics();     // 2. set up map
		findLocation();         // 2. listen to new location
		
	}

	/**
	 * Activate the friend button so it can
	 * populate all friends on map
	 */
	private void activateFriendDistanceButton() {
		Button btn = (Button) findViewById(R.id.mapview_xml_button_friend_distance);
		btn.setEnabled(true);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (lastKnownLocation != null && friendLocations.size() > 0) {
					indexFriend = indexFriend % friendLocations.size();
					GeoPoint geoMe = new GeoPoint(
						(int) (lastKnownLocation.getLatitude()* 1E6), 
						(int) (lastKnownLocation.getLongitude()* 1E6));


					GeoPoint geoFriend = new GeoPoint(
						(int) (friendLocations.get(indexFriend).getLatitude()* 1E6), 
						(int) (friendLocations.get(indexFriend).getLongitude()* 1E6));

					Location location = new Location("");
					location.setLatitude(friendLocations.get(indexFriend).getLatitude());
					location.setLongitude(friendLocations.get(indexFriend).getLongitude());

					float d = location.distanceTo(lastKnownLocation);
					userOverlay.addTwoPoints(geoMe, geoFriend, d);
					mapController.animateTo(geoFriend);
					mapController.setZoom(19);
					mapView.invalidate();
					// Toast.makeText(LocalMapViewActivity.this, (int) d + " m", Toast.LENGTH_SHORT).show();
					indexFriend++;
				}
			}
		});
	}

	/**
	 * Activate place button so it can populate
	 * all places on map
	 */
	private void activatePlaceDistanceButton() {
		Button btn = (Button) findViewById(R.id.mapview_xml_button_place_distance);
		btn.setEnabled(true);
		btn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (lastKnownLocation != null && places.size() > 0) {
					indexPlace = indexPlace % places.size();
					
					// construct a geo point of current user
					GeoPoint geoMe = new GeoPoint(
						(int) (lastKnownLocation.getLatitude()* 1E6), 
						(int) (lastKnownLocation.getLongitude()* 1E6));

					// construct a geo point of a place
					GeoPoint geoPlace = new GeoPoint(
						(int) (places.get(indexPlace).getLatitude()* 1E6), 
						(int) (places.get(indexPlace).getLongitude()* 1E6));

					// create a dummy location
					Location location = new Location("");
					location.setLatitude(places.get(indexPlace).getLatitude());
					location.setLongitude(places.get(indexPlace).getLongitude());

					// get the distance between current location 
					// and that place location
					float d = location.distanceTo(lastKnownLocation);
					
					// add these two points and its distance to overlay
					placeOverlay.addTwoPoints(geoMe, geoPlace, d);
					
					// move to that place
					mapController.animateTo(geoPlace);
					mapController.setZoom(19);
					
					// redraw 
					mapView.invalidate();
					
					// update the index to move to 
					// the next place next time user hit
					// the button
					indexPlace++;
				}
			}
		});
	}

	/**
	 * Set up the title bar
	 */
	private void setupTitleBar() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_map);
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText("");
	}

	/**
	 * Initialize the map view, along with associated icons and overlays.
	 */
	private void setupMapGraphics() {
		mapView = (MapView) findViewById(R.id.mapview_xml_map); 					  // get map view
		mapController = mapView.getController(); 									  // get map controller
		mapView.setBuiltInZoomControls(true); 										  // set zoom button
		
		mapOverlays = mapView.getOverlays(); 										  // get overlays
		Drawable drawable = getResources().getDrawable(R.drawable.ic_map_person);     // get default icon
		
		placeOverlay = new PlaceCustomItemizedOverlay(drawable, mapView); 			  // initialize place overlay
		mapOverlays.add(placeOverlay); 												  // add them to the map
		
		userOverlay = new UserCustomItemizedOverlay(drawable, mapView); 			  // initialize user overlay
		mapOverlays.add(userOverlay); 												  // add them to the map
		
		currentUserOverlay = new UserWithRadiusItemizedOverlay(drawable, mapView);	  // initialize current user overlay
		mapOverlays.add(currentUserOverlay);										  // add them to the map
	}

	/**
	 * Retrieve current location. Upon finding the current location, 
	 * set up the locate and places buttons.
	 */
	private void findLocation() {
		final Toast noLocation = Toast.makeText(LocalMapViewActivity.this, "Unable to find location", Toast.LENGTH_LONG);

		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				if(location != null)
				{
					lastKnownLocation = location;
					activateLocateButton();
					activatePingButton();
					activateFriendDistanceButton();
					activatePlaceDistanceButton();
					addUserOverlay(location);
				}
				else
				{
					/**
					 * Display that location cannot be found and finish LocalMapViewActivity
					 */
					noLocation.show();
					finish();
				}
			}
		});
		fineLocation.getLocation(this, locationResult);
	}

	/**
	 * Add user overlay for locate button; must call [mapview].invalidate() after
	 */
	private void addUserOverlay(Location location) {
		OverlayItem overlay = new OverlayItem(
			new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)), 
			"Now", 
			CurrentUser.getCurrentUser().getImageUrl());
		
		GeoPoint geoCurrent = new GeoPoint(
				(int) (location.getLatitude()* 1E6), 
				(int) (location.getLongitude()* 1E6));
		
		currentUserOverlay.addOverlay(overlay, CurrentUser.getCurrentUser());
		currentUserOverlay.changePoint(geoCurrent);
		mapController.setZoom(19);
		mapView.invalidate();
	}

	/**
	 *  This is after initialization, when user wants 
	 *  to get new location
	 */
	private void getCurrentLocation() {
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;		
				currentUserOverlay.changePoint(new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6), (int) (lastKnownLocation.getLongitude() * 1E6)));
				mapView.invalidate();
			}
		});
		fineLocation.getLocation(this, locationResult);
	}

	/**
	 * Set up the locate button to be clickable, to 
	 * have a new image, and to handle its click event.
	 */
	private void activateLocateButton() {
		ImageButton locateButton = (ImageButton) findViewById(R.id.title_bar_map_btn_locate);
		locateButton.setClickable(true);
		locateButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_map_locate_enabled));
		locateButton.setScaleType(ScaleType.FIT_XY);
		locateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getCurrentLocation();
				mapController.animateTo(new GeoPoint((int) (lastKnownLocation.getLatitude() * 1E6), (int) (lastKnownLocation.getLongitude() * 1E6)));
				mapController.setZoom(19);
				new GetMapSpotsTask(LocalMapViewActivity.this).execute(lastKnownLocation);
			}
		});
	}

	/**
	 * Set up the ping button to be clickable, to have a new image, and 
	 * to handle its click event.
	 */
	private void activatePingButton() {
		ImageButton pingButton = (ImageButton) findViewById(R.id.title_bar_map_btn_ping_me);
		pingButton.setClickable(true);
		pingButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_ping_enabled));
		pingButton.setScaleType(ScaleType.FIT_XY);
		pingButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDialog(ID_DIALOG_PING);
			}
		});
	}

	private void setupFriendButton() {
		final ImageButton friendsButton = (ImageButton) findViewById(R.id.title_bar_map_btn_friends);
		friendsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				friendsButton.setEnabled(false);
				new GetFriendLocationsTask(LocalMapViewActivity.this).execute();
			}
		});
	}

	/**
	 * Open the Main Menu activity (dashboard). If that activity is already
	 * running, a new instance of that activity will not be launched--instead,
	 * all activities on top of the old instance are removed as the old instance
	 * is brought to the top.
	 * 
	 * @param button
	 *            the button clicked
	 */
	public void goToMainMenu(View button) {
		LinearLayout homeContainer = (LinearLayout) findViewById(R.id.title_bar_home_container);
		homeContainer.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar_btn_highlight));

		final Intent intent = new Intent(this, MainMenuActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
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

	private void startDialog(int dialog) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		switch (dialog){
			case ID_DIALOG_PING:
				final EditText input = new EditText(this);
				myAlertDialog.setView(input);
				myAlertDialog.setTitle("Ping Options");
				myAlertDialog.setMessage("Enter a message and how long your ping should stay on map");

				myAlertDialog.setPositiveButton("1 Day", new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						new PingMeTask(
							LocalMapViewActivity.this, input.getText().toString(), lastKnownLocation, PING_DURATION_ONE_DAY).execute();
					}
				});

				myAlertDialog.setNeutralButton("3 Days", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						new PingMeTask(
							LocalMapViewActivity.this, input.getText().toString(), lastKnownLocation, PING_DURATION_THREE_DAY).execute();
					}
				});

				myAlertDialog.setNegativeButton("7 Days", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						new PingMeTask(
							LocalMapViewActivity.this, input.getText().toString(), lastKnownLocation, PING_DURATION_SEVEN_DAY).execute();
					}
				});
				myAlertDialog.show();
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.map_menu_xml_item_mapview :
			displayMapViewDialog();
			break;
		}
		return true;
	}

	public void updatePlaceTaskProgress(Place p) {
		placeOverlay.addOverlay(createOverlayItemByType(p), p);
		places.add(p);
		mapController.setZoom(19);
		mapView.invalidate();
	}

	public void updateFriendTaskProgress(FriendAndLocation f) {
		OverlayItem overlay = new OverlayItem(new GeoPoint((int) (f.getLatitude() * 1E6), (int) (f.getLongitude() * 1E6)), f.getTime(), f.getPictureUrl());
		friendLocations.add(f);
		userOverlay.addOverlay(overlay, f);
		mapController.setZoom(19);
		mapView.invalidate();
	}

	private OverlayItem createOverlayItemByType(Place p) {
		OverlayItem overlay = new OverlayItem(new GeoPoint((int) (p.getLatitude() * 1E6), (int) (p.getLongitude() * 1E6)), p.getName(), p.getAddress());
		overlay.setMarker(PlaceIconUtil.getMapIconByType(this, p.getType()));
		return overlay;
	}

	@Override
	public void onResume() {
		Log.v(TAG, "I'm resumed");
		super.onResume();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		super.onDestroy();
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
}