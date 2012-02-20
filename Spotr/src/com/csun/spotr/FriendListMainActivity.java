package com.csun.spotr;

import com.csun.spotr.custom_gui.FlingableTabHost;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * Description:
 * 		Main tab host for friends
 */
public class FriendListMainActivity 
	extends TabActivity {
	
	private static final String TAG = "(FriendListMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.friend_list);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
		
		Resources res = getResources(); 
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
		intent = new Intent().setClass(getApplicationContext(), FriendListFeedActivity.class);
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Chatter");
		spec = tabHost
				.newTabSpec("Chatter")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);
		// set current tab to action
		tabHost.setCurrentTab(0);
		
	
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

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}
}