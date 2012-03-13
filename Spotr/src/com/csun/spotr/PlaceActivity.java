package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.csun.spotr.adapter.PlaceItemAdapter;
import com.csun.spotr.core.adapter_item.PlaceItem;
import com.csun.spotr.custom_gui.ActionItem;
import com.csun.spotr.custom_gui.ToolbarAction;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.FineLocation.LocationResult;
import com.csun.spotr.util.GooglePlaceHelper;
import com.csun.spotr.util.JsonHelper;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Display all places around current phone's location
 **/
public class PlaceActivity 
	extends BasicSpotrActivity 
		implements IActivityProgressUpdate<PlaceItem> {
	
	private static final String TAG = "(PlaceActivity)";
	private static final String GET_SPOTS_URL = "http://107.22.209.62/android/get_spots.php";
	private static final String UPDATE_GOOGLE_PLACES_URL = "http://107.22.209.62/android/update_google_places.php";
	private static final int DIALOG_ID_LOADING = 1;
	
	private ListView list;
	private PlaceItemAdapter adapter;
	private List<PlaceItem> placeItemList = new ArrayList<PlaceItem>();
	private FineLocation fineLocation = new FineLocation();
	
	private static final int ID_BONUS 	  = 1;
	private static final int ID_LOAN 	  = 2;
	private static final int ID_TELESCOPE = 3;
	private static final int ID_TELEPORT  = 4;
	private static final int ID_SNEAK 	  = 5;
	private static final int ID_LUCK 	  = 6;	
	private static final int ID_SHORTCUT  = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place);
		// make sure keyboard of edit text do not populate
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		showDialog(DIALOG_ID_LOADING);
		setupTitleBar();
		setupListView();
		setupPowerupToolbar();
		findLocation(); 
	}
	
	public void setupDynamicSearch() {
		EditText edittextSearch = (EditText) findViewById(R.id.place_xml_edittext_search);
		edittextSearch.setEnabled(true);
		edittextSearch.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				
			}
			
			public void afterTextChanged(Editable s) {
				adapter.getFilter().filter(s.toString());
			}
		});
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_ID_LOADING) {
			ProgressDialog loadingDialog = new ProgressDialog(this);
			loadingDialog.setMessage("Loading from Google places...");
			loadingDialog.setIndeterminate(true);
			loadingDialog.setCancelable(false);
			return loadingDialog;
		}
		return null;
	}

	
	private void setupPowerupToolbar() {
		ActionItem itemBonus = new ActionItem(ID_BONUS, "Bonus", getResources().getDrawable(R.drawable.pu_bonus_32));
		ActionItem itemLoan = new ActionItem(ID_LOAN, "Loan", getResources().getDrawable(R.drawable.pu_loan_32));
        ActionItem itemLuck = new ActionItem(ID_LUCK, "Luck", getResources().getDrawable(R.drawable.pu_luck_32));
        ActionItem itemShortcut = new ActionItem(ID_SHORTCUT, "Shortcut", getResources().getDrawable(R.drawable.pu_shortcut_32));
        ActionItem itemSneakPeek = new ActionItem(ID_SNEAK, "Sneak", getResources().getDrawable(R.drawable.pu_sneak_peek_32));
        ActionItem itemTeleport = new ActionItem(ID_TELEPORT, "Teleport", getResources().getDrawable(R.drawable.pu_teleport_32));
        ActionItem itemTelescope = new ActionItem(ID_TELESCOPE, "Telescope", getResources().getDrawable(R.drawable.pu_telescope_32));

        itemBonus.setSticky(true);
        final ToolbarAction quickAction = new ToolbarAction(this, ToolbarAction.HORIZONTAL);
		
		// add action items into QuickAction
        quickAction.addActionItem(itemBonus);
		quickAction.addActionItem(itemLoan);
        quickAction.addActionItem(itemLuck);
        quickAction.addActionItem(itemShortcut);
        quickAction.addActionItem(itemSneakPeek);
        quickAction.addActionItem(itemTeleport);
        quickAction.addActionItem(itemTelescope);
        
        ImageButton tool = (ImageButton) findViewById(R.id.title_bar_btn_spots_tool);
        tool.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				quickAction.show(v);
				quickAction.setAnimStyle(ToolbarAction.ANIM_REFLECT);
			}
		});        	
	}
	
	private void setupListView() {
		list = (ListView) findViewById(R.id.place_xml_listview_places);
		adapter = new PlaceItemAdapter(this.getApplicationContext(), placeItemList);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), PlaceMainActivity.class);
				Bundle extras = new Bundle();
				extras.putInt("place_id", adapter.getCurrentItem(position).getId());
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void setupTitleBar() {
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_spots);
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText("pots Nearby");		
	}
	
	/**
	 * Retrieve current location. Upon finding the current location, set up
	 * the refresh button.
	 */
	private void findLocation() {
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				if (location != null) {
					new GetSpotsTask(PlaceActivity.this, location).execute();
				}
				activateRefreshButton();
			}
		});
		fineLocation.getLocation(this, locationResult);
	}
	
	/**
	 * Set up the refresh button to be clickable, to have a new image, and
	 * to handle its click event.
	 */
	private void activateRefreshButton() {
		ImageButton refreshButton = (ImageButton) findViewById(R.id.title_bar_btn_spots_refresh);
		refreshButton.setClickable(true);
		refreshButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_refresh_enabled));
		refreshButton.setScaleType(ScaleType.FIT_XY);
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// do nothing
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
		
		private static final String TAG = "[AsyncTask].GetSpotsTask";
		private WeakReference<PlaceActivity> ref;
		private Location location;
		
		public GetSpotsTask(PlaceActivity a, Location loc) {
			attach(a);
			location = loc;
		}
		
		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			// now sending latitude, longitude and radius to retrieve places
			data.add(new BasicNameValuePair("latitude", Double.toString(location.getLatitude())));
			data.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
			data.add(new BasicNameValuePair("radius", GooglePlaceHelper.RADIUS_IN_KM));
			return data;
		}
		
		private List<NameValuePair> constructGooglePlace() {
			// this is data we will send to our server
			List<NameValuePair> sentData = new ArrayList<NameValuePair>();
			// we reformat the original data to include only what we need
			JSONArray reformattedData = new JSONArray();
			JSONObject json = JsonHelper.getJsonFromUrl(GooglePlaceHelper.buildGooglePlacesUrl(location, GooglePlaceHelper.GOOGLE_RADIUS_IN_METER));
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
				Log.e(TAG + ".constructGooglePlace() : ", "JSON error parsing data", e );
			}
			// send data to our server
			sentData.add(new BasicNameValuePair("google_array", reformattedData.toString()));
			return sentData;
		}
		
		@Override
		protected void onProgressUpdate(PlaceItem... p) {
			if (ref != null && ref.get() != null && !ref.get().isFinishing()) {
				ref.get().dismissDialog(DIALOG_ID_LOADING);
				ref.get().updateAsyncTaskProgress(p[0]);
			}
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			// send Google data to our server to update 'spots' table
			JsonHelper.getJsonObjectFromUrlWithData(UPDATE_GOOGLE_PLACES_URL, constructGooglePlace());
			List<NameValuePair> data = prepareUploadData();
			// get places as JSON format from our database
			JSONArray jsonPlaceArray = JsonHelper.getJsonArrayFromUrlWithData(GET_SPOTS_URL, data);
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
					Log.e(TAG + "GetSpotsTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			ref.get().setupDynamicSearch();
		}

		public void attach(PlaceActivity a) {
			ref = new WeakReference<PlaceActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

	}

	public void updateAsyncTaskProgress(PlaceItem p) {
		placeItemList.add(p);
		adapter.notifyDataSetChanged();
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
