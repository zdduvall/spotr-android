package com.csun.spotr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.Button;
import android.widget.EditText;

public class AddWebLinkActivity extends Activity {
	private static final String TAG = "(AddWebLinkActivity)";
	private String webUrl = "http://google.com/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web_link);

		final WebView wv = (WebView) findViewById(R.id.web_link_xml_webview);
		wv.setWebViewClient(new WebCallBack());
		WebSettings webSettings = wv.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setPluginsEnabled(true);
		webSettings.setDefaultZoom(ZoomDensity.FAR);
		webSettings.setBuiltInZoomControls(true);
		wv.loadUrl(webUrl);
		
		final EditText edittextWebUrl = (EditText) findViewById(R.id.web_link_xml_autocompletetextview_url);
		final Button buttonAddLink = (Button) findViewById(R.id.web_link_xml_button_add_link);
		final Button buttonGo = (Button) findViewById(R.id.web_link_xml_button_go);
		
		edittextWebUrl.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (edittextWebUrl.getText().toString().length() > 0) {
					buttonAddLink.setEnabled(true);
				}
				else {
					buttonAddLink.setEnabled(false);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		
		buttonGo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				wv.loadUrl("http://" + edittextWebUrl.getText().toString().trim());
			}
		});
		
		buttonAddLink.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent data = new Intent();
				data.putExtra("link", "http://" + edittextWebUrl.getText().toString());
				setResult(RESULT_OK, data);
				finish();
			}
		});
	}

	private class WebCallBack extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
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
