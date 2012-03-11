package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class CheckInUserItemAdapter extends BaseAdapter {
	private Context context;
	private List<String> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder viewHolder;
	private int background;
	
	public CheckInUserItemAdapter(Context c, List<String> items) {
		this.context = c;
		this.items = items;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(c.getApplicationContext());
		TypedArray ta = context.obtainStyledAttributes(R.styleable.gallery_style);
		background = ta.getResourceId(R.styleable.gallery_style_android_galleryItemBackground, 1);
		ta.recycle();
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ItemViewHolder {
		ImageView imageViewPicture;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.checkin_user_item, null);
			viewHolder = new ItemViewHolder();
			viewHolder.imageViewPicture = (ImageView) convertView.findViewById(R.id.checkin_user_item_xml_imageview_picture);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ItemViewHolder) convertView.getTag();
		}

        viewHolder.imageViewPicture.setLayoutParams(new Gallery.LayoutParams(110, 110));
        viewHolder.imageViewPicture.setScaleType(ImageView.ScaleType.FIT_CENTER);
//        viewHolder.imageViewPicture.setBackgroundResource(background);
        viewHolder.imageViewPicture.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
		imageLoader.displayImage(items.get(position), viewHolder.imageViewPicture);
		return convertView;
	}
}
