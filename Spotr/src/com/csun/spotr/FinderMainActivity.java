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

public class FinderMainActivity extends BasicSpotrTabActivity {
	private final static String TAG = "(FinderMainActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "Starting FinderMainActivity");
		
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.place_main);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
		
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
	
	/**
	 * Open the Main Menu activity (dashboard). If that activity is already
	 * running, a new instance of that activity will not be launched--instead,
	 * all activities on top of the old instance are removed as the old 
	 * instance is brought to the top.
	 * @param button the button clicked
	 */
	public void goToMainMenu(View button) {
	    final Intent intent = new Intent(this, MainMenuActivity.class);
	    intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    startActivity (intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}
}
