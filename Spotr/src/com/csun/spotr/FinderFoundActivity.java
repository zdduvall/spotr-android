package com.csun.spotr;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.UploadFileHelper;
import com.csun.spotr.util.FineLocation.LocationResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FinderFoundActivity extends Activity {
	private static final String TAG = "(FinderFoundActivity)";
	private static final String FINDER_FOUND_URL = "http://107.22.209.62/android/do_found_finder.php";
	private static final int CAMERA_REQUEST_CODE = 0;
	private static final int GALLERY_REQUEST_CODE = 1;
	
	private static boolean hasImage = false;
	private static int finderId = 0;
	private static Bitmap imageBitmap = null;
	private static Location lastKnownLocation = null;
	private static FineLocation fineLocation = new FineLocation();
	
	private Button buttonAddImage;
	private Button buttonSubmit;
	private EditText editTextMessage;
	private TextView textViewCount;
	private static ImageView imageViewImage;
	
	private FinderFoundTask finderFoundTask;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finder_found);
		Log.v(TAG, "onCreate(...)");
		
		finderId = getIntent().getIntExtra("finder_id", 0);
		
		buttonAddImage = (Button) findViewById(R.id.finder_found_xml_button_add_image);
		buttonSubmit = (Button) findViewById(R.id.finder_found_xml_button_submit);
		editTextMessage = (EditText) findViewById(R.id.finder_found_xml_edittext_message);
		textViewCount = (TextView) findViewById(R.id.finder_found_xml_textview_character_count);
		imageViewImage = (ImageView) findViewById(R.id.finder_found_xml_imageview_image);
		
		finderFoundTask = new FinderFoundTask(this);
		buttonSubmit.setEnabled(false);
		
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;
			}
		});
		fineLocation.getLocation(this, locationResult);
		
		editTextMessage.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				textViewCount.setText(String.valueOf(s.length()) + "/160");
				if (s.length() > 0) {
					buttonSubmit.setEnabled(true);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		
		buttonAddImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog dialogMessage = new AlertDialog.Builder(FinderFoundActivity.this).create();
				dialogMessage.setTitle("Camera or Gallery");
				dialogMessage.setMessage("Where do you want to upload your picture from?");
				
				dialogMessage.setButton(Dialog.BUTTON_POSITIVE, "Camera", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(i, CAMERA_REQUEST_CODE);
					}
				});
				
				dialogMessage.setButton(Dialog.BUTTON_NEGATIVE, "Gallery", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						i.setType("image/*");
						startActivityForResult(i, GALLERY_REQUEST_CODE);
					}
				});
				
				dialogMessage.show();
			}
		});
		
		buttonSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(hasImage)
					finderFoundTask.execute();
				else
					Toast.makeText(getApplicationContext(), "Please add a picture", Toast.LENGTH_SHORT);
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (requestCode == CAMERA_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				imageBitmap = (Bitmap) intent.getExtras().get("data");
				imageViewImage.setImageBitmap(imageBitmap);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
				imageViewImage.setImageBitmap(imageBitmap);
				hasImage = true;
			}
		}
		
		else if (requestCode == GALLERY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Uri selectedImageUri = intent.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
				cursor.moveToFirst();

				ContentResolver cr = getContentResolver();
				InputStream in = null;
				try {
					in = cr.openInputStream(selectedImageUri);
				}
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				Options options = new Options();
				options.inSampleSize = 8;
				imageBitmap = BitmapFactory.decodeStream(in, null, options);
				imageViewImage.setImageBitmap(imageBitmap);
				hasImage = true;
			}
		}
	}
	
	private static class FinderFoundTask extends AsyncTask<String, Integer, String> 
	implements IAsyncTask<FinderFoundActivity> {
	
		private List<NameValuePair> finderData = new ArrayList<NameValuePair>();
		private WeakReference<FinderFoundActivity> ref;
		private ProgressDialog progressDialog = null;
		
		public FinderFoundTask(FinderFoundActivity a) {
			attach(a);
		}
	
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
	
		protected String doInBackground(String... params) {
			// Prepare bitmap for Base-64 encoding
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
			byte[] src = stream.toByteArray();
			
			// Add values to array list
			finderData.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
			finderData.add(new BasicNameValuePair("filename", CurrentUser.getCurrentUser().getUsername() + 
					CurrentDateTime.getUTCDateTime().trim() + ".png"));
			finderData.add(new BasicNameValuePair("image", Base64.encodeBytes(src)));
			finderData.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			finderData.add(new BasicNameValuePair("comment", ref.get().editTextMessage.getText().toString()));
			finderData.add(new BasicNameValuePair("lat", Double.toString(lastKnownLocation.getLatitude())));
			finderData.add(new BasicNameValuePair("long", Double.toString(lastKnownLocation.getLongitude())));
			
			JSONObject json = UploadFileHelper.uploadFileToServer(FINDER_FOUND_URL, finderData);
			
			String result = "";
			try {
				result = json.getString("result");
			}
			catch (JSONException e) {
				Log.e(TAG + ".FinderFoundActivity", e.toString());
			}
	
			return result;
		}
	
		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if (result.equals("success")) {
				AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
				dialogMessage.setTitle("Item marked as found!");
				dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
						+ ", the item's owner has been notified that it was found!");
				
				dialogMessage.setButton("Okay", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent();
						ref.get().setResult(RESULT_OK, intent);
						ref.get().finish();
					}
				});
				
				dialogMessage.show();
			}
			else {
				AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
				dialogMessage.setTitle("Submission error");
				dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
						+ ", there was an error processing.");
				
				dialogMessage.setButton("Okay", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent();
						ref.get().setResult(RESULT_OK, intent);
						ref.get().finish();
					}
				});
				
				dialogMessage.show();
				Log.d(TAG, "Result = FAIL");
			}
		}
	
		public void detach() {
			ref.clear();
		}

		public void attach(FinderFoundActivity a) {
			ref = new WeakReference<FinderFoundActivity>(a);			
		}
	
	}
}
