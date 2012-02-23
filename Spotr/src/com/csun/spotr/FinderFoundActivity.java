package com.csun.spotr;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class FinderFoundActivity extends Activity {
	private static final String TAG = "(FinderFoundActivity)";
	private static final int CAMERA_REQUEST_CODE = 0;
	private static final int GALLERY_REQUEST_CODE = 1;
	
	Button buttonAddImage;
	ImageView imageViewImage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.finder_found);
		Log.v(TAG, "onCreate(...)");
		
		buttonAddImage = (Button) findViewById(R.id.finder_found_xml_button_add_image);
		imageViewImage = (ImageView) findViewById(R.id.finder_found_xml_imageview_image);
		
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
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		if (requestCode == CAMERA_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Bitmap takenPictureBitmap = (Bitmap) intent.getExtras().get("data");
				imageViewImage.setImageBitmap(takenPictureBitmap);
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				takenPictureBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
//				byte[] src = stream.toByteArray();
//				String byteCode = Base64.encodeBytes(src);
			}
		}
		
		else if (requestCode == GALLERY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
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
				Options options = new Options();
				options.inSampleSize = 8;
				imageViewImage.setImageBitmap(BitmapFactory.decodeStream(in, null, options));
			}
		}
	}
}
