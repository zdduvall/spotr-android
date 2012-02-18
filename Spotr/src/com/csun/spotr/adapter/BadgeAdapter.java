package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.adapter.FinderItemAdapter.ItemViewHolder;
import com.csun.spotr.core.Badge;
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class BadgeAdapter extends BaseAdapter {
	private Context context;
	private List<Badge> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder viewHolder;
	
	public BadgeAdapter(Context c, List<Badge> items) {
		this.context = c;
		this.items = items;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(context.getApplicationContext());
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
			convertView = inflater.inflate(R.layout.badge_item, null);
			viewHolder = new ItemViewHolder();
			viewHolder.imageViewPicture = (ImageView) convertView.findViewById(R.id.badge_item_xml_imageview_badge);
			viewHolder.imageViewPicture.setLayoutParams(new GridView.LayoutParams(100, 100));
            viewHolder.imageViewPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            viewHolder.imageViewPicture.setPadding(8, 8, 8, 8);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ItemViewHolder) convertView.getTag();
		}

		imageLoader.displayImage(items.get(position).getUrl(), viewHolder.imageViewPicture);
		return convertView;
	}
}
