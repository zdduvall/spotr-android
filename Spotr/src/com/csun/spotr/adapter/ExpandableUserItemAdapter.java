package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.R;
import com.csun.spotr.adapter.UserItemAdapter.ItemViewHolder;
import com.csun.spotr.core.adapter_item.UserItem;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ExpandableUserItemAdapter extends BaseExpandableListAdapter {
	private static LayoutInflater inflater;
	
	private Context context;
	private List<UserItem> items;
	private String[] contents;
	private ItemViewHolder holder;
	private ChildViewHolder holderChild;
	
	public ImageLoader imageLoader;
	
	public ExpandableUserItemAdapter(Context context, List<UserItem> items) {
		this.context = context.getApplicationContext();
		this.items = items;
		contents = new String[2];
		contents[0] = "Send a message";
		contents[1] = "View profile";
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(context.getApplicationContext());
	}

	public Object getChild(int groupPosition, int childPosition) {
		return contents;
	}

	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return 0;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = (LinearLayout) inflater.inflate(R.layout.friend_action, null);
			holderChild = new ChildViewHolder();
			holderChild.btnMessage = (Button) convertView.findViewById(R.id.friend_action);
			holderChild.btnView = (Button) convertView.findViewById(R.id.friend_message);
			convertView.setTag(holderChild);
		}
		else {
			holderChild = (ChildViewHolder) convertView.getTag();
		}
		holderChild.btnMessage.setText(contents[0]);
		holderChild.btnView.setText(contents[1]);
		
		return convertView;
	}

	public int getChildrenCount(int groupPosition) {
		return 1;
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
		if (items.get(groupPosition) == null)
			System.out.println("WUT");
		if (items.get(groupPosition).getPictureUrl() == null)
			System.out.println("LSKD");
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.user_item, null);
			holder = new ItemViewHolder();
			holder.textViewName = (TextView) convertView.findViewById(R.id.user_item_xml_textview_name);
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.user_item_xml_imageview_picture);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewName.setText(items.get(groupPosition).getUsername());
		imageLoader.displayImage(items.get(groupPosition).getPictureUrl(), holder.imageViewPicture);		
		
		return convertView;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static class ItemViewHolder {
		TextView textViewName;
		ImageView imageViewPicture;
	}
	
	public static class ChildViewHolder {
		Button btnMessage;
		Button btnView;
	}
}
