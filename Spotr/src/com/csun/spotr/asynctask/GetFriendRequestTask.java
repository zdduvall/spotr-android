package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.FriendRequestActivity;
import com.csun.spotr.core.adapter_item.FriendRequestItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetFriendRequestTask 
    extends AsyncTask<Void, FriendRequestItem, Boolean> 
    	implements IAsyncTask<FriendRequestActivity>{
    
	private static final String TAG = "[AsyncTask].GetFriendRequestTask";
	private static final String GET_REQUEST_URL = "http://107.22.209.62/android/get_friend_requests.php";
    private List<NameValuePair> friendData = new ArrayList<NameValuePair>(); 
    private WeakReference<FriendRequestActivity> ref;
    
    public GetFriendRequestTask(FriendRequestActivity a) {
    	attach(a);
    }
    
    @Override
    protected void onProgressUpdate(FriendRequestItem... f) {
    	ref.get().updateAsyncTaskProgress(f[0]);
    }
    
    @Override
    protected void onPreExecute() {
    }
    
   @Override
    protected Boolean doInBackground(Void...voids) {
    	friendData.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
    	JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_REQUEST_URL, friendData);
    	if (array != null) { 
    		try {
    			for (int i = 0; i < array.length(); ++i) { 
    				publishProgress(
    					new FriendRequestItem(
    						array.getJSONObject(i).getInt("user_requests_tbl_friend_id"),
    						array.getJSONObject(i).getString("users_tbl_username"),
    						array.getJSONObject(i).getString("user_requests_tbl_friend_message"),
    						array.getJSONObject(i).getString("user_requests_tbl_time")));
    			}
    		}
    		catch (JSONException e) {
    			Log.e(TAG + "GetFriendRequestTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e);
    		}
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
    	if (result == true) {
    
    	}
    	detach();
    }
    
    public void attach(FriendRequestActivity a) {
    	ref = new WeakReference<FriendRequestActivity>(a);
    }
    
    public void detach() {
    	ref.clear();
    }
}