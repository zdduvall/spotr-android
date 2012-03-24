package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.csun.spotr.WeaponActivity.GetWeaponTask;
import com.csun.spotr.adapter.WeaponAdapter;
import com.csun.spotr.core.Weapon;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * NOTE: Refactoring by Chan Nguyen: 03/06/2012
 **/

/*
 * Description
 * 		Allows dropping items off at a spot.
 */
public class DropItemActivity 
	extends BasicSpotrActivity
		implements IActivityProgressUpdate<Weapon> {
	
	private static final String TAG = "(DropItemActivity)";
	private static final String GET_WEAPON_URL = "http://107.22.209.62/android/get_weapons.php";
	private static final String DROP_ITEM_URL = "http://107.22.209.62/android/do_drop_loot.php";
	
	private String usersId;
	private String spotsId;
	private String challengesId;
	private String itemId;
	
	private ListView listview;
	private WeaponAdapter adapter;
	private List<Weapon> weaponList = new ArrayList<Weapon>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weapon);
		
		initChallengeInfoFromBundle();
		
		setupListView();
		
		new GetWeaponTask(this).execute();
	}
	
	private void setupListView() {
		listview = (ListView) findViewById(R.id.weapon_xml_listview_weapons);
		adapter = new WeaponAdapter(this, weaponList);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				itemId = Integer.toString(((Weapon)(adapter.getItem(position))).getId());
				showDialog(0);
			}
		});
	}
	
	private void initChallengeInfoFromBundle() {
		Bundle extras = getIntent().getExtras();
		usersId = extras.getString("users_id");
		spotsId = extras.getString("spots_id");
		challengesId = extras.getString("challenges_id");
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Choose an option").setCancelable(true).setPositiveButton("Leave Item", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
					DropItemTask task = new DropItemTask(DropItemActivity.this, usersId, spotsId, challengesId, itemId);
					task.execute();
			}
		}).setNegativeButton("Do nothing", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});
		return builder.create();
	}
	
	private static class DropItemTask 
		extends AsyncTask<Void, Integer, String> 
			implements IAsyncTask<DropItemActivity> {
		
		private WeakReference<DropItemActivity> ref;
		private ProgressDialog progressDialog;
		private String usersId;
		private String spotsId;
		private String challengesId;
		private String itemId;
		
		public DropItemTask(DropItemActivity a, String usersId, String spotsId, String challengesId, String itemId) {
			attach(a);
			this.usersId = usersId;
			this.spotsId = spotsId;
			this.challengesId = challengesId;
			this.itemId = itemId;
		}
		
		@Override
		protected void onPreExecute() {
			// display waiting dialog
			progressDialog = new ProgressDialog(ref.get());
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		private List<NameValuePair> prepareUploadData() {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			data.add(new BasicNameValuePair("users_id", usersId));
			data.add(new BasicNameValuePair("spots_id", spotsId));
			data.add(new BasicNameValuePair("challenges_id", challengesId));
			data.add(new BasicNameValuePair("loot_id", itemId));
			return data;
		}
		
		@Override
		protected String doInBackground(Void... voids) {
			List<NameValuePair> data = prepareUploadData();
			JSONObject json = JsonHelper.getJsonObjectFromUrlWithData(DROP_ITEM_URL, data);
			String result = "";
			try {
				result = json.getString("result");
			} 
			catch (JSONException e) {
				Log.e(TAG + "DropItemTask.doInBackGround(Void ...voids) : ", "JSON error parsing data", e );
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			progressDialog.dismiss();
			if (result.equals("success")) {
				Intent intent = new Intent();
				intent.setData(Uri.parse("done"));
				ref.get().setResult(RESULT_OK, intent);
				ref.get().finish();
			}
			else {
				Log.e(TAG, "unexpected error has occured!");
			}
			
			detach();
		}
		public void attach(DropItemActivity a) {
			ref = new WeakReference<DropItemActivity>(a);
		}
		
		public void detach() {
			ref.clear();
		}
	}
	
	
	private static class GetWeaponTask 
		extends AsyncTask<Integer, Weapon, Boolean> 
			implements IAsyncTask<DropItemActivity> {

		private WeakReference<DropItemActivity> ref;

		public GetWeaponTask(DropItemActivity a) {
			attach(a);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Weapon... w) {
			ref.get().updateAsyncTaskProgress(w[0]);
		}

		@Override
		protected Boolean doInBackground(Integer... offsets) {
			List<NameValuePair> data = new ArrayList<NameValuePair>();
			// send user id
			data.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// retrieve data from server
			JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_WEAPON_URL, data);
		
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new Weapon(
								array.getJSONObject(i).getInt("users_weapon_tbl_weapon_id"), 
								array.getJSONObject(i).getDouble("users_weapon_tbl_percent"), 
								array.getJSONObject(i).getInt("users_weapon_tbl_times_left"),
								array.getJSONObject(i).getString("weapon_tbl_name"),
								array.getJSONObject(i).getString("weapon_tbl_description"),
								array.getJSONObject(i).getString("weapon_tbl_url")));
					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetWeaponTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data", e );
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}

		public void attach(DropItemActivity a) {
			ref = new WeakReference<DropItemActivity>(a);
		}

		public void detach() {
			ref.clear();
		}
	}

	public void updateAsyncTaskProgress(Weapon u) {
		weaponList.add(u);
		adapter.notifyDataSetChanged();
	}
	
	@Override 
	public void onResume() {
		Log.v(TAG, "I'm resumed");
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		Log.v(TAG, "I'm destroyed!");
		super.onDestroy();
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

	@Override
	public void onPause() {
		Log.v(TAG, "I'm paused!");
		super.onPause();
	}
}
