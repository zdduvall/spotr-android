package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.csun.spotr.adapter.PlaceLootAdapter;
import com.csun.spotr.asynctask.GetPlaceLootTask;
import com.csun.spotr.asynctask.DoTakeLootTask;
import com.csun.spotr.core.adapter_item.LootItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;

/*
 * Description:
 * 		Display Loot
 */
public class PlaceLootActivity 
	extends Activity 
		implements IActivityProgressUpdate<LootItem> {
	
	private static final String TAG = "(PlaceLootActivity)";
	private List<LootItem> items;
	private GridView gridview;
	private PlaceLootAdapter adapter;
	private int currentPlaceId;
	
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_loot);
		
		Bundle extrasBundle = getIntent().getExtras();
		currentPlaceId = extrasBundle.getInt("place_id");
		
		items = new ArrayList<LootItem>();
		gridview = (GridView) findViewById(R.id.place_loot_xml_gridview);
		adapter = new PlaceLootAdapter(this, items);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {			
		//		Bundle extras = new Bundle();
		//		extras.putInt("spotLoot", items.get(position).getSpotLoot());
		//		Intent intent = new Intent(getApplicationContext(), FinderItemDetailActivity.class);
		//		intent.putExtras(extras);
		//		startActivity(intent);
				showDialog(items.get(position).getSpotLoot() );
		//		extras.clear();
				
			}	
		});
		
	new GetPlaceLootTask(this,currentPlaceId).execute();
	}	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				items.clear();
	//			new GetFindersTask(this).execute();
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(final int spotLoot) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setMessage("What to do?").setCancelable(true).setPositiveButton("Pick Up", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				/*
				Context context = getApplicationContext();
				Toast.makeText(context, "Hello"+spotLoot, Toast.LENGTH_SHORT).show();
				*/
				new DoTakeLootTask(PlaceLootActivity.this, spotLoot).execute();
			}
		}).setNegativeButton("Leave", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});
		return builder.create();
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

	public void updateAsyncTaskProgress(LootItem s) {
		items.add(s);
		adapter.notifyDataSetChanged();
	}
}
