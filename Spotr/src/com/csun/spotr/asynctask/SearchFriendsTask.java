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

import com.csun.spotr.FriendListActionActivity;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class SearchFriendsTask 
	extends AsyncTask<Void, UserItem, Boolean> 
		implements IAsyncTask<FriendListActionActivity> {
	
	private static final String TAG = "[AsyncTask].SearchFriendTask";
	private static final String SEARCH_FRIENDS_URL = "http://107.22.209.62/android/search_friends.php";
	private WeakReference<FriendListActionActivity> ref;
	private final String criteria;
	private final int offset;

	public SearchFriendsTask(FriendListActionActivity a, String criteria, int offset) {
		this.criteria = criteria;
		this.offset = offset;
		attach(a);
	}

	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("text", criteria));
		data.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		data.add(new BasicNameValuePair("offset", Integer.toString(offset)));
		return data;
	}

	@Override
	protected void onProgressUpdate(UserItem... u) {
		ref.get().updateAsyncTaskProgress(u[0]);
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		List<NameValuePair> data = prepareUploadData();
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(SEARCH_FRIENDS_URL, data);
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(
						new UserItem(
							array.getJSONObject(i).getInt("users_tbl_id"), 
							array.getJSONObject(i).getString("users_tbl_username"), 
							array.getJSONObject(i).getString("users_tbl_user_image_url")));
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		detach();
	}

	public void attach(FriendListActionActivity a) {
		ref = new WeakReference<FriendListActionActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}
