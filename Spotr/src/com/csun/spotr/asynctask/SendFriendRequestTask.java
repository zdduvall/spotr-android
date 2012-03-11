package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.FriendListActionActivity;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class SendFriendRequestTask 
	extends AsyncTask<String, Integer, Boolean> 
		implements IAsyncTask<FriendListActionActivity> {

	private static final String TAG = "[AsyncTask].SendFriendRequestTask";
	private static final String SEND_REQUEST_URL = "http://107.22.209.62/android/send_friend_request.php";
	private WeakReference<FriendListActionActivity> ref;

	public SendFriendRequestTask(FriendListActionActivity a) {
		attach(a);
	}

	private List<NameValuePair> prepareUploadData(String userId, String friendId, String friendMessage) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("users_id", userId));
		data.add(new BasicNameValuePair("friend_id", friendId));
		data.add(new BasicNameValuePair("friend_message", friendMessage));
		return data;
	}

	@Override
	protected Boolean doInBackground(String... ids) {
		List<NameValuePair> data = prepareUploadData(ids[0], ids[1], ids[2]);
		JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(SEND_REQUEST_URL, data);
		try {
			if (json.getString("result").equals("success")) {
				return true;
			}
		}
		catch (JSONException e) {
			Log.e(TAG + ".doInBackGround(String... datas) : ", "JSON error parsing data", e );
		}
		return false;
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