package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListView;
import android.util.Log;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.location.Location;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.core.adapter_item.PlaceItem;
import com.csun.spotr.custom_gui.ActionItem;
import com.csun.spotr.custom_gui.ToolbarAction;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.FineLocation.LocationResult;
import com.csun.spotr.util.GooglePlaceHelper;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.PlaceItemAdapter;

/**
 * Description:
 * Display all places around current phone's location
 */
public class PlaceActivity 
	extends Activity 
		implements IActivityProgressUpdate<PlaceItem> {
	
	private static final 		String 				TAG = "(PlaceActivity)";
	private static final 		String 				GET_SPOTS_URL = "http://107.22.209.62/android/get_spots.php";
	private static final 		String 				UPDATE_GOOGLE_PLACES_URL = "http://107.22.209.62/android/update_google_places.php";
	
	private 					ListView 			list;
	private 					PlaceItemAdapter 	adapter;
	private 					List<PlaceItem> 	placeItemList = new ArrayList<PlaceItem>();
	private 					Location 			lastKnownLocation = null;
	private 					FineLocation 		fineLocation = new FineLocation();
	private 					Button 				refreshButton;
	
	private static final int 	ID_BONUS 	 = 1;
	private static final int 	ID_LOAN 	 = 2;
	private static final int 	ID_TELESCOPE = 3;
	private static final int 	ID_TELEPORT  = 4;
	private static final int 	ID_SNEAK 	 = 5;
	private static final int 	ID_LUCK 	 = 6;	
	private static final int 	ID_SHORTCUT  = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "I'm created!");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.place);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);

		// make sure keyboard of edit text do not populate
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

	
		list = (ListView) findViewById(R.id.place_xml_listview_places);
		adapter = new PlaceItemAdapter(this.getApplicationContext(), placeItemList);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), PlaceMainActivity.class);
				Bundle extras = new Bundle();
				extras.putInt("place_id", placeItemList.get(position).getId());
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		
		// register click event for refresh button
		refreshButton = (Button) findViewById(R.id.place_xml_button_refresh);
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				new GetSpotsTask(PlaceActivity.this).execute();
			}
		});
		
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;
				refreshButton.setEnabled(true);
			}
		});
		
		fineLocation.getLocation(this, locationResult);
		
		ActionItem itemBonus = new ActionItem(ID_BONUS, "Bonus", getResources().getDrawable(R.drawable.pu_bonus_32));
		ActionItem itemLoan = new ActionItem(ID_LOAN, "Loan", getResources().getDrawable(R.drawable.pu_loan_32));
        ActionItem itemLuck = new ActionItem(ID_LUCK, "Luck", getResources().getDrawable(R.drawable.pu_luck_32));
        ActionItem itemShortcut = new ActionItem(ID_SHORTCUT, "Shortcut", getResources().getDrawable(R.drawable.pu_shortcut_32));
        ActionItem itemSneakPeek = new ActionItem(ID_SNEAK, "Sneak", getResources().getDrawable(R.drawable.pu_sneak_peek_32));
        ActionItem itemTeleport = new ActionItem(ID_TELEPORT, "Teleport", getResources().getDrawable(R.drawable.pu_teleport_32));
        ActionItem itemTelescope = new ActionItem(ID_TELESCOPE, "Telescope", getResources().getDrawable(R.drawable.pu_telescope_32));
        
        itemBonus.setSticky(true);
        
        final ToolbarAction quickAction = new ToolbarAction(this, ToolbarAction.HORIZONTAL);
		
		//add action items into QuickAction
        quickAction.addActionItem(itemBonus);
		quickAction.addActionItem(itemLoan);
        quickAction.addActionItem(itemLuck);
        quickAction.addActionItem(itemShortcut);
        quickAction.addActionItem(itemSneakPeek);
        quickAction.addActionItem(itemTeleport);
        quickAction.addActionItem(itemTelescope);
        
        ImageView tool = (ImageView) findViewById(R.id.place_xml_imageview_toolbar_button);
        
        tool.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				quickAction.show(v);
				quickAction.setAnimStyle(ToolbarAction.ANIM_REFLECT);
			}
		});
	}

	public AlertDialog CreateAlert(String title, String message) {
		AlertDialog alert = new AlertDialog.Builder(this).create();
		alert.setTitle(title);
		alert.setMessage(message);
		return alert;
	}

	private static class GetSpotsTask 
		extends AsyncTask<Void, PlaceItem, Boolean> 
			implements IAsyncTask<PlaceActivity> {
		
		private List<NameValuePair> placeData = new ArrayList<NameValuePair>();
		private WeakReference<PlaceActivity> ref;
		
		public GetSpotsTask(PlaceActivity a) {
			attach(a);
		}
		
		private List<NameValuePair> constructGooglePlace() {
			// this is data we will send to our server
			List<NameValuePair> sentData = new ArrayList<NameValuePair>();
			// we reformat the original data to include only what we need
			JSONArray reformattedData = new JSONArray();
			JSONObject json = JsonHelper.getJsonFromUrl(GooglePlaceHelper.buildGooglePlacesUrl(ref.get().lastKnownLocation, GooglePlaceHelper.GOOGLE_RADIUS_IN_METER));
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
		protected void onProgressUpdate(PlaceItem... p) {
			ref.get().updateAsyncTaskProgress(p[0]);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			// send Google data to our server to update 'spots' table
			JsonHelper.getJsonObjectFromUrlWithData(UPDATE_GOOGLE_PLACES_URL, constructGooglePlace());
			
			// now sending latitude, longitude and radius to retrieve places
			placeData.add(new BasicNameValuePair("latitude", Double.toString(ref.get().lastKnownLocation.getLatitude())));
			placeData.add(new BasicNameValuePair("longitude", Double.toString(ref.get().lastKnownLocation.getLongitude())));
			placeData.add(new BasicNameValuePair("radius", GooglePlaceHelper.RADIUS_IN_KM));
			
			// get places as JSON format from our database
			JSONArray jsonPlaceArray = JsonHelper.getJsonArrayFromUrlWithData(GET_SPOTS_URL, placeData);
			if (jsonPlaceArray != null) {
				try {
					for (int i = 0; i < jsonPlaceArray.length(); ++i) {
						publishProgress(
							new PlaceItem(
								jsonPlaceArray.getJSONObject(i).getInt("spots_tbl_id"), 
								jsonPlaceArray.getJSONObject(i).getString("spots_tbl_name"), 
								jsonPlaceArray.getJSONObject(i).getString("spots_tbl_description")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetSpotsTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(PlaceActivity a) {
			ref = new WeakReference<PlaceActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.options_menu_xml_item_setting_icon :
			intent = new Intent("com.csun.spotr.SettingsActivity");
			startActivity(intent);
			break;
		case R.id.options_menu_xml_item_logout_icon :
			SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
			editor.clear();
			editor.commit();
			intent = new Intent("com.csun.spotr.LoginActivity");
			startActivity(intent);
			break;
		case R.id.options_menu_xml_item_mainmenu_icon :
			intent = new Intent("com.csun.spotr.MainMenuActivity");
			startActivity(intent);
			break;
			
		case R.id.options_menu_xml_item_toolbar_icon:
			HorizontalScrollView toolbar = (HorizontalScrollView)findViewById(R.id.place_xml_imageview_toolbar_button);
			if (toolbar.getVisibility() == View.VISIBLE) {
				toolbar.setVisibility(View.GONE);
			}
			else {
				toolbar.setVisibility(View.VISIBLE);
			}
			break;
		}
		return true;
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

	public void updateAsyncTaskProgress(PlaceItem p) {
		placeItemList.add(p);
		adapter.notifyDataSetChanged();
	}
}
