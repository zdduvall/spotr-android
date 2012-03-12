package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.LeaderboardActivity;
import com.csun.spotr.core.User;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.JsonHelper;

public class GetUsersTask 
	extends AsyncTask<Void, User, Boolean> 
		implements IAsyncTask<LeaderboardActivity> {

	private static final String TAG = "[AsyncTask].GetUsersTask";
	private static final String GET_USERS_URL = "http://107.22.209.62/android/get_users.php";
	private ProgressDialog progressDialog = null;
	private WeakReference<LeaderboardActivity> ref;
	int position = 0;

	public GetUsersTask(LeaderboardActivity a) {
		attach(a);
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

	@Override
	protected void onProgressUpdate(User... users) {
		progressDialog.dismiss();
		ref.get().updateAsyncTaskProgress(users[0]);
	}

	@Override
	protected Boolean doInBackground(Void... voids) {
		JSONArray array = JsonHelper.getJsonArrayFromUrl(GET_USERS_URL);
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					// update user's location
					if (array.getJSONObject(i).getInt("users_tbl_id") == CurrentUser.getCurrentUser().getId()) {
						position = i;
					}

					publishProgress(
						new User.Builder(
							// required parameters
							array.getJSONObject(i).getInt("users_tbl_id"), 
							array.getJSONObject(i).getString("users_tbl_username"), 
							array.getJSONObject(i).getString("users_tbl_password"))
					
								// optional parameters
								.challengesDone(array.getJSONObject(i).getInt("users_tbl_challenges_done"))
								.placesVisited(array.getJSONObject(i).getInt("users_tbl_places_visited"))
								.points(array.getJSONObject(i).getInt("users_tbl_points"))
								.rank(array.getJSONObject(i).getInt("users_tbl_rank"))
									.build());
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data", e);
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		ref.get().updateRank(position);
		detach();
	}

	public void attach(LeaderboardActivity a) {
		ref = new WeakReference<LeaderboardActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}