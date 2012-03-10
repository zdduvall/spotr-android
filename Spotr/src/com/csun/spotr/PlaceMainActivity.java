package com.csun.spotr;

import com.csun.spotr.custom_gui.FlingableTabHost;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Framework for Spots
 */
public class PlaceMainActivity extends TabActivity {
	private static final String TAG = "(PlaceMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.place_main);
		
		FlingableTabHost tabHost = (FlingableTabHost) getTabHost(); 
		FlingableTabHost.TabSpec spec; 
		Intent intent;

		// get place_id extras from PlaceActivity/LocalPlaceActivity
		Bundle extras = getIntent().getExtras();
		// create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(getApplicationContext(), PlaceActionActivity.class);
		// pass this extra to PlaceActionActivity
		intent.putExtras(extras);

		// initialize a TabSpec for each tab and add it to the TabHost
	    View customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    TextView tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("Missions");
		spec = tabHost
				.newTabSpec("missions")
				.setIndicator(customTabView)
				.setContent(intent);
		tabHost.addTab(spec);

		// do the same for the other tabs
		intent = new Intent().setClass(getApplicationContext(), PlaceActivityActivity.class);
		// pass this Extra to PlaceActivityActivity
		intent.putExtras(extras);
		
		customTabView = LayoutInflater.from(tabHost.getContext()).inflate(R.layout.tab_custom, null);
	    tv = (TextView) customTabView.findViewById(R.id.tabText);
	    tv.setText("News");
		spec = tabHost
				.newTabSpec("news")
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
