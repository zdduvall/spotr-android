package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csun.spotr.core.Place;
import com.csun.spotr.core.adapter_item.QuestDetailItem;
import com.csun.spotr.custom_gui.BalloonItemizedOverlay;
import com.csun.spotr.custom_gui.ImpactOverlay;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.FineLocation.LocationResult;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.PlaceIconUtil;

import com.csun.spotr.adapter.QuestDetailItemAdapter;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class QuestDetailActivity 
extends MapActivity 
implements IActivityProgressUpdate<QuestDetailItem>{

	private static final String TAG = "(QuestDetailActivity)";
	private static final String GET_QUEST_DETAIL_URL = "http://107.22.209.62/android/get_quest_detail.php";
	private static final String GIVE_QUEST_POINT_URL = "http://107.22.209.62/android/give_quest_point.php";

	private static int numQuest = 0;

	private ListView questDetailListView;
	private QuestDetailItemAdapter questDetailItemAdapter;
	private List<QuestDetailItem> questDetailList = new ArrayList<QuestDetailItem>();

	private int questId;
	private int userId;
	private int spotCompleted = 0;
	private int spotId = 0;
	private String questName = null;
	private String questDescription = null;
	private String questUrl = null;

	private	FineLocation fineLocation = new FineLocation();
	public	Location lastKnownLocation = null;

	private static final int DO_SPOT_CHALLENGE = 1;  // code number to send to child activity
	private static final int RANGE_LIMIT = 600;      // range_limit of user, unit: meter
	private static boolean flagMeButton = false;

	private CustomQuestItemizedOverlay itemizedGreenOverlay;
	private CustomQuestItemizedOverlay itemizedRedOverlay;

	private MapView mapView;
	private MapController mapController;
	private List<Overlay> mapOverlays;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quest_detail);

		initQuestDataFromIntent();

		setupMapView();

		setupListView();

		setupViewSpotButton();

		setupMeButton();

		setupQuestTitle();

		findLocation();

		new GetQuestDetailTask(this).execute();
	} 
	private void initQuestDataFromIntent() {
		// Get data from super activity
		questId = this.getIntent().getExtras().getInt("quest_id");
		numQuest = this.getIntent().getExtras().getInt("numberChallenges");
		questName = this.getIntent().getExtras().getString("quest_name");
		questDescription = this.getIntent().getExtras().getString("quest_description");
		questUrl = this.getIntent().getExtras().getString("quest_url");
		userId = CurrentUser.getCurrentUser().getId();
	}

	private void setupQuestTitle() {
		// initialize detail description of specific quest
		TextView questNameTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_name);
		TextView questDescriptionTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_description);

		questNameTextView.setText(questName);
		questDescriptionTextView.setText(questDescription);
		ImageView imageViewQuestPicture = (ImageView) findViewById(R.id.quest_detail_xml_imageview_quest_picture);
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		imageLoader.displayImage(questUrl, imageViewQuestPicture);
	}

	private void findLocation() {
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;
				activateMeAndSpotButton();
			}
		});
		fineLocation.getLocation(this, locationResult);
	}

	private void activateMeAndSpotButton() {
		final Button meButton = (Button) findViewById(R.id.quest_detail_xml_me_button);
		meButton.setEnabled(true);
		final Button viewSpotButton = (Button) findViewById(R.id.quest_detail_xml_spot_button);
		viewSpotButton.setEnabled(true);
	}

	private void setupMeButton() {
		final Button meButton = (Button) findViewById(R.id.quest_detail_xml_me_button);
		meButton.setEnabled(false);
		meButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (flagMeButton == false) {
					OverlayItem overlay = new OverlayItem(
							new GeoPoint(	
									(int) (lastKnownLocation.getLatitude() * 1E6),
									(int) (lastKnownLocation.getLongitude() * 1E6)),
									"My Current Location",
							"Hello");

					Drawable icon = getResources().getDrawable(R.drawable.ic_map_person);
					icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
					overlay.setMarker(icon);

					QuestDetailItem item = new QuestDetailItem.Builder(-1,lastKnownLocation.getLongitude(), lastKnownLocation.getLatitude()).build();
					itemizedGreenOverlay.addOverlay(overlay,item);

					mapOverlays.add(
							new ImpactOverlay(
									new GeoPoint(	
											(int) (lastKnownLocation.getLatitude() * 1E6),
											(int) (lastKnownLocation.getLongitude() * 1E6)), RANGE_LIMIT));
					flagMeButton = true;
				}

				mapController.animateTo(new GeoPoint(
						(int) (lastKnownLocation.getLatitude() * 1E6),
						(int) (lastKnownLocation.getLongitude() * 1E6)));
				mapController.setZoom(17);
			}	
		});
	}

	private void setupViewSpotButton() {
		final Button viewSpotButton = (Button) findViewById(R.id.quest_detail_xml_spot_button);
		viewSpotButton.setEnabled(false);
		//handle on click event when click on ViewSpot button
		viewSpotButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				double centerLongitude = 0;
				double centerLatitude = 0;
				for (int i = 0; i < questDetailList.size(); i++) {
					centerLongitude += questDetailList.get(i).getLongitude();
					centerLatitude += questDetailList.get(i).getLatitude();
				}
				centerLongitude = centerLongitude / questDetailList.size();
				centerLatitude = centerLatitude / questDetailList.size();
				mapController.animateTo(new GeoPoint((int) (centerLatitude * 1E6), (int) (centerLongitude * 1E6)));
				mapController.setZoom(16);	
			}
		});
	}

	private void setupMapView() {
		// initialize Map View
		mapView = (MapView) findViewById(R.id.quest_detail_xml_map);
		mapController = mapView.getController();
		mapView.setBuiltInZoomControls(true);

		// add overlay 
		mapOverlays = mapView.getOverlays();

		itemizedGreenOverlay = 
				new CustomQuestItemizedOverlay(getResources().getDrawable(R.drawable.map_maker_green), mapView);
		itemizedRedOverlay = 
				new CustomQuestItemizedOverlay(getResources().getDrawable(R.drawable.map_maker_red), mapView);

		mapOverlays.add(itemizedGreenOverlay);
		mapOverlays.add(itemizedRedOverlay);
	}

	private void setupListView() {
		questDetailListView = (ListView) findViewById(R.id.quest_detail_xml_listview_quest_list);
		questDetailItemAdapter = new QuestDetailItemAdapter(this.getApplicationContext(), questDetailList);
		questDetailListView.setAdapter(questDetailItemAdapter);
		// handle event when click on specific spot in the ListView
		questDetailListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (questDetailList.get(position).getStatus().equalsIgnoreCase("done")) {

				}
				else {
					Intent intent = new Intent("com.csun.spotr.QuestActionActivity");
					Bundle extras = new Bundle();
					extras.putInt("place_id", questDetailList.get(position).getId());
					extras.putInt("position", position);
					extras.putString("name",questDetailList.get(position).getName());
					extras.putString("description", questDetailList.get(position).getDescription());
					intent.putExtras(extras);
					startActivityForResult(intent, DO_SPOT_CHALLENGE);
				}
			}
		});
	}

	private static class GetQuestDetailTask 
	extends AsyncTask<Integer, QuestDetailItem, Boolean> 
	implements IAsyncTask<QuestDetailActivity> {

		private static final String TAG = "[AsyncTask].GetQuestDetailTask";
		private WeakReference<QuestDetailActivity> ref;

		public GetQuestDetailTask(QuestDetailActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {

		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			data.add(new BasicNameValuePair("quest_id", Integer.toString(ref.get().questId)));
			data.add(new BasicNameValuePair("spot_id", Integer.toString(ref.get().spotId)));
			return data;
		}

		@Override
		protected void onProgressUpdate(QuestDetailItem... spots) {
			ref.get().updateAsyncTaskProgress(spots[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			List<NameValuePair> data = prepareUploadData();
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_QUEST_DETAIL_URL, data);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
								new QuestDetailItem.Builder(array.getJSONObject(i).getInt("spots_tbl_id"),
										array.getJSONObject(i).getString("spots_tbl_name"), 
										array.getJSONObject(i).getString("spots_tbl_description"))

								.longitude(array.getJSONObject(i).getDouble("spots_tbl_longitude"))
								.latitude(array.getJSONObject(i).getDouble("spots_tbl_latitude"))
								.status(array.getJSONObject(i).getString("quest_user_tbl_status"))
								.url(array.getJSONObject(i).getString("spots_tbl_url"))
								.questSpotId(array.getJSONObject(i).getInt("quest_spot_tbl_id")).build());

					}
				}
				catch (JSONException e) {
					Log.e(TAG + ".doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			updateProgressBar();
			detach();
		}

		public void attach(QuestDetailActivity a) {
			ref = new WeakReference<QuestDetailActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

		private void updateProgressBar() {
			TextView challengedoneTextView = (TextView) (ref.get()).findViewById(R.id.quest_detail_xml_textview_challengedone);
			challengedoneTextView.setText(Integer.toString(ref.get().spotCompleted) + "/" + Integer.toString(numQuest));
			ProgressBar progressbar = (ProgressBar) (ref.get()).findViewById(R.id.quest_detail_progressBar);
			progressbar.setProgress(100 * ref.get().spotCompleted / numQuest);
		}
	}

	/*
	 *  An AsyncTask to update points of user when they complete the whole quest 
	 *  NOTE: just run only once.
	 */
	private static class GiveQuestPointTask 
	extends AsyncTask<Void, Void, Void> 
	implements IAsyncTask<QuestDetailActivity> {

		private static final String TAG = "[AsyncTask].GiveQuestPointTask";
		private WeakReference<QuestDetailActivity> ref;
		private int userId;
		private int questId;

		public GiveQuestPointTask(QuestDetailActivity a, int userId, int questId) {
			attach(a);
			this.userId = userId;
			this.questId = questId;
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("id", Integer.toString(userId)));
			data.add(new BasicNameValuePair("quest_id", Integer.toString(questId)));
			return data;
		}

		@Override
		protected Void doInBackground(Void... voids) {
			List<NameValuePair> data = prepareUploadData();
			/** 
			 * TODO: handle error return from server
			 **/
			Log.v(TAG, "TODO: require handling error from server");

			JsonHelper.getJsonArrayFromUrlWithData(GIVE_QUEST_POINT_URL, data);
			return null;
		}

		public void attach(QuestDetailActivity a) {
			ref = new WeakReference<QuestDetailActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

	}

	public void updateAsyncTaskProgress(QuestDetailItem q) {
		questDetailList.add(q);
		if (q.getStatus().equalsIgnoreCase("done")) {
			this.spotCompleted++;
		}

		OverlayItem overlay = new OverlayItem(new GeoPoint((int)(q.getLatitude()*1E6), (int)(q.getLongitude()*1E6)), q.getName(), q.getTruncatedDescription());
		
		// custom icon
		overlay.setMarker(PlaceIconUtil.getMapIconByType(this, 0));
		
		//Place place = new Place.Builder(q.getLongitude(), q.getLatitude(), q.getId()).build();

		if (q.getStatus().equalsIgnoreCase("done")) 
			itemizedRedOverlay.addOverlay(overlay, q);
		else 
			itemizedGreenOverlay.addOverlay(overlay, q);

		questDetailItemAdapter.notifyDataSetChanged();
	}

	// get Data back from child Activity, and run GiveQuestPointTask if complete the whole quest.
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DO_SPOT_CHALLENGE) {
			if (resultCode == RESULT_OK) {
				spotId = data.getExtras().getInt("spot_id");
				Toast.makeText(getApplicationContext(), Integer.toString(spotId), Toast.LENGTH_SHORT).show();
				if (this.spotCompleted == numQuest - 1) {
					this.showDialog(0);
					new GiveQuestPointTask(this, userId, questId).execute();
				}

				this.spotCompleted = 0;
				questDetailList.clear();
				questDetailItemAdapter.notifyDataSetChanged();
				new GetQuestDetailTask(this).execute();
			}
		}
	}

	/*
	 * Dialog to congratulate the user when they finish the whole quest--- 
	 * Can link to get weapon activity later.(non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
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

	/*
	 *  Inner class to draw Overlay balloon 
	 *  over each spot of the quest in MapView
	 */
	public class CustomQuestItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
		private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
		private List<QuestDetailItem> places = new ArrayList<QuestDetailItem>();

		public CustomQuestItemizedOverlay(Drawable defaultMarker, MapView mapView) {
			super(boundCenter(defaultMarker), mapView);
			populate();
		}

		public void addOverlay(OverlayItem overlay, QuestDetailItem item) {
			overlays.add(overlay);
			places.add(item);
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

		/*
		 *  Handle event when user click on the spot in MapView. 
		 *  If in range_limit, they can do the mission at that spot, 
		 *  if not a TOAST will pop up.(non-Javadoc)
		 *  @see com.csun.spotr.custom_gui.BalloonItemizedOverlay#onBalloonTap(int, com.google.android.maps.OverlayItem)
		 */
		@Override
		protected boolean onBalloonTap(int index, OverlayItem item) {
			Location spot = new Location("Current Spot");
			spot.setLongitude(places.get(index).getLongitude());
			spot.setLatitude(places.get(index).getLatitude());
			if (!item.getTitle().equalsIgnoreCase("My Current Location")) {
				if (spot.distanceTo(lastKnownLocation) < RANGE_LIMIT) {
					Intent intent = new Intent("com.csun.spotr.QuestActionActivity");
					Bundle extras = new Bundle();
					extras.putInt("place_id", places.get(index).getId());
					extras.putString("name",places.get(index).getName());
					extras.putString("description", places.get(index).getDescription());
					extras.putString("imageUrl",places.get(index).getUrl());
					extras.putInt("questSpotId",places.get(index).getQuestSpotId());
					intent.putExtras(extras);
					startActivityForResult(intent, DO_SPOT_CHALLENGE);
				}	
				else {
					Toast.makeText(getApplicationContext(), "Keep walking, dude", Toast.LENGTH_SHORT).show();
				}
			}
			else {
				Toast.makeText(getApplicationContext(), "I am here!!", Toast.LENGTH_SHORT).show();
			}
			return true;
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		flagMeButton = false;
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
	public void onResume() {
		Log.v(TAG, "I'm resumed");
		super.onResume();
	}
}