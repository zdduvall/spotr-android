package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.ExpandableUserItemAdapter;
import com.csun.spotr.asynctask.GetFriendsTask;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 * NOTE: Merged commit by Chris: 03/07/2012
 **/

/**
 * Description:
 * 		This class will retrieve a list of friends from database.
 *
 */
public class FriendListActivity 
	extends Activity 
		implements IActivityProgressUpdate<UserItem> {

	private static final String TAG = "(FriendListActivity)";

	public ExpandableListView listView = null;	
	public ExpandableUserItemAdapter adapter = null;
	public List<UserItem> userItemList = new ArrayList<UserItem>();
	public GetFriendsTask task = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list_main);
		setupListView();
		// make sure keyboard of edit text do not populate
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// initially, we load 10 items and show users immediately
		task = new GetFriendsTask(this, 0);
		task.execute();
	}

	public void setupDynamicSearch() {
		EditText edittextSearch = (EditText) findViewById(R.id.friend_list_main_xml_edittext_search);
		edittextSearch.setEnabled(true);
		edittextSearch.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {
				adapter.getFilter().filter(s.toString());
			}
		});
	}

	private void setupListView() {
		// initialize list view
		listView = (ExpandableListView) findViewById(R.id.friend_list_main_xml_listview_friends);

		// set indicators
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		listView.setIndicatorBounds(width - getDipsFromPixel(50), width - getDipsFromPixel(10));

		// set up list view adapter
		adapter = new ExpandableUserItemAdapter(this, userItemList);
		listView.setAdapter(adapter);

		// handle item scrolling event
		listView.setOnScrollListener(new FeedOnScrollListener());
	}

    public int getDipsFromPixel(float pixels) {
    	// get the screen's density scale
    	final float scale = getResources().getDisplayMetrics().density;
     
    	// convert the dps to pixels, based on density scale
    	return (int) (pixels * scale + 0.5f);
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

	    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	        if (loading) {
	            if (totalItemCount > previousTotal) {
	                loading = false;
	                previousTotal = totalItemCount;
	                currentPage += 10;
	            }
	        }
	        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
	            new GetFriendsTask(FriendListActivity.this, currentPage).execute();
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