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

import com.csun.spotr.adapter.FinderAdditionalItemImageAdapter;
import com.csun.spotr.singleton.CurrentDateTime;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.Base64;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.UploadFileHelper;

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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

public class FinderItemDetailActivity extends Activity {
	private static final String TAG = "(FinderItemDetailActivity)";
	private static final String GET_FINDER_ADDITIONAL_IMAGES_URL = "http://107.22.209.62/android/get_finder_additional_images.php";
	private static final String GET_FINDER_DETAIL_URL = "http://107.22.209.62/android/get_finder_detail.php";
	private static final String SUBMIT_ADDITIONAL_IMAGE = "http://107.22.209.62/android/do_add_finder_image.php";
	
	private List<String> items = new ArrayList<String>();
	private FinderAdditionalItemImageAdapter adapter;
	private static int finderId = -1;
	private GetFinderDetailTask getFinderDetailTask;
	private GetFinderImagesTask getFinderImagesTask;
	private SubmitAdditionalImageTask submitAdditionalImageTask;
	
	private Gallery gallery;
	private Button buttonFound;
	private Button buttonAddImage;
	private TextView textViewName;
	private TextView textViewDesc;
	private TextView textViewUser;
	private TextView textViewPoints;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finder_item_detail);
		
		finderId = getIntent().getExtras().getInt("finder_id");
		
		textViewName = (TextView)findViewById(R.id.finder_item_detail_xml_name);
		textViewDesc = (TextView)findViewById(R.id.finder_item_detail_xml_desc);
		textViewUser = (TextView)findViewById(R.id.finder_item_detail_xml_user);
		textViewPoints = (TextView)findViewById(R.id.finder_item_detail_xml_points);
		gallery = (Gallery) findViewById(R.id.finder_item_detail_xml_gallery);
		buttonAddImage = (Button) findViewById(R.id.finder_item_detail_xml_button_add_image);
		buttonFound = (Button) findViewById(R.id.finder_item_detail_xml_button_found);
		
		buttonAddImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				i.setType("image/*");
				startActivityForResult(i, 0);
			}
		});
		
		buttonFound.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog dialogMessage = new AlertDialog.Builder(FinderItemDetailActivity.this).create();
				dialogMessage.setTitle("Wait just a second.");
				dialogMessage.setMessage("You're about to mark this item as found.\n\n" + 
						"Are you sure you want to proceed?");
				
				dialogMessage.setButton(Dialog.BUTTON_POSITIVE, "Yes, I really found this", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent(getApplicationContext(), FinderFoundActivity.class);
						intent.putExtra("finder_id", finderId);
						setResult(RESULT_OK, intent);
						startActivity(intent);
						finish();
					}
				});
				
				dialogMessage.setButton(Dialog.BUTTON_NEGATIVE, "No thanks!", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent intent = new Intent();
						setResult(RESULT_OK, intent);
						finish();
					}
				});
				
				dialogMessage.show();
			}
		});
		
		adapter = new FinderAdditionalItemImageAdapter(this, items);

		gallery.setAdapter(adapter);
		
		getFinderDetailTask = new GetFinderDetailTask(this);
		getFinderDetailTask.execute();
		
		getFinderImagesTask = new GetFinderImagesTask(this);
		getFinderImagesTask.execute();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

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
			options.inSampleSize = 1;
			Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
			submitAdditionalImageTask = new SubmitAdditionalImageTask(FinderItemDetailActivity.this, bitmap);
		}
	}
	
	private static class SubmitAdditionalImageTask extends AsyncTask<String, Integer, String> 
	implements IAsyncTask<FinderItemDetailActivity> {
	
		private List<NameValuePair> finderImageData = new ArrayList<NameValuePair>();
		private WeakReference<FinderItemDetailActivity> ref;
		private ProgressDialog progressDialog = null;
		private Bitmap imageBitmap;
	
		public SubmitAdditionalImageTask(FinderItemDetailActivity a, Bitmap b) {
			attach(a);
			imageBitmap = b;
		}
	
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Submitting image...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
	
		protected String doInBackground(String... params) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
			byte[] src = stream.toByteArray();

			String imageBytecode = Base64.encodeBytes(src);
			String imageFile = CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png";
			
			finderImageData.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
			finderImageData.add(new BasicNameValuePair("image", imageBytecode));
			finderImageData.add(new BasicNameValuePair("filename", imageFile));
	
			JSONObject json = UploadFileHelper.uploadFileToServer(SUBMIT_ADDITIONAL_IMAGE, finderImageData);
			String result = "";
			try {
				result = json.getString("result");
			}
			catch (JSONException e) {
				Log.e(TAG + ".FinderItemDetailActivity", e.toString());
			}
	
			return result;
		}
	
		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if (result.equals("success")) {
				AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
				dialogMessage.setTitle("Image uploaded!");
				dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
						+ ", image submission successful!");
				
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
	
		public void detach() {
			ref.clear();
		}

		public void attach(FinderItemDetailActivity a) {
			ref = new WeakReference<FinderItemDetailActivity>(a);			
		}
	
	}
	
	private class GetFinderDetailTask extends AsyncTask<Integer, String, Boolean> {
		private List<NameValuePair> finderData = new ArrayList<NameValuePair>();
		private WeakReference<FinderItemDetailActivity> refActivity;
		private ProgressDialog progressDialog = null;
		private JSONArray jsonArray = null;
		
		public GetFinderDetailTask(FinderItemDetailActivity c) {
			refActivity = new WeakReference<FinderItemDetailActivity>(c);
		}
		
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(FinderItemDetailActivity.this);
			progressDialog.setMessage("Gathering item details...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}
		
		@Override 
		protected void onProgressUpdate(String...params) {
			refActivity.get().updateFinderInfo(params[0], params[1], params[2], params[3]);
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			finderData.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
			jsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_FINDER_DETAIL_URL, finderData);
			if (jsonArray != null) {
				try {
					publishProgress(jsonArray.getJSONObject(0).getString("finder_tbl_name"), 
							jsonArray.getJSONObject(0).getString("finder_tbl_description"), 
							jsonArray.getJSONObject(0).getString("finder_tbl_points"),
							jsonArray.getJSONObject(0).getString("users_tbl_username"));
					
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFindersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
					Toast.makeText(refActivity.get(), "Error retrieving item's pictures", Toast.LENGTH_SHORT);
				}
				return true;
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();			
		}
	}
	
	private class GetFinderImagesTask extends AsyncTask<Integer, String, Boolean> {
		private List<NameValuePair> finderData = new ArrayList<NameValuePair>();
		private WeakReference<FinderItemDetailActivity> refActivity;
		private ProgressDialog progressDialog = null;
		private JSONArray jsonArray = null;
		
		public GetFinderImagesTask(Activity c) {
			refActivity = new WeakReference<FinderItemDetailActivity>((FinderItemDetailActivity)c);
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(FinderItemDetailActivity.this);
			progressDialog.setMessage("Loading items...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected void onProgressUpdate(String... urls) {
			items.add(urls[0]);
			adapter.notifyDataSetChanged();
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			finderData.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
			jsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_FINDER_ADDITIONAL_IMAGES_URL, finderData);
			if (jsonArray != null) {
				try {
					for (int i = 0; i < jsonArray.length(); ++i) {
						publishProgress(jsonArray.getJSONObject(i).getString("finder_images_tbl_url"));
						Log.d(TAG, jsonArray.getJSONObject(i).getString("finder_images_tbl_url"));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFindersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
					Toast.makeText(refActivity.get(), "Error retrieving item's pictures", Toast.LENGTH_SHORT);
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			
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
	
	public void updateFinderInfo(String name, String description, String points, String userName) {
		textViewName.setText(name);
		textViewDesc.setText(description);
		textViewUser.setText(userName);
		textViewPoints.setText(points);
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if(getFinderDetailTask != null) 
				getFinderDetailTask.cancel(true);
			if(getFinderImagesTask != null)
				getFinderImagesTask.cancel(true);
			if(submitAdditionalImageTask != null)
				submitAdditionalImageTask.cancel(true);
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
