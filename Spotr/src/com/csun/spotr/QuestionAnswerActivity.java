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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description
 * 		Display question and let user response 
 **/
public class QuestionAnswerActivity 
	extends Activity {
	
	private static final String TAG = "(QuestionAnswerActivity)";
	private static final String QUESTION_ANSWER_URL = "http://107.22.209.62/android/do_question_answer.php";
	private static final int INTENT_RESULT_LINK = 0;
	private String usersId;
	private String spotsId;
	private String challengesId;
	private String challengeQuestion;
	private String userAnswer;
	private String link;
	
	private QuestionAnswerTask task = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_answer);
		initChallengeInfoFromBundle();
		setupUIandListener();
	}
	
	private void setupUIandListener() {
		TextView textViewQuestion = (TextView) findViewById(R.id.question_answer_xml_textview_question);
		textViewQuestion.setText(challengeQuestion);
		
		final TextView editTextAnswer = (EditText) findViewById(R.id.question_answer_xml_edittext_your_answer);
		final EditText editTextLink = (EditText) findViewById(R.id.question_answer_xml_edittext_link);
		
		Button buttonSubmit = (Button) findViewById(R.id.question_answer_xml_button_submit);
		Button buttonLink = (Button) findViewById(R.id.question_answer_xml_button_choose_link);
		
		buttonSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				userAnswer = editTextAnswer.getText().toString().trim();
				link = editTextLink.getText().toString().trim();
				if (userAnswer.length() > 0) {
					task = new QuestionAnswerTask(QuestionAnswerActivity.this, usersId, spotsId, challengesId, userAnswer, link);
					task.execute();
				}
				else {
					displayErrorMessage();
				}
			}
		});
		
		buttonLink.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), AddWebLinkActivity.class);
				startActivityForResult(intent, INTENT_RESULT_LINK);
			}
		});
	}
	
	private void initChallengeInfoFromBundle() {
		Bundle extras = getIntent().getExtras();
		usersId = extras.getString("users_id");
		spotsId = extras.getString("spots_id");
		challengesId = extras.getString("challenges_id");
		challengeQuestion = extras.getString("question_description");
	}

	private static class QuestionAnswerTask 
		extends AsyncTask<Void, Integer, String> 
			implements IAsyncTask<QuestionAnswerActivity> {
		
		private ProgressDialog progressDialog;
		private WeakReference<QuestionAnswerActivity> ref;
		private String usersId;
		private String spotsId;
		private String challengesId;
		private String userAnswer;
		private String link;
	
		public QuestionAnswerTask(QuestionAnswerActivity a, String usersId, String spotsId, String challengesId, String userAnswer, String link) {
			attach(a);
			this.usersId = usersId;
			this.spotsId = spotsId;
			this.challengesId = challengesId;
			this.userAnswer = userAnswer;
			this.link = link;
		}
		
		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Uploading question...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("users_id", usersId));
			data.add(new BasicNameValuePair("spots_id", spotsId));
			data.add(new BasicNameValuePair("challenges_id", challengesId));
			data.add(new BasicNameValuePair("user_answer", userAnswer));
			data.add(new BasicNameValuePair("link", link));
			return data;
		}
		
		@Override
		protected String doInBackground(Void... params) {
			List<NameValuePair> data = prepareUploadData();
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(QUESTION_ANSWER_URL, data);
			String result = "";
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "QuestionAnswerTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
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
				
		}

		public void attach(QuestionAnswerActivity a) {
			ref = new WeakReference<QuestionAnswerActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	private void displayErrorMessage() {
		AlertDialog dialogMessage = new AlertDialog.Builder(QuestionAnswerActivity.this).create();
		dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
		dialogMessage.setMessage("Answer cannot be empty! Please try again.");
		dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		dialogMessage.show();	
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
				finish();
				break;
			case R.id.options_menu_xml_item_mainmenu_icon:
				intent = new Intent("com.csun.spotr.MainMenuActivity");
				startActivity(intent);
				finish();
				break;
		}
		return true;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				String url = b.getString("link");
				EditText editTextUrl = (EditText) findViewById(R.id.question_answer_xml_edittext_link);
				editTextUrl.setText(url);
			}
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
