package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import com.csun.spotr.core.Comment;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;
import com.csun.spotr.adapter.CommentItemAdapter;

/*
 * Description:
 * 		Write a comment on an Activity wall
 */
public class CommentActivity 
	extends Activity 
		implements IActivityProgressUpdate<Comment> {

	private static final String TAG = "(CommentActivity)";
	
	private static final String POST_COMMENT_URL = "http://107.22.209.62/android/beta_do_post_comment.php";
	private static final String GET_COMMENTS_URL = "http://107.22.209.62/android/get_comments.php";
	// Commented out original to work with beta which also updates the user_requests table - ED
	//private static final String POST_COMMENT_URL = "http://107.22.209.62/android/do_post_comment.php";

	private ListView listview = null;
	private CommentItemAdapter adapter = null;
	private List<Comment> commentList = new ArrayList<Comment>();
	private int activityId;
	private int userId;
	public GetCommentTask task = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment);

		Bundle extrasBundle = getIntent().getExtras();
		activityId = extrasBundle.getInt("activity_id");
		userId = CurrentUser.getCurrentUser().getId();

		final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		final Button buttonPost = (Button) findViewById(R.id.comment_xml_button_post);
		final EditText edittextComment = (EditText) findViewById(R.id.comment_xml_edittext_user_comment);
		listview = (ListView) findViewById(R.id.comment_xml_listview);
		
		adapter = new CommentItemAdapter(this.getApplicationContext(), commentList);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		});
		
		task = new GetCommentTask(this);
		task.execute(activityId);

		buttonPost.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!edittextComment.getText().toString().equals("")) {
					String comment = edittextComment.getText().toString();
					new PostCommentTask(CommentActivity.this, userId, activityId, comment).execute();
					imm.hideSoftInputFromWindow(edittextComment.getWindowToken(), 0);
				}
				else {
					Toast.makeText(getApplicationContext(), "Comments can't be empty!", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private static class GetCommentTask 
		extends AsyncTask<Integer, Comment, Boolean> 
			implements IAsyncTask<CommentActivity> {

		private WeakReference<CommentActivity> ref;

		public GetCommentTask(CommentActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected void onProgressUpdate(Comment... u) {
			ref.get().updateAsyncTaskProgress(u[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... ids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("activity_id", ids[0].toString()));
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_COMMENTS_URL, data);

			if (array != null) {
				try {
					if (ref.get().task.isCancelled()) {
						return true;
					}
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new Comment(
								array.getJSONObject(i).getInt("comments_tbl_id"), 
								array.getJSONObject(i).getString("users_tbl_username"), 
								array.getJSONObject(i).getString("users_tbl_user_image_url"), 
								array.getJSONObject(i).getString("comments_tbl_time"), 
								array.getJSONObject(i).getString("comments_tbl_content")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetCommentTask.doInBackGround(Integer... ids) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			else {
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			detach();
		}

		public void attach(CommentActivity a) {
			ref = new WeakReference<CommentActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	private static class PostCommentTask 
		extends AsyncTask<Void, Comment, Boolean> 
			implements IAsyncTask<CommentActivity> {

		private WeakReference<CommentActivity> ref;
		private int userId;
		private int activityId;
		private String comment;

		public PostCommentTask(CommentActivity a, int userId, int activityId, String comment) {
			this.userId = userId;
			this.activityId = activityId;
			this.comment = comment;
			attach(a);
		}

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Boolean doInBackground(Void... voids) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("users_id", Integer.toString(userId)));
			data.add(new BasicNameValuePair("activity_id", Integer.toString(activityId)));
			data.add(new BasicNameValuePair("comment", comment));
			
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(POST_COMMENT_URL, data);
			String result = "";
			try {
				result = json.getString("result");
				if (result.equals("success"))
					return true;
			} 
			catch (JSONException e) {
				Log.e(TAG + "PostCommentTask.doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				Toast.makeText(ref.get().getApplicationContext(), "Comment posted!", Toast.LENGTH_LONG).show();
				ref.get().resetListViewData();
				new GetCommentTask(ref.get()).execute(activityId);
			}
		}

		public void attach(CommentActivity a) {
			ref = new WeakReference<CommentActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	public void updateAsyncTaskProgress(Comment c) {
		commentList.add(c);
		adapter.notifyDataSetChanged();
	}
	
	public void resetListViewData() {
		commentList.clear();
		adapter.notifyDataSetChanged();
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
	
	@Override
	public void onPause() {
		Log.v(TAG,"I'm paused");
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG,"I'm destroyed");
		super.onPause();
	}
}