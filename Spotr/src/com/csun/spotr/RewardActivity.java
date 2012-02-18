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
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.custom_gui.DraggableGridView;
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
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class RewardActivity 
	extends Activity 
		implements IActivityProgressUpdate<Badge> {
	
	private static final String TAG = "(RewardActivity)";
	private static final String GET_BADGES_URL = "http://107.22.209.62/android/get_badges.php";
	
	private GridView gridview;
	private BadgeAdapter adapter;
	private List<Badge> badgeList;
	private GetBadgesTask task;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.badge);

		badgeList = new ArrayList<Badge>();
		gridview = (GridView) findViewById(R.id.badge_xml_gridview);
		adapter = new BadgeAdapter(this, badgeList);
		gridview.setAdapter(adapter);

		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Intent intent = new Intent(getApplicationContext(), RewardViewActivity.class);
				startActivity(intent);
			}
		});
		
		task = new GetBadgesTask(this);
		task.execute();
	}

	private static class GetBadgesTask 
		extends AsyncTask<Integer, Badge, Boolean> 
			implements IAsyncTask<RewardActivity> {

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
								array.getJSONObject(i).getString("badges_tbl_created")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFindersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
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
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.options_menu_xml_item_setting_icon:
			intent = new Intent("com.csun.spotr.SettingsActivity");
			startActivity(intent);
			finish();
			break;
		case R.id.options_menu_xml_item_logout_icon:
			SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
			editor.clear();
			editor.commit();
			intent = new Intent("com.csun.spotr.LoginActivity");
			startActivity(intent);
			finish();
			break;
		case R.id.options_menu_xml_item_mainmenu_icon:
			intent = new Intent("com.csun.spotr.MainMenuActivity");
			startActivity(intent);
			finish();
			break;
		}
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
}
