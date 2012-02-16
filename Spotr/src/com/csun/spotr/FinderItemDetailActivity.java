package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.FinderAdditionalItemImageAdapter;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

public class FinderItemDetailActivity extends Activity {
	private static final String TAG = "(FinderItemDetailActivity)";
	private static final String GET_FINDER_ADDITIONAL_IMAGES_URL = "http://107.22.209.62/android/get_finder_additional_images.php";
	private static final String GET_FINDER_DETAIL_URL = "http://107.22.209.62/android/get_finder_detail.php";
	
	private List<String> items = new ArrayList<String>();
	private FinderAdditionalItemImageAdapter adapter;
	private int finderId;
	
	private Gallery gallery;
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
		
		adapter = new FinderAdditionalItemImageAdapter(this, items);
		gallery = (Gallery) findViewById(R.id.finder_item_detail_xml_gallery);
		gallery.setAdapter(adapter);
		
		new GetFinderDetailTask(this).execute();
		new GetFinderImagesTask(this).execute();
	}
	
	private class GetFinderDetailTask extends AsyncTask<Integer, String, Boolean> {
		private List<NameValuePair> finderData = new ArrayList<NameValuePair>();
		private WeakReference<FinderItemDetailActivity> refActivity;
		private ProgressDialog progressDialog = null;
		private JSONArray jsonArray = null;
		
		public GetFinderDetailTask(Activity c) {
			refActivity = new WeakReference<FinderItemDetailActivity>((FinderItemDetailActivity)c);
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
}
