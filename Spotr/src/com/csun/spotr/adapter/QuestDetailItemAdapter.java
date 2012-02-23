package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.core.adapter_item.QuestDetailItem;
import com.csun.spotr.util.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QuestDetailItemAdapter extends BaseAdapter {
	private Context context;
	private List<QuestDetailItem> items;
	private ItemViewHolder holder;
	private static LayoutInflater inflater;
	public ImageLoader imageLoader;

	public QuestDetailItemAdapter(Context context, List<QuestDetailItem> items) {
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

	public class ItemViewHolder {
		LinearLayout layout;
		TextView nameTextView;
		//TextView descriptionTextView;
		ImageView imageView;
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.quest_item, null);
			holder = new ItemViewHolder();
			holder.layout = (LinearLayout) convertView.findViewById(R.id.quest_item_xml_linearlayout);
			holder.nameTextView = (TextView) convertView.findViewById(R.id.quest_item_xml_textview_name);
			holder.imageView = (ImageView) convertView.findViewById(R.id.quest_item_xml_image);
			//holder.descriptionTextView = (TextView) convertView.findViewById(R.id.quest_item_xml_textview_description);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.nameTextView.setText(items.get(position).getName());
	//	imageLoader.displayImage(items.get(position).getUrl(), holder.imageView);
		if (items.get(position).getStatus().equals("done")) {
			holder.nameTextView.setTextColor(Color.parseColor("#dd3c10"));
			holder.layout.setClickable(true);
		}
		else {
			holder.nameTextView.setTextColor(Color.parseColor("#3b5998"));
			holder.layout.setClickable(false);
		}

		//holder.descriptionTextView.setText(items.get(position).getDescription());

		return convertView;
	}
}
