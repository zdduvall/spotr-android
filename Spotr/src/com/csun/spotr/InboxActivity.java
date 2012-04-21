package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import com.csun.spotr.adapter.InboxItemAdapter;
import com.csun.spotr.asynctask.GetInboxTask;
import com.csun.spotr.core.Inbox;
import com.csun.spotr.skeleton.IActivityProgressUpdate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;


/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Load messages from friends
 **/
public class InboxActivity 
	extends BasicSpotrActivity 
		implements IActivityProgressUpdate<Inbox> {

	private static final String TAG = "(InboxActivity)";
	private static final int INTENT_RESULT_VIEW_MESSAGE = 1;

	private ListView listview = null;
	private InboxItemAdapter adapter = null;
	private List<Inbox> inboxList = new ArrayList<Inbox>();
	private GetInboxTask task = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inbox);
		setupTitleBar();
		setupListView();
		setupComposeButton();
		task = new GetInboxTask(this);
		task.execute();
	}
	
	private void setupComposeButton() {
		final ImageButton buttonCompose = (ImageButton) findViewById(R.id.title_bar_inbox_btn_compose);
		buttonCompose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), ComposeMessageActivity.class);
				startActivity(intent);
			}
		});
	}
	
	private void setupListView() {
		listview = (ListView) findViewById(R.id.inbox_xml_listview);
		adapter = new InboxItemAdapter(this, inboxList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle extras = new Bundle();
				extras.putString("sender_name", inboxList.get(position).getUsername());
				extras.putString("sender_time", inboxList.get(position).getTime());
				extras.putString("sender_message", inboxList.get(position).getMessage());
				extras.putString("sender_picture_url", inboxList.get(position).getUserPictureUrl());
				Intent intent = new Intent("com.csun.spotr.InboxViewMessageActivity");
				intent.putExtras(extras);
				startActivityForResult(intent, INTENT_RESULT_VIEW_MESSAGE);
			}
		});	
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == INTENT_RESULT_VIEW_MESSAGE) {
			if (resultCode == RESULT_OK) {
				
			}
		}
	}
	
	@Override
	protected void setupTitleBar() {	
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_inbox);
		TextView title = (TextView) findViewById(R.id.title_bar_title);
		title.setText(" Inbox");
	}

	public void updateAsyncTaskProgress(Inbox i) {
		inboxList.add(i);
		adapter.notifyDataSetChanged();
	}
	
	public void resetListViewData() {
		inboxList.clear();
		adapter.notifyDataSetChanged();
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