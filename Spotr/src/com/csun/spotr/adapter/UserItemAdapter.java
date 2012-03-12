package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserItemAdapter extends BaseAdapter {
	private Context context;
	private List<UserItem> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public UserItemAdapter(Context context, List<UserItem> items) {
		this.context = context.getApplicationContext();
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
		TextView textViewName;
		ImageView imageViewPicture;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.user_item, null);
			holder = new ItemViewHolder();
			holder.textViewName = (TextView) convertView.findViewById(R.id.user_item_xml_textview_name);
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.user_item_xml_imageview_picture);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewName.setText(items.get(position).getUsername());
		imageLoader.displayImage(items.get(position).getPictureUrl(), holder.imageViewPicture);
		return convertView;
	}
}