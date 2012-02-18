package com.csun.spotr;

import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.csun.spotr.adapter.FriendFeedItemAdapter;
import com.csun.spotr.adapter.ProfileItemAdapter;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.User;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Description:
 * 		Display user's detail information
 */
public class ProfileActivity 
	extends Activity 
		implements IActivityProgressUpdate<FriendFeedItem> {
	
	private static final 	String 					TAG = "(ProfileActivity)";
	private static final 	String 					GET_USER_DETAIL_URL = "http://107.22.209.62/android/get_user_detail.php";
	private static final 	String 					GET_USER_FEEDS = "http://107.22.209.62/android/get_current_user_feeds.php";
	private static final 	String 					GET_FIRST_COMMENT_URL = "http://107.22.209.62/android/get_comment_first.php";
	
	private static final 	int 					CAMERA_PICTURE = 111;
	private static final 	int 					GALLERY_PICTURE = 222;
	
	private 				ListView 				listview;
	private 				FriendFeedItemAdapter   adapter;
	private					List<FriendFeedItem>    feedList;
	private 				Bitmap 					bitmapUserPicture = null;
	private					GetUserDetailTask		task;
	private 				int 					userId = -1;
				
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		feedList = new ArrayList<FriendFeedItem>();
		listview = (ListView) findViewById(R.id.profile_xml_listview_user_feeds);
		adapter = new FriendFeedItemAdapter(this, feedList);
		listview.setAdapter(adapter);
		
		

		task = new GetUserDetailTask(this);
		task.execute();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		ImageView temp = (ImageView) findViewById(R.id.profile_xml_imageview_user_picture);
		if (resultCode == RESULT_OK) {
			if (requestCode == GALLERY_PICTURE) {
				Uri selectedImageUri = data.getData();
				String selectedImagePath = getPath(selectedImageUri);
				bitmapUserPicture = BitmapFactory.decodeFile(selectedImagePath);
				temp.setImageBitmap(bitmapUserPicture);
			}
			else if (requestCode == CAMERA_PICTURE) {
				if (data.getExtras() != null) {
					// here is the image from camera
					bitmapUserPicture = (Bitmap) data.getExtras().get("data");
					temp.setImageBitmap(bitmapUserPicture);
				}
			}
		}
	}

	private void startDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Upload Pictures Option");
		myAlertDialog.setMessage("How do you want to set your picture?");
		myAlertDialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PICTURE);
			}
		});

		myAlertDialog.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(intent, CAMERA_PICTURE);
			}
		});
		myAlertDialog.show();
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private static class GetUserDetailTask 
		extends AsyncTask<Void, FriendFeedItem, User> 
			implements IAsyncTask<ProfileActivity> {
		
		private WeakReference<ProfileActivity> ref;
		
		public GetUserDetailTask(ProfileActivity a) {
			attach(a);
		}
		
		@Override
    	protected void onProgressUpdate(FriendFeedItem... f) {
			Log.v(TAG, "Did you go here?");
    		ref.get().updateAsyncTaskProgress(f[0]);
        }

		@Override
		protected User doInBackground(Void...voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			
			// user's detail info
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_DETAIL_URL, data);
			
			// user's feeds
			JSONArray feedArray = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_FEEDS, data);
			
			// comments in user's feed
			JSONArray commentArray;
			
			User user = null;
			try {
				user = new User.Builder( 
						// required parameters
						array.getJSONObject(0).getInt("users_tbl_id"), 
						array.getJSONObject(0).getString("users_tbl_username"), 
						array.getJSONObject(0).getString("users_tbl_password"))
							// optional parameters
							.challengesDone(array.getJSONObject(0).getInt("users_tbl_challenges_done"))
							.placesVisited(array.getJSONObject(0).getInt("users_tbl_places_visited"))
							.points(array.getJSONObject(0).getInt("users_tbl_points"))
							.imageUrl(array.getJSONObject(0).getString("users_tbl_user_image_url"))
								.build();
				
				if (isCancelled()) {
					return user;
				}
				
				if (feedArray != null) {
    				for (int i = 0; i < feedArray.length(); ++i) { 
        				String snapPictureUrl = null;
        				String userPictureUrl = null;
        				String shareUrl = null;
        				
        				if (Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.SNAP_PICTURE) {
        					snapPictureUrl = feedArray.getJSONObject(i).getString("activity_tbl_snap_picture_url");
        				}
        				
        				if(feedArray.getJSONObject(i).getString("users_tbl_user_image_url").equals("") == false) {
        					userPictureUrl = feedArray.getJSONObject(i).getString("users_tbl_user_image_url");
        				}
        				
        				if(feedArray.getJSONObject(i).has("activity_tbl_share_url") && !feedArray.getJSONObject(i).getString("activity_tbl_share_url").equals("null")) {
        					shareUrl = feedArray.getJSONObject(i).getString("activity_tbl_share_url");
        				}
        				else {
        					shareUrl = "";
        				}
        				
        				FriendFeedItem ffi = 
        					new FriendFeedItem.Builder(
        							// required parameters
        							feedArray.getJSONObject(i).getInt("activity_tbl_id"),
        							0, // not used
        							feedArray.getJSONObject(i).getString("users_tbl_username"),
        							Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")),
        							feedArray.getJSONObject(i).getString("activity_tbl_created"),
        							feedArray.getJSONObject(i).getString("spots_tbl_name"))
        								// optional parameters
        								.challengeName(feedArray.getJSONObject(i).getString("challenges_tbl_name"))
        								.challengeDescription(feedArray.getJSONObject(i).getString("challenges_tbl_description"))
        								.activitySnapPictureUrl(snapPictureUrl)
        								.friendPictureUrl(userPictureUrl)
        								.activityComment(feedArray.getJSONObject(i).getString("activity_tbl_comment"))
        								.shareUrl(shareUrl)
        								.numberOfComments(feedArray.getJSONObject(i).getInt("activity_tbl_total_comments"))
        								.likes(feedArray.getJSONObject(i).getInt("activity_tbl_likes"))
        									.build();
        				
        				
        				data.clear();
        				data.add(new BasicNameValuePair("activity_id", Integer.toString(ffi.getActivityId())));
        				commentArray = JsonHelper.getJsonArrayFromUrlWithData(GET_FIRST_COMMENT_URL, data);
        				
        				Comment firstComment = new Comment(-1, "", "", "", "");
        				
        				if (commentArray != null) {
        					firstComment.setId(commentArray.getJSONObject(0).getInt("comments_tbl_id"));
        					firstComment.setUsername(commentArray.getJSONObject(0).getString("users_tbl_username"));
        					firstComment.setPictureUrl(commentArray.getJSONObject(0).getString("users_tbl_user_image_url"));
        					firstComment.setTime(commentArray.getJSONObject(0).getString("comments_tbl_time"));
        					firstComment.setContent(commentArray.getJSONObject(0).getString("comments_tbl_content"));
        				}
        				
        				ffi.setFirstComment(firstComment);
        				publishProgress(ffi);
    				}
				}
				
			}
			catch (JSONException e) {
				Log.e(TAG + "GetUserDetailTask.doInBackground() : ", "JSON error parsing data" + e.toString());
			}
			return user;
		}
		
		@Override
		protected void onPostExecute(final User u) {
			if (u != null) {
				ref.get().updateUserView(u);
			}
			detach();
		}

		public void attach(ProfileActivity a) {
			ref = new WeakReference<ProfileActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.all_menu, menu);
		return true;
	}

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case R.id.options_menu_xml_item_setting_icon:
				intent = new Intent("com.csun.spotr.SettingsActivity");
				startActivity(intent);
				finish();
				break;
			case R.id.options_menu_xml_item_logout_icon:
				SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();
				intent = new Intent("com.csun.spotr.LoginActivity");
				startActivity(intent);
				finish();
				break;
			case R.id.options_menu_xml_item_mainmenu_icon:
				intent = new Intent("com.csun.spotr.MainMenuActivity");
				startActivity(intent);
				finish();
				break;
		}
		return true;
	}
	
	@Override
    public void onPause() {
		Log.v(TAG, "I'm paused!");
        super.onPause();
	}
	
	@Override
    public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
	
		if (bitmapUserPicture != null) {
			bitmapUserPicture.recycle();
			bitmapUserPicture = null;
		}
		
        super.onDestroy();
	}

	public void updateAsyncTaskProgress(FriendFeedItem f) {
		feedList.add(f);
		adapter.notifyDataSetChanged();
	}
	
	public void updateUserView(User u) {
		ImageLoader imageLoader = new ImageLoader(getApplicationContext());
		
		ImageView imageViewUserPicture = (ImageView) findViewById(R.id.profile_xml_imageview_user_picture);
		imageLoader.displayImage(u.getImageUrl(), imageViewUserPicture);
		
		imageViewUserPicture.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startDialog();
			}
		});
		
		TextView textViewChallengesDone = (TextView) findViewById(R.id.profile_xml_textview_challenges_done);
		textViewChallengesDone.setText(Integer.toString(u.getChallengesDone()));
		
		TextView textViewPlacesVisited = (TextView) findViewById(R.id.profile_xml_textview_places_visited);
		textViewPlacesVisited.setText(Integer.toString(u.getPlacesVisited()));
		
		TextView textViewPoints = (TextView) findViewById(R.id.profile_xml_textview_points);
		textViewPoints.setText(Integer.toString(u.getPoints()));
	}
}