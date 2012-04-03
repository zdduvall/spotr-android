package com.csun.spotr;

import com.csun.spotr.util.ImageLoader;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InboxViewMessageActivity extends Activity {
	private static final String TAG = "(InboxViewMessageActivity)";
	private String senderPictureUrl;
	private String senderName;
	private String senderTime;
	private String senderMessage;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.inbox_view_message);
		getDataFromBundle();
		displayMessageDetail();
		
		Button buttonDone = (Button) findViewById(R.id.inbox_view_message_xml_button_done);
		buttonDone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
	}
	
	private void getDataFromBundle() {
		Bundle extrasBundle = getIntent().getExtras();
    	senderName = extrasBundle.getString("sender_name");
    	senderTime = extrasBundle.getString("sender_time");
    	senderMessage = extrasBundle.getString("sender_message");
    	senderPictureUrl = extrasBundle.getString("sender_picture_url");
	}
	
	private void displayMessageDetail() {
		ImageLoader imageLoader = new ImageLoader(this);
		TextView textViewName = (TextView) findViewById(R.id.inbox_view_message_xml_textview_name);
		textViewName.setText(senderName);
		
		TextView textViewTime = (TextView) findViewById(R.id.inbox_view_message_xml_textview_time);
		textViewTime.setText(senderTime);
		
		TextView textViewMessage = (TextView) findViewById(R.id.inbox_view_message_xml_textview_message);
		textViewMessage.setText(senderMessage);
		
		ImageView imageViewPicture = (ImageView) findViewById(R.id.inbox_view_message_xml_imageview_user_picture);
		imageLoader.displayImage(senderPictureUrl, imageViewPicture);
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			setResult(RESULT_OK);
			finish();
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
