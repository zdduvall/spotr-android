package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.FinderFoundActivity;
import com.csun.spotr.singleton.CurrentDateTime;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.UploadFileHelper;

public class FinderFoundTask extends AsyncTask<String, Integer, String> 
implements IAsyncTask<FinderFoundActivity> {
	private static final String TAG = "(FinderFoundTask)";
	private static final String FINDER_FOUND_URL = "http://107.22.209.62/android/do_found_finder.php";
	private List<NameValuePair> finderData = new ArrayList<NameValuePair>();
	private WeakReference<FinderFoundActivity> ref;
	private ProgressDialog progressDialog = null;
	
	public FinderFoundTask(FinderFoundActivity a, int finderId, String src, String comment, double lat, double lon) {
		attach(a);	
		prepareUploadData(finderId, src, comment, lat, lon);
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ref.get());
		progressDialog.setMessage("Loading...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}
	
	private void prepareUploadData(int finderId, String src, String comment, double lat, double lon) {	

		finderData.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
		finderData.add(new BasicNameValuePair("filename", CurrentUser.getCurrentUser().getUsername() + 
				CurrentDateTime.getUTCDateTime().trim() + ".png"));
		finderData.add(new BasicNameValuePair("image", src));
		finderData.add(new BasicNameValuePair("user_id", Integer.toString(CurrentUser.getCurrentUser().getId())));
		finderData.add(new BasicNameValuePair("comment", comment));
		finderData.add(new BasicNameValuePair("lat", Double.toString(lat)));
		finderData.add(new BasicNameValuePair("long", Double.toString(lon)));
	}

	protected String doInBackground(String... params) {	
		JSONObject json = UploadFileHelper.uploadFileToServer(FINDER_FOUND_URL, finderData);
		
		String result = "";
		try {
			result = json.getString("result");
		}
		catch (JSONException e) {
			Log.e(TAG + ".FinderFoundActivity", "JSONFailception", e );
		}

		return result;
	}

	@Override
	protected void onPostExecute(String result) {
		progressDialog.dismiss();
		if (result.equals("success")) {
			AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
			dialogMessage.setTitle("Item marked as found!");
			dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
					+ ", the item's owner has been notified that it was found!");
			
			dialogMessage.setButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent();
					ref.get().setResult(Activity.RESULT_OK, intent);
					ref.get().finish();
				}
			});
			
			dialogMessage.show();
		}
		else {
			AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
			dialogMessage.setTitle("Submission error");
			dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
					+ ", there was an error processing.");
			
			dialogMessage.setButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent();
					ref.get();
					ref.get().setResult(Activity.RESULT_OK, intent);
					ref.get().finish();
				}
			});
			
			dialogMessage.show();
			Log.d(TAG, "Result = FAIL");
		}
	}

	public void detach() {
		ref.clear();
	}

	public void attach(FinderFoundActivity a) {
		ref = new WeakReference<FinderFoundActivity>(a);			
	}

}
