package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.FriendRequestActivity;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class UpdateFriendTask 
    extends AsyncTask<String, Integer, Boolean> 
    	implements IAsyncTask<FriendRequestActivity> {
    
	private static final String TAG = "[AsyncTask].UpdateFriendTask";
    private WeakReference<FriendRequestActivity> ref;
    
    public UpdateFriendTask(FriendRequestActivity a) {
    	attach(a);
    }
    
    private List<NameValuePair> prepareUploadData() {
		List<NameValuePair> datas = new ArrayList<NameValuePair>();
		datas.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
    	datas.add(new BasicNameValuePair("friend_id", Integer.toString(ref.get().currentSelectedFriendId)));
		return datas;
	}
    
    @Override
    protected Boolean doInBackground(String... urls) {
    	List<NameValuePair> datas = prepareUploadData();
    	JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(urls[0], datas);
    	
    	try {
    		if (json.getString("result").equals("success")) {
    			return true;
    		}
    	} 
    	catch (JSONException e) {
    		Log.e(TAG + "AcceptFriendTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e);
    	}
    	return false;
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