package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csun.spotr.PlaceLootActivity;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class DoTakeLootTask 
	extends AsyncTask<Void, Void, String> 
		implements IAsyncTask<PlaceLootActivity> {

	private static final String TAG = "[AsyncTask].DoTakeLootTask";
	private static final String DO_TAKE_LOOT_URL = "http://107.22.209.62/android/do_take_loot.php";
	private WeakReference<PlaceLootActivity> ref;
	private int spotLoot;
	

	public DoTakeLootTask(PlaceLootActivity a, int b) {
		attach(a);
		spotLoot = b;
	}

	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		data.add(new BasicNameValuePair("spot_loot_id", Integer.toString(spotLoot)));
		return data;
	}

	@Override
	protected String doInBackground(Void... voids) {
		List<NameValuePair> data = prepareUploadData();
		JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(DO_TAKE_LOOT_URL, data);
		String result = "";
		try {
			result = json.getString("result");
		}
		catch (JSONException e) {
			Log.e(TAG + ".doInBackGround(Location... locations) : ", "JSON error parsing data", e );
		}
		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		if (result.equals("fail")) {
			Toast.makeText(ref.get().getApplicationContext(), "uh oh", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(ref.get().getApplicationContext(), "hooray it's yours!", Toast.LENGTH_SHORT).show();
		}
	}

	public void attach(PlaceLootActivity a) {
		ref = new WeakReference<PlaceLootActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}