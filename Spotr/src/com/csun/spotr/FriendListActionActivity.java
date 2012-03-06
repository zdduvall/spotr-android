package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.UserItemAdapter;

/**
 * Description:
 * 		Handle search for friends and send messages 
 */
public class FriendListActionActivity 
	extends Activity 
		implements IActivityProgressUpdate<UserItem> {
	
	private static final String TAG = "(FriendListActionActivity)";
	private static final String SEARCH_FRIENDS_URL = "http://107.22.209.62/android/search_friends.php";
	private static final String SEND_REQUEST_URL = "http://107.22.209.62/android/send_friend_request.php";
	
	private ListView 			listview = null;
	private UserItemAdapter 	adapter = null;
	private List<UserItem> 		userItemList = null;
	
	private EditText 			editTextSearch = null;
	private	SearchFriendsTask 	task = null;

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
				// start task
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

	private class SearchFriendsTask 
		extends AsyncTask<Void, UserItem, Boolean> 
			implements IAsyncTask<FriendListActionActivity> {
			
		private WeakReference<FriendListActionActivity> ref;
		private final String criteria;
		private final int offset;

		public SearchFriendsTask(FriendListActionActivity a, String criteria, int offset) {
			this.criteria = criteria;
			this.offset = offset;
			attach(a);
		}

		@Override
		protected void onPreExecute() {
		
		}

		@Override
		protected void onProgressUpdate(UserItem... u) {
			ref.get().updateAsyncTaskProgress(u[0]);
		}

		@Override
		protected Boolean doInBackground(Void...voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("text", criteria));
			data.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			data.add(new BasicNameValuePair("offset", Integer.toString(offset)));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(SEARCH_FRIENDS_URL, data);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new UserItem(
								array.getJSONObject(i).getInt("users_tbl_id"), 
								array.getJSONObject(i).getString("users_tbl_username"), 
								array.getJSONObject(i).getString("users_tbl_user_image_url")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "SearchFriendTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(FriendListActionActivity a) {
			ref = new WeakReference<FriendListActionActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
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

	private static class SendFriendRequestTask 
		extends AsyncTask<String, Integer, Boolean> 
			implements IAsyncTask<FriendListActionActivity> {
		
		private static final String TAG = "[AsyncTask].SendFriendRequestTask";
		private WeakReference<FriendListActionActivity> ref;

		public SendFriendRequestTask(FriendListActionActivity a) {
			attach(a);
		}
		
		@Override
		protected Boolean doInBackground(String... datas) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("users_id", datas[0].toString()));
			data.add(new BasicNameValuePair("friend_id", datas[1].toString()));
			data.add(new BasicNameValuePair("friend_message", datas[2].toString()));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(SEND_REQUEST_URL, data);
			
			try {
				if (json.getString("result").equals("success")) {
					return true;
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(String... datas) : ", "JSON error parsing data" + e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}
		
		public void attach(FriendListActionActivity a) {
			ref = new WeakReference<FriendListActionActivity>(a);
		}
		
		public void detach() {
			ref.clear();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.options_menu_xml_item_setting_icon :
			intent = new Intent("com.csun.spotr.SettingsActivity");
			startActivity(intent);
			finish();
			break;
		case R.id.options_menu_xml_item_logout_icon :
			SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
			editor.clear();
			editor.commit();
			intent = new Intent("com.csun.spotr.LoginActivity");
			startActivity(intent);
			finish();
			break;
		case R.id.options_menu_xml_item_mainmenu_icon :
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
}
