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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Take a picture and upload to server
 */
public class SnapPictureActivity 
	extends Activity {

	private static final String TAG = "(SnapPictureActivity)";
	private static final String SNAP_PICTURE_URL = "http://107.22.209.62/images/upload_picture.php";
	
	private static final int INTENT_RESULT_LINK = 0;
	private static final int INTENT_RESULT_TAKE_PICTURE = 1;
	
	private Bitmap takenPictureBitmap = null;
	private String usersId;
	private String spotsId;
	private String challengesId;
	private String comment;
	private String link;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.snap_picture);
		initChallengeInfoFromBundle();
		setupLinkButton();
		setupTakePicture();
	}
	
	private void initChallengeInfoFromBundle() {
		Bundle extras = getIntent().getExtras();
		usersId = extras.getString("users_id");
		spotsId = extras.getString("spots_id");
		challengesId = extras.getString("challenges_id");
		
		// dummy comment
		comment = "hello snap picture";
	}
	
	private void setupLinkButton() {
		final Button buttonLink = (Button) findViewById(R.id.snap_picture_xml_button_choose_link);
		buttonLink.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), AddWebLinkActivity.class);
				startActivityForResult(intent, INTENT_RESULT_LINK);
			}
		});
	}
	
	private void setupTakePicture() {
		final ImageView imageViewTakePicture = (ImageView) findViewById(R.id.snap_picture_xml_imageview_go);
		imageViewTakePicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, INTENT_RESULT_TAKE_PICTURE);
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_RESULT_LINK) {
			if (resultCode == RESULT_OK) {
				Bundle b = data.getExtras();
				String url = b.getString("link");
				EditText editTextUrl = (EditText) findViewById(R.id.snap_picture_xml_edittext_link);
				editTextUrl.setText(url);
			}
		
		}
		else if (requestCode == INTENT_RESULT_TAKE_PICTURE) {
			if (resultCode == RESULT_OK) {
				// get bitmap from the take picture activity
				takenPictureBitmap = (Bitmap) data.getExtras().get("data");
				// display it
				displayNewTakenImage(takenPictureBitmap);
				// now activate upload button
				activateUploadButton();
			}
		}
		else {
			Log.e(TAG, "Unexpected result has occurred!");
		}
	}
	
	private String getByteCodeFromBitmap(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// compress picture and add to stream (PNG)
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		// create raw data src
		byte[] src = stream.toByteArray();
		// encode it
		return Base64.encodeBytes(src);
	}
	
	private void activateUploadButton() {
		final Button buttonUpload = (Button) findViewById(R.id.snap_picture_xml_button_next);
		buttonUpload.setEnabled(true);
		buttonUpload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String byteCode = getByteCodeFromBitmap(takenPictureBitmap);
				EditText editTextLink = (EditText) findViewById(R.id.snap_picture_xml_edittext_link);
				link = editTextLink.getText().toString();
				UploadPictueTask task = new UploadPictueTask(SnapPictureActivity.this, byteCode, usersId, spotsId, challengesId, comment, link);
				task.execute();
			}
		});
	}
	
	private void displayNewTakenImage(Bitmap takenPictureBitmap) {
		ImageView imageViewPreview = (ImageView) findViewById(R.id.snap_picture_xml_imageview_preview_picture);
		imageViewPreview.setImageBitmap(takenPictureBitmap);
	}

	private static class UploadPictueTask 
		extends AsyncTask<Void, Integer, String> 
			implements IAsyncTask<SnapPictureActivity> {
		
		private static final String TAG = "[AsyncTask].UploadPictureTask";
		private WeakReference<SnapPictureActivity> ref;
		private String pictureByteCode;
		private String usersId;
		private String spotsId;
		private String challengesId;
		private String comment;
		private String link;
		
		public UploadPictueTask(SnapPictureActivity a, String pictureByteCode, String usersId, String spotsId, String challengesId, String comment, String link) {
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
		
		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> datas = new ArrayList<NameValuePair>();
			datas.add(new BasicNameValuePair("image", pictureByteCode));
			datas.add(new BasicNameValuePair("file_name",  getPictureFileName()));
			datas.add(new BasicNameValuePair("users_id", usersId));
			datas.add(new BasicNameValuePair("spots_id", spotsId));
			datas.add(new BasicNameValuePair("challenges_id", challengesId));
			datas.add(new BasicNameValuePair("comment", comment));
			datas.add(new BasicNameValuePair("link", link));
			return datas;
		}

		@Override
		protected String doInBackground(Void... voids) {
			List<NameValuePair> datas = prepareUploadData();
			JSONObject json = UploadFileHelper.uploadFileToServer(SNAP_PICTURE_URL, datas);
			String result = "";
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
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
			else if (result.equals("fail")) {
				AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
				dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
				dialogMessage.setMessage("You can only upload one picture a day for any spot. Try again later.");
				dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogMessage.show();	
			}
			else {
				Log.e(TAG, "unexpected error has occured!");
			}
			
			detach();
		}
		
		public void attach(SnapPictureActivity a) {
			ref = new WeakReference<SnapPictureActivity>(a);
		}
		
		public void detach() {
			ref.clear();
		}
		
		private String getPictureFileName() {
			return CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png";
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
}
