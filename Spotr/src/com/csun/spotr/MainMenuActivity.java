package com.csun.spotr;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.csun.spotr.adapter.FriendRequestItemAdapter;
import com.csun.spotr.core.adapter_item.FriendRequestItem;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IActivityProgressUpdate;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

/**
 * Description:
 * 		Main menu
 */

public class MainMenuActivity 
	extends BasicSpotrActivity {
	
	private static final String TAG = "(MainMenuActivity)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_menu_original);
		setupTitleBar();
	}
	
	@Override
	protected void setupTitleBar() {
		super.setupTitleBar();
		ImageView homeBeacon = (ImageView) findViewById(R.id.title_bar_home_beacon);
		homeBeacon.setVisibility(View.INVISIBLE);
		
		LinearLayout homeContainer = (LinearLayout) findViewById(R.id.title_bar_home_container);
		homeContainer.setClickable(false);
	}
		
	public void getActivity(View mainMenuButton) {
		int id =  ((Button) mainMenuButton).getId();
		Intent intent;
		if (id == R.id.main_menu_btn_me) {
			Bundle extras = new Bundle();
			extras.putInt("user_id", CurrentUser.getCurrentUser().getId());
			intent = new Intent(getApplicationContext(), ProfileMainActivity.class);
			intent.putExtras(extras);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_map) {
			intent = new Intent(getApplicationContext(), LocalMapViewActivity.class);
			startActivity(intent);
		}
		/**
		 * NOTE: Due to deadline, and uploading image issue, 
		 * we decided to temporarily remove this feature
		 *  
		 * @author chan
		 * @date 04/02/2012
		 */
		// else if (id == R.id.main_menu_btn_spot_it) {
		//	intent = new Intent(getApplicationContext(), FinderMainActivity.class);
		//	intent.putExtra("user_id", CurrentUser.getCurrentUser().getId());
		//	startActivity(intent);
		// }
		else if (id == R.id.main_menu_btn_spots) {
			intent = new Intent(getApplicationContext(), PlaceActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_friends) {
			intent = new Intent(getApplicationContext(), FriendListMainActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_quests) {
			intent = new Intent(getApplicationContext(), QuestActivity.class);
			startActivity(intent);
		}
		else if (id == R.id.main_menu_btn_inventory) {
			intent = new Intent(getApplicationContext(), InventoryActivity.class);
			startActivity(intent);   
		}
		else if (id == R.id.main_menu_btn_inbox) {
			intent = new Intent(getApplicationContext(), InboxActivity.class);
			startActivity(intent);   
		}
		else if (id == R.id.main_menu_btn_friend_request) {
			intent = new Intent(getApplicationContext(), FriendRequestActivity.class);
			startActivity(intent);   
		}
		else {
			// should never go here
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.options_menu_xml_item_setting_icon :
			intent = new Intent("com.csun.spotr.SettingsActivity");
			startActivity(intent);
			break;
		case R.id.options_menu_xml_item_logout_icon :
			SharedPreferences.Editor editor = getSharedPreferences("Spotr", MODE_PRIVATE).edit();
			editor.clear();
			intent = new Intent("com.csun.spotr.LoginActivity");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		}
		return true;
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			super.finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}