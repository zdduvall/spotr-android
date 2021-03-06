package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Sign up for a new account
 **/
public class SignupActivity 
	extends BasicSpotrActivity {
	
	private static final String TAG = "(SignupActivity)";
	private static final String SIGN_UP_URL = "http://107.22.209.62/android/signup.php";
	
	private boolean passwordVisible = false;
	//private boolean validInformation = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		setupTitleBar();
		setupSignupRoutine();
	}
	
	private void setupSignupRoutine() {
		final EditText edittextEmail = (EditText) findViewById(R.id.signup_xml_edittext_email_id);
		final EditText edittextPassword = (EditText) findViewById(R.id.signup_xml_edittext_password_id);
		final EditText edittextConfirmPassword = (EditText) findViewById(R.id.signup_xml_edittext_confirmpassword_id);
		final CheckBox checkboxVisible = (CheckBox) findViewById(R.id.signup_xml_checkbox_visible_characters);
		final Button buttonSignup = (Button) findViewById(R.id.signup_xml_button_signup);

		checkboxVisible.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (passwordVisible) {
					edittextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					edittextConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
					passwordVisible = false;
				}
				else {
					edittextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					edittextConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
					passwordVisible = true;
				}
			}
		});

		buttonSignup.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String email = edittextEmail.getText().toString();
				String password = edittextPassword.getText().toString();
				String confirmpassword = edittextConfirmPassword.getText().toString();
				if (!email.contains("@")) {
					showDialog(1);
				}
				else if (!password.equals(confirmpassword)) {
					showDialog(0);
				}
				else {
					SignupTask task = new SignupTask(SignupActivity.this, edittextEmail.getText().toString().trim(), edittextPassword.getText().toString().trim());
					task.execute();
				}
			}
		});
	}
	
	protected void setupTitleBar() {
		super.setupTitleBar();
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText("ign up");
	}
	
	private static class SignupTask 
		extends AsyncTask<Void, Integer, Boolean> 
			implements IAsyncTask<SignupActivity> {
		
		private List<NameValuePair> datas = new ArrayList<NameValuePair>();
		private WeakReference<SignupActivity> ref;
		private String email;
		private String password;
		
		public SignupTask(SignupActivity a, String email, String password) {
			this.email = email;
			this.password = password;
			attach(a);
		}
		
		@Override
		protected void onPreExecute() {
			datas.add(new BasicNameValuePair("username", email));
			datas.add(new BasicNameValuePair("password", password));
		}
		
		@Override
		protected Boolean doInBackground(Void... voids) {
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(SIGN_UP_URL, datas);
			try {
				if (json.getString("result").equals("success"))
					return true;
			}
			catch (Exception e) {
				Log.e(TAG + "SignupTask.doInBackground(Void... voids)", "JSON error parsing data", e );
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == false) {
				ref.get().showDialog(3);
			}
			else {
				ref.get().showDialog(2);
			}
			
			detach();
		}

		public void attach(SignupActivity a) {
			ref = new WeakReference<SignupActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}


	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0 :
			return 
				new AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Error")
					.setMessage("Passwords do not match.\n Please try again.")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {

						}
					}).create();
			
		case 1 :
			return 
				new AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Error")
					.setMessage("Invalid email address.\n Please try again.")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							
						}
					}).create();
			
		case 2 :
			return 
				new AlertDialog.Builder(this)
					.setTitle("Woohoo!")
					.setMessage("Your account has been created. Log in and have fun playing Spotr!")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startActivity(new Intent("com.csun.spotr.LoginActivity"));
						}
					}).create();
			
		case 3 :
			return new AlertDialog.Builder(this)
				.setIcon(R.drawable.error_circle)
				.setTitle("Error")
				.setMessage("This email is already in use.\n Please try again.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

					}
				}).create();
		}
		return null;
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
