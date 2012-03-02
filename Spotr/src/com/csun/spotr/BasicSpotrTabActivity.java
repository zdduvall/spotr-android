package com.csun.spotr;


import android.app.TabActivity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
/**
* This class provides a foundation of basic features (e.g. Spotr's custom
* title bar) upon which tab-activities can be built. 
*/
public class BasicSpotrTabActivity extends TabActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.dummy_tab_host);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_basic);
	
		setupTitleBar();
	}
	
	protected void setupTitleBar() {
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}
	
	/**
	 * Open the Main Menu activity (dashboard). If that activity is already
	 * running, a new instance of that activity will not be launched--instead,
	 * all activities on top of the old instance are removed as the old 
	 * instance is brought to the top.
	 * @param button the button clicked
	 */
	public void goToMainMenu(View button) {
		button.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar_btn_highlight));
	    final Intent intent = new Intent(this, MainMenuActivity.class);
	    intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    startActivity (intent);
	}
}
