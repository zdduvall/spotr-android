package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csun.spotr.FinderActivity;
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetFindersTask 
	extends AsyncTask<Integer, SeekingItem, Boolean> 
		implements IAsyncTask<FinderActivity> {
	
	private static final String TAG = "(GetFindersTask)";
	private static final String GET_FINDERS_URL = "http://107.22.209.62/android/get_finders.php";

	private WeakReference<FinderActivity> ref;
	private ProgressDialog progressDialog = null;
	
	public GetFindersTask(FinderActivity a) {
		attach(a);
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ref.get());
		progressDialog.setMessage("Loading items...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}
	
	@Override
	protected void onProgressUpdate(SeekingItem... s) {
		ref.get().updateAsyncTaskProgress(s[0]);
	}
	
	@Override
	protected Boolean doInBackground(Integer... offsets) {
		JSONArray array = JsonHelper.getJsonArrayFromUrl(GET_FINDERS_URL);
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(
						new SeekingItem(
							array.getJSONObject(i).getInt("finder_tbl_id"), 
							array.getJSONObject(i).getString("finder_tbl_name"), 
							array.getJSONObject(i).getString("finder_tbl_image_url")));
				}
			}
			catch (JSONException e) {
				Log.e(TAG + "GetFindersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
			}
			return true;
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		if (result == false) {
			Toast.makeText(ref.get().getApplicationContext(), "No items", Toast.LENGTH_LONG);
		}
		detach();
	}
	
	public void attach(FinderActivity a) {
		ref = new WeakReference<FinderActivity>(a);
	}
	
	public void detach() {
		ref.clear();
	}
}
