package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.csun.spotr.adapter.FriendRequestItemAdapter;
import com.csun.spotr.core.adapter_item.FriendRequestItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.asynctask.GetFriendRequestTask;
import com.csun.spotr.asynctask.UpdateFriendTask;

/**
 * Description:
 * 		Main menu
 */

public class FriendRequestActivity 
	extends BasicSpotrActivity
		implements IActivityProgressUpdate<FriendRequestItem> {
	
	private static final String TAG = "(MainMenuActivity)";
	private static final String ADD_FRIEND_URL = "http://107.22.209.62/android/add_friend.php";
	private static final String IGNORE_FRIEND_URL = "http://107.22.209.62/android/ignore_friend.php";
	
	private List<FriendRequestItem> friendRequestList = null;
	public int currentSelectedFriendId;
	private ListView listview;
	private FriendRequestItemAdapter adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_request);
		setupTitleBar();
		
		friendRequestList = new ArrayList<FriendRequestItem>();
		listview = (ListView) findViewById(R.id.friend_request_xml_listview);
		adapter = new FriendRequestItemAdapter(getApplicationContext(), friendRequestList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startDialog(position);
				listview.getChildAt((int) id).setVisibility(View.GONE);
				listview.invalidate();
			}
		});
	
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
							GetFriendRequestTask task = new GetFriendRequestTask(FriendRequestActivity.this);
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
	
	public void startDialog(final int position) {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Request Dialog");
		myAlertDialog.setMessage("Accept this Friend Request?");
		myAlertDialog.setPositiveButton("Accept Request", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				UpdateFriendTask task = new UpdateFriendTask(FriendRequestActivity.this);
				if(friendRequestList.size() != 0) {
					currentSelectedFriendId = friendRequestList.get(position).getFriendId();
					task.execute(ADD_FRIEND_URL);
				}
			}
		});
		
		myAlertDialog.setNegativeButton("Decline Request", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				UpdateFriendTask task = new UpdateFriendTask(FriendRequestActivity.this);
				if(friendRequestList.size() != 0) {
					currentSelectedFriendId = friendRequestList.get(position).getFriendId();
					task.execute(IGNORE_FRIEND_URL);
				}
			}
		});
		myAlertDialog.show();
	}
}