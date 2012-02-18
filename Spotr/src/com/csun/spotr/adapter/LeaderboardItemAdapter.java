package com.csun.spotr.adapter;

import java.util.List;
import java.util.zip.Inflater;

import com.csun.spotr.R;
import com.csun.spotr.core.User;
import com.csun.spotr.singleton.CurrentUser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class LeaderboardItemAdapter extends BaseAdapter {
	private Context context;
	private List<User> items;
	private static LayoutInflater inflater;
	private ItemViewHolder holder;
	private boolean isFound;
	private int positionFound;
	
	public LeaderboardItemAdapter(Context c, List<User> items) {
		super();
		this.context = c.getApplicationContext();
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		isFound = false;
		positionFound = 0;
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
	
	public void setPositionFound(int position) {
		isFound = true;
		positionFound = position;
	}

	private static class ItemViewHolder {
		TextView textViewRank;
		TextView textViewUsername;
		TextView textViewChallengesDone;
		TextView textViewPlacesVisited;
		TextView textViewPoints;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.leaderboard_item, null);
			
			if (items.get(position).getId() == CurrentUser.getCurrentUser().getId()) {
				// add 1 because we wish to set the rank, not the item's index in the list
				CurrentUser.setRank(position + 1);
			}
			
			holder = new ItemViewHolder();
			holder.textViewRank = (TextView) convertView.findViewById(R.id.leaderboard_item_xml_textview_rank);
			holder.textViewUsername = (TextView) convertView.findViewById(R.id.leaderboard_item_xml_textview_username);
			holder.textViewChallengesDone = (TextView) convertView.findViewById(R.id.leaderboard_item_xml_textview_cd);
			holder.textViewPlacesVisited = (TextView) convertView.findViewById(R.id.leaderboard_item_xml_textview_pv);
			holder.textViewPoints = (TextView) convertView.findViewById(R.id.leaderboard_item_xml_textview_pts);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewRank.setText(Integer.toString(items.get(position).getRank()));
		holder.textViewUsername.setText(items.get(position).getUsername());
		holder.textViewChallengesDone.setText(Integer.toString(items.get(position).getChallengesDone()));
		holder.textViewPlacesVisited.setText(Integer.toString(items.get(position).getPlacesVisited()));
		holder.textViewPoints.setText(Integer.toString(items.get(position).getPoints()));
		
		// check if current view belongs to a player's requested view
		if (isFound && position == positionFound) {
			holder.textViewRank.setTextColor(convertView.getResources().getColor(R.color.aluminum6));
			holder.textViewUsername.setTextColor(convertView.getResources().getColor(R.color.aluminum6));
			holder.textViewChallengesDone.setTextColor(convertView.getResources().getColor(R.color.aluminum6));
			holder.textViewPlacesVisited.setTextColor(convertView.getResources().getColor(R.color.aluminum6));
			holder.textViewPoints.setTextColor(convertView.getResources().getColor(R.color.aluminum6));
			holder.textViewRank.setTypeface(null, Typeface.BOLD);
			holder.textViewUsername.setTypeface(null, Typeface.BOLD);
			holder.textViewChallengesDone.setTypeface(null, Typeface.BOLD);
			holder.textViewPlacesVisited.setTypeface(null, Typeface.BOLD);
			holder.textViewPoints.setTypeface(null, Typeface.BOLD);
		}
		else {
			holder.textViewRank.setTextColor(convertView.getResources().getColor(R.color.aluminum4));
			holder.textViewUsername.setTextColor(convertView.getResources().getColor(R.color.aluminum4));
			holder.textViewChallengesDone.setTextColor(convertView.getResources().getColor(R.color.aluminum4));
			holder.textViewPlacesVisited.setTextColor(convertView.getResources().getColor(R.color.aluminum4));
			holder.textViewPoints.setTextColor(convertView.getResources().getColor(R.color.aluminum4));
			holder.textViewRank.setTypeface(null, Typeface.NORMAL);
			holder.textViewUsername.setTypeface(null, Typeface.NORMAL);
			holder.textViewChallengesDone.setTypeface(null, Typeface.NORMAL);
			holder.textViewPlacesVisited.setTypeface(null, Typeface.NORMAL);
			holder.textViewPoints.setTypeface(null, Typeface.NORMAL);
		}
		
		// alternate row color
		if (position % 2 == 0)
			convertView.setBackgroundResource(R.color.aluminum2);
		else
			convertView.setBackgroundResource(R.color.aluminum1);
		return convertView;
	}
}