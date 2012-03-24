package com.csun.spotr.adapter;

import java.util.ArrayList;
import java.util.List;

import com.csun.spotr.core.PowerUp;
import com.csun.spotr.core.Weapon;
import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class WeaponAdapter extends BaseAdapter {
	private List<Weapon> items;
	private List<Weapon> origin;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public WeaponAdapter(Context context, List<Weapon> items) {
		this.items = items;
		this.origin = items;
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
		TextView textViewDescription;
		//TextView textViewPower;
		//TextView textViewTimesLeft;
		ImageView imageViewIcon;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.weapon_item, null);
			holder = new ItemViewHolder();
			holder.textViewTitle = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_title);
			holder.textViewDescription = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_description);
			//holder.textViewTimesLeft = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_times_left);
			//holder.textViewPower = (TextView) convertView.findViewById(R.id.weapon_item_xml_textview_power);
			holder.imageViewIcon = (ImageView) convertView.findViewById(R.id.weapon_item_xml_imageview_icon);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		/*
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
		//*/
		holder.textViewTitle.setText(items.get(position).getName());
		holder.textViewDescription.setText(items.get(position).getDescription());
		//holder.imageViewIcon.setImageResource(R.drawable.pu_shortcut);
		imageLoader.displayImage(items.get(position).getImageUrl(), holder.imageViewIcon);

		//holder.textViewPower.setText(Double.toString(items.get(position).getPercent()));
		//holder.textViewTimesLeft.setText(Integer.toString(items.get(position).getNumUses()));
		return convertView;
	}

	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<Weapon>) results.values;
				WeaponAdapter.this.notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				List<Weapon> filteredResults = null;
				
				// use original data
				if (constraint.toString().equals("")) {
					filteredResults = origin;
				}
				else {
					filteredResults = getFilterList(constraint);
				}

				results.values = filteredResults;
				return results;
			}
			
			private List<Weapon> getFilterList(CharSequence constraint) {
				List<Weapon> data = new ArrayList<Weapon>();
				for (Weapon w : origin) { 
					if (Integer.toString(w.getId()).contains(constraint))
						data.add(w);
				}
				return data;
			}
		};
	}
}