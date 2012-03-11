package com.csun.spotr.adapter;

import java.util.ArrayList;
import java.util.List;

import com.csun.spotr.ProfileActivity;
import com.csun.spotr.R;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is similar to the old UserItemAdapter, except now, we also take into
 * account children. In this case, the children are all identical, with the 
 * same view configurations and text. Furthermore, each group, i.e., friend, 
 * has only one child.
 * @author Chris
 *
 */
public class ExpandableUserItemAdapter extends BaseExpandableListAdapter {
	private static LayoutInflater inflater;
	
	private Context context;
	private List<UserItem> items;
	private List<UserItem> origin;
	private String[] contents;
	private GroupViewHolder holderGroup;
	private ChildViewHolder holderChild;
	private ImageLoader imageLoader;
	
	public ExpandableUserItemAdapter(Context context, List<UserItem> items) {
		this.context = context.getApplicationContext();
		this.items = items;
		this.origin = items;
		contents = new String[2];
		contents[0] = "Send a message";
		contents[1] = "View profile";
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(context.getApplicationContext());
	}

	public Object getChild(int groupPosition, int childPosition) {
		return contents; // always the same in every case
	}

	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.friend_actions, null);
			holderChild = new ChildViewHolder();
			holderChild.btnSendMessage = (Button) convertView.findViewById(R.id.friend_btn_send_message);
			holderChild.btnViewProfile = (Button) convertView.findViewById(R.id.friend_btn_view_profile);
			convertView.setTag(holderChild);
		}
		else {
			holderChild = (ChildViewHolder) convertView.getTag();
		}
		setupChildButtons(groupPosition);
		
		return convertView;
	}
	
	/**
	 * Set up button labels and listeners.
	 * @param groupPosition the child's parent position
	 */
	private void setupChildButtons(int groupPosition) {
		holderChild.btnSendMessage.setTag(groupPosition);
		holderChild.btnViewProfile.setTag(groupPosition);
		
		holderChild.btnSendMessage.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Set up messaging friends
			}
			
		});
		
		holderChild.btnViewProfile.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int groupPosition = (Integer) holderChild.btnSendMessage.getTag();
				UserItem friend = items.get(groupPosition);
				Bundle extras = new Bundle();
				extras.putInt("user_id", friend.getId());
				
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtras(extras);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
			
		});	
	}

	public int getChildrenCount(int groupPosition) {
		return 1; // each friend will have only 1 child
	}

	public Object getGroup(int groupPosition) {
		return items.get(groupPosition);
	}

	public int getGroupCount() {
		return items.size();
	}

	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.user_item, null);
			holderGroup = new GroupViewHolder();
			holderGroup.textViewName = (TextView) convertView.findViewById(R.id.user_item_xml_textview_name);
			holderGroup.imageViewPicture = (ImageView) convertView.findViewById(R.id.user_item_xml_imageview_picture);
			convertView.setTag(holderGroup);
		}
		else {
			holderGroup = (GroupViewHolder) convertView.getTag();
		}
		holderGroup.textViewName.setText(items.get(groupPosition).getUsername());
		imageLoader.displayImage(items.get(groupPosition).getPictureUrl(), holderGroup.imageViewPicture);		
		
		return convertView;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}
	
	private static class GroupViewHolder {
		TextView textViewName;
		ImageView imageViewPicture;
	}
	
	public static class ChildViewHolder {
		Button btnSendMessage;
		Button btnViewProfile;
	}
	
	public Filter getFilter() {
		return new Filter() {
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				items = (List<UserItem>) results.values;
				ExpandableUserItemAdapter.this.notifyDataSetChanged();
			}

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				List<UserItem> filteredResults = null;
				
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
			
			private List<UserItem> getFilterList(CharSequence constraint) {
				String criteria = constraint.toString().toLowerCase();
				List<UserItem> data = new ArrayList<UserItem>();
				for (UserItem u : origin) { 
					if (u.getUsername().toLowerCase().contains(criteria))
						data.add(u);
				}
				return data;
			}
		};
	}
}
