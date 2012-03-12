package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.core.Event;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EventAdapter extends BaseAdapter {
	private Context context;
	private List<Event> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;
	
	public EventAdapter(Context c, List<Event> items) {
		context = c;
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
		TextView textViewContext;
		ImageView imageViewPicture;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.event_item, null);
			holder = new ItemViewHolder();
			holder.textViewName = (TextView) convertView.findViewById(R.id.event_item_xml_textview_name);
			holder.textViewContext = (TextView) convertView.findViewById(R.id.event_item_xml_textview_context);
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.event_item_xml_imageview_picture);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewName.setText(items.get(position).getName());
		holder.textViewContext.setText(items.get(position).getContext());
		
		// we will have a set of 356 days of a year
		holder.imageViewPicture.setImageResource(R.drawable.event_april_26);
		return convertView;
	}
}
