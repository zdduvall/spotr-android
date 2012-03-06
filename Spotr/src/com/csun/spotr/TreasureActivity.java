package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.csun.spotr.adapter.TreasureAdapter;
import com.csun.spotr.core.Treasure;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

/**
 * Description: 
 * Display user's treasures
 **/
public class TreasureActivity 
	extends Activity
		implements IActivityProgressUpdate<Treasure> {

	private static final String TAG = "(TreasureActivity)";
	private static final String GET_USER_TREASURES_URL = "http://107.22.209.62/android/get_user_treasures.php";

	private ListView listview;
	private TreasureAdapter adapter;
	private List<Treasure> treasureList = new ArrayList<Treasure>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.treasure);

		setupListView();
		new GetUserTreasuresTask(this).execute();
	}
	
	private void setupListView() {
		listview = (ListView) findViewById(R.id.treasure_xml_listview);
		adapter = new TreasureAdapter(this, treasureList);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
			}
		});
	}
	
	private static class GetUserTreasuresTask
		extends AsyncTask<Integer, Treasure, Boolean> 
			implements IAsyncTask<TreasureActivity> {

		private WeakReference<TreasureActivity> ref;

		public GetUserTreasuresTask(TreasureActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
			
		}

		@Override
		protected void onProgressUpdate(Treasure... t) {
			ref.get().updateAsyncTaskProgress(t[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			// send user id
			data.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// retrieve data from server
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_TREASURES_URL, data);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new Treasure(
								array.getJSONObject(i).getInt("treasure_tbl_id"),
								array.getJSONObject(i).getString("treasure_tbl_name"), 
								array.getJSONObject(i).getString("treasure_tbl_icon_url"), 
								array.getJSONObject(i).getString("treasure_tbl_type"), 
								array.getJSONObject(i).getString("treasure_tbl_code"), 
								array.getJSONObject(i).getString("treasure_tbl_company"), 
								array.getJSONObject(i).getString("treasure_tbl_expiration_date")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetUserTreasureTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}

		public void attach(TreasureActivity a) {
			ref = new WeakReference<TreasureActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	public void updateAsyncTaskProgress(Treasure u) {
		treasureList.add(u);
		adapter.notifyDataSetChanged();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		return null;
	}
	
	@Override
	public void onPause() {
		Log.v(TAG,"I'm paused");
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG,"I'm destroyed");
		super.onPause();
	}
}
