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

import com.csun.spotr.ExpandableFriendListActivity;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetFriendTaskExpandable 
	extends AsyncTask<Void, UserItem, Boolean> 
		implements IAsyncTask<ExpandableFriendListActivity> {

	private static final String TAG = "[AsyncTask].GetFriendTask";
	private static final String GET_FRIENDS_URL = "http://107.22.209.62/android/get_friends.php";
	private WeakReference<ExpandableFriendListActivity> ref;
	private int offset;

	public GetFriendTaskExpandable(ExpandableFriendListActivity a, int offset) {
		attach(a);
		this.offset = offset;
	}

	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
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
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_FRIENDS_URL, data);
		if (array != null) {
			try {
				if (ref.get().task.isCancelled()) {
					return true;
				}
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(
						new UserItem(
							array.getJSONObject(i).getInt("users_tbl_id"), 
							array.getJSONObject(i).getString("users_tbl_username"), array.getJSONObject(i).getString("users_tbl_user_image_url")));
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Integer... offsets) : ", "JSON error parsing data", e);
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		ref.get().setupDynamicSearch();
		detach();
	}

	public void attach(ExpandableFriendListActivity a) {
		ref = new WeakReference<ExpandableFriendListActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}
