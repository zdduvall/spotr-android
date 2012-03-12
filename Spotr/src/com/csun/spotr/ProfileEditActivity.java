package com.csun.spotr;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentDateTime;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.Base64;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.UploadFileHelper;
import com.csun.spotr.util.UrlConstant;

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
import android.widget.Toast;

public class ProfileEditActivity extends Activity {
	private static final String TAG = "(ProfileEditActivity)";
	private static final String UPDATE_USER_DETAIL_URL = "http://107.22.209.62/android/update_user_detail.php";
	private static final String UPDATE_PICTURE_URL = "http://107.22.209.62/images/upload_user_picture.php";
	private static final int INTENT_RESULT_CAMERA_PICTURE  = 0;
	private static final int INTENT_RESULT_GALLERY_PICTURE = 1;

	private int userId = -1;
	private Bitmap bitmapUserPicture = null;
	private String userImageUrl = "";
	private String newPictureName = CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png";
	private boolean pictureChanged = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_edit);

		Bundle extrasBundle = getIntent().getExtras();
		userId = extrasBundle.getInt("user_id");
		userImageUrl = extrasBundle.getString("imageUrl");
		
		setupUserDetailInformation(extrasBundle);
		setupUserPictureImageView(extrasBundle);
	}
	
	private void setupUserPictureImageView(Bundle extrasBundle) {
		String imageUrl = extrasBundle.getString("imageUrl");
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		ImageView imageViewUserPicture = (ImageView) findViewById(R.id.profile_edit_xml_imageview_user_picture);
		imageLoader.displayImage(imageUrl, imageViewUserPicture);
		imageViewUserPicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDialog();
			}
		});
	}

	private void setupUserDetailInformation(Bundle extrasBundle) {
		final Button saveButton = (Button) findViewById(R.id.profile_edit_xml_button_save);
		final EditText edittextEmail = (EditText) findViewById(R.id.profile_edit_xml_edittext_email);
		final EditText edittextPassword = (EditText) findViewById(R.id.profile_edit_xml_edittext_password);
		final EditText edittextName = (EditText) findViewById(R.id.profile_edit_xml_edittext_name);
		final EditText edittextEducation = (EditText) findViewById(R.id.profile_edit_xml_edittext_education);
		final EditText edittextHometown = (EditText) findViewById(R.id.profile_edit_xml_edittext_hometown);
		final EditText edittextHobbies = (EditText) findViewById(R.id.profile_edit_xml_edittext_hobbies);

		edittextEmail.setText(extrasBundle.getString("email"));
		edittextPassword.setText(extrasBundle.getString("password"));
		edittextName.setText(extrasBundle.getString("name"));
		edittextEducation.setText(extrasBundle.getString("education"));
		edittextHometown.setText(extrasBundle.getString("hometown"));
		edittextHobbies.setText(extrasBundle.getString("hobbies"));

		// set up save button
		setupSaveButton(edittextEmail, edittextPassword, edittextName, edittextEducation, edittextHometown, edittextHobbies);
	}

	private void setupSaveButton(final EditText edittextEmail, final EditText edittextPassword, final EditText edittextName, final EditText edittextEducation, final EditText edittextHometown, final EditText edittextHobbies) {

		final Button saveButton = (Button) findViewById(R.id.profile_edit_xml_button_save);
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				ProfileEditTask task = new ProfileEditTask(ProfileEditActivity.this, userId, edittextEmail.getText().toString().trim(), edittextPassword.getText().toString().trim(), edittextName.getText().toString().trim(), edittextEducation.getText().toString().trim(), edittextHometown.getText().toString().trim(), edittextHobbies.getText().toString().trim());
				task.execute();
			}
		});
	}

	private static class ProfileEditTask 
		extends AsyncTask<Void, Integer, Boolean> 
			implements IAsyncTask<ProfileEditActivity> {

		private static final String TAG = "[AsyncTask].ProfileEditTask";
		private WeakReference<ProfileEditActivity> ref;

		private int userId;
		private String username;
		private String password;
		private String name;
		private String education;
		private String hometown;
		private String hobbies;

		public ProfileEditTask(ProfileEditActivity a, int userId, String email, String password, String name, String education, String hometown, String hobbies) {
			this.userId = userId;
			this.username = email;
			this.password = password;
			this.name = name;
			this.education = education;
			this.hometown = hometown;
			this.hobbies = hobbies;
			attach(a);
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			data.add(new BasicNameValuePair("username", username));
			data.add(new BasicNameValuePair("password", password));
			data.add(new BasicNameValuePair("name", name));
			data.add(new BasicNameValuePair("education", education));
			data.add(new BasicNameValuePair("hometown", hometown));
			data.add(new BasicNameValuePair("hobbies", hobbies));
			return data;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = prepareUploadData();
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(UPDATE_USER_DETAIL_URL, data);
			try {
				if (json.getString("result").equals("success"))
					return true;
			}
			catch (JSONException e) {
				Log.e(TAG, ".doInBackground(Void... voids)");
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result == true) {
				sendDataBack();
			}
			else { 
				Toast.makeText(ref.get().getApplicationContext(), "Update use info failed!", Toast.LENGTH_SHORT);
			}
			detach();
		}

		private void sendDataBack() {
			Intent intent = new Intent();
			intent.putExtra("username", username);
			/*
			 * If the user has changed his/her picture, 
			 * we send back the new url
			 */
			if (!ref.get().pictureChanged) {
				intent.putExtra("user_image_url", ref.get().userImageUrl);
			}
			else {
				intent.putExtra("user_image_url", UrlConstant.URL_CONSTANT_IMAGE + ref.get().newPictureName);
			}
			ref.get().setResult(RESULT_OK, intent);
			ref.get().finish();
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
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), INTENT_RESULT_GALLERY_PICTURE);
			}
		});

		myAlertDialog.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, INTENT_RESULT_CAMERA_PICTURE);
			}
		});
		myAlertDialog.show();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ImageView temp = (ImageView) findViewById(R.id.profile_edit_xml_imageview_user_picture);
		if (resultCode == RESULT_OK) {
			if (requestCode == INTENT_RESULT_GALLERY_PICTURE) {
				Uri selectedImageUri = data.getData();
				String selectedImagePath = getPath(selectedImageUri);
				bitmapUserPicture = BitmapFactory.decodeFile(selectedImagePath);
				temp.setImageBitmap(bitmapUserPicture);
			}
			else if (requestCode == INTENT_RESULT_CAMERA_PICTURE) {
				if (data.getExtras() != null) {
					bitmapUserPicture = (Bitmap) data.getExtras().get("data");
					temp.setImageBitmap(bitmapUserPicture);
				}
			}
			else {
				Log.e(TAG, "Unexpected error has occured!");
			}

			UploadPictueTask task = new UploadPictueTask(this, getPictureByteCode(bitmapUserPicture));
			task.execute();
		}
	}
	
	private String getPictureByteCode(Bitmap bitmap) {
		// create byte stream array
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// compress picture and add to stream (PNG)
		bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
		// create raw data src
		byte[] src = stream.toByteArray();
		// encode it
		return Base64.encodeBytes(src);
	}

	private static class UploadPictueTask 
		extends AsyncTask<Void, Integer, Boolean> 
			implements IAsyncTask<ProfileEditActivity> {

		private WeakReference<ProfileEditActivity> ref;
		private String picturebyteCode;

		public UploadPictueTask(ProfileEditActivity a, String pbc) {
			attach(a);
			picturebyteCode = pbc;
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("image", picturebyteCode));
			data.add(new BasicNameValuePair("file_name", ref.get().newPictureName));
			data.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			return data;
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> datas = prepareUploadData();
			JSONObject json = UploadFileHelper.uploadFileToServer(UPDATE_PICTURE_URL, datas);
			try {
				if (json.getString("result").equals("success"))
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
				Toast.makeText(ref.get().getApplicationContext(), "Picture changed", Toast.LENGTH_SHORT).show();
				ref.get().pictureChanged = true;
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