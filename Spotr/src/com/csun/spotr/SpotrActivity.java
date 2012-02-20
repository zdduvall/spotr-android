package com.csun.spotr;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

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
}