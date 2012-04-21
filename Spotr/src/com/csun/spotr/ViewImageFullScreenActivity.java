package com.csun.spotr;

import com.csun.spotr.util.ImageLoader;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.widget.ImageView;

public class ViewImageFullScreenActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_image);
		
		Bundle extrasBundle = getIntent().getExtras();
		String url = extrasBundle.getString("image_url");
		
		ImageLoader il = new ImageLoader(this);
		ImageView imv = (ImageView) findViewById(R.id.view_image_xml_imageview_picture);
		il.displayImage(url, imv);
	}
}
