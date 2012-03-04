package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.core.adapter_item.FriendRequestItem;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FriendRequestItemAdapter extends BaseAdapter {
	private Context context;
	private List<FriendRequestItem> items;
	private static LayoutInflater inflater;

	public FriendRequestItemAdapter(Context context, List<FriendRequestItem> items) {
		this.context = context.getApplicationContext();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
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
		TextView textViewOrder;
		TextView textViewMessage;
		TextView textViewTime;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.friend_request_item, null);
			holder = new ItemViewHolder();
			holder.textViewOrder = (TextView) convertView.findViewById(R.id.friend_request_item_xml_textview_order);
			holder.textViewMessage = (TextView) convertView.findViewById(R.id.friend_request_item_xml_textview_message);
			holder.textViewTime = (TextView) convertView.findViewById(R.id.friend_request_item_xml_textview_time);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}
		holder.textViewOrder.setText(items.get(position).getFriendName());
		if(items.get(position).getType() == 1)
		{
			holder.textViewMessage.setText(" has sent you a request \"" + items.get(position).getMessage() + "\"");
		}
		else if(items.get(position).getType() == 2)
		{
			holder.textViewMessage.setText(" has replied to your comment.");
		}
		else //curently assumed reward
			holder.textViewMessage.setText(" REWARD TIME!");
		holder.textViewTime.setText(items.get(position).getTime());
		
		return convertView;
	}
}