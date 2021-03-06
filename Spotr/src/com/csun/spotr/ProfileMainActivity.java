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

public class ProfileMainActivity extends TabActivity {
	private final static String TAG = "(ProfileMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.place_main);
		
		FlingableTabHost tabHost = (FlingableTabHost) getTabHost(); 
		FlingableTabHost.TabSpec spec; 
		Intent intent; 
		
		// get use_id from ProfileMainActivity
		Bundle extras = getIntent().getExtras();
		int currentUserId = extras.getInt("user_id");
		// tab 1
		intent = new Intent().setClass(getApplicationContext(), ProfileActivity.class);
		// pass it to ProfileActivity
		intent.putExtra("user_id", currentUserId);
		
		// set custom views for tabs
	    View customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    TextView tv = (TextView) customTabView.findViewById(R.id.tabText);

	    // tab 1
	    tv.setText("Profile");
		spec = tabHost
				.newTabSpec("profile")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);

		// tab 2
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
		tv = (TextView) customTabView.findViewById(R.id.tabText);
		tv.setText("Rankings");
		intent = new Intent().setClass(getApplicationContext(), LeaderboardActivity.class);
		spec = tabHost
				.newTabSpec("rankings")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);

		// tab 3
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
		tv = (TextView) customTabView.findViewById(R.id.tabText);
		tv.setText("Rewards");
		intent = new Intent().setClass(getApplicationContext(), RewardActivity.class);
		spec = tabHost
				.newTabSpec("reward")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);
		// set current tab to action
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
