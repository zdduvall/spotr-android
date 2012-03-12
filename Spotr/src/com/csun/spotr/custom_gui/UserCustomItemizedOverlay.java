package com.csun.spotr.custom_gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.csun.spotr.ProfileActivity;
import com.csun.spotr.core.FriendAndLocation;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class UserCustomItemizedOverlay extends UserBalloonItemizedOverlay<OverlayItem> {
	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private List<FriendAndLocation> friendLocationList = new ArrayList<FriendAndLocation>();
	private GeoPoint me = null;
	private GeoPoint friend = null;
	private double distance = 0.0;
	private Paint paint;
	private Paint textPaint;
	
	private Context context;

	public UserCustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		context = mapView.getContext();
		populate();
		paint = initPaint();
		textPaint = getTextPaint();
	}

	private Paint initPaint() {
		Paint paint = new Paint();
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(3);
		paint.setColor(Color.RED);
		paint.setStrokeCap(Cap.ROUND);
		return paint;
	}
	
	private Paint getTextPaint() {
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setTextSize(25);
		return paint;
	}
	
	public void addTwoPoints(GeoPoint pMe, GeoPoint pFriend, double d) {
		me = pMe;
		friend = pFriend;
		distance = d;
	}
	
	public void addOverlay(OverlayItem overlay, FriendAndLocation fal) {
		overlays.add(overlay);
		friendLocationList.add(fal);
		populate();
	}

	public void clear() {
		overlays.clear();
		friendLocationList.clear();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		Intent intent = new Intent(context.getApplicationContext(), ProfileActivity.class);
		intent.putExtra("user_id", friendLocationList.get(index).getUserId());
		context.startActivity(intent);
		return true;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (me != null && friend != null) {
			Projection projection = mapView.getProjection();
			
			/*
			 * Construct to point on Canvas
			 */
			Point pMe = new Point();
			projection.toPixels(me, pMe);
			
			Point pFriend = new Point();
			projection.toPixels(friend, pFriend);
			
			
			Path path = new Path();
			path.moveTo(pMe.x, pMe.y);
		    path.lineTo(pFriend.x, pFriend.y);
		    // draw line
		    canvas.drawPath(path, paint);
		    // draw text
		    canvas.drawText((int) distance + " m", pFriend.x + 50,  pFriend.y, textPaint);
		}
		super.draw(canvas, mapView, shadow);
	}
}