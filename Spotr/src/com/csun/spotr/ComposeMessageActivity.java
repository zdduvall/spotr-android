package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.core.Inbox;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class ComposeMessageActivity extends Activity {

	private static final String TAG = "(ComposeMessageActivity)";
	private static final String SEND_INBOX_MESSAGE_URL = "http://107.22.209.62/android/send_inbox_message.php";
	
	private EditText editTextTo;
	private EditText editTextMessage;
	private int toUserId = -1;
	private SendMessageTask task = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose_message);
		setupSendMessageUI();
	}
	
	private void setupSendMessageUI() {
		final Button buttonChooseUser = (Button) findViewById(R.id.compose_message_xml_button_choose_user);
		editTextTo = (EditText) findViewById(R.id.compose_message_xml_edittext_to);
		editTextMessage = (EditText) findViewById(R.id.compose_message_xml_edittext_message);
		final TextView textViewCount = (TextView) findViewById(R.id.compose_message_xml_textview_character_count);
		final Button buttonSend = (Button) findViewById(R.id.compose_message_xml_button_send);
		editTextTo.setEnabled(false);

		buttonChooseUser.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplication(), ChooseUserActivity.class);
				startActivityForResult(intent, 0);
			}
		});

		editTextMessage.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				textViewCount.setText(String.valueOf(s.length()) + "/160");
				if (s.length() > 0 && editTextTo.getText().toString().length() > 0) {
					buttonSend.setEnabled(true);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});

		buttonSend.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				task = new SendMessageTask(
					ComposeMessageActivity.this, 
					CurrentUser.getCurrentUser().getId(), 
					toUserId, 
					editTextMessage.getText().toString());
				
				task.execute();
			}
		});
	}

	private static class SendMessageTask 
		extends AsyncTask<Void, Inbox, Boolean> 
			implements IAsyncTask<ComposeMessageActivity> {

		private static final String TAG = "[AsyncTask].SendMessageTask";
		private WeakReference<ComposeMessageActivity> ref;
		private int userId;
		private int friendId;
		private String message;

		public SendMessageTask(ComposeMessageActivity a, int userId, int friendId, String message) {
			this.userId = userId;
			this.friendId = friendId;
			this.message = message;
			attach(a);
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			data.add(new BasicNameValuePair("friend_id", Integer.toString(friendId)));
			data.add(new BasicNameValuePair("friend_message", message));
			return data;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = prepareUploadData();
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(SEND_INBOX_MESSAGE_URL, data);
			String result = "";
			try {
				result = json.getString("result");
				if (result.equals("success"))
					return true;
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(ref.get().getApplicationContext(), "Message is sent!", Toast.LENGTH_SHORT).show();
			}
		}

		public void attach(ComposeMessageActivity a) {
			ref = new WeakReference<ComposeMessageActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				toUserId = b.getInt("user_id");
				editTextTo.setText(b.getString("username"));
			}
		}
	}

	/*Commenting since it's unused.
	private boolean canSend() {
		if (toUserId != -1 && editTextMessage.getText().toString().length() > 0) {
			return true;
		}
		return false;
	}*/
	
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
