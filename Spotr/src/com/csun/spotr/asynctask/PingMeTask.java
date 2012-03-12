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

import com.csun.spotr.LocalMapViewActivity;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class PingMeTask 
	extends AsyncTask<Void, Void, String> 
		implements IAsyncTask<LocalMapViewActivity> {

	private static final String TAG = "[AsyncTask].PingMeTask";
	private static final String PING_ME_URL = "http://107.22.209.62/android/ping_me.php";
	private WeakReference<LocalMapViewActivity> ref;
	private String pingMessage = null;
	private int pingDuration;
	private Location location;

	public PingMeTask(LocalMapViewActivity a, String msg, Location loc, int duration) {
		attach(a);
		pingMessage = msg;
		pingDuration = duration;
		location = loc;
	}

	private List<NameValuePair> prepareUploadData(Location location) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("latitude", Double.toString(location.getLatitude())));
		data.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
		data.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		data.add(new BasicNameValuePair("message", pingMessage));
		data.add(new BasicNameValuePair("duration", Integer.toString(pingDuration)));
		return data;
	}

	@Override
	protected String doInBackground(Void... voids) {
		List<NameValuePair> data = prepareUploadData(location);
		JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(PING_ME_URL, data);
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
			Toast.makeText(ref.get().getApplicationContext(), "Can't update your location due to network connection error.", Toast.LENGTH_SHORT).show();
		}
		else {
			Toast.makeText(ref.get().getApplicationContext(), "Ping succeeded!", Toast.LENGTH_SHORT).show();
		}
	}

	public void attach(LocalMapViewActivity a) {
		ref = new WeakReference<LocalMapViewActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}