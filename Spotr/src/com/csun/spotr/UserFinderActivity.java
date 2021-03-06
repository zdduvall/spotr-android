package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import com.csun.spotr.adapter.FinderItemAdapter;
import com.csun.spotr.core.adapter_item.SeekingItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class UserFinderActivity extends Activity implements IActivityProgressUpdate<SeekingItem> {
	private static final String TAG = "(UserFinderActivity)";
	private static final String GET_USER_FINDERS_URL = "http://107.22.209.62/android/get_user_finders.php";
	private List<SeekingItem> items;
	private GridView gridview;
	private FinderItemAdapter adapter;
	private Button buttonCreateItem;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
/*		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar); */
		
		setContentView(R.layout.finder);

		buttonCreateItem = (Button) findViewById(R.id.finder_xml_button);
		items = new ArrayList<SeekingItem>();
		gridview = (GridView) findViewById(R.id.finder_xml_gridview);
		adapter = new FinderItemAdapter(this, items);
		gridview.setAdapter(adapter);
		gridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Bundle extras = new Bundle();
				extras.putInt("finder_id", items.get(position).getId());
				Intent intent = new Intent(getApplicationContext(), FinderItemDetailActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
			}
		});
		
		buttonCreateItem.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int dummy = 0;
				Intent intent = new Intent(getApplicationContext(), CreateFinderActivity.class);
				startActivityForResult(intent, dummy);
			}
		});
		
		new GetUserFindersTask(this).execute();
	}

	private static class GetUserFindersTask 
		extends AsyncTask<Integer, SeekingItem, Boolean> 
			implements IAsyncTask<UserFinderActivity> {
		
		private WeakReference<UserFinderActivity> ref;
		private ProgressDialog progressDialog = null;
		private List<NameValuePair> userData = new ArrayList<NameValuePair>();
		
		public GetUserFindersTask(UserFinderActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Loading items...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.show();
			
			userData.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		}

		@Override
		protected void onProgressUpdate(SeekingItem... s) {
			ref.get().updateAsyncTaskProgress(s[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_USER_FINDERS_URL, userData);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new SeekingItem(
								array.getJSONObject(i).getInt("finder_tbl_id"), 
								array.getJSONObject(i).getString("finder_tbl_name"), 
								array.getJSONObject(i).getString("finder_tbl_image_url")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetFindersTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();
			if (result == false) {
				Toast.makeText(ref.get().getApplicationContext(), "No items", Toast.LENGTH_LONG);
			}
			detach();
		}

		public void attach(UserFinderActivity a) {
			ref = new WeakReference<UserFinderActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				items.clear();
				new GetUserFindersTask(this).execute();
			}
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
		super.onDestroy();
	}

	public void updateAsyncTaskProgress(SeekingItem s) {
		items.add(s);
		adapter.notifyDataSetChanged();
	}
}
