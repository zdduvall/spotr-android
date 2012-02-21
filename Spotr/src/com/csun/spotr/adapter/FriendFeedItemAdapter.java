package com.csun.spotr.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.csun.spotr.CommentActivity;
import com.csun.spotr.LocalMapViewActivity;
import com.csun.spotr.LoginActivity;
import com.csun.spotr.R;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

public class FriendFeedItemAdapter extends BaseAdapter {
	private static final String TAG = "(FriendFeedItemAdapter)";
	private List<FriendFeedItem> items;
	private static LayoutInflater inflater;
	public ImageLoader imageLoader;
	private Context context;
	private ItemViewHolder holder;
	private boolean me;

	private static final String LIKE_ACTIVITY_URL = "http://107.22.209.62/android/do_like_activity.php";
	
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
		TextView textViewTotalComments;
		WebView webview;
		
		/*
		 * 1st comment
		 */
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
			holder.textViewTotalComments = (TextView) convertView.findViewById(R.id.friend_list_feed_item_xml_textview_number_of_comments);
			
			/*
			 * 1st comment
			 */
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
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtras(extras);
				context.startActivity(intent);
			}
		});
		
		holder.buttonLike.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new LikeActivityTask(FriendFeedItemAdapter.this).execute(items.get(position).getActivityId());
				items.get(position).setLikes(items.get(position).getLikes() + 1);
				notifyDataSetChanged();
			}
		});
		
		holder.buttonLike.setText("Like + " + Integer.toString(items.get(position).getLikes()));
		holder.textViewTotalComments.setText(Integer.toString(items.get(position).getNumberOfComments()) + " comments.");
		
		Comment c = items.get(position).getFirstComment(); 
		if (c.getId() != 1) {
			holder.firstLayout.setVisibility(View.VISIBLE);
			holder.firstImageViewUserPicture.setVisibility(View.VISIBLE);
			holder.firstTextViewUsername.setVisibility(View.VISIBLE);
			holder.firstTextViewTime.setVisibility(View.VISIBLE);
			holder.firstTextViewContent.setVisibility(View.VISIBLE);
			
			imageLoader.displayImage(c.getPictureUrl(), holder.firstImageViewUserPicture);
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
			holder.buttonComment.setEnabled(false);
			holder.buttonLike.setEnabled(false);
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
	
		private WeakReference<FriendFeedItemAdapter> ref;
		
	
		public LikeActivityTask(FriendFeedItemAdapter a) {
			attach(a);
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected Boolean doInBackground(Integer... activity) {
			List<NameValuePair> datas = new ArrayList<NameValuePair>();
			datas.add(new BasicNameValuePair("activityId", activity[0].toString() ));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(LIKE_ACTIVITY_URL, datas);
			String result = "";
			try {
				result = json.getString("result");
				if (result.equals("success"))
					return true;
			}
			catch (Exception e) {
				Log.e(TAG + "LikeActivityTask.doInBackground(Void... voids)", "JSON error parsing data" + e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
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