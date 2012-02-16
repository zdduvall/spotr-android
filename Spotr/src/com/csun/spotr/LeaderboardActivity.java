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
	private int button_position;//for debugging
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard);
		// initialize list view
		listview = (ListView) findViewById(R.id.leaderboard_xml_listview_users);
		adapter = new LeaderboardItemAdapter(LeaderboardActivity.this, userList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// handle click 
			}
		});
		
		new GetUsersTask().execute();
		
		Button buttonChangeView = (Button) findViewById(R.id.leaderboard_xml_button_change_view);
		buttonChangeView.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				// run new task
				if(small_view == false)
				{
					small_view = true;
					userList.clear();
					adapter.notifyDataSetChanged();
				}
				else
				{
					small_view = false;
					userList.clear();
					adapter.notifyDataSetChanged();
				}
					
				
				listview.post(new Runnable() {
					public void run() {
						new GetUsersTask().execute();
				}});
				 
			}
		});
		
		Button buttonWhere = (Button) findViewById(R.id.leaderboard_xml_button_where_am_i);
		buttonWhere.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				// run new task
				button_position = CurrentUser.getSelectedPosition();
				System.out.println("button position: " + button_position);//for debugging
				listview.setSelector(R.drawable.leaderboard_listview_users_backgroundselected);
				listview.setSelection(button_position);
/*
				listview.post(new Runnable() {
					public void run() {
						//listview.setFocusable(true);
						//listview.setSelection(button_position);
						//listview.requestFocus(button_position);
						//listview.getChildAt(button_position);
						//listview.getFocusedChild().setBackgroundColor(Color.BLUE);
						listview.getChildAt(button_position).setBackgroundResource(R.drawable.leaderboard_listview_users_backgroundselected);
						//v.setEnabled(false);
						//v.setBackgroundColor(color.transparent);
						System.out.println("position on button click: " + button_position);//for debugging
				}}); */
			}
		});
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
			// adapter.notifyDataSetInvalidated();
	    }
		
		@Override
		protected Boolean doInBackground(Void...voids) {
			JSONArray array = JsonHelper.getJsonArrayFromUrl(GET_USERS_URL);
			if (array != null) { 
				try {
					
					User current_user = CurrentUser.getCurrentUser();
					int position = current_user.getRank();
					String name = current_user.getUsername();
					System.out.println(position); //for debugging
					System.out.println(name); //for debugging

					
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
								System.out.println("name for rank: " + current_name);//for debugging
								if(current_name.equalsIgnoreCase(name)) {
									position = array.getJSONObject(i).getInt("users_tbl_rank");
									CurrentUser.setSelectedPostion(position);
									System.out.println("position changed");//for debugging
								}
									

									
						}
					System.out.println("CurrentUser position: " + CurrentUser.getSelectedPosition());//for debugging
					int end_list = position + 10;
					if(position < 10)
						position = 10;
					if(end_list > array.length())
						end_list = array.length();
					
					System.out.println(position); //for debugging
					
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
			progressDialog.dismiss();
			

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