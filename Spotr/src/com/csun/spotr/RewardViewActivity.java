package com.csun.spotr;

import com.csun.spotr.util.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class RewardViewActivity extends Activity {
	private static final String TAG = "(RewardViewActivity)";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reward_view);
		
		TextView textViewName = (TextView) findViewById(R.id.reward_view_xml_textview_name);
		TextView textViewDescription = (TextView) findViewById(R.id.reward_view_xml_textview_description);
		TextView textViewPoints = (TextView) findViewById(R.id.reward_view_xml_textview_points);
		TextView textViewDate = (TextView) findViewById(R.id.reward_view_xml_textview_date);
		ImageView imageViewBadge = (ImageView) findViewById(R.id.reward_view_xml_imageview_badge);
		
		Bundle extras = getIntent().getExtras();
		int id = extras.getInt("id");
		textViewName.setText(extras.getString("name"));
		textViewDescription.setText(extras.getString("description"));
		textViewDate.setText(extras.getString("date"));
		textViewPoints.setText(Integer.toString(extras.getInt("points")));
		
		ImageLoader imageLoader = new ImageLoader(this);
		imageLoader.displayImage(extras.getString("url"), imageViewBadge);
	}
}