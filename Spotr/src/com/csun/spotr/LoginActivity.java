package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LoginActivity 
	extends BasicSpotrActivity 
		implements IActivityProgressUpdate<Integer> {
	
	private static final String TAG = "(LoginActivity)";
	private static final String LOGIN_URL = "http://107.22.209.62/android/login.php";
	private final static int LOGIN_ERROR = 0;
	private final static int CONNECTION_ERROR = 1;
	
	private EditText edittextUsername;
	private EditText edittextPassword;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private boolean prefsSavePassword = false;
	private boolean passwordVisible = false;
	private boolean savePassword = true;//false;
	private LoginTask task = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login);
		setupTitleBar();

		prefs = getSharedPreferences("Spotr", MODE_PRIVATE);
		prefsSavePassword = prefs.getBoolean("savePassword", false);
		String prefsUsername = prefs.getString("username", "");
		String prefsPassword = prefs.getString("password", "");

		CheckBox checkVisible = (CheckBox) findViewById(R.id.login_xml_checkbox_visible_characters);
		CheckBox checkSavePassword = (CheckBox) findViewById(R.id.login_xml_checkbox_remember_password);
		edittextUsername = (EditText) findViewById(R.id.login_xml_edittext_email_id);
		edittextPassword = (EditText) findViewById(R.id.login_xml_edittext_password_id);
		Button buttonLogin = (Button) findViewById(R.id.login_xml_button_login);
		Button buttonSignup = (Button) findViewById(R.id.login_xml_button_signup);
		
		// check Internet connection
		if (isNetworkAvailableAndConnected() == false) {
			showDialog(CONNECTION_ERROR);
			buttonLogin.setEnabled(false);
			buttonSignup.setEnabled(false);
		}
	
		// check saved password
		if (prefsSavePassword) {
			edittextUsername.append(prefsUsername);
			edittextPassword.append(prefsPassword);
			// performLogin();
		}

		checkVisible.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (passwordVisible) {
					edittextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					passwordVisible = false;
				}
				else {
					edittextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					passwordVisible = true;
				}
			}
		});

		buttonLogin.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				AlphaAnimation alpha = new AlphaAnimation(1, 0.2f);
//				alpha.setDuration(5000);
//				Button button = (Button) v;
//				button.startAnimation(alpha);
				performLogin();
			}
		});
		
		buttonSignup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), SignupActivity.class));
				finish();
			}
		});

		checkSavePassword.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!savePassword)
					savePassword = true;
				else
					savePassword = false;
			}
		});
		
		edittextPassword.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					performLogin();
					return true;
				}
				return false;
			}
		});
	}
	
	protected void setupTitleBar() {
		super.setupTitleBar();
		
		ImageView homeBeacon = (ImageView) findViewById(R.id.title_bar_home_beacon);
		homeBeacon.setVisibility(View.INVISIBLE);
		
		LinearLayout homeContainer = (LinearLayout) findViewById(R.id.title_bar_home_container);
		homeContainer.setClickable(false);
	}
	
	protected void performLogin() {
		task = new LoginTask(this, edittextUsername.getText().toString(), edittextPassword.getText().toString(), savePassword, LOGIN_URL);
		task.execute();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case LOGIN_ERROR :
			return new 
				AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Error Message")
					.setMessage("Invalid Username/Password.\n Please try again.")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							
						}
					}).create();
		
		case CONNECTION_ERROR: 
			return new 
					AlertDialog.Builder(this)
						.setIcon(R.drawable.error_circle)
						.setTitle("Error network connection")
						.setMessage("Please turn on your network connection and try again!")
						.setPositiveButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								
							}
						}).create();
		}
		return null;
	}

	public void onPause() {
		super.onPause();
		finish();
	}
	
	private static class LoginTask 
		extends AsyncTask<Void, Integer, Integer> 
			implements IAsyncTask<LoginActivity> {
		
		private String username;
		private String password; 
		private boolean isSavedPassword;
		private WeakReference<LoginActivity> ref;
		private String url;
		
		public LoginTask(LoginActivity a, String username, String password, boolean isSavedPassword, String url) {
			attach(a);
			this.username = username;
			this.password = password;
			this.isSavedPassword = isSavedPassword;
			this.url = url;
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected Integer doInBackground(Void... voids) {
			List<NameValuePair> datas = new ArrayList<NameValuePair>();
			datas.add(new BasicNameValuePair("username", username));
			datas.add(new BasicNameValuePair("password", password));
			// get data from the our database 
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(LOGIN_URL, datas);
			int userId = -1;
			try {
				userId = json.getInt("result");
			}
			catch (Exception e) {
				Log.e(TAG + "LoginTask.doInBackground(Void... voids)", "JSON error parsing data" + e.toString());
			}
			return userId;
		}

		@Override
		protected void onPostExecute(Integer userId) {
			if (userId == -1) {
				ref.get().showDialog(0);
			}
			else {
				ref.get().updateAsyncTaskProgress(userId);
			}
		}

		public void attach(LoginActivity a) {
			ref = new WeakReference<LoginActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	private boolean isNetworkAvailableAndConnected() {
		ConnectivityManager conManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return false;
		}
		else if (!networkInfo.isConnected()) {
			return false;
		}
		else if (!networkInfo.isAvailable()) {
			return false;
		}
		return true;
	}

	public void updateAsyncTaskProgress(Integer id) {
		if (savePassword) {
			editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
			editor.putBoolean("savePassword", true);
			editor.putString("username", edittextUsername.getText().toString());
			editor.putString("password", edittextPassword.getText().toString());
			editor.commit();
		}
		
		// set current user
		CurrentUser.setCurrentUser(id, edittextUsername.getText().toString(), edittextPassword.getText().toString());
		startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
		finish();
	}
	
}