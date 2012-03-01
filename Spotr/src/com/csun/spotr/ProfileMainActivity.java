package com.csun.spotr;

import com.csun.spotr.custom_gui.FlingableTabHost;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

public class ProfileMainActivity extends BasicSpotrTabActivity {
	private final static String TAG = "(ProfileMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
}
