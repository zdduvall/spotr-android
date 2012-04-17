package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.csun.spotr.adapter.PlaceActionItemAdapter;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Place;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;


/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		The Missions tab content in Spots.
 **/
public class PlaceActionActivity 
	extends Activity 
		implements IActivityProgressUpdate<Challenge> {

	private static final String TAG = "(PlaceActionActivity)";
	private static final String GET_CHALLENGES_URL = "http://107.22.209.62/android/get_challenges_from_place.php";
	private static final String GET_SPOT_DETAIL_URL = "http://107.22.209.62/android/get_spot_detail.php";

	public int currentPlaceId = 0;
	public int currentChosenItem;
	public ListView list = null;
	private	PlaceActionItemAdapter adapter = null;
	private List<Challenge> challengeList = new ArrayList<Challenge>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_action);
		// get place id
		Bundle extrasBundle = getIntent().getExtras();
		currentPlaceId = extrasBundle.getInt("place_id");

		setupInfoButton();
		setupTreasureButton();
		setupLootButton();
		setupListView();
		// run GetPlaceDetailTask
		new GetPlaceDetailTask(PlaceActionActivity.this).execute();
		// run GetChallengeTask
		new GetChallengesTask(PlaceActionActivity.this).execute();
	}

	
	
	private void setupLootButton() {
		Button buttonLoot = (Button) findViewById(R.id.place_action_xml_button_loot);
		buttonLoot.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Bundle extras = new Bundle();
				extras.putInt("place_id", currentPlaceId);
				Intent intent = new Intent(PlaceActionActivity.this.getApplicationContext(), PlaceLootActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
				
			}
		});
	}
	
	
	private void setupInfoButton() {
		Button buttonMoreInfo = (Button) findViewById(R.id.place_info_xml_button_moreinfo);
		buttonMoreInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Bundle extras = new Bundle();
				extras.putInt("place_id", currentPlaceId);
				Intent intent = new Intent(PlaceActionActivity.this.getApplicationContext(), PlaceInfoActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
	}

	private void setupTreasureButton() {
		/**
		 * Description
		 * To open a treasure, user need to have at least 1000 pts. 
		 * However this not a guarantee that they will earn a treasure
		 * There are 50% chances for that to happen. If they agree to take
		 * the risk, then the system will subtract 1000 pts from their 
		 * account (users_tbl_points).
		 * 
		 * NOTE: subtracting points will be done in server side, and
		 * this feature is not yet implemented.
		 * 
		 * Plan: Sprint 10
		 **/
		Button buttonTreasure = (Button) findViewById(R.id.place_action_xml_button_treasure);
		buttonTreasure.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				 AlertDialog.Builder builder = new AlertDialog.Builder(PlaceActionActivity.this);
				 builder.setMessage("(Treasures == 1000 pts) IsWorthIt()?")
				 	    .setCancelable(false)
				        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				        	public void onClick(DialogInterface dialog, int id) {
				        		// create a random generator
				        		Random random = new Random();

				        		// only run GenerateTreasureActivity if 
				        		// the random number return is greater than 0.5
				            	if (random.nextDouble() > 0.5) {
				            		Bundle extras = new Bundle();
				            		extras.putInt("place_id", currentPlaceId);
				            		Intent intent = new Intent(getApplicationContext(), GenerateTreasureActivity.class);
				            		intent.putExtras(extras);
				            		startActivity(intent);
				            	}
				            	else {
				            		Intent intent = new Intent(getApplicationContext(), NoTreasureActivity.class);
				            		startActivity(intent);
				            	}
				            }
				         })
				         .setNegativeButton("No", new DialogInterface.OnClickListener() {
				        	 public void onClick(DialogInterface dialog, int id) {
				        		 dialog.cancel();
				             }
				           });

				AlertDialog alert = builder.create();
				alert.show();
			}
		});
	}
	
	private void setupListView() {
		// initialize list view of challenges
		list = (ListView) findViewById(R.id.place_action_xml_listview_actions);
		adapter = new PlaceActionItemAdapter(this, challengeList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Challenge c = (Challenge) challengeList.get(position);
				// set current item chosen so that later we can make some side effects
				currentChosenItem = position;

				if (c.getType() == Challenge.Type.CHECK_IN) {
					Intent intent = new Intent("com.csun.spotr.CheckInActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.WRITE_ON_WALL) {
					Intent intent = new Intent("com.csun.spotr.WriteOnWallActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.SNAP_PICTURE) {
					Intent intent = new Intent("com.csun.spotr.SnapPictureActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.SNAP_PICTURE_CHALLENGE) {
					Intent intent = new Intent("com.csun.spotr.SnapPictureChallengeActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("description", c.getDescription());
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.QUESTION_ANSWER) {
					Intent intent = new Intent("com.csun.spotr.QuestionAnswerActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					extras.putString("question_description", c.getDescription());
					intent.putExtras(extras);
					startActivity(intent);
				}
				else if (c.getType() == Challenge.Type.DROP_ITEM) {
					Intent intent = new Intent("com.csun.spotr.DropItemActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					//extras.putString("question_description", c.getDescription());
					intent.putExtras(extras);
					startActivity(intent);
				}
				else { // c.getType == Challenge.Type.OTHER 

				}
			}
		});
	}

	private static class GetChallengesTask 
		extends AsyncTask<String, Challenge, Boolean> 
			implements IAsyncTask<PlaceActionActivity> {

		private static final String TAG = "[AsyncTask].GetChallengeTask";
		private WeakReference<PlaceActionActivity> ref;

		public GetChallengesTask(PlaceActionActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
	    protected void onProgressUpdate(Challenge... c) {
			ref.get().updateAsyncTaskProgress(c[0]);
	    }

		@Override
		protected Boolean doInBackground(String... text) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("place_id", Integer.toString(ref.get().currentPlaceId)));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_CHALLENGES_URL, data);

			if (array != null) { 
				try {
					for (int i = 0; i < array.length(); ++i) {
						if (!array.getJSONObject(i).getString("challenges_tbl_type").equals("OTHER")) {
							publishProgress(								
							new Challenge.Builder(
									// required parameters
									array.getJSONObject(i).getInt("challenges_tbl_id"),
									Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")),
									array.getJSONObject(i).getInt("challenges_tbl_points")) 
										// optional parameters
										.name(array.getJSONObject(i).getString("challenges_tbl_name"))
										.description(array.getJSONObject(i).getString("challenges_tbl_description"))
											.build());
						}
					}
				}
				catch (JSONException e) {
					Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
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

		public void attach(PlaceActionActivity a) {
			ref = new WeakReference<PlaceActionActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	private static class GetPlaceDetailTask 
		extends AsyncTask<Void, Integer, Place> 
			implements IAsyncTask<PlaceActionActivity> {

		private static final String TAG = "[AsyncTask].GetPlaceDetailTask]";
		private WeakReference<PlaceActionActivity> ref;

		public GetPlaceDetailTask(PlaceActionActivity a) {
			attach(a);
		}

		@Override
		protected Place doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("place_id", Integer.toString(ref.get().currentPlaceId)));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_SPOT_DETAIL_URL, data);
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
			ref.get().updatePlaceDetailAsyncTaskProgress(p);
			detach();
		}

		public void attach(PlaceActionActivity a) {
			ref = new WeakReference<PlaceActionActivity>(a);
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

	public void updateAsyncTaskProgress(Challenge c) {
		challengeList.add(c);
		adapter.notifyDataSetChanged();
	}


	public void updatePlaceDetailAsyncTaskProgress(final Place p) {
		TextView name = (TextView) findViewById(R.id.place_activity_xml_textview_name);
		name.setText(p.getName());

		TextView description = (TextView) findViewById(R.id.place_activity_xml_textview_description);
		String formattedAddress = formatAddress(p.getAddress());

		description.setText(formattedAddress);//p.getAddress());
	}

	/**
	 * NOTE: THIS IS A TEMPORARY IMPLEMENTATION
	 * Formats an address string to have line breaks. 
	 * @param address the string retrieved from the database
	 * @return a nicely formatted string
	 */
	private String formatAddress(String address) {
		String[] parts = address.split("\\, ");

		// Check that we get 4 substrings:
		//	   1) street
		//     2) city
		//     3) state and zip
		//     4) country --> ignored
		// Otherwise, return the address unchanged. 
		if (parts.length == 4) {
			address = "";			
			String[] stateAndZip = parts[2].split("\\ ");
			address = parts[0] + "\n"
					+ parts[1] + ", " + stateAndZip[0] + ", " + stateAndZip[1];		
		}
		return address;
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			return new 
				AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Warning!")
					.setMessage("You checked in recently. You can only check in once every 24 hours. :(!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

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
