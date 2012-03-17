package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.core.adapter_item.QuestItem;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class QuestItemAdapter extends BaseAdapter {
	private Context context;
	private List<QuestItem> items;
	private static LayoutInflater inflater=null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public QuestItemAdapter(Context context, List<QuestItem> items) {
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
		TextView nameTextView;
		TextView typesTextView;
		ImageView imageViewPicture;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.quest_item,null);
			holder = new ItemViewHolder();
			holder.nameTextView = (TextView) convertView.findViewById(R.id.quest_item_xml_textview_name);
			holder.typesTextView = (TextView) convertView.findViewById(R.id.quest_item_xml_textview_description);
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.quest_item_xml_image);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
			//holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.quest_item_xml_image);
		}
		holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.quest_item_xml_image);
		holder.nameTextView.setText(items.get(position).getName());
		holder.typesTextView.setText(items.get(position).getDescription());
		if (holder.imageViewPicture != null)
			{
				imageLoader.displayImage(items.get(position).getUrl(), holder.imageViewPicture);
			}
		return convertView;
	}
}