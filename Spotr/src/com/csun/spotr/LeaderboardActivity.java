package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.R.color;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.Button;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.LeaderboardItemAdapter;
import com.csun.spotr.core.User;

public class LeaderboardActivity extends Activity {
	private static final String TAG = "(LeaderboardActivity)";
	private static final String GET_USERS_URL = "http://107.22.209.62/android/get_users.php";
	private ListView listview = null;
	private LeaderboardItemAdapter adapter = null;
	private List<User> userList = new ArrayList<User>();
	private boolean small_view = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard);
		
		setupListView();
		new GetUsersTask().execute();				
	}
	
	private void setupListView() {
		// initialize list view
		listview = (ListView) findViewById(R.id.leaderboard_xml_listview_users);		
		adapter = new LeaderboardItemAdapter(LeaderboardActivity.this, userList);
		listview.setAdapter(adapter);
	}
	
	private class GetUsersTask extends AsyncTask<Void, User, Boolean> {
		private  ProgressDialog progressDialog = null;
		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(LeaderboardActivity.this);
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}
		
		@Override
	    protected void onProgressUpdate(User... users) {
			progressDialog.dismiss();
			userList.add(users[0]);
			adapter.notifyDataSetChanged();
	    }
		
		@Override
		protected Boolean doInBackground(Void...voids) {
			JSONArray array = JsonHelper.getJsonArrayFromUrl(GET_USERS_URL);
			if (array != null) { 
				try {
					
					User current_user = CurrentUser.getCurrentUser();
					int position = current_user.getRank();
					String name = current_user.getUsername();
					
					for (int i = 0; i < array.length(); ++i) { 
							
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
											.build();
								String current_name = array.getJSONObject(i).getString("users_tbl_username");
								if(current_name.equalsIgnoreCase(name)) {
									position = array.getJSONObject(i).getInt("users_tbl_rank");
									CurrentUser.setRank(position);
								}
									

									
						}
					int end_list = position + 10;
					if(position < 10)
						position = 10;
					if(end_list > array.length())
						end_list = array.length();
										
					if(small_view == true)
						for (int i = position - 10; i < end_list; ++i)
						{
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
					if(small_view == false)
						for (int i = 0; i < array.length(); ++i)
						{
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
					Log.e(TAG + "GetFriendTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			// highlight player row and put it in view
			int rank = CurrentUser.getRank();
			adapter.setPositionFound(rank - 1);
			listview.setSelection(rank - 1);

			progressDialog.dismiss();
			

		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.leaderboard_menu, menu);
		return true;
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
			case R.id.options_menu_xml_item_refresh_icon:
				userList.clear();
				adapter.notifyDataSetChanged();
				new GetUsersTask().execute();
				break;
		}
		return true;
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
}