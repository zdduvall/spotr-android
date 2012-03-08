package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.ExpandableUserItemAdapter;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

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
	private static final String GET_FRIENDS_URL = "http://107.22.209.62/android/get_friends.php";

	public ExpandableListView listView = null;	
	public ExpandableUserItemAdapter adapter = null;
	public List<UserItem> userItemList = new ArrayList<UserItem>();
	public GetFriendsTask task = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list_main);
		setupListView();

		// initially, we load 10 items and show users immediately
		task = new GetFriendsTask(this, 0);
		task.execute();
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
		
		// set up group indicator
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				listView.setGroupIndicator(getResources().getDrawable(R.drawable.ic_expander_default));				
			}
			
		});
		
		listView.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				listView.setGroupIndicator(getResources().getDrawable(R.drawable.ic_expander_default));
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				listView.setGroupIndicator(getResources().getDrawable(R.drawable.ic_expander));
			}
		});
		
		listView.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			public void onGroupCollapse(int groupPosition) {
				listView.setGroupIndicator(getResources().getDrawable(R.drawable.ic_expander));
			}
		});
		
		// handle item scrolling event
		listView.setOnScrollListener(new FeedOnScrollListener());
	}
	
    public int getDipsFromPixel(float pixels) {
    	// get the screen's density scale
    	final float scale = getResources().getDisplayMetrics().density;
     
    	// convert the dps to pixels, based on density scale
    	return (int) (pixels * scale + 0.5f);
    }
    
	private static class GetFriendsTask 
		extends	AsyncTask<Void, UserItem, Boolean> 
		implements IAsyncTask<FriendListActivity> {

		private static final String TAG = "[AsyncTask].GetFriendTask";
		private WeakReference<FriendListActivity> ref;
		private int offset;

		public GetFriendsTask(FriendListActivity a, int offset) {
			attach(a);
			this.offset = offset;
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			data.add(new BasicNameValuePair("offset", Integer.toString(offset)));
			return data;
		}

		@Override
		protected void onProgressUpdate(UserItem... u) {
			ref.get().updateAsyncTaskProgress(u[0]);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = prepareUploadData();
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_FRIENDS_URL, data);
			if (array != null) {
				try {
					if (ref.get().task.isCancelled()) {
						return true;
					}
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new UserItem(
								array.getJSONObject(i).getInt("users_tbl_id"), 
								array.getJSONObject(i).getString("users_tbl_username"), 
								array.getJSONObject(i).getString("users_tbl_user_image_url")));
					}
				} catch (JSONException e) {
					Log.e(TAG + ".doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(FriendListActivity a) {
			ref = new WeakReference<FriendListActivity>(a);
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
