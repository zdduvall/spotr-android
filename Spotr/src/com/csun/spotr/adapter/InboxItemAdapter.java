package com.csun.spotr.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.csun.spotr.R;
import com.csun.spotr.adapter.CommentItemAdapter.ItemViewHolder;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.Inbox;
import com.csun.spotr.util.ImageLoader;

public class InboxItemAdapter extends BaseAdapter {
	private Context context;
	private List<Inbox> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public InboxItemAdapter(Context context, List<Inbox> items) {
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
		LinearLayout layoutItem;
		TextView textViewUsername;
		ImageView imageViewPicture;
		TextView textViewTime;
		TextView textViewMessage;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.inbox_item, null);
			holder = new ItemViewHolder();
			holder.layoutItem = (LinearLayout) convertView.findViewById(R.id.inbox_item_xml_linearlayout);
			holder.textViewUsername = (TextView) convertView.findViewById(R.id.inbox_item_xml_textview_name);
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.inbox_item_xml_imageview_user_picture);
			holder.textViewTime = (TextView) convertView.findViewById(R.id.inbox_item_xml_textview_time);
			holder.textViewMessage = (TextView) convertView.findViewById(R.id.inbox_item_xml_textview_message);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewUsername.setText(items.get(position).getUsername());
		holder.textViewTime.setText(items.get(position).getTime());
		holder.textViewMessage.setText(items.get(position).getMessage());
		imageLoader.displayImage(items.get(position).getUserPictureUrl(), holder.imageViewPicture);
		
		if (items.get(position).isNew() == 0) {
			holder.layoutItem.setBackgroundColor(Color.GRAY);
			Log.v("Hello:", "GRAY");
		}
		else {
			holder.layoutItem.setBackgroundColor(Color.parseColor("#f7f7f7"));
			Log.v("Hello:", "Normal");
		}
		
		return convertView;
	}
}