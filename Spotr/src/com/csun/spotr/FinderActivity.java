package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

import com.csun.spotr.adapter.FinderItemAdapter;
import com.csun.spotr.asynctask.GetFindersTask;
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.skeleton.IActivityProgressUpdate;

/*
 * Description:
 * 		Display lost items
 */
public class FinderActivity 
	extends Activity 
		implements IActivityProgressUpdate<SeekingItem> {
	
	private static final String TAG = "(FinderActivity)";
	private List<SeekingItem> items;
	private GridView gridview;
	private FinderItemAdapter adapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finder);
		
		Button buttonCreateItem = (Button) findViewById(R.id.finder_xml_button);
		items = new ArrayList<SeekingItem>();
		gridview = (GridView) findViewById(R.id.finder_xml_gridview);
		adapter = new FinderItemAdapter(this, items);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle extras = new Bundle();
				extras.putInt("finder_id", items.get(position).getId());
				Intent intent = new Intent(getApplicationContext(), FinderItemDetailActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		
		buttonCreateItem.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int dummy = 0;
				Intent intent = new Intent(getApplicationContext(), CreateFinderActivity.class);
				startActivityForResult(intent, dummy);
			}
		});
		
		new GetFindersTask(this).execute();
	}	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				items.clear();
				new GetFindersTask(this).execute();
			}
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

	public void updateAsyncTaskProgress(SeekingItem s) {
		items.add(s);
		adapter.notifyDataSetChanged();
	}
}
