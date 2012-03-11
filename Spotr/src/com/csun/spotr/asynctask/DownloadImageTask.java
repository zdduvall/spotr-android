package com.csun.spotr.asynctask;

import com.csun.spotr.singleton.CustomHttpClient;
import com.google.android.maps.OverlayItem;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadImageTask extends AsyncTask<String, Integer, Drawable> {
	private Context context;
	private OverlayItem overlay;

	public DownloadImageTask(Context context, OverlayItem overlay) {
		this.context = context;
		this.overlay = overlay;
	}

	protected Drawable doInBackground(String... urls) {
		return scaleBitmapToDrawable(downloadImage(urls));
	}

	@Override
	protected void onPostExecute(Drawable result) {
		result.setBounds(0, 0, result.getIntrinsicWidth(), result.getIntrinsicHeight());
		overlay.setMarker(result);
	}
	
	private Drawable scaleBitmapToDrawable(Bitmap bitmap) {
		Drawable d = new BitmapDrawable(Bitmap.createScaledBitmap(bitmap, 45, 45, true));
		bitmap.recycle();
		bitmap = null;
		return d;
	}

	private Bitmap downloadImage(String... urls) {
		HttpClient httpClient = CustomHttpClient.getHttpClient();
		try {
			HttpGet request = new HttpGet(urls[0]);
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, 60000); // 1 minute
			request.setParams(params);
			HttpResponse response = httpClient.execute(request);
			byte[] image = EntityUtils.toByteArray(response.getEntity());
			Bitmap mBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
			return mBitmap;
		}
		catch (IOException e) {
		}
		return null;
	}
}