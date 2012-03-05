package com.csun.spotr;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentDateTime;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.Base64;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.UploadFileHelper;
import com.csun.spotr.core.User;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class ProfileEditActivity extends Activity {
	private static final 	String 		TAG = "(ProfileEditActivity)";
	private static final 	String 		UPDATE_USER_DETAIL_URL = "http://107.22.209.62/android/update_user_detail.php";
	private static final    String		UPDATE_PICTURE_URL = "http://107.22.209.62/images/upload_user_picture.php";
	private					Button		saveButton;	
	private 				EditText 				edittextEmail = null;
	private 				EditText 				edittextPassword = null;
	private 				EditText 				edittextName = null;
	private 				EditText 				edittextEducation = null;
	private 				EditText 				edittextHometown = null;
	private 				EditText 				edittextHobbies = null;
	private static			String					user_email = null;
	private static			String					user_password = null;
	private static			String					user_name = null;
	private static			String					user_education = null;
	private static			String					user_hometown = null;
	private static			String					user_hobbies = null;
	private 				int 					userId = -1;
	private 				Bitmap 					bitmapUserPicture = null;
	
	private static final 	int 					CAMERA_PICTURE = 111;
	private static final 	int 					GALLERY_PICTURE = 222;
	//private					SetUserDetailTask		task;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_edit);
		
		Bundle extrasBundle = getIntent().getExtras();
		userId = extrasBundle.getInt("user_id");
		user_email = extrasBundle.getString("email");
		user_password = extrasBundle.getString("password");
		user_name = extrasBundle.getString("name");
		user_education = extrasBundle.getString("education");
		user_hometown = extrasBundle.getString("hometown");
		user_hobbies = extrasBundle.getString("hobbies");
		
		saveButton = (Button) findViewById(R.id.profile_edit_xml_button_save);
		edittextEmail = (EditText) findViewById(R.id.profile_edit_xml_edittext_email);
		edittextPassword = (EditText) findViewById(R.id.profile_edit_xml_edittext_password);
		edittextName = (EditText) findViewById(R.id.profile_edit_xml_edittext_name);
		edittextEducation = (EditText) findViewById(R.id.profile_edit_xml_edittext_education);
		edittextHometown = (EditText) findViewById(R.id.profile_edit_xml_edittext_hometown);
		edittextHobbies = (EditText) findViewById(R.id.profile_edit_xml_edittext_hobbies);

		edittextEmail.setText(user_email);
		edittextPassword.setText(user_password);
		edittextName.setText(user_name);
		edittextEducation.setText(user_education);
		edittextHometown.setText(user_hometown);
		edittextHobbies.setText(user_hobbies);
		
		String imageUrl = extrasBundle.getString("imageUrl");
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		ImageView imageViewUserPicture = (ImageView) findViewById(R.id.profile_edit_xml_imageview_user_picture);
		imageLoader.displayImage(imageUrl, imageViewUserPicture);

		imageViewUserPicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDialog();
			}
		});
	

		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				user_email = edittextEmail.getText().toString();
				user_password = edittextPassword.getText().toString();
				user_name = edittextName.getText().toString();
				user_education = edittextEducation.getText().toString();
				user_hometown = edittextHometown.getText().toString();
				user_hobbies = edittextHobbies.getText().toString();
				ProfileEditTask task = new ProfileEditTask(ProfileEditActivity.this, userId, user_email, user_password, user_name, user_education
						, user_hometown, user_hobbies);
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
	private String name;
	private String education;
	private String hometown;
	private String hobbies;
	
	public ProfileEditTask(ProfileEditActivity a, int userId, String email, String password, String name, String education, String hometown, 
			String hobbies) {
		this.userId = userId;
		this.email = email;
		this.password = password;
		this.name = name;
		this.education = education;
		this.hometown = hometown;
		this.hobbies = hobbies;
		attach(a);
	}
	
	@Override
	protected void onPreExecute() {
		datas.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
		datas.add(new BasicNameValuePair("username", email));
		datas.add(new BasicNameValuePair("password", password));
		datas.add(new BasicNameValuePair("name", name));
		datas.add(new BasicNameValuePair("education", education));
		datas.add(new BasicNameValuePair("hometown", hometown));
		datas.add(new BasicNameValuePair("hobbies", hobbies));
		
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
		/*
		if (result == false) {
			ref.get().showDialog(3);
		}
		else {
			ref.get().showDialog(2);
		}
		*/
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ImageView temp = (ImageView) findViewById(R.id.profile_edit_xml_imageview_user_picture);
		if (resultCode == RESULT_OK) {
			if (requestCode == GALLERY_PICTURE) {
				Uri selectedImageUri = data.getData();
				String selectedImagePath = getPath(selectedImageUri);
				bitmapUserPicture = BitmapFactory.decodeFile(selectedImagePath);
				temp.setImageBitmap(bitmapUserPicture);
			}
			else if (requestCode == CAMERA_PICTURE) {
				if (data.getExtras() != null) {
					// here is the image from camera
					bitmapUserPicture = (Bitmap) data.getExtras().get("data");
					temp.setImageBitmap(bitmapUserPicture);
				}
			}
			
			// create byte stream array
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			
			// compress picture and add to stream (PNG)
			bitmapUserPicture.compress(Bitmap.CompressFormat.JPEG, 70, stream);
			
			// create raw data src
			byte[] src = stream.toByteArray();
			
			// encode it
			String byteCode = Base64.encodeBytes(src);
			
			UploadPictueTask task = new UploadPictueTask(this, byteCode);
			task.execute();
		}
	}
	
	private static class UploadPictueTask 
	extends AsyncTask<Void, Integer, String> 
		implements IAsyncTask<ProfileEditActivity> {
	
	private WeakReference<ProfileEditActivity> ref;
	private String picturebyteCode;
	
	public UploadPictueTask(ProfileEditActivity a, String pbc) {
		attach(a);
		picturebyteCode = pbc;
	}
	
	@Override
	protected void onPreExecute() {
		
	}

	@Override
	protected String doInBackground(Void... voids) {
		List<NameValuePair> datas = new ArrayList<NameValuePair>();
		// send encoded data to server
		datas.add(new BasicNameValuePair("image", picturebyteCode));
		
		// send a file name where file name = "username" + "current date time UTC", to make sure that we have a unique id picture every time.
		// since the username is unique, we should take advantage of this otherwise two or more users could potentially snap pictures at the same time.
		datas.add(new BasicNameValuePair("file_name",  CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png"));
		
		// send the rest of data
		datas.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		
		
		// get JSON to check result
		JSONObject json = UploadFileHelper.uploadFileToServer(UPDATE_PICTURE_URL, datas);
		String result = "";
		try {
			result = json.getString("result");
		} 
		catch (JSONException e) {
			Log.e(TAG + "UploadPictueTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result.equals("success")) {
			/*
			Toast.makeText(ref.get().getApplicationContext(), "Upload picture done!", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setData(Uri.parse("done"));
			ref.get().setResult(RESULT_OK, intent);
			ref.get().finish();
			*/
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
	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
}