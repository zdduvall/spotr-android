package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.FriendRequestItemAdapter;
import com.csun.spotr.core.adapter_item.FriendRequestItem;
import com.csun.spotr.custom_gui.DashboardLayout;
import com.csun.spotr.custom_gui.DraggableGridView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.Button;

/**
 * Description:
 * 		Main menu
 */

public class MainMenuActivity 
	extends BasicSpotrActivity 
		implements IActivityProgressUpdate<FriendRequestItem> {
	
	private static final 	String 						TAG = "(MainMenuActivity)";
/*  Reason Comment: BETA php script commented out due to the fact that the group ran out of time
 *  for implementing actual LIVE notifications.
 *  Left in comments in the event LIVE notifications want to be implemented AFTER the project showcase
 *  Date commented out: March 10, 2012
 *  Commenter:	Edgardo A. Campos
 *  WHAT WAS COMMENTED OUT:
 *  
 *	private static final 	String 						GET_REQUEST_URL = "http://107.22.209.62/android/beta_get_friend_requests.php";
 *
 */	
	private static final 	String 						GET_REQUEST_URL = "http://107.22.209.62/android/get_friend_requests.php";
	private static final 	String 						ADD_FRIEND_URL = "http://107.22.209.62/android/add_friend.php";
	private static final 	String 						IGNORE_FRIEND_URL = "http://107.22.209.62/android/ignore_friend.php";
	
	private 				List<FriendRequestItem> 	friendRequestList = null;
	private 				int 						currentSelectedFriendId;
	private 				DashboardLayout 			dashboard = null;
	private 				ListView 					listview;
	private 				FriendRequestItemAdapter 	adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_original);
		
		setupTitleBar();
		
		friendRequestList = new ArrayList<FriendRequestItem>();
		listview = (ListView) findViewById(R.id.main_menu_xml_slide_content);
		adapter = new FriendRequestItemAdapter(getApplicationContext(), friendRequestList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startDialog(position);
				listview.getChildAt((int) id).setVisibility(View.GONE);
				listview.invalidate();
			}
		});
	
		/*
		 * Chan Nguyen (3/3/2012): temporary disable due to crashing other activities
		 */
		 GetFriendRequestTask task = new GetFriendRequestTask(this);
		 task.execute();
	}
	
	@Override
	protected void setupTitleBar() {
		super.setupTitleBar();
		ImageView homeBeacon = (ImageView) findViewById(R.id.title_bar_home_beacon);
		homeBeacon.setVisibility(View.INVISIBLE);
		
		LinearLayout homeContainer = (LinearLayout) findViewById(R.id.title_bar_home_container);
		homeContainer.setClickable(false);
	}
		
	public void getActivity(View mainMenuButton) {
		int id =  ((Button) mainMenuButton).getId();
		Intent intent;
		if (id == R.id.main_menu_btn_me) {
			Bundle extras = new Bundle();
			extras.putInt("user_id", CurrentUser.getCurrentUser().getId());
			intent = new Intent(getApplicationContext(), ProfileMainActivity.class);
			intent.putExtras(extras);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_map) {
			intent = new Intent(getApplicationContext(), LocalMapViewActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_spot_it) {
			intent = new Intent(getApplicationContext(), FinderMainActivity.class);
			intent.putExtra("user_id", CurrentUser.getCurrentUser().getId());
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_spots) {
			intent = new Intent(getApplicationContext(), PlaceActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_friends) {
			intent = new Intent(getApplicationContext(), FriendListMainActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_quests) {
			intent = new Intent(getApplicationContext(), QuestActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_ping) {
		 	// remove ping map activity
		}
		else if (id == R.id.main_menu_btn_inventory) {
			intent = new Intent(getApplicationContext(), InventoryActivity.class);
			startActivity(intent);   
		}
		else if (id == R.id.main_menu_btn_inbox) {
			intent = new Intent(getApplicationContext(), InboxActivity.class);
			startActivity(intent);   
		}
		else {
			// should never go here
		}

	}

	private static class GetFriendRequestTask 
		extends AsyncTask<Void, FriendRequestItem, Boolean> 
			implements IAsyncTask<MainMenuActivity>{
		
		private List<NameValuePair> friendData = new ArrayList<NameValuePair>(); 
		private WeakReference<MainMenuActivity> ref;
		
		public GetFriendRequestTask(MainMenuActivity a) {
			attach(a);
		}
		
		@Override
		protected void onProgressUpdate(FriendRequestItem... f) {
			ref.get().updateAsyncTaskProgress(f[0]);
	    }
		
		@Override
		protected void onPreExecute() {
		}
		
		
		/*  Reason Comment:
		 *  Removing item in doInBackground(Void...voids) to return Notification feed back to a 
		 *  plain friends list notification. Does not interact with old PHP script
		 *
		 *  Date commented out: March 10, 2012
		 *  Commenter:	Edgardo A. Campos
		 *  
		 *  WHAT WAS COMMENTED OUT/REMOVED:	
		 *  
		 *  array.getJSONObject(i).getInt("user_requests_tbl_type")));
		 */	
		@Override
		protected Boolean doInBackground(Void...voids) {
			friendData.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_REQUEST_URL, friendData);
			if (array != null) { 
				try {
					for (int i = 0; i < array.length(); ++i) { 
						publishProgress(
							new FriendRequestItem(
								array.getJSONObject(i).getInt("user_requests_tbl_friend_id"),
								array.getJSONObject(i).getString("users_tbl_username"),
								array.getJSONObject(i).getString("user_requests_tbl_friend_message"),
								array.getJSONObject(i).getString("user_requests_tbl_time")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFriendRequestTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {

			}
			detach();
		}
		
		public void attach(MainMenuActivity a) {
			ref = new WeakReference<MainMenuActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	private static class UpdateFriendTask 
		extends AsyncTask<String, Integer, Boolean> 
			implements IAsyncTask<MainMenuActivity> {
		
		private List<NameValuePair> datas = new ArrayList<NameValuePair>(); 
		private WeakReference<MainMenuActivity> ref;
		
		public UpdateFriendTask(MainMenuActivity a) {
			attach(a);
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected Boolean doInBackground(String... urls) {
			datas.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			datas.add(new BasicNameValuePair("friend_id", Integer.toString(ref.get().currentSelectedFriendId)));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(urls[0], datas);
			
			try {
				if (json.getString("result").equals("success")) {
					return true;
				}
			} 
			catch (JSONException e) {
				Log.e(TAG + "AcceptFriendTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				//Commented out Toast Text for now
				//Toast.makeText(ref.get().getApplicationContext(), "Friendship is upto date!", Toast.LENGTH_SHORT).show();
			}
			detach();
		}

		public void attach(MainMenuActivity a) {
			ref = new WeakReference<MainMenuActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
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
			System.out.println("SUCKS");
			if(!editor.commit()) System.out.println("LOLWUT");
			intent = new Intent("com.csun.spotr.LoginActivity");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return true;
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			super.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void updateAsyncTaskProgress(FriendRequestItem f) {
		friendRequestList.add(f);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			return new 
				AlertDialog.Builder(this)
					.setTitle("Process dialog")
					.setMessage("Great!!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							GetFriendRequestTask task = new GetFriendRequestTask(MainMenuActivity.this);
							task.execute();
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
	
	/*  Reason Comment:
	 *  Removed the start dialog box that created item specific dialog boxes for Friend Requests,
	 *  User Comments, and Rewards for the Notification Feed.
	 * 
	 *  Date commented out: March 10, 2012
	 *  Commenter:	Edgardo A. Campos
	 *  
	 *  WHAT WAS COMMENTED OUT:	
	 *	
	 *	an if/else branch that got the type for each specific friendRequestList.get(position) and generated 
	 *  a dialogBox with accept/decline options based on the type of notification.
	 *  estimated 18-28 lines of code
	 *  
	 */	
	
	public void startDialog(final int position) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Request Dialog");
		myAlertDialog.setMessage("Accept this Friend Request?");
		myAlertDialog.setPositiveButton("Accept Request", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				UpdateFriendTask task = new UpdateFriendTask(MainMenuActivity.this);
				if(friendRequestList.size() != 0)
				{
					currentSelectedFriendId = friendRequestList.get(position).getFriendId();
					task.execute(ADD_FRIEND_URL);
				}
			}
		});
		
		myAlertDialog.setNegativeButton("Decline Request", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				UpdateFriendTask task = new UpdateFriendTask(MainMenuActivity.this);
				if(friendRequestList.size() != 0)
				{
					currentSelectedFriendId = friendRequestList.get(position).getFriendId();
					task.execute(IGNORE_FRIEND_URL);
				}
			}
		});
		myAlertDialog.show();
	}
}