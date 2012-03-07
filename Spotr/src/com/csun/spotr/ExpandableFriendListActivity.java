package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.ExpandableUserItemAdapter;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;

public class ExpandableFriendListActivity extends Activity implements
		IActivityProgressUpdate<UserItem> {
	private static final String TAG = "(FriendListActivity)";
	private static final String GET_FRIENDS_URL = "http://107.22.209.62/android/get_friends.php";

	public ExpandableUserItemAdapter adapter = null;
	public List<UserItem> userItemList = new ArrayList<UserItem>();
	public GetFriendsTask task = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ExpandableListView listView = new ExpandableListView(this);
		listView.setGroupIndicator(null);
		listView.setChildIndicator(null);

		adapter = new ExpandableUserItemAdapter(this, userItemList);
		listView.setAdapter(adapter);
		listView.setVisibility(View.VISIBLE);
		listView.setOnScrollListener(new FeedOnScrollListener());
		setContentView(listView);
	}

	public void updateAsyncTaskProgress(UserItem u) {
		userItemList.add(u);
		adapter.notifyDataSetChanged();
	}

	private static class GetFriendsTask extends
			AsyncTask<Void, UserItem, Boolean> implements
			IAsyncTask<ExpandableFriendListActivity> {

		private WeakReference<ExpandableFriendListActivity> ref;
		private int offset;

		public GetFriendsTask(ExpandableFriendListActivity a, int offset) {
			attach(a);
			this.offset = offset;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(UserItem... u) {
			ref.get().updateAsyncTaskProgress(u[0]);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();

			// send user id
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser
					.getCurrentUser().getId())));

			// send offset
			data.add(new BasicNameValuePair("offset", Integer.toString(offset)));

			// retrieve data from server
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(
					GET_FRIENDS_URL, data);

			if (array != null) {
				try {
					if (ref.get().task.isCancelled()) {
						return true;
					}
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(new UserItem(array.getJSONObject(i)
								.getInt("users_tbl_id"), array.getJSONObject(i)
								.getString("users_tbl_username"), array
								.getJSONObject(i).getString(
										"users_tbl_user_image_url")));
					}
				} catch (JSONException e) {
					Log.e(TAG
							+ "GetFriendTask.doInBackGround(Integer... offsets) : ",
							"JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(ExpandableFriendListActivity a) {
			ref = new WeakReference<ExpandableFriendListActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		super.onDestroy();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Error Message")
					.setMessage("No friends!")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).create();

		case 1:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.error_circle)
					.setTitle("Error Message")
					.setMessage("<undefined>")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).create();
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			task.cancel(true);
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public class FeedOnScrollListener implements OnScrollListener {
		private int visibleThreshold = 10;
		private int currentPage = 0;
		private int previousTotal = 0;
		private boolean loading = true;

		public FeedOnScrollListener() {

		}

		public FeedOnScrollListener(int visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if (loading) {
				if (totalItemCount > previousTotal) {
					loading = false;
					previousTotal = totalItemCount;
					currentPage += 10;
				}
			}
			if (!loading
					&& (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				new GetFriendsTask(ExpandableFriendListActivity.this,
						currentPage).execute();
				loading = true;
			}
		}

		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO : not use
		}
	}

}
