package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.csun.spotr.ProfileActivity;
import com.csun.spotr.adapter.FriendFeedItemAdapter;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.User;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.DialogId;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

public class GetUserFeedTask 
		extends AsyncTask<Void, FriendFeedItem, Boolean> 
			implements IAsyncTask<ProfileActivity> {
		
	private static final String TAG = "[AsyncTask].GetUserFeedTask";
	private static final String GET_USER_FEEDS = "http://107.22.209.62/android/get_current_user_feeds.php";
	private static final String GET_FIRST_COMMENT_URL = "http://107.22.209.62/android/get_comment_first.php";
	
	private WeakReference<ProfileActivity> ref;
	private int userId;
	private int offset;
	
	public GetUserFeedTask(ProfileActivity a, int userId, int offset) {
		this.userId = userId;
		this.offset = offset;
		attach(a);
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
			Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e);
		}
		
		return firstComment;
	}
	
	@Override 
	protected void onPreExecute() {
		
	}
	
	@Override
	protected void onProgressUpdate(FriendFeedItem... f) {
		if (isActivityStillRunning()) 
			ref.get().updateAsyncTaskProgress(f[0]);
    }
	
	private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
		data.add(new BasicNameValuePair("offset", Integer.toString(offset)));
		return data;
	}

	@Override
	protected Boolean doInBackground(Void...voids) {
		if (isCancelled()) {
			return false;
		}
		
		List<NameValuePair> data = prepareUploadData();
		// user's feeds
		JSONArray feedArray = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_FEEDS, data);
		
		try {
			if (isCancelled()) {
				return false;
			}
			
			if (feedArray != null) {
				for (int i = 0; i < feedArray.length(); ++i) { 
					
					String snapPictureUrl = "";
					String userPictureUrl = "";
					String shareUrl = "";
					String treasureIconUrl = "";
					String company = "";
					
					if (Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.SNAP_PICTURE) {
						snapPictureUrl = feedArray.getJSONObject(i).getString("activity_tbl_snap_picture_url");
					}
					
					if (Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.FIND_TREASURE) {
						treasureIconUrl = feedArray.getJSONObject(i).getString("activity_tbl_treasure_icon_url");
						company = feedArray.getJSONObject(i).getString("activity_tbl_treasure_company");
					}
					
					if (feedArray.getJSONObject(i).getString("users_tbl_user_image_url").equals("") == false) {
						userPictureUrl = feedArray.getJSONObject(i).getString("users_tbl_user_image_url");
					}
					
					if (feedArray.getJSONObject(i).has("activity_tbl_share_url") && !feedArray.getJSONObject(i).getString("activity_tbl_share_url").equals("null")) {
						shareUrl = feedArray.getJSONObject(i).getString("activity_tbl_share_url");
					}
					else {
						shareUrl = "";
					}
    				
    				FriendFeedItem ffi = 
    					new FriendFeedItem.Builder(
    							// required parameters
    							feedArray.getJSONObject(i).getInt("activity_tbl_id"),
    							0, // not used
    							feedArray.getJSONObject(i).getString("users_tbl_username"),
    							Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")),
    							feedArray.getJSONObject(i).getString("activity_tbl_created"),
    							feedArray.getJSONObject(i).getString("spots_tbl_name"))
    				
    								// optional parameters
    								.challengeName(feedArray.getJSONObject(i).getString("challenges_tbl_name"))
    								.challengeDescription(feedArray.getJSONObject(i).getString("challenges_tbl_description"))
    								.activitySnapPictureUrl(snapPictureUrl)
    								.friendPictureUrl(userPictureUrl)
    								.activityComment(feedArray.getJSONObject(i).getString("activity_tbl_comment"))
    								.shareUrl(shareUrl)
    								.numberOfComments(feedArray.getJSONObject(i).getInt("activity_tbl_total_comments"))
    								.likes(feedArray.getJSONObject(i).getInt("activity_tbl_likes"))
    								.treasureIconUrl(treasureIconUrl)
									.treasureCompany(company)
    									.build();
    				
    				
    				ffi.setFirstComment(getFirstComment(ffi.getActivityId()));
					publishProgress(ffi);
				}
			}
			
		}
		catch (JSONException e) {
			Log.e(TAG + "GetUserFeedTask.doInBackground() : ", "JSON error parsing data", e );
		}
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		detach();
	}

	public void attach(ProfileActivity a) {
		ref = new WeakReference<ProfileActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
	
	private boolean isActivityStillRunning() {
		return (ref != null && ref.get() != null && !ref.get().isFinishing());
	}
}