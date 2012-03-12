package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ListView;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.adapter.LeaderboardItemAdapter;
import com.csun.spotr.asynctask.GetUsersTask;
import com.csun.spotr.core.User;


/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class LeaderboardActivity 
	extends Activity 
		implements IActivityProgressUpdate<User> {
	
	private static final String TAG = "(LeaderboardActivity)";
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
	
	public void updateRank(int position) {
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