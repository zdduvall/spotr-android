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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.csun.spotr.core.adapter_item.QuestDetailItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import com.csun.spotr.adapter.QuestDetailItemAdapter;

public class QuestDetailActivity extends Activity {
	private static final String TAG = "(QuestDetailActivity)";
	private static final String GET_QUEST_DETAIL_URL = "http://107.22.209.62/android/get_quest_detail.php";
	private ListView questDetailListView;
	private QuestDetailItemAdapter questDetailItemAdapter;
	private List<QuestDetailItem> questDetailList = new ArrayList<QuestDetailItem>();

	private int questId;
	private int questPoints = 0;
	private int questCompleted = 0;
	private static int numQuest = 0;
	private int spotId = 0;

	static final int DO_SPOT_CHALLENGE = 1;

	private TextView playernameTextView;
	private TextView pointsTextView;
	private TextView challengedoneTextView;
	private ProgressBar progressbar;
	
	private int pStatus = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.quest_detail);

		questId = this.getIntent().getExtras().getInt("quest_id");
		questPoints = this.getIntent().getExtras().getInt("quest_points");
		numQuest = this.getIntent().getExtras().getInt("numberChallenges");

		questDetailListView = (ListView) findViewById(R.id.quest_detail_xml_listview_quest_list);
		questDetailItemAdapter = new QuestDetailItemAdapter(this.getApplicationContext(), questDetailList);
		questDetailListView.setAdapter(questDetailItemAdapter);

		// initialize detail description of specific quest
		playernameTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_playername);
		pointsTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_points);
		challengedoneTextView = (TextView) findViewById(R.id.quest_detail_xml_textview_challengedone);
		progressbar = (ProgressBar) findViewById(R.id.quest_detail_progressBar);

		playernameTextView.setText(CurrentUser.getCurrentUser().getUsername());
	//	progressbar.setProgress(questCompleted/numQuest);
		
		// handle event when click on specific quest
		questDetailListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent("com.csun.spotr.QuestActionActivity");
				Bundle extras = new Bundle();
				extras.putInt("place_id", questDetailList.get(position).getId());
				extras.putInt("position", position);
				intent.putExtras(extras);
				startActivityForResult(intent, DO_SPOT_CHALLENGE);

			}
		});

		new GetQuestDetailTask(this).execute();

		pointsTextView.setText(Integer.toString(questPoints));
		// challengedoneTextView.setText(Integer.toString(questCompleted) + "/"
		// + Integer.toString(numQuest));

	}

	private static class GetQuestDetailTask extends AsyncTask<Integer, QuestDetailItem, Boolean> implements IAsyncTask<QuestDetailActivity> {

		private List<NameValuePair> clientData = new ArrayList<NameValuePair>();
		private WeakReference<QuestDetailActivity> ref;

		public GetQuestDetailTask(QuestDetailActivity a) {
			attach(a);
		}

		private JSONArray userJsonArray = null;

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(QuestDetailItem... spots) {
			ref.get().updateAsyncTaskProgress(spots[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			// send user id
			clientData.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// send quest id
			clientData.add(new BasicNameValuePair("quest_id", Integer.toString(ref.get().questId)));
			clientData.add(new BasicNameValuePair("spot_id", Integer.toString(ref.get().spotId)));
			// retrieve data from server
			userJsonArray = JsonHelper.getJsonArrayFromUrlWithData(GET_QUEST_DETAIL_URL, clientData);
			if (userJsonArray != null) {
				try {
					for (int i = 0; i < userJsonArray.length(); ++i) {
						publishProgress(new QuestDetailItem(userJsonArray.getJSONObject(i).getInt("spots_tbl_id"), userJsonArray.getJSONObject(i).getString("spots_tbl_name"), userJsonArray.getJSONObject(i).getString("spots_tbl_description"), userJsonArray.getJSONObject(i).getString("quest_user_tbl_status")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetQuestDetailTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			ref.get().challengedoneTextView.setText(Integer.toString(ref.get().questCompleted) + "/" + Integer.toString(numQuest));
			ref.get().progressbar.setProgress(100*ref.get().questCompleted/numQuest);
			//ref.get().questCompleted = 0;
			//ref.get().questId = 0;
			detach();
		}

		public void attach(QuestDetailActivity a) {
			ref = new WeakReference<QuestDetailActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused");
		super.onPause();
	}

	public void updateAsyncTaskProgress(QuestDetailItem q) {
		questDetailList.add(q);
		if (q.getStatus().equalsIgnoreCase("done")) {
			this.questCompleted++;
		}
		questDetailItemAdapter.notifyDataSetChanged();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == DO_SPOT_CHALLENGE) {
			if (resultCode == RESULT_OK) {
				int position = data.getExtras().getInt("position");
				spotId = questDetailList.get(position).getId();
				this.questCompleted = 0;
				questDetailList.clear();
				questDetailItemAdapter.notifyDataSetChanged();
				new GetQuestDetailTask(this).execute();
			}
		}
	}
}