package com.csun.spotr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class SpotrActivity extends Activity {
	private static final String TAG = "(SpotrActivity)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences refs = getSharedPreferences("Spotr", MODE_PRIVATE);
		Intent i;
		if (refs.equals(null)) {
			i = new Intent(getApplicationContext(), LoginActivity.class);
		}
		else {
			i = new Intent(getApplicationContext(), LoginActivity.class);
		}

		startActivity(i);
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Toast.makeText(this, "Back one more time to exits!", Toast.LENGTH_LONG);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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