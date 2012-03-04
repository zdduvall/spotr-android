package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class CheckInActivity extends Activity {
	private static final String TAG = "(CheckInActivity)";
	private static final String DO_CHECK_IN_URL = "http://107.22.209.62/android/do_check_in.php";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.check_in);
	}
	
	private static class CheckInTask 
		extends AsyncTask<String, Integer, String> 
			implements IAsyncTask<CheckInActivity> {
	
		private WeakReference<CheckInActivity> ref;
	
		public CheckInTask(CheckInActivity a) {
			attach(a);
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected String doInBackground(String... ids) {
			/*
			 * 1. Retrieve data from [activity] table where $users_id and $places_id
			 * 2. Check the result of this query:
			 * 		a. If the result is null, then user hasn't visited this place yet which also implies that he has not done any challenges.
			 * 		   Thus we can update the current user:
			 * 		   i.  Update [activity] table with $users_id, $places_id, $challenges_id   
			 * 		   ii. Update [users] table with 
			 * 			   + $challenges_done = $challenges_done + 1
			 * 			   + $points += challenges.points
			 * 			   + $places_visited = $places_visited + 1
			 * 		b. If the result is not null, update [activity] table with $users_id, $places_id, $challenges_id with CURRENT_TIMESTAMP, but
			 * 		   don't run the statement:
			 * 		       + $places_visited = $places_visited + 1
			 * 3. All these complexity is done at server side, i.e. php script, so we only need to post to the server three parameters:
			 * 	    a. users_id
			 * 		b. places_id
			 * 		c. challenges_id
			 * 4. The return of this query is the number points is added the points added to the user account.
			 */
			
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("users_id", ids[0]));
			data.add(new BasicNameValuePair("spots_id", ids[1]));
			data.add(new BasicNameValuePair("challenges_id", ids[2]));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(DO_CHECK_IN_URL, data);
			
			String result = "";
			
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "CheckInTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return result;
		}
	
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				
			}
			detach();
		}
	
		public void attach(CheckInActivity a) {
			ref = new WeakReference<CheckInActivity>(a);
		}
	
		public void detach() {
			ref.clear();
		}
	}
}
