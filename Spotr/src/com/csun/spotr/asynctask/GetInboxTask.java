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

import com.csun.spotr.InboxActivity;
import com.csun.spotr.core.Inbox;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetInboxTask 
	extends AsyncTask<Integer, Inbox, Boolean> 
		implements IAsyncTask<InboxActivity> {

	private static final String TAG = "[AsyncTask].GetInboxTask";
	private static final String GET_INBOX_URL = "http://107.22.209.62/android/get_inbox.php";
	private WeakReference<InboxActivity> ref;

	public GetInboxTask(InboxActivity a) {
		attach(a);
	}

	@Override
	protected void onProgressUpdate(Inbox... i) {
		ref.get().updateAsyncTaskProgress(i[0]);
	}

	@Override
	protected Boolean doInBackground(Integer... ids) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_INBOX_URL, data);

		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(
						new Inbox(
							array.getJSONObject(i).getInt("inbox_tbl_id"), 
							array.getJSONObject(i).getString("users_tbl_username"), 
							array.getJSONObject(i).getString("users_tbl_user_image_url"), 
							array.getJSONObject(i).getString("inbox_tbl_subject_line"), 
							array.getJSONObject(i).getString("inbox_tbl_message"), 
							array.getJSONObject(i).getString("inbox_tbl_time"), 
							array.getJSONObject(i).getInt("inbox_tbl_is_new")));
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Integer... ids) : ", "JSON error parsing data", e);
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

	public void attach(InboxActivity a) {
		ref = new WeakReference<InboxActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}