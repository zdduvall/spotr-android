package com.csun.spotr.util;

import com.csun.spotr.singleton.CustomHttpClient;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonHelper {
	private static final String TAG = "(JsonHelper)";
	
	public static JSONObject getJsonFromUrl(String url) {
		InputStream input = null;
		String result = "";
		JSONObject json = null;
		try {
			HttpClient httpclient = CustomHttpClient.getHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			input = entity.getContent();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonFromUrl(String url)", "Error in http connection ", e );
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
			StringBuilder content = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				content.append(line + "\n");
			}
			input.close();
			result = content.toString();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonFromURL(String url)", "Error parsing result ", e );
		}

		try {
			json = new JSONObject(result);
		}
		catch (JSONException e) {
			Log.e(TAG + ".getJsonFromURL()", "Error converting data ", e );
		}
		return json;
	}
	
	public static JSONArray getJsonArrayFromUrl(String url) {
		InputStream input = null;
		String result = "";
		JSONArray jsonArray = null;
		try {
			HttpClient httpclient = CustomHttpClient.getHttpClient();
			HttpPost httppost = new HttpPost(url);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			input = entity.getContent();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonArrayFromUrl(String url)", "Error in http connection " + e ); 
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
			StringBuilder content = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				content.append(line + "\n");
			}
			input.close();
			result = content.toString();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonArrayFromUrl(String url)", "Error parsing result ", e );
		}

		try {
			jsonArray = new JSONArray(result);
		}
		catch (JSONException e) {
			Log.e(TAG + "getJsonArrayFromUrl(String url)", "Error converting data ", e );
		}
		return jsonArray;
	}
	
	public static JSONArray getJsonArrayFromUrlWithData(String url, List<NameValuePair> datas) {
		InputStream input = null;
		String result = "";
		JSONArray jsonArray = null;
		try {
			HttpClient httpclient = CustomHttpClient.getHttpClient();
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(datas));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			input = entity.getContent();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonArrayFromUrlWithData(String url, List<NameValuePair> datas)", "Error in http connection ", e ); 
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
			StringBuilder content = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				content.append(line + "\n");
			}
			input.close();
			result = content.toString();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonArrayFromUrlWithData(String url, List<NameValuePair> datas)", "Error parsing result ", e);
		}
		
		try {
			if ( result != null && !result.equals("null\n") ) 
				jsonArray = new JSONArray(result);
		}
		catch (JSONException e) {
			Log.v(TAG + "getJsonArrayFromUrlWithData(String url, List<NameValuePair> datas)", "Error converting data ", e );
			return null; // should this be here?
		}
		return jsonArray;
	}
	
	public static JSONObject getJsonObjectFromUrlWithData(String url, List<NameValuePair> datas) {
		InputStream input = null;
		String result = "";
		JSONObject json = null;
		try {
			HttpClient httpclient = CustomHttpClient.getHttpClient();
			HttpPost httppost = new HttpPost(url);
			httppost.setEntity(new UrlEncodedFormEntity(datas));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			input = entity.getContent();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonObjectFromUrlWithData(String url, List<NameValuePair> datas)", "Error in http connection ", e ); 
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input, "iso-8859-1"), 8);
			StringBuilder content = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				content.append(line + "\n");
			}
			input.close();
			result = content.toString();
		}
		catch (Exception e) {
			Log.e(TAG + ".getJsonObjectFromUrlWithData(String url, List<NameValuePair> datas)", "Error parsing result ", e );
		}
		try {
			json = new JSONObject(result);
		}
		catch (JSONException e) {
			Log.e(TAG + ".getJsonObjectFromUrlWithData(String url, List<NameValuePair> datas)", "Error converting data ", e );
		}
		return json;
	}
}
