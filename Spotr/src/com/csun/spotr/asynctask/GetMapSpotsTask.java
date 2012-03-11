package com.csun.spotr.asynctask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.csun.spotr.LocalMapViewActivity;
import com.csun.spotr.core.Place;
import com.csun.spotr.skeleton.IAsyncTask;
import com.csun.spotr.util.GooglePlaceHelper;
import com.csun.spotr.util.JsonHelper;

public class GetMapSpotsTask 
	extends AsyncTask<Location, Place, Boolean> 
		implements IAsyncTask<LocalMapViewActivity> {
	
	private static final String TAG = "[AsyncTask].GetSpotsTask";
	private static final String GET_SPOTS_URL = "http://107.22.209.62/android/get_spots.php";
	private static final String UPDATE_GOOGLE_PLACES_URL = "http://107.22.209.62/android/update_google_places.php";
	private WeakReference<LocalMapViewActivity> ref;

	public GetMapSpotsTask(LocalMapViewActivity a) {
		attach(a);
	}

	private List<NameValuePair> constructGooglePlace(Location loc) {
		// this is data we will send to our server
		List<NameValuePair> sentData = new ArrayList<NameValuePair>();
		// we reformat the original data to include only what we need
		JSONArray reformattedData = new JSONArray();
		JSONObject json = JsonHelper.getJsonFromUrl(GooglePlaceHelper.buildGooglePlacesUrl(loc, GooglePlaceHelper.GOOGLE_RADIUS_IN_METER));
		JSONObject temp = null;

		try {
			JSONArray originalGoogleDataArray = json.getJSONArray("results");
			for (int i = 0; i < originalGoogleDataArray.length(); i++) {
				// id: is used to verify place existence
				JSONObject e = new JSONObject();
				e.put("id", originalGoogleDataArray.getJSONObject(i).getString("id"));
				e.put("name", originalGoogleDataArray.getJSONObject(i).getString("name"));
				e.put("lat", originalGoogleDataArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
				e.put("lon", originalGoogleDataArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
				temp = JsonHelper.getJsonFromUrl(GooglePlaceHelper.buildGooglePlaceDetailsUrl(originalGoogleDataArray.getJSONObject(i).getString("reference")));

				if (temp.getJSONObject("result").has("formatted_address")) {
					e.put("addr", temp.getJSONObject("result").getString("formatted_address"));
				}
				else {
					e.put("addr", "default address");
				}

				if (temp.getJSONObject("result").has("formatted_phone_number")) {
					e.put("phone", temp.getJSONObject("result").getString("formatted_phone_number"));
				}
				else {
					e.put("phone", "(888) 888-8888");
				}

				if (temp.getJSONObject("result").has("url")) {
					e.put("url", temp.getJSONObject("result").getString("url"));
				}
				else {
					e.put("url", "https://www.google.com/");
				}

				// put e
				reformattedData.put(e);
			}
		}
		catch (JSONException e) {
			Log.e(TAG + ".constructGooglePlace() : ", "JSON error parsing data" + e.toString());
		}
		// send data to our server
		sentData.add(new BasicNameValuePair("google_array", reformattedData.toString()));
		return sentData;
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(Place... p) {
		ref.get().updatePlaceTaskProgress(p[0]);
	}

	private List<NameValuePair> prepareUploadData(Location location) {
		List<NameValuePair> data = new ArrayList<NameValuePair>();
		data.add(new BasicNameValuePair("latitude", Double.toString(location.getLatitude())));
		data.add(new BasicNameValuePair("longitude", Double.toString(location.getLongitude())));
		data.add(new BasicNameValuePair("radius", GooglePlaceHelper.RADIUS_IN_KM));
		return data;
	}

	@Override
	protected Boolean doInBackground(Location... locations) {
		// send Google data to our server to update 'spots' table
		JsonHelper.getJsonObjectFromUrlWithData(UPDATE_GOOGLE_PLACES_URL, constructGooglePlace(locations[0]));
		List<NameValuePair> data = prepareUploadData(locations[0]);
		JSONArray array = JsonHelper.getJsonArrayFromUrlWithData(GET_SPOTS_URL, data);
		if (array != null) {
			try {
				for (int i = 0; i < array.length(); ++i) {
					publishProgress(new Place.Builder(
						// require parameters
						array.getJSONObject(i).getDouble("spots_tbl_longitude"), 
						array.getJSONObject(i).getDouble("spots_tbl_latitude"), 
						array.getJSONObject(i).getInt("spots_tbl_id"))
					
							// optional parameters
							.name(array.getJSONObject(i).getString("spots_tbl_name"))
							.type(array.getJSONObject(i).getInt("spots_tbl_type"))
							.address(array.getJSONObject(i).getString("spots_tbl_description"))
								.build());
				}
			}
			catch (JSONException e) {
				Log.e(TAG + ".doInBackGround(Void ...voids) : ", "JSON error parsing data" + e.toString());
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

	public void attach(LocalMapViewActivity a) {
		ref = new WeakReference<LocalMapViewActivity>(a);
	}

	public void detach() {
		ref.clear();
	}
}