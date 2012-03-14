package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csun.spotr.FinderItemDetailActivity;
import com.csun.spotr.util.JsonHelper;

public class GetFinderDetailTask extends AsyncTask<Integer, String, Boolean> {
	private static final String TAG = "(GetFinderDetailTask)";
	private static final String GET_FINDER_DETAIL_URL = "http://107.22.209.62/android/get_finder_detail.php";
	
	private WeakReference<FinderItemDetailActivity> refActivity;
	private ProgressDialog progressDialog = null;
	private int finderId;
	
	public GetFinderDetailTask(FinderItemDetailActivity c, int id) {
		refActivity = new WeakReference<FinderItemDetailActivity>(c);
		finderId = id;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(refActivity.get());
		progressDialog.setMessage("Please wait...");
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
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
		
		JSONArray jsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_FINDER_DETAIL_URL, data);
		
		if (jsonArray != null) {
			try {
				publishProgress(jsonArray.getJSONObject(0).getString("finder_tbl_name"), 
						jsonArray.getJSONObject(0).getString("finder_tbl_description"), 
						jsonArray.getJSONObject(0).getString("finder_tbl_points"),
						jsonArray.getJSONObject(0).getString("users_tbl_username"));
				
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
