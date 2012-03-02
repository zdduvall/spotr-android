package com.csun.spotr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

/**
 * This class provides a foundation of basic features (e.g. Spotr's custom
 * title bar) upon which activities can be built. 
 */
public class BasicSpotrActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.dummy);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_basic);
	
		setupTitleBar();
	}
	
	protected void setupTitleBar() {
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText("potr");
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
}