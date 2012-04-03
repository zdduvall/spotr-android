package com.csun.spotr.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import com.csun.spotr.CommentActivity;
import com.csun.spotr.R;
import com.csun.spotr.WebviewActivity;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

public class FriendFeedItemAdapter extends BaseAdapter {
	private static final String TAG = "(FriendFeedItemAdapter)";
	private static final String LIKE_ACTIVITY_URL = "http://107.22.209.62/android/do_like_activity.php";
	private List<FriendFeedItem> items;
	private static LayoutInflater inflater;
	public ImageLoader imageLoader;
	private Context context;
	private ItemViewHolder holder;
	private boolean me;
	
	public FriendFeedItemAdapter(Context context, List<FriendFeedItem> items, boolean me) {
		this.context = context.getApplicationContext();
		this.items = items;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(context.getApplicationContext());
		this.me = me;
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

	public void incrementLike(int position){
		items.get(position).setLikes(items.get(position).getLikes() + 1);
		notifyDataSetChanged();
	}

	public static class ItemViewHolder {
		ImageView imageViewUserPicture;
		TextView textViewUsernameAndContent;
		TextView textViewPlaceName;
		TextView textViewTime;
		TextView textViewDetail;
		ImageView imageViewSnapPicture;
		Button buttonComment;
		Button buttonLike;
		TextView textViewTotalComments;
		TextView textViewWebLink;
		
		String missionDescription;
		
		/**
		 * Treasure section
		 **/
		LinearLayout treasureLayout;
		ImageView imageViewTreasureIcon;
		TextView textViewTreasureCompany;
		
		
		/**
		 * 1st comment
		 **/
		LinearLayout firstLayout;
		ImageView firstImageViewUserPicture;
		TextView firstTextViewUsername;
		TextView firstTextViewTime;
		TextView firstTextViewContent;
	}
	
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.friend_list_feed_item, null);
			holder = new ItemViewHolder();
			holder.textViewUsernameAndContent = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_username_content);
			holder.imageViewUserPicture = (ImageView) convertView.findViewById(R.id.friend_list_feed_item_xml_imageview_user_picture);
			holder.textViewPlaceName = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_placename);
			holder.textViewTime = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_time);
			holder.textViewDetail = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_detail);
			holder.imageViewSnapPicture = (ImageView) convertView.findViewById(R.id.friend_list_feed_item_xml_imageview_snap_picture);
			holder.textViewWebLink = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_weblink);
			holder.buttonComment = (Button) convertView.findViewById(R.id.friend_list_feed_item_xml_button_comment);
			holder.buttonLike = (Button) convertView.findViewById(R.id.friend_list_feed_item_xml_button_like);
			holder.textViewTotalComments = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_number_of_comments);
			
			/**
			 * Treasure section
			 **/
			holder.treasureLayout = (LinearLayout) convertView.findViewById(R.id.friend_list_feed_item_xml_linearlayout_treasure_section);
			holder.imageViewTreasureIcon = (ImageView) convertView.findViewById(R.id.friend_list_feed_item_xml_imageview_treausre_icon);
			holder.textViewTreasureCompany = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_treausre_company);
			
			/**
			 * 1st comment
			 **/
			holder.firstImageViewUserPicture = (ImageView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_1st_picture);
			holder.firstTextViewUsername = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_1st_username);
    		holder.firstTextViewTime = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_1st_time);
    		holder.firstTextViewContent = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_1st_content);
    		holder.firstLayout = (LinearLayout) convertView.findViewById(R.id.friend_list_feed_item_xml_linearlayout_1st_comment);
    		
			convertView.setTag(holder);
		}
		else {
			holder = (ItemViewHolder) convertView.getTag();
		}
		
		imageLoader.displayImageRound(items.get(position).getFriendPictureUrl(), holder.imageViewUserPicture);
		holder.textViewPlaceName.setText("@" + items.get(position).getPlaceName());
		holder.textViewTime.setText(items.get(position).getActivityTime());
		
		/**
		 *  Add share url from user
		 **/
		if (items.get(position).getShareUrl().equals("")) {
			holder.textViewWebLink.setVisibility(View.GONE);
		}
		else {
			holder.textViewWebLink.setVisibility(View.VISIBLE);
			holder.textViewWebLink.setText(items.get(position).getShareUrl());
			holder.textViewWebLink.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Uri uriUrl = Uri.parse(items.get(position).getShareUrl());
					Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);  
					launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(launchBrowser);
				}
			});
		}
		// end 
		
		if (items.get(position).getChallengeType() == Challenge.Type.CHECK_IN) {
			// required view
			holder.missionDescription = "checked in";
			
			// optional view
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.GONE);
			holder.textViewTreasureCompany.setVisibility(View.GONE);
			holder.imageViewTreasureIcon.setVisibility(View.GONE);
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.SNAP_PICTURE) {
			// required view
			holder.missionDescription = "took a picture";
			
			// optional view
			holder.imageViewSnapPicture.setVisibility(View.VISIBLE);
			holder.textViewDetail.setVisibility(View.GONE);
			holder.textViewTreasureCompany.setVisibility(View.GONE);
			holder.imageViewTreasureIcon.setVisibility(View.GONE);
			
			// populate data into optional view
			imageLoader.displayImage(items.get(position).getActivitySnapPictureUrl(), holder.imageViewSnapPicture);
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.WRITE_ON_WALL) {
			// required view
			holder.missionDescription = "wrote";
			
			// optional view
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.VISIBLE);
			holder.textViewTreasureCompany.setVisibility(View.GONE);
			holder.imageViewTreasureIcon.setVisibility(View.GONE);
			
			// populate data into optional view
			holder.textViewDetail.setText(items.get(position).getActivityComment());
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.QUESTION_ANSWER) {
			// required view
			holder.missionDescription = "answered a question";
			
			// optional view
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.VISIBLE);
			holder.textViewTreasureCompany.setVisibility(View.GONE);
			holder.imageViewTreasureIcon.setVisibility(View.GONE);
			
			// populate data into optional view
			holder.textViewDetail.setText(items.get(position).getActivityComment());
		}
		else if (items.get(position).getChallengeType() == Challenge.Type.FIND_TREASURE) {
			// required view
			holder.missionDescription = "found treasure";
			
			// optional view
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.GONE); // no need for additional commentary for this type
			holder.textViewTreasureCompany.setVisibility(View.VISIBLE);
			holder.imageViewTreasureIcon.setVisibility(View.VISIBLE);
			
			// populate data into optional view
			holder.textViewDetail.setText(items.get(position).getActivityComment());
			holder.textViewTreasureCompany.setText(items.get(position).getTreasureCompany());
			imageLoader.displayImage(items.get(position).getTreasureIconUrl(), holder.imageViewTreasureIcon);
		}
		else {
			// required view
			holder.missionDescription = "other";
			
			// optional view
			holder.imageViewSnapPicture.setVisibility(View.GONE);
			holder.textViewDetail.setVisibility(View.GONE);
			holder.textViewTreasureCompany.setVisibility(View.GONE);
			holder.imageViewTreasureIcon.setVisibility(View.GONE);
		}
		
		String username = items.get(position).getFriendName();
		holder.textViewUsernameAndContent.setText(
				Html.fromHtml("<b>" + username + "</b>" + " " + holder.missionDescription));
		
		holder.buttonComment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Bundle extras = new Bundle();
				extras.putInt("activity_id", items.get(position).getActivityId());
				Intent intent = new Intent(context.getApplicationContext(), CommentActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtras(extras);
				context.startActivity(intent);
			}
		});
		
		
		holder.buttonLike.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new LikeActivityTask(FriendFeedItemAdapter.this, position).execute(items.get(position).getActivityId());
			//	items.get(position).setLikes(items.get(position).getLikes() + 1);
			}
		});
		holder.buttonLike.setText("Like +" + Integer.toString(items.get(position).getLikes()));
		int numComments = items.get(position).getNumberOfComments();
		if (numComments > 0) {
			String label = (numComments == 1) ? " comment" : " comments";
			holder.textViewTotalComments.setText(Integer.toString(numComments) + label);
			holder.textViewTotalComments.setVisibility(View.VISIBLE);
		}
		else
			holder.textViewTotalComments.setVisibility(View.GONE);
		
		Comment c = items.get(position).getFirstComment(); 
		if (c.getId() != -1) {
			holder.firstLayout.setVisibility(View.VISIBLE);
			holder.firstImageViewUserPicture.setVisibility(View.VISIBLE);
			holder.firstTextViewUsername.setVisibility(View.VISIBLE);
			holder.firstTextViewTime.setVisibility(View.VISIBLE);
			holder.firstTextViewContent.setVisibility(View.VISIBLE);
			
			imageLoader.displayImageRound(c.getPictureUrl(), holder.firstImageViewUserPicture);
			holder.firstTextViewUsername.setText(c.getUsername());
			holder.firstTextViewTime.setText(c.getTime());
			holder.firstTextViewContent.setText(c.getContent());
		}
		else {
			holder.firstLayout.setVisibility(View.GONE);
			holder.firstImageViewUserPicture.setVisibility(View.GONE);
			holder.firstTextViewUsername.setVisibility(View.GONE);
			holder.firstTextViewTime.setVisibility(View.GONE);
			holder.firstTextViewContent.setVisibility(View.GONE);
		}
		
		if (me) {
			holder.buttonLike.setClickable(false);
		}
		
		return convertView;
	}
	
	private class Helper extends WebViewClient {
		@Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
	        view.loadUrl(url);
	        return true;
	    }
	}
	
	private static class LikeActivityTask
		extends AsyncTask<Integer, Void, Boolean> 
			implements IAsyncTask<FriendFeedItemAdapter> {
		
		int position;
	
		private WeakReference<FriendFeedItemAdapter> ref;
		
		public LikeActivityTask(FriendFeedItemAdapter a, int p) {
			position = p;
			attach(a);
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected Boolean doInBackground(Integer... activity) {
			List<NameValuePair> datas = new ArrayList<NameValuePair>();
			System.out.println(activity[0]);
			datas.add(new BasicNameValuePair("activityId", activity[0].toString() ));
			datas.add(new BasicNameValuePair("usersId", Integer.toString(CurrentUser.getCurrentUser().getId()) ));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(LIKE_ACTIVITY_URL, datas);
			String result = "";
			try {
				result = json.getString("result");
				if (result.equals("success")){
					return true;
				}
			}
			catch (Exception e) {
				Log.e(TAG + "LikeActivityTask.doInBackground(Void... voids)", "JSON error parsing data", e );
			}
			return false;			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(result)
				ref.get().incrementLike(position);
			detach();
		}

		public void attach(FriendFeedItemAdapter a) {
			ref = new WeakReference<FriendFeedItemAdapter>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
}