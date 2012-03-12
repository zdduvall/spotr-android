package com.csun.spotr;

import com.csun.spotr.custom_gui.FlingableTabHost;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Main tab host for friends
 **/
public class FriendListMainActivity extends TabActivity {
	
	private static final String TAG = "(FriendListMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // temporary while it's decided whether or not to use BasicSpotrTabActivity
		setContentView(R.layout.friend_list);
		
		FlingableTabHost tabHost = (FlingableTabHost) getTabHost(); 
		FlingableTabHost.TabSpec spec; 
		Intent intent;

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(getApplicationContext(), FriendListActivity.class); 
	    View customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    TextView tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Friends");
		spec = tabHost
				.newTabSpec("Friends")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);

		// Initialize a TabSpec for each tab and add it to the TabHost
		intent = new Intent().setClass(getApplicationContext(), ExpandableFriendListFeedActivity.class);
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Chatter");
		spec = tabHost
				.newTabSpec("Chatter")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);
		
		// Do the same for the other tabs
		intent = new Intent().setClass(getApplicationContext(), FriendListActionActivity.class);
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Find");
		spec = tabHost
				.newTabSpec("Find")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
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