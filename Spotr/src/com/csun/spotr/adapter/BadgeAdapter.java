package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.adapter.FinderItemAdapter.ItemViewHolder;
import com.csun.spotr.core.Badge;
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

public class BadgeAdapter extends BaseAdapter {
	private Context context;
	private List<Badge> items;
	private static LayoutInflater inflater = null;
	private ItemViewHolder holder;
	
	public BadgeAdapter(Context c, List<Badge> items) {
		this.context = c;
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
		ImageView imageViewPicture;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.badge_item, null);
			holder = new ItemViewHolder();
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.badge_item_xml_imageview_badge);
			holder.imageViewPicture.setLayoutParams(new GridView.LayoutParams(100, 100));
            holder.imageViewPicture.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.imageViewPicture.setPadding(8, 8, 8, 8);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}
		
		holder.imageViewPicture.setImageResource(getImageResourceId(items.get(position).getId()));
		return convertView;
	}
	
	private int getImageResourceId(int badgeId) {
		int id = 1;
		switch (badgeId) {
		case Badge.BADGE_100_POINTS:
			id = R.drawable.ic_badges_1;
			break;
			
		case Badge.BADGE_1000_POINTS:
			id = R.drawable.ic_badges_2;
			break;
			
		case Badge.BADGE_10000_POINTS:
			id = R.drawable.ic_badges_3;
			break;
			
		case Badge.BADGE_FIRST_CHECK_IN:
			id = R.drawable.ic_badges_4;
			break;
			
		case Badge.BADGE_FIRST_QUEST:
			id = R.drawable.ic_badges_5;
			break;
			
		case Badge.BADGE_NEWBIE:
			id = R.drawable.ic_badges_6;
			break;
			
		case Badge.BADGE_TRAVELER:
			id = R.drawable.ic_badges_7;
			break;
			
		case Badge.BADGE_EXPLORER:
			id = R.drawable.ic_badges_8;
			break;
			
		case Badge.BADGE_ADVENTURE:
			id = R.drawable.ic_badges_9;
			break;
			
		case Badge.BADGE_POWER_ATHELETE:
			id = R.drawable.ic_badges_10;
			break;
			
		case Badge.BADGE_BOOK_WORM:
			id = R.drawable.ic_badges_11;
			break;
			
		case Badge.BADGE_COFFEE_AFICIANADO:
			id = R.drawable.ic_badges_12;
			break;
			
		case Badge.BADGE_MR_PHOTOGRAPHER:
			id = R.drawable.ic_badges_13;
			break;
			
		case Badge.BADGE_NIGHT_OWL:
			id = R.drawable.ic_badges_14;
			break;
			
		case Badge.BADGE_JAVA_LEGENDRY:
		 	id = R.drawable.ic_badges_15;
			break;
		}
		return id;
	}
	
}
