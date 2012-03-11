package com.csun.spotr;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class EventActivity extends Activity {
	private static final String TAG = "(EventActivity)";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event);
		Bundle extrasBundle = getIntent().getExtras();
		String eventLink = extrasBundle.getString("event_link");
		setupWebView(eventLink);
	}
	
	private void setupWebView(String eventLink) {
		final WebView wv = (WebView) findViewById(R.id.event_xml_webview);
		wv.setWebViewClient(new WebCallBack());
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setPluginsEnabled(true);
		webSettings.setDefaultZoom(ZoomDensity.FAR);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webSettings.setLoadsImagesAutomatically(true);
		webSettings.setBuiltInZoomControls(true);
		wv.loadUrl(eventLink);
	}

	private class WebCallBack extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
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
