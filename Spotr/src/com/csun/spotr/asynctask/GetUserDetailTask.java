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

import com.csun.spotr.ProfileActivity;
import com.csun.spotr.core.User;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetUserDetailTask 
	extends AsyncTask<Void, FriendFeedItem, User> 
		implements IAsyncTask<ProfileActivity> {
	
	private static final String TAG = "[AsyncTask].GetUserDetailTask";
	private static final String GET_USER_DETAIL_URL = "http://107.22.209.62/android/get_user_detail.php";
	private WeakReference<ProfileActivity> ref;
	private int userId;
	
	public GetUserDetailTask(ProfileActivity a, int userId) {
		this.userId = userId;
		attach(a);
	}
	
	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
		return data;
	}
	
	@Override
	protected User doInBackground(Void...voids) {
		if (isCancelled()) {
			return null;
		}
		
		List<NameValuePair> data = prepareUploadData();
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_DETAIL_URL, data);
		
		if (isCancelled()) {
			return null;
		}
		
		User user = null;
		
		if (array != null) {
			try {
				user = new User.Builder(
						// required parameters
						array.getJSONObject(0).getInt("users_tbl_id"),
						array.getJSONObject(0).getString(
								"users_tbl_username"), array.getJSONObject(
								0).getString("users_tbl_password"))
						// optional parameters
						.challengesDone(
								array.getJSONObject(0).getInt(
										"users_tbl_challenges_done"))
						.placesVisited(
								array.getJSONObject(0).getInt(
										"users_tbl_places_visited"))
						.points(array.getJSONObject(0).getInt(
								"users_tbl_points"))
						.imageUrl(
								array.getJSONObject(0).getString(
										"users_tbl_user_image_url"))
						.numFriends(
								array.getJSONObject(0)
										.getInt("num_friends"))
						.numBadges(
								array.getJSONObject(0).getInt("num_badges"))
						.realname(
								array.getJSONObject(0).getString(
										"users_tbl_real_name"))
						.education(
								array.getJSONObject(0).getString(
										"users_tbl_education"))
						.hometown(
								array.getJSONObject(0).getString(
										"users_tbl_hometown"))
						.hobbies(
								array.getJSONObject(0).getString(
										"users_tbl_hobbies")).build();
	
			} 
			catch (JSONException e) {
				Log.e(TAG + ".doInBackground() : ", "JSON error parsing data", e);
			}
		}
		return user;
	}
	
	@Override
	protected void onPostExecute(final User u) {
		if (u != null && !isActivityDone()) {
			ref.get().updateUserView(u);
		}
		detach();
	}
	
	public void attach(ProfileActivity a) {
		ref = new WeakReference<ProfileActivity>(a);
	}
	
	public void detach() {
		ref.clear();
	}
	
	private boolean isActivityDone() {
		return (ref != null && ref.get() != null && !ref.get().isFinishing());
	}
}
