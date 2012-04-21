package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.adapter.FriendFeedItemAdapter;
import com.csun.spotr.asynctask.GetPlaceFeedTask;
import com.csun.spotr.core.adapter_item.FriendFeedItem;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Display feeds of a place
 */
public class PlaceActivityActivity 
	extends Activity 
		implements IActivityProgressUpdate<FriendFeedItem> {
	
	private static final String TAG = "(PlaceActivityActivity)";
	
	public int currentPlaceId = 0;
	private ListView listview = null;
	private FriendFeedItemAdapter adapter = null;
	private List<FriendFeedItem> placeFeedList = new ArrayList<FriendFeedItem>();
	private GetPlaceFeedTask task = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friend_list_feed);
        Bundle extrasBundle = getIntent().getExtras();
		currentPlaceId = extrasBundle.getInt("place_id");
		setupListView();
		task = new GetPlaceFeedTask(this, 0);
		task.execute();	
    }
    
    private void setupListView() {
    	listview = (ListView) findViewById(R.id.friend_list_feed_xml_listview);
		adapter = new FriendFeedItemAdapter(this, placeFeedList, false);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
			}
		});
		
		listview.setOnScrollListener(new FeedOnScrollListener());
    }
    
	public void updateAsyncTaskProgress(FriendFeedItem f) {
		placeFeedList.add(f);
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
	
	public class FeedOnScrollListener implements OnScrollListener {
	    private int visibleThreshold = 5;
	    private int currentPage = 0;
	    private int previousTotal = 0;
	    private boolean loading = true;
	 
	    public FeedOnScrollListener() {
	    	
	    }
	    
	    public FeedOnScrollListener(int visibleThreshold) {
	        this.visibleThreshold = visibleThreshold;
	    }
	 
	    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	        if (loading) {
	            if (totalItemCount > previousTotal) {
	                loading = false;
	                previousTotal = totalItemCount;
	                currentPage += 5;
	            }
	        }
	        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
	            new GetPlaceFeedTask(PlaceActivityActivity.this, currentPage).execute();
	            loading = true;
	        }
	    }
	 
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
	    	// TODO : not use
	    }
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
