package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.csun.spotr.adapter.FriendFeedItemAdapter;
import com.csun.spotr.asynctask.GetUserDetailTask;
import com.csun.spotr.asynctask.GetUserFeedTask;
import com.csun.spotr.core.Challenge;
import com.csun.spotr.core.Comment;
import com.csun.spotr.core.User;
import com.csun.spotr.core.adapter_item.FriendFeedItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.DialogId;
import com.csun.spotr.util.ImageLoader;
import com.csun.spotr.util.JsonHelper;

/**
 * Description:
 * 		Display user's detail information
 */
public class ProfileActivity 
	extends Activity 
		implements IActivityProgressUpdate<FriendFeedItem> {
	
	private static final String TAG = "(ProfileActivity)";
	public static final int DIALOG_ID_LOADING = 2;
	private static final int INTENT_RESULT_EDIT_PROFILE = 1;
	
	private ListView 				listview;
	private FriendFeedItemAdapter   adapter;
	private	List<FriendFeedItem>    feedList;
	private Bitmap 					bitmapUserPicture = null;
	private	GetUserDetailTask		task;
	private int 					userId = -1;
		
	private	String					imageLocation;
	
	private String realname = "n/a";
	private String education = "n/a";
	private String hometown = "n/a";
	private String hobbies = "n/a";
	
	private boolean canEditProfile;
				
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getParent() == null) // use custom title bar only if not in a tabhost
			requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	
		setContentView(R.layout.profile);
		if (getParent() == null)
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar_basic);
		
		
		Bundle extrasBundle = getIntent().getExtras();
		userId = extrasBundle.getInt("user_id");
				
		feedList = new ArrayList<FriendFeedItem>();
		listview = (ListView) findViewById(R.id.profile_xml_listview_user_feeds);
		
		//determine if like button can be clicked
		if(userId == CurrentUser.getCurrentUser().getId())
			adapter = new FriendFeedItemAdapter(this, feedList, true);
		else if(userId != CurrentUser.getCurrentUser().getId())
			adapter = new FriendFeedItemAdapter(this, feedList, false);
		listview.setAdapter(adapter);
		listview.setOnScrollListener(new FeedOnScrollListener());
				
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
	
		// wait for user's data available 
		canEditProfile = false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == INTENT_RESULT_EDIT_PROFILE) {
				Bundle bundle = data.getExtras();
				String imageUrl = bundle.getString("user_image_url"); 
				String username = bundle.getString("username");
				realname = bundle.getString("name");
				ImageView imageViewUserPicture = (ImageView) findViewById(R.id.profile_xml_imageview_user_picture);
				ImageLoader imageLoader = new ImageLoader(getApplicationContext());
				imageLoader.displayImage(imageUrl, imageViewUserPicture);
				TextView textViewName = (TextView) findViewById(R.id.profile_xml_textview_profilename);
				textViewName.setText(username);
				updateFeedListView(username, imageUrl);
			}
		}
	}
	
	private void updateFeedListView(String name, String imageUrl) {
		for (int i = 0; i < feedList.size(); ++i) {
			feedList.get(i).setFriendName(name);
			feedList.get(i).setFriendPictureUrl(imageUrl); 
		}
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DialogId.ID_LOADING) {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage("Loading...");
			pd.setIndeterminate(true);
			pd.setCancelable(false);
			return pd;
		}
		return null;
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
				
		// now user can edit his/her profile
		if(userId != CurrentUser.getCurrentUser().getId())
			canEditProfile = false;
		else
			canEditProfile = true;

		realname = u.getRealname();
		education = u.getEducation();
		hometown = u.getHometown();
		hobbies = u.getHobbies();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.profile_menu, menu);
		
		// can't edit profile until user data has been retrieved
		if (canEditProfile)
			menu.getItem(0).setEnabled(false);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.profile_menu_xml_edit_profile:
			Bundle extras = new Bundle();
			extras.putInt("user_id", CurrentUser.getCurrentUser().getId());
			extras.putString("email", CurrentUser.getCurrentUser().getUsername());
			extras.putString("password", CurrentUser.getCurrentUser().getPassword());
			extras.putString("imageUrl", imageLocation);
			extras.putString("name", realname);
			extras.putString("education", education);
			extras.putString("hometown", hometown);
			extras.putString("hobbies", hobbies);
			intent = new Intent("com.csun.spotr.ProfileEditActivity");
			intent.putExtras(extras);
			startActivityForResult(intent, INTENT_RESULT_EDIT_PROFILE);
		}
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (canEditProfile)
			menu.getItem(0).setEnabled(true);
		else
			menu.getItem(0).setEnabled(false);
		return true;
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
	
	@Override 
	public void onResume() {
		Log.v(TAG, "I'm resumed");
		super.onResume();
	}
	
	@Override
	public void onRestart() {
		Log.v(TAG, "I'm restarted!");
		super.onRestart();
	}

	@Override
	public void onStop() {
		Log.v(TAG, "I'm stopped!");
		super.onStop();
	}
}