package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.core.adapter_item.FriendRequestItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
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

	
	/*  Reason Comment:
	 *  Removed if/else ladder. Part of NOTIFICATION FEED
	 *   
	 *  Date commented out: March 10, 2012
	 *  Commenter:	Edgardo A. Campos
	 *  
	 *  WHAT WAS COMMENTED OUT:	
	 *  
	 *  if/else ladder. items.get(position).getType() returns an Integer value. Notification list
	 *  message would have been patterned to the notification type. Strings are placeholders and can change at any time
	 */	
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
		holder.textViewMessage.setText(" has sent you a request \"" + items.get(position).getMessage() + "\"");
		holder.textViewTime.setText(items.get(position).getTime());
		return convertView;
	}
}