package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.core.PowerUp;
import com.csun.spotr.core.Weapon;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WeaponAdapter extends BaseAdapter {
	private List<Weapon> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public WeaponAdapter(Context context, List<Weapon> items) {
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
		TextView textViewTitle;
		TextView textViewPower;
		TextView textViewTimesLeft;
		ImageView imageViewIcon;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.weapon_item, null);
			holder = new ItemViewHolder();
			holder.textViewTitle = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_title);
			holder.textViewTimesLeft = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_times_left);
			holder.textViewPower = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_power);
			holder.imageViewIcon = (ImageView) convertView.findViewById(R.id.weapon_item_xml_imageview_icon);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		switch (items.get(position).getId()) {
			case PowerUp.BONUS :
				holder.textViewTitle.setText("BONUS");
				holder.imageViewIcon.setImageResource(R.drawable.pu_bonus);
				break;
			case PowerUp.TELESCOPE :
				holder.textViewTitle.setText("TELESCOPE");
				holder.imageViewIcon.setImageResource(R.drawable.pu_telescope);
				break;
			case PowerUp.TELEPORT :
				holder.textViewTitle.setText("TELEPORT");
				holder.imageViewIcon.setImageResource(R.drawable.pu_teleport);
				break;
			case PowerUp.PRAYER :
				holder.textViewTitle.setText("PRAYER");
				holder.imageViewIcon.setImageResource(R.drawable.pu_prayer);
				break;
			case PowerUp.LUCK :
				holder.textViewTitle.setText("LUCK");
				holder.imageViewIcon.setImageResource(R.drawable.pu_luck);
				break;
			case PowerUp.SNEAK_PEEK :
				holder.textViewTitle.setText("SNEAK PEEK");
				holder.imageViewIcon.setImageResource(R.drawable.pu_sneak_peek);
				break;
			case PowerUp.SHORTCUT :
				holder.textViewTitle.setText("SHORTCUT");
				holder.imageViewIcon.setImageResource(R.drawable.pu_shortcut);
				break;
			case PowerUp.LOAN :
				holder.textViewTitle.setText("LOAN");
				holder.imageViewIcon.setImageResource(R.drawable.pu_loan);
				break;
		}

		holder.textViewPower.setText(Double.toString(items.get(position).getPercent()));
		holder.textViewTimesLeft.setText(Integer.toString(items.get(position).getNumUses()));
		return convertView;
	}
}