package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.csun.spotr.LocalMapViewActivity;
import com.csun.spotr.core.FriendAndLocation;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetFriendLocationsTask 
	extends AsyncTask<Void, FriendAndLocation, Boolean> 
		implements IAsyncTask<LocalMapViewActivity> {

	private static final String TAG = "[AsyncTask].GetFriendLocationsTask";
	private static final String GET_FRIEND_LOCATION_URL = "http://107.22.209.62/android/get_friend_locations.php";
	private WeakReference<LocalMapViewActivity> ref;

	public GetFriendLocationsTask(LocalMapViewActivity a) {
		attach(a);
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(FriendAndLocation... f) {
		ref.get().updateFriendTaskProgress(f[0]);
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_FRIEND_LOCATION_URL, data);
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					if (array.getJSONObject(i).has("users_locations_tbl_latitude") && array.getJSONObject(i).has("users_locations_tbl_longitude")) {
						publishProgress(
							new FriendAndLocation(
								array.getJSONObject(i).getInt("users_tbl_id"), 
								array.getJSONObject(i).getString("users_tbl_username"), 
								array.getJSONObject(i).getDouble("users_locations_tbl_latitude"), 
								array.getJSONObject(i).getDouble("users_locations_tbl_longitude"), 
								array.getJSONObject(i).getString("users_locations_tbl_created"), 
								array.getJSONObject(i).getString("users_locations_tbl_msg"), 
								array.getJSONObject(i).getString("users_tbl_user_image_url")));
					}
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (result == false) {
			Toast.makeText(ref.get().getApplicationContext(), "No friends found!", Toast.LENGTH_SHORT).show();
		}
		detach();
	}

	public void attach(LocalMapViewActivity a) {
		ref = new WeakReference<LocalMapViewActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}