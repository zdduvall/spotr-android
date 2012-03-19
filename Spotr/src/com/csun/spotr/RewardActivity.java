package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.BadgeAdapter;
import com.csun.spotr.core.Badge;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class RewardActivity 
	extends Activity 
		implements IActivityProgressUpdate<Badge> {
	
	private static final String TAG = "(RewardActivity)";
	private static final String GET_BADGES_URL = "http://107.22.209.62/android/get_badges.php";
	private static final String GET_MISSING_BADGES_URL = "http://107.22.209.62/android/get_missing_badges.php";
	private static final int INTENT_RESULT_REMOVE_BADGE = 0;
	
	private GridView gridviewbadges;
	private GridView gridviewmissingbadges;
	private BadgeAdapter adapter;
	private BadgeAdapter adapter2;
	private List<Badge> badgeList;
	private List<Badge> missingBadgeList;
	private GetBadgesTask task;
	private GetMissingBadgesTask task2;
	private int removePosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.badge);
		setupBadgeGridView();
		task = new GetBadgesTask(this);
		task.execute();
		task2 = new GetMissingBadgesTask(this);
		task2.execute();
	}
	
	private void setupBadgeGridView() {
		badgeList = new ArrayList<Badge>();
		missingBadgeList = new ArrayList<Badge>();
		gridviewbadges = (GridView) findViewById(R.id.badge_xml_gridview_userbadges);
		gridviewmissingbadges = (GridView)findViewById(R.id.badge_xml_gridview_missingbadges);
		adapter = new BadgeAdapter(this, badgeList);
		adapter2 = new BadgeAdapter(this, missingBadgeList);
		gridviewbadges.setAdapter(adapter);
		gridviewbadges.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), RewardViewActivity.class);
				intent.putExtra("id", badgeList.get(position).getId());
				intent.putExtra("name", badgeList.get(position).getName());
				intent.putExtra("description", badgeList.get(position).getDescription());
				intent.putExtra("date", badgeList.get(position).getDate());
				intent.putExtra("url", badgeList.get(position).getUrl());
				intent.putExtra("points", badgeList.get(position).getPoints());
				startActivityForResult(intent, INTENT_RESULT_REMOVE_BADGE);
				removePosition = position;
			}
		});
		gridviewmissingbadges.setAdapter(adapter2);
		gridviewmissingbadges.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), RewardViewActivity.class);
				intent.putExtra("id", missingBadgeList.get(position).getId());
				intent.putExtra("name", missingBadgeList.get(position).getName());
				intent.putExtra("description", missingBadgeList.get(position).getDescription());
				intent.putExtra("date", missingBadgeList.get(position).getDate());
				intent.putExtra("url", missingBadgeList.get(position).getUrl());
				intent.putExtra("points", -1);
				startActivityForResult(intent, INTENT_RESULT_REMOVE_BADGE);
				removePosition = position;
			}
		});
	}

	private static class GetBadgesTask 
		extends AsyncTask<Integer, Badge, Boolean> 
			implements IAsyncTask<RewardActivity> {

		private static final String TAG = "[AsyncTask].GetBadgesTask";
		private WeakReference<RewardActivity> ref;
		private ProgressDialog progressDialog = null;

		public GetBadgesTask(RewardActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Loading items...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected void onProgressUpdate(Badge... b) {
			ref.get().updateAsyncTaskProgress(b[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_BADGES_URL, data);
			
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new Badge(
								array.getJSONObject(i).getInt("badges_tbl_id"), 
								array.getJSONObject(i).getString("badges_tbl_name"),
								array.getJSONObject(i).getString("badges_tbl_description"),
								array.getJSONObject(i).getString("badges_tbl_img"),
								array.getJSONObject(i).getString("badges_tbl_created"),
								array.getJSONObject(i).getInt("badges_tbl_points")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetBadgesTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result == false) {
				Toast.makeText(ref.get().getApplicationContext(), "No items", Toast.LENGTH_LONG);
			}
			detach();
		}

		public void attach(RewardActivity a) {
			ref = new WeakReference<RewardActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	private static class GetMissingBadgesTask 
	extends AsyncTask<Integer, Badge, Boolean> 
		implements IAsyncTask<RewardActivity> {

	private static final String TAG = "[AsyncTask].GetBadgesTask";
	private WeakReference<RewardActivity> ref;
	private ProgressDialog progressDialog = null;

	public GetMissingBadgesTask(RewardActivity a) {
		attach(a);
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ref.get());
		progressDialog.setMessage("Loading items...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	@Override
	protected void onProgressUpdate(Badge... b) {
		ref.get().updateAsyncTask2Progress(b[0]);
	}

	@Override
	protected Boolean doInBackground(Integer... offsets) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_MISSING_BADGES_URL, data);
		
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(
						new Badge(
							array.getJSONObject(i).getInt("badges_tbl_id"), 
							array.getJSONObject(i).getString("badges_tbl_name"),
							array.getJSONObject(i).getString("badges_tbl_description"),
							array.getJSONObject(i).getString("badges_tbl_img"),
							array.getJSONObject(i).getString("badges_tbl_created"),
							array.getJSONObject(i).getInt("badges_tbl_points")));
				}
			}
			catch (JSONException e) {
				Log.e(TAG + "GetMissingBadgesTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
			}
			return true;
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		if (result == false) {
			Toast.makeText(ref.get().getApplicationContext(), "No items", Toast.LENGTH_LONG);
		}
		detach();
	}

	public void attach(RewardActivity a) {
		ref = new WeakReference<RewardActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
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

	public void updateAsyncTaskProgress(Badge b) {
		badgeList.add(b);
		adapter.notifyDataSetChanged();
	}
	
	public void updateAsyncTask2Progress(Badge b) {
		missingBadgeList.add(b);
		adapter2.notifyDataSetChanged();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_RESULT_REMOVE_BADGE) {
			if (resultCode == RESULT_OK) {
				badgeList.remove(removePosition);
				adapter.notifyDataSetChanged();
			}
		}
	}
}
