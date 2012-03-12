package com.csun.spotr.custom_gui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;
import com.google.android.maps.OverlayItem;

public class UserBalloonOverlayView<Item extends OverlayItem> extends FrameLayout {
	private LinearLayout layout;
	private TextView name;
	private ImageView picture;
	private ImageLoader imageLoader;

	public UserBalloonOverlayView(Context context, int balloonBottomOffset) {
		super(context);
		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);
		
		imageLoader = new ImageLoader(context.getApplicationContext());

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.user_ballon_overlay, layout);
		name = (TextView) v.findViewById(R.id.user_balloon_overlay_xml_name);
		picture = (ImageView) v.findViewById(R.id.user_ballon_overlay_xml_picture);

		ImageView close = (ImageView) v.findViewById(R.id.close_img_button);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				layout.setVisibility(GONE);
			}
		});

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		addView(layout, params);
	}

	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item
	 *            - The overlay item containing the relevant view data (title
	 *            and snippet).
	 */
	public void setData(Item item) {
		layout.setVisibility(VISIBLE);
		if (item.getTitle() != null) {
			name.setVisibility(VISIBLE);
			name.setText(item.getTitle());
		}
		else {
			name.setVisibility(GONE);
		}
		if (item.getSnippet() != null) {
			picture.setVisibility(VISIBLE);
			imageLoader.displayImage(item.getSnippet(), picture);
		}
		else {
			picture.setVisibility(GONE);
		}
	}

}