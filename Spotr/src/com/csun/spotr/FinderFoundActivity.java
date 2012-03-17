package com.csun.spotr;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.csun.spotr.asynctask.FinderFoundTask;
import com.csun.spotr.util.Base64;
import com.csun.spotr.util.FineLocation;
import com.csun.spotr.util.FineLocation.LocationResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FinderFoundActivity extends Activity {
	private static final String TAG = "(FinderFoundActivity)";
	private static final int CAMERA_REQUEST_CODE = 0;
	private static final int GALLERY_REQUEST_CODE = 1;
	
	private static boolean hasImage = false;
	private static int finderId = 0;
	private static Bitmap imageBitmap = null;
	private static Location lastKnownLocation = null;
	private static FineLocation fineLocation = new FineLocation();
	
	private Button buttonAddImage;
	private Button buttonSubmit;
	private EditText editTextMessage;
	private TextView textViewCount;
	private static ImageView imageViewImage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finder_found);
		Log.v(TAG, "onCreate(...)");
		
		finderId = getIntent().getIntExtra("finder_id", 0);
		
		buttonAddImage = (Button) findViewById(R.id.finder_found_xml_button_add_image);
		buttonSubmit = (Button) findViewById(R.id.finder_found_xml_button_submit);
		editTextMessage = (EditText) findViewById(R.id.finder_found_xml_edittext_message);
		textViewCount = (TextView) findViewById(R.id.finder_found_xml_textview_character_count);
		imageViewImage = (ImageView) findViewById(R.id.finder_found_xml_imageview_image);
		
		buttonSubmit.setEnabled(false);
		
		LocationResult locationResult = (new LocationResult() {
			@Override
			public void gotLocation(final Location location) {
				lastKnownLocation = location;
			}
		});
		fineLocation.getLocation(this, locationResult);
		
		editTextMessage.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				textViewCount.setText(String.valueOf(s.length()) + "/160");
				if (s.length() > 0) {
					buttonSubmit.setEnabled(true);
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void afterTextChanged(Editable s) {

			}
		});
		
		buttonAddImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				AlertDialog dialogMessage = new AlertDialog.Builder(FinderFoundActivity.this).create();
				dialogMessage.setTitle("Camera or Gallery");
				dialogMessage.setMessage("Where do you want to upload your picture from?");
				
				dialogMessage.setButton(Dialog.BUTTON_POSITIVE, "Camera", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
						startActivityForResult(i, CAMERA_REQUEST_CODE);
					}
				});
				
				dialogMessage.setButton(Dialog.BUTTON_NEGATIVE, "Gallery", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						i.setType("image/*");
						startActivityForResult(i, GALLERY_REQUEST_CODE);
					}
				});
				
				dialogMessage.show();
			}
		});
		
		buttonSubmit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(hasImage)
					new FinderFoundTask(FinderFoundActivity.this, 
										finderId, getImageBytecode(), 
										editTextMessage.getText().toString(), 
										lastKnownLocation.getLatitude(), 
										lastKnownLocation.getLongitude()).execute();
				else
					Toast.makeText(getApplicationContext(), "Please add a picture", Toast.LENGTH_SHORT);
			}
		});
	}
	
	private String getImageBytecode() {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
		byte[] src = stream.toByteArray();
		String result = Base64.encodeBytes(src);
		
		return result;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (requestCode == CAMERA_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				getImageBitmap(intent);
				hasImage = true;
			}
		}
		
		else if (requestCode == GALLERY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				imageBitmap = BitmapFactory.decodeStream(getInputStream(intent), null, getOptions());
				imageViewImage.setImageBitmap(imageBitmap);
				hasImage = true;
			}
		}
	}
	
	private void getImageBitmap(Intent intent) {
		imageBitmap = (Bitmap) intent.getExtras().get("data");
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
		imageViewImage.setImageBitmap(imageBitmap);
	}
	
	private InputStream getInputStream(Intent intent) {
		Uri selectedImageUri = intent.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
		cursor.moveToFirst();

		ContentResolver cr = getContentResolver();
		InputStream in = null;
		try {
			in = cr.openInputStream(selectedImageUri);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return in;
	}
	
	private Options getOptions() {
		Options options = new Options();
		options.inSampleSize = 8;
		return options;
	}
	
}
