package com.csun.spotr.adapter;

import java.util.List;

import com.csun.spotr.core.Comment;
import com.csun.spotr.R;
import com.csun.spotr.util.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentItemAdapter extends BaseAdapter {
	private Context context;
	private List<Comment> items;
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	private ItemViewHolder holder;

	public CommentItemAdapter(Context context, List<Comment> items) {
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
		TextView textViewUsername;
		ImageView imageViewPicture;
		TextView textViewTime;
		TextView textViewContent;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.comment_item, null);
			holder = new ItemViewHolder();
			holder.textViewUsername = (TextView) convertView.findViewById(R.id.comment_item_xml_textview_post_comments_user_name);
			holder.imageViewPicture = (ImageView) convertView.findViewById(R.id.comment_item_xml_textview_post_comments_user_picture);
			holder.textViewTime = (TextView) convertView.findViewById(R.id.comment_item_xml_textview_post_comments_time);
			holder.textViewContent = (TextView) convertView.findViewById(R.id.comment_item_xml_textview_post_comments_content);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}

		holder.textViewUsername.setText(items.get(position).getUsername());
		holder.textViewTime.setText(items.get(position).getTime());
		holder.textViewContent.setText(items.get(position).getContent());
		imageLoader.displayImage(items.get(position).getPictureUrl(), holder.imageViewPicture);
		
		return convertView;
	}
}