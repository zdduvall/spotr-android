package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.csun.spotr.adapter.ChooseUserItemAdapter;
import com.csun.spotr.adapter.UserItemAdapter;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

/**
 * Description: This class will retrieve a list of friends from database.
 */
public class ChooseUserActivity 
	extends Activity 
		implements IActivityProgressUpdate<UserItem> {

	private static final String TAG = "(ChooseUserActivity)";
	private static final String GET_FRIENDS_URL = "http://107.22.209.62/android/get_choose_users.php";
	private ListView listview = null;

	public ChooseUserItemAdapter adapter = null;
	public List<UserItem> userItemList = new ArrayList<UserItem>();
	public GetFriendsTask task = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.choose_user);

		// initialize list view
		listview = (ListView) findViewById(R.id.choose_user_xml_listview);

		// set up list view adapter
		adapter = new ChooseUserItemAdapter(this.getApplicationContext(), userItemList);
		listview.setAdapter(adapter);
		listview.setVisibility(View.VISIBLE);

		// handle item click event
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent data = new Intent();
				data.putExtra("user_id", userItemList.get(position).getId());
				data.putExtra("username", userItemList.get(position).getUsername());
				setResult(RESULT_OK, data);
				finish();
			}
		});

		// initially, we load 10 items and show user immediately
		task = new GetFriendsTask(this, true);
		task.execute();
	}

	private static class GetFriendsTask 
		extends AsyncTask<Integer, UserItem, Boolean> 
			implements IAsyncTask<ChooseUserActivity> {

		private List<NameValuePair> data = new ArrayList<NameValuePair>();
		private WeakReference<ChooseUserActivity> ref;
		private JSONArray userJsonArray = null;

		public GetFriendsTask(ChooseUserActivity a, boolean flag) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(UserItem... u) {
			ref.get().updateAsyncTaskProgress(u[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			// send user id
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// retrieve data from server
			userJsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_FRIENDS_URL, data);
			if (userJsonArray != null) {
				try {
					if(ref.get().task.isCancelled()){
						return true;
					}
					for (int i = 0; i < userJsonArray.length(); ++i) {
						publishProgress(
							new UserItem(
								userJsonArray.getJSONObject(i).getInt("users_tbl_id"), 
								userJsonArray.getJSONObject(i).getString("users_tbl_username"), 
								userJsonArray.getJSONObject(i).getString("users_tbl_user_image_url")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFriendTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(ChooseUserActivity a) {
			ref = new WeakReference<ChooseUserActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	public void updateAsyncTaskProgress(UserItem u) {
		userItemList.add(u);
		adapter.notifyDataSetChanged();
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
}
