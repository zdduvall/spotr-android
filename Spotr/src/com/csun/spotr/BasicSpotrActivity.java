package com.csun.spotr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

/**
 * This class provides a foundation of basic features (e.g. Spotr's custom
 * title bar) upon which activities can be built. 
 */
public class BasicSpotrActivity extends Activity {
	private static final String TAG = "(BasicSpotrActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.dummy);
	}
	
	protected void setupTitleBar() {	
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_basic);
	}
	
	/**
	 * Attempt to minimize banding.
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}
	
	@Override
	public void onPause() {
		Log.v(TAG,"I'm paused");
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG,"I'm destroyed");
		super.onPause();
	}
}
