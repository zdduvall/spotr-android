package com.csun.spotr;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;

import com.csun.spotr.adapter.FinderItemAdapter;
import com.csun.spotr.core.adapter_item.SeekingItem;

public class CommentActivity extends Activity {
	private static final String TAG = "(CommentActivity)";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment);
	}
}