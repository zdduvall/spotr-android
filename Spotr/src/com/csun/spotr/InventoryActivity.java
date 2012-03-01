package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import com.csun.spotr.adapter.WeaponAdapter;
import com.csun.spotr.core.Weapon;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

/**
 * Description: Display user's weapons
 */
public class InventoryActivity 
	extends BasicSpotrActivity 
		implements IActivityProgressUpdate<Weapon> {

	private static final String TAG = "(WeaponActivity)";
	private static final String GET_WEAPON_URL = "http://107.22.209.62/android/get_weapons.php";

	private ListView listview;
	private WeaponAdapter adapter;
	private List<Weapon> weaponList = new ArrayList<Weapon>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.weapon);
//		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);

		listview = (ListView) findViewById(R.id.weapon_xml_listview_weapons);
		adapter = new WeaponAdapter(this, weaponList);
		listview.setAdapter(adapter);
		
		listview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showDialog(0);
			}
		});
		

		new GetWeaponTask(this).execute();
	}
	
//	/**
//	 * Open the Main Menu activity (dashboard). If that activity is already
//	 * running, a new instance of that activity will not be launched--instead,
//	 * all activities on top of the old instance are removed as the old 
//	 * instance is brought to the top.
//	 * @param button the button clicked
//	 */
//	public void goToMainMenu(View button) {
//	    final Intent intent = new Intent(this, MainMenuActivity.class);
//	    intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP);
//	    startActivity (intent);
//	}

	private static class GetWeaponTask 
		extends AsyncTask<Integer, Weapon, Boolean> 
			implements IAsyncTask<InventoryActivity> {

		private List<NameValuePair> clientData = new ArrayList<NameValuePair>();
		private WeakReference<InventoryActivity> ref;
		private JSONArray array;

		public GetWeaponTask(InventoryActivity a) {
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
			// send user id
			clientData.add(new BasicNameValuePair("id", Integer.toString(CurrentUser.getCurrentUser().getId())));
			// retrieve data from server
			array = JsonHelper.getJsonArrayFromUrlWithData(GET_WEAPON_URL, clientData);
			if (array != null) {
				try {
					for (int i = 0; i < array.length(); ++i) {
						publishProgress(
							new Weapon(
								array.getJSONObject(i).getInt("weapon_tbl_id"), 
								array.getJSONObject(i).getDouble("weapon_tbl_percent"), 
								array.getJSONObject(i).getInt("weapon_tbl_num_uses")));

					}
				}
				catch (JSONException e) {
					Log.e(TAG + "GetWeaponTask.doInBackGround(Integer... offsets) : ", "JSON error parsing data" + e.toString());
				}
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}

		public void attach(InventoryActivity a) {
			ref = new WeakReference<InventoryActivity>(a);
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
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Choose an option").setCancelable(true).setPositiveButton("Apply", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		}).setNegativeButton("View", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

			}
		});
		return builder.create();
	}
}
