package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.PlaceActivityActivity;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.FriendFeedItemAdapter;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.adapter_item.FriendFeedItem;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;


public class GetPlaceFeedTask 
   	extends AsyncTask<Void, FriendFeedItem, Boolean> 
   		implements IAsyncTask<PlaceActivityActivity> {
    	
    private static final String TAG = "[AsyncTask].GetPlaceFeedTask";
    private static final String GET_PLACE_FEED_URL = "http://107.22.209.62/android/get_activities.php";
    private static final String GET_FIRST_COMMENT_URL = "http://107.22.209.62/android/get_comment_first.php";
	private WeakReference<PlaceActivityActivity> ref;
	private int offset; 
	
	public GetPlaceFeedTask(PlaceActivityActivity a, int offset) {
		// DEBUG
		Log.v(TAG, "GetPlaceFeedTask runs with offset: " + offset);
		
		attach(a);
		this.offset = offset;
	}
	
	@Override
	protected void onPreExecute() {
	}
	
	@Override
    protected void onProgressUpdate(FriendFeedItem... f) {
		ref.get().updateAsyncTaskProgress(f[0]);
    }
	
	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>(); 
		data.add(new BasicNameValuePair("spots_id", Integer.toString(ref.get().currentPlaceId)));
		data.add(new BasicNameValuePair("offset", Integer.toString(offset)));
		return data;
	}
	
	private Comment getFirstComment(int activityId) {
		List<NameValuePair> data = new ArrayList<NameValuePair>(); 
		data.add(new BasicNameValuePair("activity_id", Integer.toString(activityId)));
		Comment firstComment = new Comment(-1, "", "", "", "");
		JSONArray temp = JsonHelper.getJsonArrayFromUrlWithData(GET_FIRST_COMMENT_URL, data);
		try {
			if (temp != null) {
				firstComment.setId(temp.getJSONObject(0).getInt("comments_tbl_id"));
				firstComment.setUsername(temp.getJSONObject(0).getString("users_tbl_username"));
				firstComment.setPictureUrl(temp.getJSONObject(0).getString("users_tbl_user_image_url"));
				firstComment.setTime(temp.getJSONObject(0).getString("comments_tbl_time"));
				firstComment.setContent(temp.getJSONObject(0).getString("comments_tbl_content"));
			}	
		}
		catch (JSONException e) {
			Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
		}
		
		return firstComment;
	}
	
	@Override
	protected Boolean doInBackground(Void... voids) {
		// cancel task: check before task run
		if (isCancelled()) {
			return true;
		}
		
		List<NameValuePair> data = prepareUploadData();
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_PLACE_FEED_URL, data);
		
		// cancel task: check after fetching data from the server
		if (isCancelled()) {
			return true;
		}
		
		if (array != null) { 
			try {
				for (int i = 0; i < array.length(); ++i) { 
					// cancel task: check while is running
					if (isCancelled()) {
						return true;
					}

					String snapPictureUrl = "";
					String userPictureUrl = "";
					String shareUrl = "";
					String treasureIconUrl = "";
					String company = "";
				
					if (Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.SNAP_PICTURE) {
						snapPictureUrl = array.getJSONObject(i).getString("activity_tbl_snap_picture_url");
					}
					
					if (Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.FIND_TREASURE) {
						treasureIconUrl = array.getJSONObject(i).getString("activity_tbl_treasure_icon_url");
						company = array.getJSONObject(i).getString("activity_tbl_treasure_company");
					}
					
					if(array.getJSONObject(i).getString("users_tbl_user_image_url").equals("") == false) {
						userPictureUrl = array.getJSONObject(i).getString("users_tbl_user_image_url");
					}
					
					if(array.getJSONObject(i).has("activity_tbl_share_url") && !array.getJSONObject(i).getString("activity_tbl_share_url").equals("null")) {
						shareUrl = array.getJSONObject(i).getString("activity_tbl_share_url");
					}
					
					FriendFeedItem ffi = 
						new FriendFeedItem.Builder(
							// required parameters
							array.getJSONObject(i).getInt("activity_tbl_id"),
							0, // don't use friend id for place
							array.getJSONObject(i).getString("users_tbl_username"),
							Challenge.returnType(array.getJSONObject(i).getString("challenges_tbl_type")),
							array.getJSONObject(i).getString("activity_tbl_created"),
							array.getJSONObject(i).getString("spots_tbl_name"))
								// optional parameters
								.challengeName(array.getJSONObject(i).getString("challenges_tbl_name"))
								.challengeDescription(array.getJSONObject(i).getString("challenges_tbl_description"))
								.activitySnapPictureUrl(snapPictureUrl)
								.friendPictureUrl(userPictureUrl)
								.activityComment(array.getJSONObject(i).getString("activity_tbl_comment"))
								.shareUrl(shareUrl)
								.numberOfComments(array.getJSONObject(i).getInt("activity_tbl_total_comments"))
    							.likes(array.getJSONObject(i).getInt("activity_tbl_likes"))
    							.treasureIconUrl(treasureIconUrl)
								.treasureCompany(company)
    								.build();
					
					ffi.setFirstComment(getFirstComment(ffi.getActivityId()));
					publishProgress(ffi);
				}
			}
			catch (JSONException e) {
				Log.e(TAG + "GetPlaceFeedTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
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

	public void attach(PlaceActivityActivity a) {
		ref = new WeakReference<PlaceActivityActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}
	
