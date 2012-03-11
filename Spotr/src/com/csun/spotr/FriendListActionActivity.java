package com.csun.spotr;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.adapter.UserItemAdapter;
import com.csun.spotr.asynctask.SearchFriendsTask;
import com.csun.spotr.asynctask.SendFriendRequestTask;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/11/2012
 **/

/**
 * Description:
 * 		Handle search for friends and send messages 
 */
public class FriendListActionActivity 
	extends Activity 
		implements IActivityProgressUpdate<UserItem> {
	
	private static final String TAG = "(FriendListActionActivity)";
	
	private ListView listview = null;
	private UserItemAdapter adapter = null;
	private List<UserItem> userItemList = null;
	
	private EditText editTextSearch = null;
	private	SearchFriendsTask task = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list_search);
		setupTextSearchBar();
		setupListView();
		setupSearchButton();
	}
	
	private void setupTextSearchBar() {
		editTextSearch = (EditText) findViewById(R.id.friend_list_action_xml_edittext_search);
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
	}
	
	private void setupSearchButton() {
		Button buttonSearch = (Button) findViewById(R.id.friend_list_action_xml_button_search);
		buttonSearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				userItemList.clear();
				adapter.notifyDataSetChanged();
				task = new SearchFriendsTask(FriendListActionActivity.this, editTextSearch.getText().toString(), 0);
				task.execute();
			}
		});
	}
	
	private void setupListView() {
		listview = (ListView) findViewById(R.id.friend_list_action_xml_listview_search_friends);
		userItemList = new ArrayList<UserItem>();
		adapter = new UserItemAdapter(this.getApplicationContext(), userItemList);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startDialog(userItemList.get(position));
			}
		});
		
		listview.setOnScrollListener(new SeachFriendsOnScrollListener());
	}

	private void startDialog(final UserItem user) {
		AlertDialog.Builder builder;
		final AlertDialog dialog;
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		final View layout = inflater.inflate(R.layout.friend_request_dialog, null);
		
		builder = new AlertDialog.Builder(this);
		builder.setTitle("Send request");
		builder.setView(layout);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				EditText editTextMessage = (EditText) layout.findViewById(R.id.friend_request_dialog_xml_edittext_message);
				String message = " ";
				if (editTextMessage.getText() != null) {
					message = editTextMessage.getText().toString();
				}
				
				SendFriendRequestTask task = new SendFriendRequestTask(FriendListActionActivity.this);
				task.execute(Integer.toString(CurrentUser.getCurrentUser().getId()), Integer.toString(user.getId()), message);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
			}
		});
		
		dialog = builder.create();
		dialog.show();
	}

	public void updateAsyncTaskProgress(UserItem u) {
		userItemList.add(u);
		adapter.notifyDataSetChanged();
	}
	
	public class SeachFriendsOnScrollListener implements OnScrollListener {
	    private int visibleThreshold = 10;
	    private int currentPage = 0;
	    private int previousTotal = 0;
	    private boolean loading = true;
	 
	    public SeachFriendsOnScrollListener() {
	    	
	    }
	    public SeachFriendsOnScrollListener(int visibleThreshold) {
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
	        	new SearchFriendsTask(FriendListActionActivity.this, editTextSearch.getText().toString(), currentPage).execute();
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
