package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;


/**
 * Description:
 * 		Generate a random treasure from server and return to user
 */
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
		
		private WeakReference<GenerateTreasureActivity> ref;
		private int placeId;
		private int userId;

		public GetRandomTreasureTask(GenerateTreasureActivity a, int userId, int placeId) {
			attach(a);
			this.userId = userId;
			this.placeId = placeId;
		}

		@Override
		protected void onPreExecute() {
		
		}

		@Override
		protected void onProgressUpdate(Treasure... t) {
			ref.get().updateAsyncTaskProgress(t[0]);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("spot_id", Integer.toString(placeId)));
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			
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
					Log.e(TAG + "GetRandomTreasureTask.doInBackGround(Void... voids) : ", "JSON error parsing data" + e.toString());
					
					
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
		TextView textViewName = (TextView) findViewById(R.id.generate_treasure_xml_textview_name);
		TextView textViewCompany = (TextView) findViewById(R.id.generate_treasure_xml_textview_company);
		TextView textViewExpirationDate = (TextView) findViewById(R.id.generate_treasure_xml_textview_expiration_date);
		TextView textViewBarcode = (TextView) findViewById(R.id.generate_treasure_xml_textview_barcode);
		ImageView imageViewIcon = (ImageView) findViewById(R.id.generate_treasure_xml_imageview_icon);
		
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		textViewName.setText(t.getName());
		textViewCompany.setText(t.getCompany());
		textViewExpirationDate.setText(t.getExpirationDate());
		textViewBarcode.setText(t.getCode());
		imageLoader.displayImage(t.getIconUrl(), imageViewIcon);
	}
}