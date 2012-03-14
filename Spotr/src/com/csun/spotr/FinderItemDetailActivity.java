package com.csun.spotr;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.FinderAdditionalItemImageAdapter;
import com.csun.spotr.asynctask.GetFinderDetailTask;
import com.csun.spotr.asynctask.SubmitAdditionalImageTask;
import com.csun.spotr.util.JsonHelper;

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
	
	private List<String> items = new ArrayList<String>();
	private FinderAdditionalItemImageAdapter adapter;
	private static int finderId = -1;
	
	private GetFinderDetailTask getFinderDetailTask;
	private GetFinderImagesTask getFinderImagesTask;
	private SubmitAdditionalImageTask submitAdditionalImageTask;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finder_item_detail);
		
		finderId = getIntent().getExtras().getInt("finder_id");
		
		final Gallery gallery = (Gallery) findViewById(R.id.finder_item_detail_xml_gallery);
		final Button buttonAddImage = (Button) findViewById(R.id.finder_item_detail_xml_button_add_image);
		final Button buttonFound = (Button) findViewById(R.id.finder_item_detail_xml_button_found);
		
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
				dialogMessage.setMessage("You're about to mark this item as found.\n\n" + "Are you sure you want to proceed?");
				
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
		new GetFinderDetailTask(this, finderId).execute();
		new GetFinderImagesTask(this).execute();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {			
			Bitmap bitmap = BitmapFactory.decodeStream(getInputStream(intent), null, getOptions());
			new SubmitAdditionalImageTask(FinderItemDetailActivity.this, bitmap, finderId).execute();
		}
	}
	
	private InputStream getInputStream(Intent intent) {
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
		
		return in;
	}
	
	private Options getOptions() {
		Options options = new Options();
		options.inSampleSize = 1;
		
		return options;
	}
	
	private class GetFinderImagesTask extends AsyncTask<Integer, String, Boolean> {
		private WeakReference<FinderItemDetailActivity> refActivity;
		private ProgressDialog progressDialog = null;
		
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
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
			
			JSONArray jsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_FINDER_ADDITIONAL_IMAGES_URL, data);
			
			if (jsonArray != null) {
				try {
					for (int i = 0; i < jsonArray.length(); ++i) {
						publishProgress(jsonArray.getJSONObject(i).getString("finder_images_tbl_url"));
						Log.d(TAG, jsonArray.getJSONObject(i).getString("finder_images_tbl_url"));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFindersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
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
		TextView textViewName = (TextView)findViewById(R.id.finder_item_detail_xml_name);
		textViewName.setText(name);
		
		TextView textViewDesc = (TextView)findViewById(R.id.finder_item_detail_xml_desc);
		textViewDesc.setText(description);
		
		TextView textViewUser = (TextView)findViewById(R.id.finder_item_detail_xml_user);
		textViewUser.setText(userName);
		
		TextView textViewPoints = (TextView)findViewById(R.id.finder_item_detail_xml_points);
		textViewPoints.setText(points);
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (getFinderDetailTask != null) 
				getFinderDetailTask.cancel(true);
			
			if (getFinderImagesTask != null)
				getFinderImagesTask.cancel(true);
			
			if (submitAdditionalImageTask != null)
				submitAdditionalImageTask.cancel(true);
			
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
