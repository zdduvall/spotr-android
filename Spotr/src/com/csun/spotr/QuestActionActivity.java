package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.adapter.PlaceActionItemAdapter;
import com.csun.spotr.adapter.QuestActionItemAdapter;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

/**
 * Description:
 * 		The Missions tab content in Spots.
 */
public class QuestActionActivity 
	extends Activity 
		implements IActivityProgressUpdate<Challenge> {
	
	private static final String TAG = "(QuestActionActivity)";
	private static final String GET_QUEST_CHALLENGES_URL = "http://107.22.209.62/android/get_quest_from_place.php";
	private static final String DO_CHECK_IN_URL = "http://107.22.209.62/android/do_check_in.php";
	private static final int DO_ACTION_ACTIVITY = 0;
	
	public int currentPlaceId;
	public int currentquestSpotId;
	
	public int currentSpotPosition;
	public int currentChosenItem;
	public int currentUserId = -1;
	
	
	public ListView list = null;
	private	QuestActionItemAdapter adapter = null;
	private List<Challenge> challengeList = new ArrayList<Challenge>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quest_action);
		Bundle extrasBundle = getIntent().getExtras();
		currentPlaceId = extrasBundle.getInt("place_id");
		currentSpotPosition = extrasBundle.getInt("position");
		currentquestSpotId = extrasBundle.getInt("questSpotId");
		currentUserId = CurrentUser.getCurrentUser().getId();
		displayTitle(extrasBundle);
		setupListView();
		new GetChallengesTask(QuestActionActivity.this).execute();
	}
	
	private void displayTitle(Bundle extrasBundle) {
		TextView nameTextView = (TextView) findViewById(R.id.quest_action_xml_name);
		TextView descriptionTextView = (TextView) findViewById(R.id.quest_action_xml_description);
		nameTextView.setText(extrasBundle.getString("name"));
		descriptionTextView.setText(extrasBundle.getString("description"));
		ImageView imageViewQuestPicture = (ImageView) findViewById(R.id.quest_action_xml_image);
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		imageLoader.displayImage(extrasBundle.getString("imageUrl"), imageViewQuestPicture);
	}
	
	private void setupListView() {
		// initialize list view of challenges
		list = (ListView) findViewById(R.id.quest_action_xml_listview_actions);
		adapter = new QuestActionItemAdapter(this, challengeList);
				
		TextView padding = new TextView(getApplicationContext());
		padding.setHeight(0);
		
		// add top padding to first item and add bottom padding to last item
		list.addHeaderView(padding, null, false);
		list.addFooterView(padding, null, false);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Challenge c = (Challenge) list.getAdapter().getItem(position);//challengeList.get(position);
				// set current item chosen so that later we can make some side effects
				currentChosenItem = c.getId();
				
				if (c.getType() == Challenge.Type.CHECK_IN) {
					Intent intent = new Intent("com.csun.spotr.CheckInActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivityForResult(intent, DO_ACTION_ACTIVITY);
					
				}
				else if (c.getType() == Challenge.Type.WRITE_ON_WALL) {
					Intent intent = new Intent("com.csun.spotr.WriteOnWallActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivityForResult(intent, DO_ACTION_ACTIVITY);
						
				}
				else if (c.getType() == Challenge.Type.SNAP_PICTURE) {
					Intent intent = new Intent("com.csun.spotr.SnapPictureActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					intent.putExtras(extras);
					startActivityForResult(intent, DO_ACTION_ACTIVITY);
					
				}
				else if (c.getType() == Challenge.Type.QUESTION_ANSWER) {
					Intent intent = new Intent("com.csun.spotr.QuestionAnswerActivity");
					Bundle extras = new Bundle();
					extras.putString("users_id", Integer.toString(CurrentUser.getCurrentUser().getId()));
					extras.putString("spots_id", Integer.toString(currentPlaceId));
					extras.putString("challenges_id", Integer.toString(c.getId()));
					extras.putString("question_description", c.getDescription());
					intent.putExtras(extras);
					startActivityForResult(intent,DO_ACTION_ACTIVITY);
				}
				else { // c.getType == Challenge.Type.OTHER 
					
				}
			}
		});
	}
	
	private static class GetChallengesTask 
		extends AsyncTask<Void, Challenge, Boolean> 
			implements IAsyncTask<QuestActionActivity> {
		
		private static final String TAG = "[AsyncTask].GetChallengeTask";
		private WeakReference<QuestActionActivity> ref;
		
		public GetChallengesTask(QuestActionActivity questActionActivity) {
			attach(questActionActivity);
		}
		
		@Override
		protected void onPreExecute() {
			
		}
		
		@Override
	    protected void onProgressUpdate(Challenge... c) {
			ref.get().updateAsyncTaskProgress(c[0]);
	    }

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("place_id", Integer.toString(ref.get().currentPlaceId)));
			data.add(new BasicNameValuePair("questSpotId", Integer.toString(ref.get().currentquestSpotId)));
			data.add(new BasicNameValuePair("user_id", Integer.toString(ref.get().currentUserId)));
			data.add(new BasicNameValuePair("challenge_id", Integer.toString(ref.get().currentChosenItem)));
			
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_QUEST_CHALLENGES_URL, data);
			if (array != null) { 
				try {
					for (int i = 0; i < array.length(); ++i) {
						//if (!array.getJSONObject(i).getString("challenges_tbl_type").equals("OTHER")) {
							publishProgress(								
								new Challenge.Builder(
									// required parameters
									array.getJSONObject(i).getInt("challenges_tbl_id"),
									Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")),
									array.getJSONObject(i).getInt("challenges_tbl_points")) 
										// optional parameters
										.name(array.getJSONObject(i).getString("challenges_tbl_name"))
										.description(array.getJSONObject(i).getString("challenges_tbl_description"))
										.status(array.getJSONObject(i).getString("mission_user_tbl_status"))
											.build());
					//	}
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

		public void attach(QuestActionActivity questActionActivity) {
			ref = new WeakReference<QuestActionActivity>(questActionActivity);
		}

		public void detach() {
			ref.clear();
		}

	}
		
	protected void onActivityResult(int requestCode, int resultCode,Intent data) {
		if (requestCode == DO_ACTION_ACTIVITY) {
			if (resultCode == RESULT_OK) {
				challengeList.clear();
				adapter.notifyDataSetChanged();
				new GetChallengesTask(QuestActionActivity.this).execute();
				/*Intent i = new Intent();
				i.putExtra("spot_id", currentPlaceId);
				setResult(RESULT_OK, i);
				finish();*/
			}
    	}
	}

	public void updateAsyncTaskProgress(Challenge c) {
		challengeList.add(c);
		adapter.notifyDataSetChanged();
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
							Intent data1 = new Intent();
							data1.putExtra("position", currentSpotPosition);
							setResult(RESULT_CANCELED, data1);
							//---closes the activity---
							//finish();
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
