package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/*
 * Description
 * 		Post a message on to wall
 */
public class WriteOnWallActivity 
		extends BasicSpotrActivity {
	
	private static final 	String 		TAG = "(WriteOnWallActivity)";
	private static final 	String 		WRITE_ON_WALL_URL = "http://107.22.209.62/android/do_write_on_wall.php";
	
	private 				String 		usersId;
	private 				String 		spotsId;
	private 				String 		challengesId;
	private 				String 		message;
	private 				String 		link;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "I'm created!");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.write_on_wall);
		
		final Button buttonPost = (Button) findViewById(R.id.write_on_wall_xml_button_submit);
		buttonPost.setEnabled(false);
		
		final Button buttonLink = (Button) findViewById(R.id.write_on_wall_xml_button_choose_link);
		final TextView textViewCount = (TextView) findViewById(R.id.write_on_wall_xml_textview_character_count);
		final EditText editTextMessage = (EditText) findViewById(R.id.write_on_wall_xml_edittext_message_box);
		
		Bundle extras = getIntent().getExtras();
		usersId = extras.getString("users_id");
		spotsId = extras.getString("spots_id");
		challengesId = extras.getString("challenges_id");

		editTextMessage.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				textViewCount.setText(String.valueOf(s.length()) + "/100");
				if (s.length() > 0) {
					buttonPost.setEnabled(true);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		
		buttonPost.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				message = editTextMessage.getText().toString();
				EditText editTextUrl = (EditText) findViewById(R.id.write_on_wall_xml_edittext_link);
				link = editTextUrl.getText().toString();
				if (message.length() > 0) {
					WriteOnWallTask task = new WriteOnWallTask(WriteOnWallActivity.this, usersId, spotsId, challengesId, message, link);
					task.execute();
				}
				else {
					displayErrorMessage();
				}
			}
		});
		
		buttonLink.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int dummy = 0;
				Intent intent = new Intent(getApplicationContext(), AddWebLinkActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		
	}
	
	private void displayErrorMessage() {
		AlertDialog dialogMessage = new AlertDialog.Builder(WriteOnWallActivity.this).create();
		dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
		dialogMessage.setMessage("Message cannot be empty! Please try again.");
		dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogMessage.show();	
	}
	
	private static class WriteOnWallTask 
		extends AsyncTask<Void, Integer, String> 
			implements IAsyncTask<WriteOnWallActivity> {
		
		private WeakReference<WriteOnWallActivity> ref;
		private ProgressDialog progressDialog;
		private String usersId;
		private String spotsId;
		private String challengesId;
		private String message;
		private String link;
		
		public WriteOnWallTask(WriteOnWallActivity a, String usersId, String spotsId, String challengesId, String message, String link) {
			attach(a);
			this.usersId = usersId;
			this.spotsId = spotsId;
			this.challengesId = challengesId;
			this.message = message;
			this.link = link;
		}
		
		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			
			data.add(new BasicNameValuePair("users_id", usersId));
			data.add(new BasicNameValuePair("spots_id", spotsId));
			data.add(new BasicNameValuePair("challenges_id", challengesId));
			data.add(new BasicNameValuePair("comment", message));
			data.add(new BasicNameValuePair("link", link));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(WRITE_ON_WALL_URL, data);
			String result = "";
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "UploadMessageTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if (result.equals("success")) {
				Intent intent = new Intent();
				intent.setData(Uri.parse("done"));
				ref.get().setResult(RESULT_OK, intent);
				ref.get().finish();
			}
			
			detach();
		}
		public void attach(WriteOnWallActivity a) {
			ref = new WeakReference<WriteOnWallActivity>(a);
		}
		
		public void detach() {
			ref.clear();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.options_menu_xml_item_setting_icon:
				intent = new Intent("com.csun.spotr.SettingsActivity");
				startActivity(intent);
				break;
			case R.id.options_menu_xml_item_logout_icon:
				SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();
				intent = new Intent("com.csun.spotr.LoginActivity");
				startActivity(intent);
				break;
			case R.id.options_menu_xml_item_mainmenu_icon:
				intent = new Intent("com.csun.spotr.MainMenuActivity");
				startActivity(intent);
				break;
		}
		return true;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				String url = b.getString("link");
				EditText editTextUrl = (EditText) findViewById(R.id.write_on_wall_xml_edittext_link);
				editTextUrl.setText(url);
			}
		}
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
