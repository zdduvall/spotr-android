package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.csun.spotr.core.adapter_item.QuestItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import com.csun.spotr.adapter.QuestItemAdapter;

/**
 * Description:
 * 		Multiple challenges of multiple places 
 */
public class QuestActivity 
	extends BasicSpotrActivity 
		implements IActivityProgressUpdate<QuestItem> {
	
	private static final String TAG = "(QuestActivity)";
	private static final String GET_QUEST_URL = "http://107.22.209.62/android/get_quest.php";
	
	private ListView listview;
	private QuestItemAdapter adapter;
	private List<QuestItem> questList = new ArrayList<QuestItem>();
	private GetQuestTask task = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quest);
		setupTitleBar();
		setupListView();
		task = new GetQuestTask(this);
		task.execute();
	}
	
	private void setupListView() {
		listview = (ListView) findViewById(R.id.quest_xml_listview_quest_list);
		adapter = new QuestItemAdapter(getApplicationContext(), questList);
		listview.setAdapter(adapter);
		
		// handle event when click on specific quest
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent("com.csun.spotr.QuestDetailActivity");
				Bundle extras = new Bundle();
				extras.putInt("quest_id",questList.get(position).getId());
				extras.putInt("quest_points",questList.get(position).getPoints());
				extras.putInt("numberChallenges", questList.get(position).getSpotnum());
				extras.putString("quest_name", questList.get(position).getName());
				extras.putString("quest_description", questList.get(position).getDescription());
				extras.putString("quest_url", questList.get(position).getUrl());
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		
		/**
		 * TODO: need to handle OnsSrollListener event for ListView
		 **/
		Log.v(TAG, "need to handle OnScrollListener event for ListView");
	}
	
	@Override
	protected void setupTitleBar() {
		super.setupTitleBar();
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText("undial Quests");
	}
	
	private static class GetQuestTask 
		extends AsyncTask<Integer, QuestItem, Boolean> 
			implements IAsyncTask<QuestActivity> {
		
		private static final String TAG = "[AsyncTask].GetQuestTask";
		private WeakReference<QuestActivity> ref;
		
		public GetQuestTask(QuestActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
			
		}

		@Override
		protected void onProgressUpdate(QuestItem... q) {
			ref.get().updateAsyncTaskProgress(q[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			
			// cancel before task runs
			if (isCancelled()) {
				return false;
			}
			
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			JSONArray userJsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_QUEST_URL, data);
			
			// cancel after getting data from sever
			if (isCancelled()) {
				return false;
			}
			
			if (userJsonArray != null) {
				try {
					for (int i = 0; i < userJsonArray.length(); ++i) {
						
						// cancel while populating data
						if (isCancelled()) {
							return false;
						}
			
						publishProgress(
							new QuestItem(
								userJsonArray.getJSONObject(i).getInt("quest_tbl_id"), 
								userJsonArray.getJSONObject(i).getString("quest_tbl_name"),
								userJsonArray.getJSONObject(i).getInt("quest_tbl_points"),
								userJsonArray.getJSONObject(i).getInt("quest_tbl_spotnum"),
								userJsonArray.getJSONObject(i).getString("quest_tbl_description"),
								userJsonArray.getJSONObject(i).getString("quest_tbl_url")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + ".doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(QuestActivity a) {
			ref = new WeakReference<QuestActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	public void updateAsyncTaskProgress(QuestItem q) {
		questList.add(q);
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
	public void onResume() {
		Log.v(TAG, "I'm resumed");
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		super.onDestroy();
	}

	@Override
	public void onRestart() {
		Log.v(TAG, "I'm restarted!");
		super.onRestart();
	}

	@Override
	public void onStop() {
		Log.v(TAG, "I'm stopped!");
		super.onStop();
	}

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}
}