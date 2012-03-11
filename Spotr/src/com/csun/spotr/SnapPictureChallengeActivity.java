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
import com.csun.spotr.util.UploadFileHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Description:
 * 		Take a picture and upload to server
 */
public class SnapPictureChallengeActivity 
	extends Activity {

	private static final 	String 			TAG = "(SnapPictureChallengeActivity)";
	private static final 	String 			SNAP_PICTURE_URL = "http://107.22.209.62/images/upload_picture.php";
	
	private 				Bitmap 			takenPictureBitmap = null;
	private 				String 			usersId;
	private 				String 			spotsId;
	private 				String 			challengesId;
	private 				String 			description;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snap_picture_challenge);
		
		Bundle extras = getIntent().getExtras();
		usersId = extras.getString("users_id");
		spotsId = extras.getString("spots_id");
		challengesId = extras.getString("challenges_id");
		description = extras.getString("description");
		
		TextView instructions = (TextView) findViewById(R.id.snap_picture_xml_snap_picture_challenge_description);
		instructions.setText(description); 
			
		final ImageView imageViewTakePicture = (ImageView) findViewById(R.id.snap_picture_xml_imageview_go);
		imageViewTakePicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, 1);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				String url = b.getString("link");
				EditText editTextUrl = (EditText) findViewById(R.id.snap_picture_xml_edittext_link);
				editTextUrl.setText(url);
			}
		
		}
		else {
			if (resultCode == RESULT_OK) {
				ImageView imageViewPreview = (ImageView) findViewById(R.id.snap_picture_xml_imageview_preview_picture);
				// here is the image from camera
				takenPictureBitmap = (Bitmap) data.getExtras().get("data");
				// initialize image view
				imageViewPreview.setImageBitmap(takenPictureBitmap);
				final Button buttonUpload = (Button) findViewById(R.id.snap_picture_xml_button_next);
				// only enable this button when data is available
				buttonUpload.setEnabled(true);
				buttonUpload.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// disable both buttons to avoid user hit the back button, then hit upload again
						buttonUpload.setEnabled(false);
						// start upload picture to server
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						// compress picture and add to stream (PNG)
						takenPictureBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
						// create raw data src
						byte[] src = stream.toByteArray();
						// encode it
						String byteCode = Base64.encodeBytes(src);
						
						
						UploadPictueTask task = new UploadPictueTask(SnapPictureChallengeActivity.this, byteCode, usersId, spotsId, challengesId, description, null);
						task.execute();
					}
				});
			}
		}
	}

	private static class UploadPictueTask 
		extends AsyncTask<Void, Integer, String> 
			implements IAsyncTask<SnapPictureChallengeActivity> {
		
		private WeakReference<SnapPictureChallengeActivity> ref;
		private String pictureByteCode;
		private String usersId;
		private String spotsId;
		private String challengesId;
		private String comment;
		private String link;
		
		public UploadPictueTask(SnapPictureChallengeActivity a, String pictureByteCode, String usersId, String spotsId, String challengesId, String comment, String link) {
			attach(a);
			this.pictureByteCode = pictureByteCode;
			this.usersId = usersId;
			this.spotsId = spotsId;
			this.challengesId = challengesId;
			this.comment = comment;
			this.link = link;
		}
		
		@Override
		protected void onPreExecute() {
			
		}

		@Override
		protected String doInBackground(Void... voids) {
			
			List<NameValuePair> datas = new ArrayList<NameValuePair>();
			// send encoded data to server
			datas.add(new BasicNameValuePair("image", pictureByteCode));
			// send a file name where file name = "username" + "current date time UTC", to make sure that we have a unique id picture every time.
			// since the username is unique, we should take advantage of this otherwise two or more users could potentially snap pictures at the same time.
			datas.add(new BasicNameValuePair("file_name",  CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png"));
			// send the rest of data
			datas.add(new BasicNameValuePair("users_id", usersId));
			datas.add(new BasicNameValuePair("spots_id", spotsId));
			datas.add(new BasicNameValuePair("challenges_id", challengesId));
			datas.add(new BasicNameValuePair("comment", comment));
			datas.add(new BasicNameValuePair("link", link));
			
			// get JSON to check result
			JSONObject json = UploadFileHelper.uploadFileToServer(SNAP_PICTURE_URL, datas);
		
			String result = "";
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "UploadPictueTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				Toast.makeText(ref.get().getApplicationContext(), "Upload picture done!", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent();
				intent.setData(Uri.parse("done"));
				ref.get().setResult(RESULT_OK, intent);
				ref.get().finish();
			}
			
			detach();
		}
		
		public void attach(SnapPictureChallengeActivity a) {
			ref = new WeakReference<SnapPictureChallengeActivity>(a);
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
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		if (takenPictureBitmap != null) {
			takenPictureBitmap.recycle(); 
			takenPictureBitmap = null;
		}
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.options_menu_xml_item_setting_icon:
				intent = new Intent("com.csun.spotr.SettingsActivity");
				startActivity(intent);
				finish();
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
}
