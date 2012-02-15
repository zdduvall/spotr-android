package com.csun.spotr.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.csun.spotr.CommentActivity;
import com.csun.spotr.R;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.util.ImageLoader;

public class FriendFeedItemAdapter extends BaseAdapter {
	private List<FriendFeedItem> items;
	private static LayoutInflater inflater;
	public ImageLoader imageLoader;
	private Context context;
	private ItemViewHolder holder;
	
	public FriendFeedItemAdapter(Context context, List<FriendFeedItem> items) {
		this.context = context;
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
		ImageView imageViewUserPicture;
		TextView textViewUsername;
		TextView textViewPlaceName;
		TextView textViewTime;
		TextView textViewContent;
		TextView textViewDetail;
		ImageView imageViewSnapPicture;
		Button buttonComment;
		Button buttonLike;
		WebView webview;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.friend_list_feed_item, null);
			holder = new ItemViewHolder();
			holder.textViewUsername = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_username);
			holder.imageViewUserPicture = (ImageView) convertView.findViewById(R.id.friend_list_feed_item_xml_imageview_user_picture);
			holder.textViewPlaceName = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_placename);
			holder.textViewTime = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_time);
			holder.textViewContent = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_content);
			holder.textViewDetail = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_detail);
			holder.imageViewSnapPicture = (ImageView) convertView.findViewById(R.id.friend_list_feed_item_xml_imageview_snap_picture);
			holder.webview = (WebView) convertView.findViewById(R.id.friend_list_feed_item_xml_webview);
			holder.buttonComment = (Button) convertView.findViewById(R.id.friend_list_feed_item_xml_button_comment);
			holder.buttonLike = (Button) convertView.findViewById(R.id.friend_list_feed_item_xml_button_like);
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}
		
		imageLoader.displayImage(items.get(position).getFriendPictureUrl(), holder.imageViewUserPicture);
		holder.textViewUsername.setText(items.get(position).getFriendName());
		holder.textViewPlaceName.setText("@ " + items.get(position).getPlaceName());
		holder.textViewTime.setText("about " + items.get(position).getActivityTime());
		
		
		
		if (items.get(position).getShareUrl().equals("")) {
			holder.webview.setVisibility(View.GONE);
		}
		else {
			holder.webview.setVisibility(View.VISIBLE);
			WebSettings webSettings = holder.webview.getSettings();
			webSettings.setJavaScriptEnabled(true);
			webSettings.setPluginsEnabled(true);
			webSettings.setDefaultZoom(ZoomDensity.FAR);
			webSettings.setBuiltInZoomControls(true);
			holder.webview.setWebViewClient(new Helper());
			holder.webview.loadUrl(items.get(position).getShareUrl());
		}
		
		if (items.get(position).getChallengeType() == Challenge.Type.CHECK_IN) {
			holder.textViewContent.setText("check-in");
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.GONE);
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.SNAP_PICTURE) {
			holder.textViewContent.setText("snap-picture");
			holder.imageViewSnapPicture.setVisibility(View.VISIBLE);
			imageLoader.displayImage(items.get(position).getActivitySnapPictureUrl(), holder.imageViewSnapPicture);
			holder.textViewDetail.setVisibility(View.GONE);
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.WRITE_ON_WALL) {
			holder.textViewContent.setText("write-on-wall");
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.VISIBLE);
			holder.textViewDetail.setText(items.get(position).getActivityComment());
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.QUESTION_ANSWER) {
			holder.textViewContent.setText("answer-question");
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.VISIBLE);
			holder.textViewDetail.setText(items.get(position).getActivityComment());
		}
		else {
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.GONE);
		}
		
		holder.buttonComment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle extras = new Bundle();
				extras.putInt("activity_id", items.get(position).getActivityId());
				Intent intent = new Intent(context.getApplicationContext(), CommentActivity.class);
				intent.putExtras(extras);
				context.startActivity(intent);
			}
		});
		
		holder.buttonLike.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				items.get(position).setLikes(items.get(position).getLikes() + 1);
				notifyDataSetChanged();
			}
		});
		
		holder.buttonLike.setText("Like + " + Integer.toString(items.get(position).getLikes()));
		return convertView;
	}
	
	private class Helper extends WebViewClient {
		@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}
}