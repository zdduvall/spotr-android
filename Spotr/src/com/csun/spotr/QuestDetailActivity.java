package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.csun.spotr.core.Place;
import com.csun.spotr.core.Place.Builder;
import com.csun.spotr.core.adapter_item.QuestDetailItem;
import com.csun.spotr.custom_gui.BalloonItemizedOverlay;
import com.csun.spotr.custom_gui.CustomQuestItemizedOverlay;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.FineLocation.LocationResult;
import com.csun.spotr.util.JsonHelper;

import com.csun.spotr.adapter.QuestDetailItemAdapter;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class QuestDetailActivity 
extends MapActivity 
implements IActivityProgressUpdate<Place>{
	private static final 	String TAG = "(QuestDetailActivity)";
	private static final 	String GET_QUEST_DETAIL_URL = "http://107.22.209.62/android/get_quest_detail.php";
	private static final 	String GIVE_QUEST_POINT_URL = "http://107.22.209.62/android/give_quest_point.php";
	private 				ListView questDetailListView;
	private 				QuestDetailItemAdapter questDetailItemAdapter;
	private 				List<QuestDetailItem> questDetailList = new ArrayList<QuestDetailItem>();

	private 				int questId;
	private 				int spotCompleted = 0;
	private static 			int numQuest = 0;
	private 				int spotId = 0;
	private 				String questName = null;
	private 				String questDescription = null;

	private 				MapView mapView = null;
	private 				List<Overlay> mapOverlays = null;
	private 				MapController mapController = null;
	private					FineLocation fineLocation = new FineLocation();
	public					Location lastKnownLocation = null;
	public					CustomQuestItemizedOverlay itemizedGreenOverlay = null;
	public					CustomQuestItemizedOverlay itemizedRedOverlay = null;

	static final 	int DO_SPOT_CHALLENGE = 1;

	private 				TextView challengedoneTextView;
	private 				ProgressBar progressbar;
	private 				TextView questNameTextView = null;
	private 				TextView questDescriptionTextView = null;
	private 				Button meButton;
	private					Button viewSpotButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quest_detail);

		questId = this.getIntent().getExtras().getInt("quest_id");
		numQuest = this.getIntent().getExtras().getInt("numberChallenges");
		questName = this.getIntent().getExtras().getString("quest_name");
		questDescription = this.getIntent().getExtras().getString("quest_description");

		questDetailListView = (ListView) findViewById(R.id.quest_detail_xml_listview_quest_list);
		questDetailItemAdapter = new QuestDetailItemAdapter(this.getApplicationContext(), questDetailList);
		questDetailListView.setAdapter(questDetailItemAdapter);

		//Initialize Button
		meButton = (Button) findViewById(R.id.quest_detail_xml_me_button);
		viewSpotButton = (Button) findViewById(R.id.quest_detail_xml_spot_button);

		//Initialize Map View
		mapView = (MapView) findViewById(R.id.quest_detail_xml_map);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);
		mapOverlays = mapView.getOverlays();
		Drawable drawablegreen = getResources().getDrawable(R.drawable.map_maker_green);
		Drawable drawablered = getResources().getDrawable(R.drawable.map_maker_red);
		itemizedGreenOverlay = new CustomQuestItemizedOverlay(drawablegreen, mapView);
		itemizedRedOverlay = new CustomQuestItemizedOverlay(drawablered,mapView);
		mapOverlays.add(itemizedGreenOverlay);
		mapOverlays.add(itemizedRedOverlay);
		
		// initialize detail description of specific quest
		challengedoneTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_challengedone);
		progressbar = (ProgressBar) findViewById(R.id.quest_detail_progressBar);
		questNameTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_name);
		questDescriptionTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_description);

		questNameTextView.setText(questName);
		questDescriptionTextView.setText(questDescription);

		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation (final Location location) {
				lastKnownLocation = location;
			}
		});
		fineLocation.getLocation(this, locationResult);
		//handle on lick event on Me Button

		meButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				OverlayItem overlay = new OverlayItem(
						new GeoPoint(	(int) (lastKnownLocation.getLatitude() * 1E6),
								(int) (lastKnownLocation.getLongitude() * 1E6)),
								"My Current Location",
						"Hello");
				Drawable icon = getResources().getDrawable(R.drawable.map_circle_marker_red);
				icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
				overlay.setMarker(icon);

				Place place = new Place.Builder(lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude(), -1).build();
				itemizedGreenOverlay.addOverlay(overlay,place);

				mapController.animateTo(new GeoPoint(
						(int) (lastKnownLocation.getLatitude() * 1E6),
						(int) (lastKnownLocation.getLongitude() * 1E6)));
				mapController.setZoom(16);
			}	
		});

		//handle on click event when click on ViewSpot button
		viewSpotButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				double centerLongitude = 0;
				double centerLatitude = 0;
				for (int i=0;i < questDetailList.size();i++)
				{
					centerLongitude += questDetailList.get(i).getLongitude();
					centerLatitude += questDetailList.get(i).getLatitude();
				}
				centerLongitude = centerLongitude / questDetailList.size();
				centerLatitude = centerLatitude / questDetailList.size();
				mapController.animateTo(new GeoPoint(
						(int) (centerLatitude * 1E6),
						(int) (centerLongitude * 1E6)));
				mapController.setZoom(17);	
			}
		});

		// handle event when click on specific quest
		questDetailListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//	if (questDetailList.get(position).getStatus().equalsIgnoreCase("done"))
				//{

				//}
				//else
				{
					Intent intent = new Intent("com.csun.spotr.QuestActionActivity");
					Bundle extras = new Bundle();
					extras.putInt("place_id", questDetailList.get(position).getId());
					extras.putInt("position", position);
					intent.putExtras(extras);
					startActivityForResult(intent, DO_SPOT_CHALLENGE);
				}
			}
		});

		new GetQuestDetailTask(this).execute();

	}

	private static class GetQuestDetailTask extends AsyncTask<Integer, QuestDetailItem, Boolean> implements IAsyncTask<QuestDetailActivity> {

		private List<NameValuePair> clientData = new ArrayList<NameValuePair>();
		private WeakReference<QuestDetailActivity> ref;

		public GetQuestDetailTask(QuestDetailActivity a) {
			attach(a);
		}

		private JSONArray userJsonArray = null;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(QuestDetailItem... spots) {
			ref.get().updateAsyncTaskProgress(spots[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			// send user id
			clientData.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// send quest id
			clientData.add(new BasicNameValuePair("quest_id", Integer.toString(ref.get().questId)));
			clientData.add(new BasicNameValuePair("spot_id", Integer.toString(ref.get().spotId)));
			// retrieve data from server
			userJsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_QUEST_DETAIL_URL, clientData);
			if (userJsonArray != null) {
				try {
					for (int i = 0; i < userJsonArray.length(); ++i) {
						publishProgress(new QuestDetailItem(userJsonArray.getJSONObject(i).getInt("spots_tbl_id"), 
								userJsonArray.getJSONObject(i).getString("spots_tbl_name"), 
								userJsonArray.getJSONObject(i).getString("spots_tbl_description"),
								userJsonArray.getJSONObject(i).getDouble("spots_tbl_longitude"),
								userJsonArray.getJSONObject(i).getDouble("spots_tbl_latitude"),
								userJsonArray.getJSONObject(i).getString("quest_user_tbl_status")								
								));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetQuestDetailTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			ref.get().challengedoneTextView.setText(Integer.toString(ref.get().spotCompleted) + "/" + Integer.toString(numQuest));
			ref.get().progressbar.setProgress(100*ref.get().spotCompleted/numQuest);
			detach();
		}

		public void attach(QuestDetailActivity a) {
			ref = new WeakReference<QuestDetailActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	private static class GiveQuestPointTask extends AsyncTask<Integer, QuestDetailItem, Boolean> implements IAsyncTask<QuestDetailActivity> {

		private List<NameValuePair> clientData = new ArrayList<NameValuePair>();
		private WeakReference<QuestDetailActivity> ref;
		private JSONArray userJsonArray = null;

		public GiveQuestPointTask(QuestDetailActivity a) {
			attach(a);
		}
		public void attach(QuestDetailActivity a) {
			ref = new WeakReference<QuestDetailActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			clientData.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// send quest id
			clientData.add(new BasicNameValuePair("quest_id", Integer.toString(ref.get().questId)));
			// retrieve data from server
			userJsonArray = JsonHelper.getJsonArrayFromUrlWithData(GIVE_QUEST_POINT_URL, clientData);

			return null;
		}
	}
	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused");
		super.onPause();
	}

	public void updateAsyncTaskProgress(QuestDetailItem q) {
		questDetailList.add(q);
		if (q.getStatus().equalsIgnoreCase("done")) {
			this.spotCompleted++;
		}

		OverlayItem overlay = new OverlayItem(new GeoPoint((int)(q.getLatitude()*1E6), (int)(q.getLongitude()*1E6)),q.getName(),q.getDescription());
		Place place = new Place.Builder(q.getLongitude(),q.getLatitude(),q.getId()).build();
		if (q.getStatus().equalsIgnoreCase("done"))
		{
			itemizedRedOverlay.addOverlay(overlay, place);
		}
		else
		{
			itemizedGreenOverlay.addOverlay(overlay, place);
		}
		questDetailItemAdapter.notifyDataSetChanged();

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DO_SPOT_CHALLENGE) {
			if (resultCode == RESULT_OK) {
				spotId = data.getExtras().getInt("spot_id");
				if (this.spotCompleted == numQuest-1)
				{
					this.showDialog(0);
					new GiveQuestPointTask(this).execute();
				}
				this.spotCompleted = 0;
				questDetailList.clear();
				questDetailItemAdapter.notifyDataSetChanged();
				new GetQuestDetailTask(this).execute();
			}
		}
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			return new 
					AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_main_menu_treasure_pressed)
			.setTitle("Congratulation!")
			.setMessage("You have completed the quest!!!")
			.setPositiveButton("Quest List", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					Intent data1 = new Intent();
					setResult(RESULT_OK, data1);
					finish();

				}
			})
			.setNegativeButton("Back", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					dialog.dismiss();
				}
			}).create();


		case 1: 
			return new 
					AlertDialog.Builder(this)
			.setIcon(R.drawable.error_circle)
			.setTitle("Error Message")
			.setMessage("<undefined>")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {

				}
			}).create();
		}
		return null;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	public void updateAsyncTaskProgress(Place u) {
		// TODO Auto-generated method stub

	}
	
	public class CustomQuestItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
		private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
		private List<Place> places = new ArrayList<Place>();
		private Context context;

		public CustomQuestItemizedOverlay(Drawable defaultMarker, MapView mapView) {
			super(boundCenter(defaultMarker), mapView);
			context = mapView.getContext();
			populate();
		}

		public void addOverlay(OverlayItem overlay, Place place) {
			overlays.add(overlay);
			places.add(place);
			populate();
		}

		public void clear() {
			overlays.clear();
			places.clear();
		}

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
			if (places.get(index).getId() != -1) {
				Intent intent = new Intent("com.csun.spotr.QuestActionActivity");
				Bundle extras = new Bundle();
				extras.putInt("place_id", places.get(index).getId());
				
				intent.putExtras(extras);
				startActivityForResult(intent, DO_SPOT_CHALLENGE);
			}
			return true;
		}
	}
	
	 @Override
		public boolean onOptionsItemSelected(MenuItem item) {
			Intent intent;
			switch (item.getItemId()) {
				case R.id.options_menu_xml_item_setting_icon:
					intent = new Intent("com.csun.spotr.SettingsActivity");
					startActivity(intent);
					finish();
					break;
				case R.id.options_menu_xml_item_logout_icon:
					SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
					editor.clear();
					editor.commit();
					intent = new Intent("com.csun.spotr.LoginActivity");
					startActivity(intent);
					finish();
					break;
				case R.id.options_menu_xml_item_mainmenu_icon:
					intent = new Intent("com.csun.spotr.MainMenuActivity");
					startActivity(intent);
					finish();
					break;
					
				case R.id.options_menu_xml_item_toolbar_icon:
					HorizontalScrollView toolbar = (HorizontalScrollView)findViewById(R.id.quest_detail_xml_toolbar);
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
}