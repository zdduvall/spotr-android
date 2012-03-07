package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.LeaderboardItemAdapter;
import com.csun.spotr.core.User;


/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class LeaderboardActivity 
	extends Activity 
		implements IActivityProgressUpdate<User> {
	
	private static final String TAG = "(LeaderboardActivity)";
	private static final String GET_USERS_URL = "http://107.22.209.62/android/get_users.php";
	private ListView listview = null;
	private LeaderboardItemAdapter adapter = null;
	private List<User> userList = new ArrayList<User>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard);
		setupListView();
		new GetUsersTask(this).execute();				
	}
	
	private void setupListView() {
		// initialize list view
		listview = (ListView) findViewById(R.id.leaderboard_xml_listview_users);		
		adapter = new LeaderboardItemAdapter(LeaderboardActivity.this, userList);
		listview.setAdapter(adapter);
	}
	
	private static class GetUsersTask 
		extends AsyncTask<Void, User, Boolean> 
			implements IAsyncTask<LeaderboardActivity> {
		
		private static final String TAG = "[AsyncTask].GetUsersTask";
		private  ProgressDialog progressDialog = null;
		private WeakReference<LeaderboardActivity> ref;
		int position = 0;
		
		public GetUsersTask(LeaderboardActivity a) {
			attach(a);
		}
		
		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}
		
		@Override
	    protected void onProgressUpdate(User... users) {
			progressDialog.dismiss();
			ref.get().updateAsyncTaskProgress(users[0]);
	    }
		
		@Override
		protected Boolean doInBackground(Void...voids) {
			JSONArray array = JsonHelper.getJsonArrayFromUrl(GET_USERS_URL);
			if (array != null) { 
				try {
					for (int i = 0; i < array.length(); ++i) {
						// update user's location
						if (array.getJSONObject(i).getInt("users_tbl_id") == CurrentUser.getCurrentUser().getId()) {
							position = i;
						}
						
						publishProgress(
							new User.Builder(
								// required parameters
								array.getJSONObject(i).getInt("users_tbl_id"),
								array.getJSONObject(i).getString("users_tbl_username"),
								array.getJSONObject(i).getString("users_tbl_password"))
									// optional parameters
									.challengesDone(array.getJSONObject(i).getInt("users_tbl_challenges_done"))
									.placesVisited(array.getJSONObject(i).getInt("users_tbl_places_visited"))
									.points(array.getJSONObject(i).getInt("users_tbl_points"))
									.rank(array.getJSONObject(i).getInt("users_tbl_rank"))
										.build());
					}	
				}
				catch (JSONException e) {
					Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			ref.get().updateRank(position);
			detach();
		}

		public void attach(LeaderboardActivity a) {
			ref = new WeakReference<LeaderboardActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	private void updateRank(int position) {
		adapter.setPositionFound(position);
		listview.setSelection(position);
	}
	
	public void updateAsyncTaskProgress(User u) {
		userList.add(u);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.leaderboard_menu, menu);
		return true;
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