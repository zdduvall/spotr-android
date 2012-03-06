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

public class FinderMainActivity extends TabActivity {
	private final static String TAG = "(FinderMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "Starting FinderMainActivity");
		
		setContentView(R.layout.place_main);
		
		FlingableTabHost tabHost = (FlingableTabHost) getTabHost();
		FlingableTabHost.TabSpec spec; 
		Intent intent; 
		
		Bundle extras = getIntent().getExtras();
		int currentUserId = extras.getInt("user_id");
		
		// Set initial intent (for Tab 1)
		intent = new Intent().setClass(getApplicationContext(), FinderActivity.class);
		intent.putExtra("user_id", currentUserId);
		
		// Set custom tab layouts
		View customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    TextView tv = (TextView) customTabView.findViewById(R.id.tabText);
	    
	    // Tab 1
	    tv.setText("All");
		spec = tabHost
				.newTabSpec("all")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);
		
		// Tab 2
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
		tv = (TextView) customTabView.findViewById(R.id.tabText);
		tv.setText("My Items");
		intent = new Intent().setClass(getApplicationContext(), UserFinderActivity.class);
		spec = tabHost
				.newTabSpec("user")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(0);
	}
	
	private void setupTitleBar() {
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText("pot and Steal");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}
}
