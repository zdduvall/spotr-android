package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.adapter.CheckInUserItemAdapter;
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Toast;

public class CheckInActivity 
	extends Activity 
		implements IActivityProgressUpdate<String> {
	
	private static final String TAG = "(CheckInActivity)";
	private static final String DO_CHECK_IN_URL = "http://107.22.209.62/android/do_check_in.php";
	private static final String GET_CHECKIN_USERS_URL = "http://107.22.209.62/android/get_checkin_users.php";
	
	private String usersId;
	private String spotsId;
	private String challengesId;
	private CheckInTask task;
	private List<String> userImageList;
	private	Gallery gallery;
	private CheckInUserItemAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_in);
		
		Bundle extras = getIntent().getExtras();
		usersId = extras.getString("users_id");
		spotsId = extras.getString("spots_id");
		challengesId = extras.getString("challenges_id");
		
		Button checkin = (Button) findViewById(R.id.check_in_xml_button_checkin);
		checkin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				task = new CheckInTask(CheckInActivity.this, usersId, spotsId, challengesId);
				task.execute();
			}
		});
		
		userImageList = new ArrayList<String>();
		gallery = (Gallery) findViewById(R.id.check_in_xml_gallery_checkin_people);
		adapter = new CheckInUserItemAdapter(this, userImageList);
		gallery.setAdapter(adapter);
		
		new GetCheckInUsersTask(this, spotsId).execute();
	}
	
	private static class CheckInTask 
		extends AsyncTask<Void, Void, String> 
			implements IAsyncTask<CheckInActivity> {
	
		private WeakReference<CheckInActivity> ref;
		private String usersId;
		private String spotsId;
		private String challengesId;
	
		public CheckInTask(CheckInActivity a, String usersId, String spotsId, String challengesId) {
			attach(a);
			this.usersId = usersId;
			this.spotsId = spotsId;
			this.challengesId = challengesId;
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected String doInBackground(Void... voids) {
			/*
			 * 1. Retrieve data from [activity] table where $users_id and $places_id
			 * 2. Check the result of this query:
			 * 		a. If the result is null, then user hasn't visited this place yet which also implies that he has not done any challenges.
			 * 		   Thus we can update the current user:
			 * 		   i.  Update [activity] table with $users_id, $places_id, $challenges_id   
			 * 		   ii. Update [users] table with 
			 * 			   + $challenges_done = $challenges_done + 1
			 * 			   + $points += challenges.points
			 * 			   + $places_visited = $places_visited + 1
			 * 		b. If the result is not null, update [activity] table with $users_id, $places_id, $challenges_id with CURRENT_TIMESTAMP, but
			 * 		   don't run the statement:
			 * 		       + $places_visited = $places_visited + 1
			 * 3. All these complexity is done at server side, i.e. php script, so we only need to post to the server three parameters:
			 * 	    a. users_id
			 * 		b. places_id
			 * 		c. challenges_id
			 * 4. The return of this query is the number points is added the points added to the user account.
			 */
			
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("users_id", usersId));
			data.add(new BasicNameValuePair("spots_id", spotsId));
			data.add(new BasicNameValuePair("challenges_id", challengesId));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(DO_CHECK_IN_URL, data);
			
			String result = "";
			
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "CheckInTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return result;
		}
	
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				Button checkin = (Button) ref.get().findViewById(R.id.check_in_xml_button_checkin);
				checkin.setBackgroundColor(Color.RED);
			}
			else {
				ref.get().showDialog(0);
			}
			detach();
		}
	
		public void attach(CheckInActivity a) {
			ref = new WeakReference<CheckInActivity>(a);
		}
	
		public void detach() {
			ref.clear();
		}
	}
	
	private static class GetCheckInUsersTask 
		extends AsyncTask<Integer, String, Boolean> 
			implements IAsyncTask<CheckInActivity> {
	
		private WeakReference<CheckInActivity> ref;
		private String spotsId;
		
		public GetCheckInUsersTask(CheckInActivity a, String spotsId) {
			Log.v(TAG, "run?");
			attach(a);
			this.spotsId = spotsId;
		}


		@Override
		protected void onProgressUpdate(String... s) {
			ref.get().updateAsyncTaskProgress(s[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("place_id", spotsId));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_CHECKIN_USERS_URL, data);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							array.getJSONObject(i).getString("users_tbl_user_image_url"));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetCheckInUsersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(CheckInActivity a) {
			ref = new WeakReference<CheckInActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
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

	public void updateAsyncTaskProgress(String u) {
		userImageList.add(u);
		adapter.notifyDataSetChanged();
	}
}
