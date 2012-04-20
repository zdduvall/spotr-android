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

import com.csun.spotr.PlaceLootActivity;
import com.csun.spotr.core.adapter_item.LootItem;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetPlaceLootTask 
	extends AsyncTask<Integer, LootItem, Boolean> 
		implements IAsyncTask<PlaceLootActivity> {
	
	private static final String TAG = "(GetPlaceLootTask)";
	private static final String GET_PLACE_LOOT_URL = "http://107.22.209.62/android/get_place_loot.php";

	private int spotId;
	
	private WeakReference<PlaceLootActivity> ref;
	
	public GetPlaceLootTask(PlaceLootActivity a, int id) {
		attach(a);
		spotId = id;
	}
	
	@Override
	protected void onPreExecute() {
	}
	
	@Override
	protected void onProgressUpdate(LootItem... s) {
		ref.get().updateAsyncTaskProgress(s[0]);
	}
	
	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("spot_id", Integer.toString(spotId)));
		return data;
	}
	
	@Override
	protected Boolean doInBackground(Integer... offsets) {

		List<NameValuePair> data = prepareUploadData();
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_PLACE_LOOT_URL,data);
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(
						new LootItem(
							array.getJSONObject(i).getInt("spots_loot_tbl_id"),
							array.getJSONObject(i).getInt("spots_loot_tbl_weapon_id"), 
							array.getJSONObject(i).getString("weapon_tbl_name"), 
							array.getJSONObject(i).getString("weapon_tbl_url")));
				}
			}
			catch (JSONException e) {
				Log.e(TAG + "GetPlaceLoot.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
			}
			return true;
		}
		return false;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		if (result == false) {
			Toast.makeText(ref.get().getApplicationContext(), "No items", Toast.LENGTH_LONG);
		}
		detach();
	}
	
	public void attach(PlaceLootActivity a) {
		ref = new WeakReference<PlaceLootActivity>(a);
	}
	
	public void detach() {
		ref.clear();
	}
}
