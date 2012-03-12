package com.csun.spotr;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class WebviewActivity extends Activity {
	private static final String TAG = "(WebviewActivity)";
	private String webUrl = "http://google.com/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// set layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		// get place id
		Bundle extrasBundle = getIntent().getExtras();
		webUrl = extrasBundle.getString("place_web_url");
		
		setupWebView();
	}
	
	private void setupWebView() {
		WebView wv = (WebView) findViewById(R.id.webview_xml_place_webview);
		wv.setWebViewClient(new WebCallBack());
		WebSettings settings = wv.getSettings();
		settings.setBuiltInZoomControls(true);
		wv.loadUrl(webUrl);
	}

	private class WebCallBack extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
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
