package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class RewardViewActivity extends Activity {
	private static final String TAG = "(RewardViewActivity)";
	private static final String CONVERT_BADGE_TO_POINTS_URL = "http://107.22.209.62/android/convert_badge_to_points.php";
	
	private ConvertBadgeTask task;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reward_view);
		final Bundle extras = getIntent().getExtras();
		displayBadgeInfo(extras);
		displayImage(extras.getString("url"));
		setupConvertButton(CurrentUser.getCurrentUser().getId(), extras.getInt("id"), extras.getInt("points"));
	}
	
	private void displayBadgeInfo(Bundle extras) {
		TextView textViewName = (TextView) findViewById(R.id.reward_view_xml_textview_name);
		TextView textViewRewardedDate = (TextView)findViewById(R.id.reward_view_xml_textview_rewarddate);
		TextView textViewPointLabel = (TextView)findViewById(R.id.reward_view_xml_textview_pointlabel);
		TextView textViewDescription = (TextView) findViewById(R.id.reward_view_xml_textview_description);
		TextView textViewPoints = (TextView) findViewById(R.id.reward_view_xml_textview_points);
		TextView textViewDate = (TextView) findViewById(R.id.reward_view_xml_textview_date);
		Button buttonConvert = (Button) findViewById(R.id.reward_view_xml_button_convert);
		
		if (extras.getInt("points") == -1) {
			textViewRewardedDate.setVisibility(View.GONE);
			textViewPointLabel.setVisibility(View.GONE);
			buttonConvert.setVisibility(View.GONE);
			textViewPoints.setVisibility(View.GONE);
			textViewDate.setVisibility(View.GONE);
		}
		
		textViewName.setText(extras.getString("name"));
		textViewDescription.setText(extras.getString("description"));
		textViewDate.setText(extras.getString("date"));
		textViewPoints.setText(Integer.toString(extras.getInt("points")));
	}
	
	private void displayImage(String url) {
		ImageView imageViewBadge = (ImageView) findViewById(R.id.reward_view_xml_imageview_badge);
		ImageLoader imageLoader = new ImageLoader(this);
		imageLoader.displayImage(url, imageViewBadge);
	}
	
	private void setupConvertButton(final int userId, final int badgeId, final int points) {
		Button buttonConvert = (Button) findViewById(R.id.reward_view_xml_button_convert);
		buttonConvert.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				task = new ConvertBadgeTask(RewardViewActivity.this, userId, badgeId, points);
				task.execute();
			}
		});
	}
	private static class ConvertBadgeTask 
		extends AsyncTask<Void, Integer, String> 
			implements IAsyncTask<RewardViewActivity> {
			
		private WeakReference<RewardViewActivity> ref;
		private int userId;
		private int badgeId;
		private int points;
		
		public ConvertBadgeTask(RewardViewActivity a, int userId, int badgeId, int points) {
			attach(a);
			this.userId = userId;
			this.badgeId = badgeId;
			this.points = points;
		}
		
		@Override
		protected void onPreExecute() {
			
		}

		@Override
		protected String doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			data.add(new BasicNameValuePair("badge_id", Integer.toString(badgeId)));
			data.add(new BasicNameValuePair("points", Integer.toString(points)));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(CONVERT_BADGE_TO_POINTS_URL, data);
			String result = "";
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "ConvertBadgeTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result.equals("success")) {
				Toast.makeText(ref.get(), "Successful", Toast.LENGTH_LONG).show();
				Intent data = new Intent();
				ref.get().setResult(RESULT_OK, data);
				ref.get().finish();
			}
			detach();
		}

		public void attach(RewardViewActivity a) {
			ref = new WeakReference<RewardViewActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
}