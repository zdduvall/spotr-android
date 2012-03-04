package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.core.Treasure;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TreasureAdapter extends BaseAdapter {
	private Context context;
	private List<Treasure> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public TreasureAdapter(Context context, List<Treasure> items) {
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
		TextView name;
		ImageView icon;
		TextView company;
		TextView expirationDate;
		TextView barcode;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.treasure_item, null);
			holder = new ItemViewHolder();
			
			holder.name = (TextView) convertView.findViewById(R.id.treasure_xml_textview_name);
			holder.icon = (ImageView) convertView.findViewById(R.id.treasure_xml_imageview_icon);
			holder.company = (TextView) convertView.findViewById(R.id.treasure_xml_textview_company);
			holder.expirationDate = (TextView) convertView.findViewById(R.id.treasure_xml_textview_expiration_date);
			holder.barcode = (TextView) convertView.findViewById(R.id.treasure_xml_textview_barcode);
			
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.name.setText(items.get(position).getName());
		imageLoader.displayImage(items.get(position).getIconUrl(), holder.icon);
		holder.company.setText(items.get(position).getCompany());
		holder.expirationDate.setText(items.get(position).getExpirationDate());
		holder.barcode.setText(items.get(position).getCode());
			
		return convertView;
	}
}