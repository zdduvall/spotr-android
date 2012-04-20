package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.core.Place;
import com.csun.spotr.custom_gui.BalloonItemizedOverlay;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.PlaceIconUtil;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Display detail information of a place
 **/
public class PlaceInfoActivity 
	extends MapActivity 
		implements IActivityProgressUpdate<Place> {
	
	private static final String TAG = "(PlaceInfoActivity)";
	private static final String GET_SPOT_DETAIL_URL = "http://107.22.209.62/android/get_spot_detail.php";
	
	public int currentPlaceId = 0;
	private MapView mapView = null;
	private List<Overlay> mapOverlays = null;
	private MapController mapController = null;
	private MyItemizedOverlay itemizedOverlay = null;
	private Drawable mapMarker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.place_info);
		Bundle extrasBundle = getIntent().getExtras();
		currentPlaceId = extrasBundle.getInt("place_id");
		setupMapGraphics();
		setupMapOverlays();
		new GetPlaceDetailTask(this).execute();
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_basic);
		((TextView) findViewById(R.id.title_bar_title)).setText("pot Info");
	}
	
	private void setupMapGraphics() {
		mapView = (MapView) findViewById(R.id.place_info_xml_mapview);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(false);
		mapView.invalidate();
	}

	private void setupMapOverlays() {
		mapOverlays = mapView.getOverlays();
		mapMarker = PlaceIconUtil.getMapIconByType(this, PlaceIconUtil.IC_MAP_DEFAULT);
		itemizedOverlay = new MyItemizedOverlay(mapMarker, mapView);
		mapOverlays.add(itemizedOverlay);
	}

	private static class GetPlaceDetailTask 
		extends AsyncTask<Void, Integer, Place> 
			implements IAsyncTask<PlaceInfoActivity> {
		
		private List<NameValuePair> placeData = new ArrayList<NameValuePair>();
		private WeakReference<PlaceInfoActivity> ref;
		
		public GetPlaceDetailTask(PlaceInfoActivity a) {
			attach(a);
		}

		@Override
		protected Place doInBackground(Void... voids) {
			placeData.add(new BasicNameValuePair("place_id", Integer.toString(ref.get().currentPlaceId)));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_SPOT_DETAIL_URL, placeData);
			Place place = null;
			if (array != null) {
				try {
					// create a place
					place = new Place.Builder(
					// required parameters
							array.getJSONObject(0).getDouble(
									"spots_tbl_longitude"), array
									.getJSONObject(0).getDouble(
											"spots_tbl_latitude"), array
									.getJSONObject(0).getInt("spots_tbl_id"))
							// optional parameters
							.name(array.getJSONObject(0).getString(
									"spots_tbl_name"))
							.address(
									array.getJSONObject(0).getString(
											"spots_tbl_description")).build();

					if (array.getJSONObject(0).has("spots_tbl_phone")) {
						place.setPhoneNumber(array.getJSONObject(0).getString(
								"spots_tbl_phone"));
					}

					if (array.getJSONObject(0).has("spots_tbl_url")) {
						place.setWebsiteUrl(array.getJSONObject(0).getString(
								"spots_tbl_url"));
					}
				} catch (JSONException e) {
					Log.e(TAG
							+ "GetPlaceDetailTask.doInBackground(Void...voids) : ",
							"JSON error parsing data", e);
				}
			}
			return place;
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onPostExecute(final Place p) {
			ref.get().updateAsyncTaskProgress(p);
			detach();
		}

		public void attach(PlaceInfoActivity a) {
			ref = new WeakReference<PlaceInfoActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private class MyItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
		private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
		private List<Place> placeInformations = new ArrayList<Place>();

		public MyItemizedOverlay(Drawable defaultMarker, MapView mapView) {
			super(boundCenter(defaultMarker), mapView);
		}

		public void addOverlay(OverlayItem overlay, Place place) {
			overlays.add(overlay);
			placeInformations.add(place);
			populate();
		}

		/*Commenting since it isn't used.
		public void clear() {
			overlays.clear();
			placeInformations.clear();
		}*/

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}

		@Override
		protected boolean onBalloonTap(int index, OverlayItem item) {
			return true;
		}
	}

	public void updateAsyncTaskProgress(final Place p) {
		displayGeneralInfo(p);
		formatPhoneButton(p);
		displayOverlayOnMap(p);
	}
	
	private void displayGeneralInfo(final Place p) {
		TextView name = (TextView) findViewById(R.id.place_info_xml_textview_name);
		name.setText(p.getName());

		TextView description = (TextView) findViewById(R.id.place_info_xml_textview_description);
		description.setText(p.getAddress().length() > 0 ? p.getAddress() : 
			"Coordinates: (" + p.getLatitude() + ", " + p.getLongitude() + ")");

		TextView url = (TextView) findViewById(R.id.place_info_xml_textview_url);
		String URL = p.getWebsiteUrl();
		if (p.getWebsiteUrl().equals("null")) 
			URL = "http://maps.google.com/maps?q=" + p.getLatitude() + "+" + p.getLongitude();
		
		url.setText( URL );
		final String url2 = URL;
		url.setClickable(true);
		url.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle extras = new Bundle();
				extras.putString("place_web_url", url2);
				Intent intent = new Intent(v.getContext(), WebviewActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
	}
	
	private void formatPhoneButton(final Place p) {
		Button phoneButton = (Button) findViewById(R.id.place_info_xml_button_phone_number);
		if (!p.getPhoneNumber().equals("null")) {
			phoneButton.setText(p.getPhoneNumber());
			final String phoneUrl = "tel:"
					+ p.getPhoneNumber().replaceAll("-", "").replace("(", "")
							.replace(")", "").replace(" ", "");
			phoneButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_CALL, Uri
							.parse(phoneUrl));
					startActivity(intent);
				}
			});
		}
		else phoneButton.setVisibility(View.GONE);
	}
	
	private void displayOverlayOnMap(final Place p) {
		OverlayItem overlay = new OverlayItem(new GeoPoint((int) (p.getLatitude() * 1E6), (int) (p.getLongitude() * 1E6)), p.getName(), p.getAddress());
		itemizedOverlay.addOverlay(overlay, p);
		mapController.animateTo(new GeoPoint((int) (p.getLatitude() * 1E6), (int) (p.getLongitude() * 1E6)));
		mapController.setZoom(16);
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
