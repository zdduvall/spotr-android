package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.core.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfileEditActivity extends Activity {
	private static final 	String 		TAG = "(ProfileEditActivity)";
	private static final 	String 		UPDATE_USER_DETAIL_URL = "http://107.22.209.62/android/update_user_detail.php";
	private					Button		doneButton;	
	private 				EditText 				edittextEmail = null;
	private 				EditText 				edittextPassword = null;
	private static			String					user_email = null;
	private static			String					user_password = null;
	private 				int 					userId = -1;
	
	private static final 	int 					CAMERA_PICTURE = 111;
	private static final 	int 					GALLERY_PICTURE = 222;
	//private					SetUserDetailTask		task;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_edit);
		
		doneButton = (Button) findViewById(R.id.profile_edit_xml_button_done);
		edittextEmail = (EditText) findViewById(R.id.profile_edit_xml_edittext_email);
		edittextPassword = (EditText) findViewById(R.id.profile_edit_xml_edittext_password);
		Bundle extrasBundle = getIntent().getExtras();
		userId = extrasBundle.getInt("user_id");
		user_email = extrasBundle.getString("email");
		user_password = extrasBundle.getString("password");
		edittextEmail.setText(user_email);
		edittextPassword.setText(user_password);
		
		String imageUrl = extrasBundle.getString("imageUrl");
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		ImageView imageViewUserPicture = (ImageView) findViewById(R.id.profile_edit_xml_imageview_user_picture);
		imageLoader.displayImage(imageUrl, imageViewUserPicture);

		imageViewUserPicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDialog();
			}
		});
	

		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				user_email = edittextEmail.getText().toString();
				user_password = edittextPassword.getText().toString();
				System.out.println("user password from profile edit: " + user_password);
				ProfileEditTask task = new ProfileEditTask(ProfileEditActivity.this, userId, user_email, user_password);
				task.execute();
				Intent intent;
				Bundle extras = new Bundle();
				extras.putInt("user_id", CurrentUser.getCurrentUser().getId());
				intent = new Intent(getApplicationContext(), ProfileMainActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
				finish();
			}
		});
		
	}
	
	private static class ProfileEditTask 
	extends AsyncTask<Void, Integer, Boolean> 
		implements IAsyncTask<ProfileEditActivity> {
	
	private List<NameValuePair> datas = new ArrayList<NameValuePair>();
	private WeakReference<ProfileEditActivity> ref;
	private int userId;
	private String email;
	private String password;
	
	public ProfileEditTask(ProfileEditActivity a, int userId, String email, String password) {
		this.userId = userId;
		this.email = email;
		this.password = password;
		attach(a);
	}
	
	@Override
	protected void onPreExecute() {
		System.out.println("email from asynctask: "+ email);
		System.out.println("pass from asynctask: "+ password);
		datas.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
		datas.add(new BasicNameValuePair("username", email));
		datas.add(new BasicNameValuePair("password", password));
		
	}
	
	@Override
	protected Boolean doInBackground(Void... voids) {
		JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(UPDATE_USER_DETAIL_URL, datas);
		//JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(UPDATE_USER_DETAIL_URL, datas);
		/*
		try {
			if (json.getString("result").equals("success"))
				return true;
		}
		catch (Exception e) {
			Log.e(TAG + "SignupTask.doInBackground(Void... voids)", "JSON error parsing data" + e.toString());
		}
		return false;
		*/
		return true;
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

	public void attach(ProfileEditActivity a) {
		ref = new WeakReference<ProfileEditActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}
	
	private void startDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Upload Pictures Option");
		myAlertDialog.setMessage("How do you want to set your picture?");
		myAlertDialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PICTURE);
			}
		});

		myAlertDialog.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CAMERA_PICTURE);
			}
		});
		myAlertDialog.show();
	}
	
}