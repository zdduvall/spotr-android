package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChooseUserItemAdapter extends BaseAdapter {
	private Context context;
	private List<UserItem> items;
	private static LayoutInflater inflater = null;
	private ItemViewHolder holder;

	public ChooseUserItemAdapter(Context context, List<UserItem> items) {
		this.context = context.getApplicationContext();
		this.items = items;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.choose_user_item, null);
			holder = new ItemViewHolder();
			holder.textViewName = (TextView) convertView.findViewById(R.id.choose_user_item_xml_textview_username);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewName.setText(items.get(position).getUsername());
		return convertView;
	}
}