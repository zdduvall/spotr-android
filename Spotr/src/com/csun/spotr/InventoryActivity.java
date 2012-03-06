package com.csun.spotr;

import com.csun.spotr.custom_gui.FlingableTabHost;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Description:
 * 		Main tab host for friends
 */
public class InventoryActivity 
	extends TabActivity {
	
	private static final String TAG = "(InventoryActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inventory);
		
		FlingableTabHost tabHost = (FlingableTabHost) getTabHost(); 
		FlingableTabHost.TabSpec spec; 
		Intent intent; 

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(getApplicationContext(), WeaponActivity.class); 
	    View customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    TextView tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Loots");
		spec = tabHost
				.newTabSpec("Loots")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);

		// Initialize a TabSpec for each tab and add it to the TabHost
		intent = new Intent().setClass(getApplicationContext(), TreasureActivity.class);
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Gifts");
		spec = tabHost
				.newTabSpec("Gifts")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);
		
		// Do the same for the other tabs
		intent = new Intent().setClass(getApplicationContext(), WeaponActivity.class);
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Others");
		spec = tabHost
				.newTabSpec("Others")
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
}