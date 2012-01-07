package com.csun.spotr;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.FriendFeed;
import com.csun.spotr.core.PlaceLog;
import com.csun.spotr.gui.FriendListFeedItemAdapter;
import com.csun.spotr.gui.PlaceActivityItemAdapter;
import com.csun.spotr.gui.UserActivityItemAdapter;
import com.csun.spotr.helper.ImageHelper;
import com.csun.spotr.helper.JsonHelper;
import com.csun.spotr.singleton.CurrentUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FriendListFeedActivity extends Activity {
	private static final String TAG = "(FriendListFeedActivity)";
	private static final String GET_FRIEND_FEED_URL = "http://107.22.209.62/android/get_friend_feeds.php";
	private List<FriendFeed> friendFeedList = new ArrayList<FriendFeed>();
	private ListView list;
	private FriendListFeedItemAdapter adapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list_feed);
		
		list = (ListView) findViewById(R.id.friend_list_feed_xml_listview);
		adapter = new FriendListFeedItemAdapter(FriendListFeedActivity.this, friendFeedList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});
		new GetFriendFeedTask().execute();
    }
    
    private class GetFriendFeedTask extends AsyncTask<Void, FriendFeed, Boolean> {
		private List<NameValuePair> datas = new ArrayList<NameValuePair>(); 
		private ProgressDialog progressDialog = null;
		
		@Override
		protected void onPreExecute() {
			datas.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// display waiting dialog
			progressDialog = new ProgressDialog(FriendListFeedActivity.this);
			progressDialog.setMessage("Sending request...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}
		
		@Override
		  protected void onProgressUpdate(FriendFeed... feeds) {
			progressDialog.dismiss();
			friendFeedList.add(feeds[0]);
			adapter.notifyDataSetChanged();
			// adapter.notifyDataSetInvalidated();
	    }
		
		@Override
		protected Boolean doInBackground(Void...voids) {
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_FRIEND_FEED_URL, datas);
			if (array != null) { 
				try {
					for (int i = 0; i < array.length(); ++i) { 
						Uri snapPictureUri = null;
						Uri userPictureUri = null;
						
						if (Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.SNAP_PICTURE) {
							snapPictureUri = constructUriFromBitmap(ImageHelper.downloadImage(array.getJSONObject(i).getString("activity_tbl_snap_picture_url")), 100);
						}
						
						if(ImageHelper.downloadImage(array.getJSONObject(i).getString("users_tbl_user_image_url")) != null) {
							userPictureUri = constructUriFromBitmap(ImageHelper.downloadImage(array.getJSONObject(i).getString("users_tbl_user_image_url")), 1);
						}
						
						publishProgress(
							new FriendFeed.Builder(
									// required parameters
									array.getJSONObject(i).getInt("activity_tbl_id"),
									array.getJSONObject(i).getInt("friends_tbl_friend_id"),
									array.getJSONObject(i).getString("users_tbl_username"),
									Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")),
									array.getJSONObject(i).getString("activity_tbl_created"),
									array.getJSONObject(i).getString("spots_tbl_name"))
										// optional parameters
										.challengeName(array.getJSONObject(i).getString("challenges_tbl_name"))
										.challengeDescription(array.getJSONObject(i).getString("challenges_tbl_description"))
										.activitySnapPictureUri(snapPictureUri)
										.friendPictureUri(userPictureUri)
										.activityComment(array.getJSONObject(i).getString("activity_tbl_comment"))
											.build());
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFriendFeedTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result == false) {
				AlertDialog dialogMessage = new AlertDialog.Builder(FriendListFeedActivity.this).create();
				dialogMessage.setTitle("Hello " + CurrentUser.getCurrentUser().getUsername());
				dialogMessage.setMessage("There are no friend feeds yet!");
				dialogMessage.setButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogMessage.show();
			}
		}
			
	}
    
    private Uri constructUriFromBitmap(Bitmap bitmap, int quality) {
		ContentValues values = new ContentValues(1);
		values.put(Media.MIME_TYPE, "image/jpeg");
		Uri uri = getContentResolver().insert(Media.EXTERNAL_CONTENT_URI, values);
		try {
		    OutputStream outStream = getContentResolver().openOutputStream(uri);
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
		    outStream.close();
		} 
		catch (Exception e) {
		    Log.e(TAG, "exception while writing image", e);
		}
		bitmap.recycle();
		return uri;
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.options_menu_xml_item_setting_icon:
				intent = new Intent("com.csun.spotr.SettingsActivity");
				startActivity(intent);
				finish();
				break;
			case R.id.options_menu_xml_item_logout_icon:
				SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();
				intent = new Intent("com.csun.spotr.LoginActivity");
				startActivity(intent);
				finish();
				break;
			case R.id.options_menu_xml_item_mainmenu_icon:
				intent = new Intent("com.csun.spotr.MainMenuActivity");
				startActivity(intent);
				finish();
				break;
		}
		return true;
	}
    
    @Override
    public void onPause() {
		// clean up
    	Log.v(TAG, "I'm paused!");
        super.onPause();
	}
    
    @Override
    public void onDestroy() {
		// clean up
    	Log.v(TAG, "I'm destroyed!");
        for (FriendFeed feed: friendFeedList) {
        	if (feed.getChallengeType() == Challenge.Type.SNAP_PICTURE)
        		getContentResolver().delete(feed.getActivitySnapPictureUri(), null, null);
        	if (feed.getFriendPictureUri() != null)
        		getContentResolver().delete(feed.getFriendPictureUri(), null, null);
        }
        super.onDestroy();
	}
}