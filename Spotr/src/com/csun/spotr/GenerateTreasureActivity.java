package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.core.Treasure;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/**
 * Description:
 * 		Generate a random treasure from server and return to user
 **/
public class GenerateTreasureActivity 
	extends Activity 
		implements IActivityProgressUpdate<Treasure> {
	
	private static final String TAG = "(TreasureActivity)";
	private static final String GET_RANDOM_TREASURE_URL = "http://107.22.209.62/android/get_treasure.php";

	private int placeId;
	private int userId;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.generate_treasure);
		
		Bundle extrasBundle = getIntent().getExtras();
		placeId = extrasBundle.getInt("place_id");
		userId = CurrentUser.getCurrentUser().getId();
		
		// get treasure 
		new GetRandomTreasureTask(this, userId, placeId).execute();
	}
	
	private class GetRandomTreasureTask 
		extends AsyncTask<Void, Treasure, Boolean> 
			implements IAsyncTask<GenerateTreasureActivity> {
		
		private static final String TAG = "[AsyncTask].GetRandomTreasureTask";
		private WeakReference<GenerateTreasureActivity> ref;
		private int placeId;
		private int userId;

		public GetRandomTreasureTask(GenerateTreasureActivity a, int userId, int placeId) {
			attach(a);
			this.userId = userId;
			this.placeId = placeId;
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("spot_id", Integer.toString(placeId)));
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			return data;
		}

		@Override
		protected void onProgressUpdate(Treasure... t) {
			ref.get().updateAsyncTaskProgress(t[0]);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = prepareUploadData();
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_RANDOM_TREASURE_URL, data);
			if (array != null) {
				try {
					publishProgress(
						new Treasure(
							array.getJSONObject(0).getInt("treasure_tbl_id"),
							array.getJSONObject(0).getString("treasure_tbl_name"),
							array.getJSONObject(0).getString("treasure_tbl_icon_url"),
							array.getJSONObject(0).getString("treasure_tbl_type"),
							array.getJSONObject(0).getString("treasure_tbl_code"),
							array.getJSONObject(0).getString("treasure_tbl_company"),
							array.getJSONObject(0).getString("treasure_tbl_expiration_date"))
						);
				}
				catch (JSONException e) {
					Log.e(TAG + ".doInBackGround(Void... voids) : ", "JSON error parsing data" + e.toString());
					
					
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

		public void attach(GenerateTreasureActivity a) {
			ref = new WeakReference<GenerateTreasureActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	public void updateAsyncTaskProgress(Treasure t) {
		displayTreasureInfo(t);
		displayTreasureImage(t);
	}
	
	private void displayTreasureInfo(Treasure t) {
		TextView textViewName = (TextView) findViewById(R.id.generate_treasure_xml_textview_name);
		TextView textViewCompany = (TextView) findViewById(R.id.generate_treasure_xml_textview_company);
		TextView textViewExpirationDate = (TextView) findViewById(R.id.generate_treasure_xml_textview_expiration_date);
		TextView textViewBarcode = (TextView) findViewById(R.id.generate_treasure_xml_textview_barcode);
		
		textViewName.setText(t.getName());
		textViewCompany.setText(t.getCompany());
		textViewExpirationDate.setText(t.getExpirationDate());
		textViewBarcode.setText(t.getCode());
	}
	
	private void displayTreasureImage(Treasure t) {
		ImageView imageViewIcon = (ImageView) findViewById(R.id.generate_treasure_xml_imageview_icon);
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		imageLoader.displayImage(t.getIconUrl(), imageViewIcon);
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