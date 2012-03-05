package com.csun.spotr;

import java.io.ByteArrayOutputStream;
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
import com.csun.spotr.singleton.CurrentDateTime;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.Base64;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.util.UploadFileHelper;

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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

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
	private static final    String					UPDATE_PICTURE_URL = "http://107.22.209.62/images/upload_user_picture.php";
	
	private static final 	int 					CAMERA_PICTURE = 111;
	private static final 	int 					GALLERY_PICTURE = 222;
	
	private 				ListView 				listview;
	private 				FriendFeedItemAdapter   adapter;
	private					List<FriendFeedItem>    feedList;
	private 				Bitmap 					bitmapUserPicture = null;
	private					GetUserDetailTask		task;
	private 				int 					userId = -1;
	
	private					Button					editButton;
	
	private					OnClickListener			friendsClick;
	private					OnClickListener			badgeClick;
	private					String					imageLocation;
				
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		
		editButton = (Button) findViewById(R.id.profile_xml_button_edit);
		
		// Views created to make entire area clickable
		View friendsButton1 = (View) findViewById(R.id.profile_xml_friends_1);
		View friendsButton2 = (View) findViewById(R.id.profile_xml_friends_2);
		View friendsButton3 = (View) findViewById(R.id.profile_xml_friends_3);
		View badgeButton1 = (View) findViewById(R.id.profile_xml_badges_1);
		View badgeButton2 = (View) findViewById(R.id.profile_xml_badges_2);
		View badgeButton3 = (View) findViewById(R.id.profile_xml_badges_3);
		
		feedList = new ArrayList<FriendFeedItem>();
		listview = (ListView) findViewById(R.id.profile_xml_listview_user_feeds);
		adapter = new FriendFeedItemAdapter(this, feedList, true);
		listview.setAdapter(adapter);
		listview.setOnScrollListener(new FeedOnScrollListener());
		
		Bundle extrasBundle = getIntent().getExtras();
		userId = extrasBundle.getInt("user_id");

		// get user detail task for top portion
		if (userId != -1) {
			task = new GetUserDetailTask(this, userId);
			task.execute();
		}
		
		// run another task to display user's feeds
		new GetUserFeedTask(this, userId, 0).execute();
		
		ImageView imageViewUserPicture = (ImageView) findViewById(R.id.profile_xml_imageview_user_picture);
		imageViewUserPicture.setClickable(false);
		if(imageViewUserPicture.isClickable())
			imageViewUserPicture.setClickable(false);
	
		editButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Intent intent;
				Bundle extras = new Bundle();
				extras.putInt("user_id", CurrentUser.getCurrentUser().getId());
				extras.putString("email", CurrentUser.getCurrentUser().getUsername());
				extras.putString("password", CurrentUser.getCurrentUser().getPassword());
				extras.putString("imageUrl", imageLocation);
				intent = new Intent("com.csun.spotr.ProfileEditActivity");
				intent.putExtras(extras);
				startActivity(intent);
				finish();
			}
		});
		

		friendsClick = (new OnClickListener() {
			public void onClick(View view) {
				Intent intent;
				intent = new Intent(getApplicationContext(), FriendListMainActivity.class);
				startActivity(intent);
			}
		});
		
		friendsButton1.setOnClickListener(friendsClick);
		friendsButton2.setOnClickListener(friendsClick);
		friendsButton3.setOnClickListener(friendsClick);
		
		badgeClick = (new OnClickListener() {
			public void onClick(View view) {
				Intent intent;
				intent = new Intent(getApplicationContext(), RewardActivity.class);
				startActivity(intent);
				//finish();
			}
		});
		
		badgeButton1.setOnClickListener(badgeClick);
		badgeButton2.setOnClickListener(badgeClick);
		badgeButton3.setOnClickListener(badgeClick);
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
			
			// create byte stream array
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			
			// compress picture and add to stream (PNG)
			bitmapUserPicture.compress(Bitmap.CompressFormat.JPEG, 70, stream);
			
			// create raw data src
			byte[] src = stream.toByteArray();
			
			// encode it
			String byteCode = Base64.encodeBytes(src);
			
			UploadPictueTask task = new UploadPictueTask(this, byteCode);
			task.execute();
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
		private int userId;
		
		public GetUserDetailTask(ProfileActivity a, int userId) {
			this.userId = userId;
			attach(a);
		}
		
		@Override
    	protected void onProgressUpdate(FriendFeedItem... f) {
    		ref.get().updateAsyncTaskProgress(f[0]);
        }

		@Override
		protected User doInBackground(Void...voids) {
			if (isCancelled()) {
				return null;
			}
			
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			
			// user's detail info
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_DETAIL_URL, data);
			
			if (isCancelled()) {
				return null;
			}
				
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
							.numFriends(array.getJSONObject(0).getInt("num_friends"))
							.numBadges(array.getJSONObject(0).getInt("num_badges"))
								.build();
				
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
	
	private static class UploadPictueTask 
    	extends AsyncTask<Void, Integer, String> 
    		implements IAsyncTask<ProfileActivity> {
    	
    	private WeakReference<ProfileActivity> ref;
    	private String picturebyteCode;
    	
    	public UploadPictueTask(ProfileActivity a, String pbc) {
    		attach(a);
    		picturebyteCode = pbc;
    	}
    	
    	@Override
    	protected void onPreExecute() {
    		
    	}
    
    	@Override
    	protected String doInBackground(Void... voids) {
    		List<NameValuePair> datas = new ArrayList<NameValuePair>();
    		// send encoded data to server
    		datas.add(new BasicNameValuePair("image", picturebyteCode));
    		// send a file name where file name = "username" + "current date time UTC", to make sure that we have a unique id picture every time.
    		// since the username is unique, we should take advantage of this otherwise two or more users could potentially snap pictures at the same time.
    		datas.add(new BasicNameValuePair("file_name",  CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png"));
    		
    		// send the rest of data
    		datas.add(new BasicNameValuePair("users_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
    		
    		
    		// get JSON to check result
    		JSONObject json = UploadFileHelper.uploadFileToServer(UPDATE_PICTURE_URL, datas);
    		String result = "";
    		try {
    			result = json.getString("result");
    		} 
    		catch (JSONException e) {
    			Log.e(TAG + "UploadPictueTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
    		}
    		return result;
    	}
    	
    	@Override
    	protected void onPostExecute(String result) {
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
		inflater.inflate(R.menu.profile_setting_menu, menu);
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
			case R.id.profile_setting_menu_xml_edit:
				intent = new Intent("com.csun.spotr.ProfileEditActivity");
				Bundle extras = new Bundle();
				extras.putInt("user_id", CurrentUser.getCurrentUser().getId());
				extras.putString("email", CurrentUser.getCurrentUser().getUsername());
				extras.putString("password", CurrentUser.getCurrentUser().getPassword());
				extras.putString("imageUrl", imageLocation);
				intent = new Intent("com.csun.spotr.ProfileEditActivity");
				intent.putExtras(extras);
				startActivity(intent);
				finish();
				break;
		}
		return true;
	}
	
	private static class GetUserFeedTask 
		extends AsyncTask<Void, FriendFeedItem, Boolean> 
			implements IAsyncTask<ProfileActivity> {
		
		private WeakReference<ProfileActivity> ref;
		private int userId;
		private int offset;
		
		public GetUserFeedTask(ProfileActivity a, int userId, int offset) {
			this.userId = userId;
			this.offset = offset;
			attach(a);
		}
		
		@Override
    	protected void onProgressUpdate(FriendFeedItem... f) {
    		ref.get().updateAsyncTaskProgress(f[0]);
        }

		@Override
		protected Boolean doInBackground(Void...voids) {
			if (isCancelled()) {
				return false;
			}
			
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("user_id", Integer.toString(userId)));
			data.add(new BasicNameValuePair("offset", Integer.toString(offset)));
			
			// user's feeds
			JSONArray feedArray = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_FEEDS, data);
			
			// comments in user's feed
			JSONArray commentArray;
			
			try {
				if (isCancelled()) {
					return false;
				}
				
				if (feedArray != null) {
    				for (int i = 0; i < feedArray.length(); ++i) { 
    					
    					String snapPictureUrl = "";
    					String userPictureUrl = "";
    					String shareUrl = "";
    					String treasureIconUrl = "";
    					String company = "";
    					
    					if (Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.SNAP_PICTURE) {
    						snapPictureUrl = feedArray.getJSONObject(i).getString("activity_tbl_snap_picture_url");
    					}
    					
    					if (Challenge.returnType(feedArray.getJSONObject(i).getString("challenges_tbl_type")) == Challenge.Type.FIND_TREASURE) {
    						treasureIconUrl = feedArray.getJSONObject(i).getString("activity_tbl_treasure_icon_url");
    						company = feedArray.getJSONObject(i).getString("activity_tbl_treasure_company");
    					}
    					
    					if (feedArray.getJSONObject(i).getString("users_tbl_user_image_url").equals("") == false) {
    						userPictureUrl = feedArray.getJSONObject(i).getString("users_tbl_user_image_url");
    					}
    					
    					if (feedArray.getJSONObject(i).has("activity_tbl_share_url") && !feedArray.getJSONObject(i).getString("activity_tbl_share_url").equals("null")) {
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
        								.treasureIconUrl(treasureIconUrl)
    									.treasureCompany(company)
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
				Log.e(TAG + "GetUserFeedTask.doInBackground() : ", "JSON error parsing data" + e.toString());
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
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
		imageLocation = u.getImageUrl();
		
		TextView textViewName = (TextView) findViewById(R.id.profile_xml_textview_profilename);
		textViewName.setText(u.getUsername());
		
		TextView textViewChallengesDone = (TextView) findViewById(R.id.profile_xml_textview_challenges_done);
		textViewChallengesDone.setText(Integer.toString(u.getChallengesDone()));
		
		TextView textViewPlacesVisited = (TextView) findViewById(R.id.profile_xml_textview_places_visited);
		textViewPlacesVisited.setText(Integer.toString(u.getPlacesVisited()));
		
		TextView textViewPoints = (TextView) findViewById(R.id.profile_xml_textview_points);
		textViewPoints.setText(Integer.toString(u.getPoints()));
		
		TextView textViewNumFriends = (TextView) findViewById(R.id.profile_xml_textview_numfriends);
		textViewNumFriends.setText(Integer.toString(u.getNumFriends()));
		
		TextView textViewNumBadges = (TextView) findViewById(R.id.profile_xml_textview_numrewards);
		textViewNumBadges.setText(Integer.toString(u.getNumBadges()));
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			task.cancel(true);
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public class FeedOnScrollListener implements OnScrollListener {
	    private int visibleThreshold = 5;
	    private int currentPage = 0;
	    private int previousTotal = 0;
	    private boolean loading = true;
	 
	    public FeedOnScrollListener() {
	    	
	    }
	    
	    public FeedOnScrollListener(int visibleThreshold) {
	        this.visibleThreshold = visibleThreshold;
	    }
	 
	    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	        if (loading) {
	            if (totalItemCount > previousTotal) {
	                loading = false;
	                previousTotal = totalItemCount;
	                currentPage += 5;
	            }
	        }
	        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
	            new GetUserFeedTask(ProfileActivity.this, userId, currentPage).execute();
	            loading = true;
	        }
	    }
	 
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
	    	// TODO : not use
	    }
	}
}