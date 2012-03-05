package com.csun.spotr;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.JsonHelper;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class CreateFinderActivity extends Activity {
	private static final String TAG = "(CreateLostItemActivity)";
	private static final String GET_USER_POINTS_URL = "http://107.22.209.62/android/get_user_points.php";
	private static final String SUBMIT_ITEM_URL = "http://107.22.209.62/android/do_create_finder.php";
	private static final int CAMERA_REQUEST_CODE = 0;
	private static final int GALLERY_REQUEST_CODE = 1;
	
	private static Integer userPoints = 0;
	private static String itemName = null;
	private static String itemDesc = null;
	private static int itemPoints = 0;
	private static String itemBytecode = null;
	private static String itemFile = null;
	private static int userId = 0;
	private static Location lastKnownLocation = null;
	private static FineLocation fineLocation = new FineLocation();

	private ImageView imageViewPicture;
	private Bitmap bitmapPicture = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_lost_item);

		// Hide the keyboard
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		final EditText editTextName = (EditText) findViewById(R.id.create_lost_item_xml_edittext_name);
		final EditText editTextDescription = (EditText) findViewById(R.id.create_lost_item_xml_edittext_description);
		final EditText editTextPoints = (EditText) findViewById(R.id.create_lost_item_xml_edittext_points);
		
		final Button buttonSelectImage = (Button) findViewById(R.id.create_lost_item_xml_button_choose_image);
		final Button buttonPointsPlus = (Button) findViewById(R.id.create_lost_item_xml_button_plus);
		final Button buttonPointsMinus = (Button) findViewById(R.id.create_lost_item_xml_button_minus);
		final Button buttonSubmit = (Button) findViewById(R.id.create_lost_item_xml_button_upload);
		imageViewPicture = (ImageView) findViewById(R.id.create_lost_item_xml_imageview_item_images);

		editTextPoints.setText(Integer.toString(itemPoints));
		
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;
			}
		});
		fineLocation.getLocation(this, locationResult);

		buttonSelectImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog dialogMessage = new AlertDialog.Builder(CreateFinderActivity.this).create();
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

		buttonPointsPlus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemPoints < userPoints)
					itemPoints++;
				
				editTextPoints.setText(Integer.toString(itemPoints));
			}
		});

		buttonPointsMinus.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (itemPoints > 0)
					itemPoints--;
				
				editTextPoints.setText(Integer.toString(itemPoints));
			}
		});

		buttonSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 70, stream);
				byte[] src = stream.toByteArray();

				itemBytecode = Base64.encodeBytes(src);
				itemName = editTextName.getText().toString();
				itemDesc = editTextDescription.getText().toString();
				itemFile = CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png";
				userId = CurrentUser.getCurrentUser().getId();

				new SubmitFinder(CreateFinderActivity.this).execute();
			}
		});

		Log.d(TAG, "currentUser: " + CurrentUser.getCurrentUser().getId());

		// Get user's current points (via AsyncTask)
		new GetUserPoints(this, CurrentUser.getCurrentUser().getId()).execute();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (resultCode == RESULT_OK) {
			if (requestCode == GALLERY_REQUEST_CODE) {
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
				bitmapPicture = BitmapFactory.decodeStream(in, null, options);
				imageViewPicture.setImageBitmap(bitmapPicture);
			}
			
			if (requestCode == CAMERA_REQUEST_CODE) {
				bitmapPicture = (Bitmap) intent.getExtras().get("data");
				imageViewPicture.setImageBitmap(bitmapPicture);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 70, stream);
				imageViewPicture.setImageBitmap(bitmapPicture);
			}
		}
	}

	// We need the user's points to determine how many he/she can distribute
	private static class GetUserPoints 
		extends AsyncTask<Void, Integer, Boolean> 
			implements IAsyncTask<CreateFinderActivity> {
		
		private WeakReference<CreateFinderActivity> ref;
		private List<NameValuePair> jsonList = new ArrayList<NameValuePair>();
		private int userId;

		public GetUserPoints(CreateFinderActivity a, int id) {
			attach(a);
			userId = id;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			jsonList.add(new BasicNameValuePair("id", Integer.toString(userId)));
			JSONArray jsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_POINTS_URL, jsonList);
			if (jsonArray != null) {
				try {
					if (jsonArray.getJSONObject(0).getString("users_tbl_points") != null) {
						userPoints = jsonArray.getJSONObject(0).getInt("users_tbl_points");
						Log.d(TAG, "userPoints: " + userPoints);
					}
				}
				catch (JSONException e) {
					Log.e(TAG + ": GetUserPoints()", e.toString());
				}

				return true;
			}

			return false;
		}

		protected void onPostExecute(Boolean result) {
			if (result == false) {
				Toast.makeText(ref.get().getApplicationContext(), "Oops! There was an error", Toast.LENGTH_LONG);
			}
			
			detach();
		}

		public void attach(CreateFinderActivity a) {
			ref = new WeakReference<CreateFinderActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

	}

	private static class SubmitFinder 
		extends AsyncTask<String, Integer, String> 
			implements IAsyncTask<CreateFinderActivity> {
		
		private List<NameValuePair> finderData = new ArrayList<NameValuePair>();
		private WeakReference<CreateFinderActivity> ref;
		private ProgressDialog progressDialog = null;

		public SubmitFinder(CreateFinderActivity a) {
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
			finderData.add(new BasicNameValuePair("name", itemName));
			finderData.add(new BasicNameValuePair("desc", itemDesc));
			finderData.add(new BasicNameValuePair("points", Integer.toString(itemPoints)));
			finderData.add(new BasicNameValuePair("image", itemBytecode));
			finderData.add(new BasicNameValuePair("filename", itemFile));
			finderData.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			finderData.add(new BasicNameValuePair("latitude", Double.toString(lastKnownLocation.getLatitude())));
			finderData.add(new BasicNameValuePair("longitude", Double.toString(lastKnownLocation.getLongitude())));

			JSONObject json = UploadFileHelper.uploadFileToServer(SUBMIT_ITEM_URL, finderData);
			String result = "";
			try {
				result = json.getString("result");
			}
			catch (JSONException e) {
				Log.e(TAG + ".submitLostItem", e.toString());
			}

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if (result.equals("success")) {
				AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
				dialogMessage.setTitle("Submission uploaded!");
				dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
						+ ", submission successful.");
				
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
				Log.d(TAG, "Result = FAIL");
			}
		}

		public void attach(CreateFinderActivity a) {
			ref = new WeakReference<CreateFinderActivity>(a);
		}

		public void detach() {
			ref.clear();
		}

	}

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		super.onDestroy();
	}
}
