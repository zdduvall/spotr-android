package com.csun.spotr.adapter;

import java.util.ArrayList;
import java.util.List;

import com.csun.spotr.core.adapter_item.PlaceItem;
import com.csun.spotr.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class PlaceItemAdapter extends BaseAdapter {
	private Context context;
	private List<PlaceItem> items;
	private List<PlaceItem> origin;
	private static LayoutInflater inflater;
	private ItemViewHolder holder;
	
	public PlaceItemAdapter(Context context, List<PlaceItem> items) {
		super();
		this.context = context.getApplicationContext();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		this.origin = items;
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
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.place_item, null);
			holder = new ItemViewHolder();
			holder.nameTextView = (TextView) convertView.findViewById(R.id.place_item_xml_textview_name);
			holder.typesTextView = (TextView) convertView.findViewById(R.id.place_item_xml_textview_address);

			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.nameTextView.setText(items.get(position).getName());
		holder.typesTextView.setText(items.get(position).getAddress());		
		return convertView;
	}
	
	/**
	 * NOTE: THIS IS A TEMPORARY IMPLEMENTATION
	 * Formats an address string to have line breaks. 
	 * @param address the string retrieved from the database
	 * @return a nicely formatted string
	 */
	private String formatAddress(String address) {
		String[] parts = address.split("\\, ");
		
		// Check that we get 4 substrings:
		//	   1) street
		//     2) city
		//     3) state and zip
		//     4) country --> ignored
		// Otherwise, return the address unchanged. 
		if (parts.length == 4) {
			address = "";			
			String[] stateAndZip = parts[2].split("\\ ");
			address = parts[0] + "\n"
					+ parts[1] + ", " + stateAndZip[0] + ", " + stateAndZip[1];		
		}
		return address;
	}
	
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<PlaceItem>) results.values;
				PlaceItemAdapter.this.notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				List<PlaceItem> filteredResults = null;
				
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
			
			private List<PlaceItem> getFilterList(CharSequence constraint) {
				List<PlaceItem> data = new ArrayList<PlaceItem>();
				String criteria = constraint.toString().toLowerCase();
				for (PlaceItem p : origin) { 
					if (p.getName().toLowerCase().contains(criteria))
						data.add(p);
				}
				return data;
			}
		};
	}
	
	public PlaceItem getCurrentItem(int position) {
		return new ArrayList<PlaceItem>(items).get(position);
	}
}