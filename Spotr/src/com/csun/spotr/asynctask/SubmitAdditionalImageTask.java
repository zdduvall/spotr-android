package com.csun.spotr.asynctask;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.FinderItemDetailActivity;
import com.csun.spotr.singleton.CurrentDateTime;
import com.csun.spotr.singleton.CurrentUser;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.Base64;
import com.csun.spotr.util.UploadFileHelper;

public class SubmitAdditionalImageTask
	extends AsyncTask<String, Integer, String> 
		implements IAsyncTask<FinderItemDetailActivity> {
	private static final String TAG = "(SubmitAdditionalImageTask)";
	private static final String SUBMIT_ADDITIONAL_IMAGE = "http://107.22.209.62/android/do_add_finder_image.php";

	private WeakReference<FinderItemDetailActivity> ref;
	private ProgressDialog progressDialog = null;
	private Bitmap imageBitmap;
	private int finderId;
	
	public SubmitAdditionalImageTask(FinderItemDetailActivity a, Bitmap b, int id) {
		attach(a);
		imageBitmap = b;
		finderId = id;
	}
	
	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(ref.get());
		progressDialog.setMessage("Submitting image...");
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);
		progressDialog.show();
	}
	
	protected String doInBackground(String... params) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
		byte[] src = stream.toByteArray();
	
		String imageBytecode = Base64.encodeBytes(src);
		String imageFile = CurrentUser.getCurrentUser().getUsername() + CurrentDateTime.getUTCDateTime().trim() + ".png";
		
		data.add(new BasicNameValuePair("finder_id", Integer.toString(finderId)));
		data.add(new BasicNameValuePair("image", imageBytecode));
		data.add(new BasicNameValuePair("filename", imageFile));
	
		JSONObject json = UploadFileHelper.uploadFileToServer(SUBMIT_ADDITIONAL_IMAGE, data);
		String result = "";
		try {
			result = json.getString("result");
		}
		catch (JSONException e) {
			Log.e(TAG + ".FinderItemDetailActivity", "JSONFailception", e );
		}
	
		return result;
	}
	
	@Override
	protected void onPostExecute(String result) {
		progressDialog.dismiss();
		if (result.equals("success")) {
			AlertDialog dialogMessage = new AlertDialog.Builder(ref.get()).create();
			dialogMessage.setTitle("Image uploaded!");
			dialogMessage.setMessage("Hey " + CurrentUser.getCurrentUser().getUsername() 
					+ ", image submission successful!");

			dialogMessage.setButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent();
					ref.get().setResult(FinderItemDetailActivity.RESULT_OK, intent);
					ref.get().finish();
				}
			});

			dialogMessage.show();
		}
		else {
			Log.d(TAG, "Result = FAIL");
		}
	}
	
	public void detach() {
		ref.clear();
	}

	public void attach(FinderItemDetailActivity a) {
		ref = new WeakReference<FinderItemDetailActivity>(a);			
	}
}