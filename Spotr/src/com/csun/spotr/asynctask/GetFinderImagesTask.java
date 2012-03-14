package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csun.spotr.FinderItemDetailActivity;
import com.csun.spotr.adapter.FinderAdditionalItemImageAdapter;
import com.csun.spotr.util.JsonHelper;

public class GetFinderImagesTask extends AsyncTask<Integer, String, Boolean> {
	private static final String TAG = "(GetFinderImagesTask)";
	private static final String GET_FINDER_ADDITIONAL_IMAGES_URL = "http://107.22.209.62/android/get_finder_additional_images.php";
	
	private WeakReference<FinderItemDetailActivity> refActivity;
	private ProgressDialog progressDialog = null;
	private List<String> items = null;
	private int finderId;
	private FinderAdditionalItemImageAdapter adapter = null;
	
	public GetFinderImagesTask(Activity c, List<String> i, FinderAdditionalItemImageAdapter a, int finderId) {
		refActivity = new WeakReference<FinderItemDetailActivity>((FinderItemDetailActivity)c);
		items = i;
		adapter = a;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(refActivity.get());
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
