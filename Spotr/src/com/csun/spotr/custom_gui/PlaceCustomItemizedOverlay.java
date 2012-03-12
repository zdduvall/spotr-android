package com.csun.spotr.custom_gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.csun.spotr.core.Place;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class PlaceCustomItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private List<Place> places = new ArrayList<Place>();
	private Context context;
	private GeoPoint me = null;
	private GeoPoint place = null;
	private Paint paint;
	private Paint textPaint;
	private double distance = 0.0;

	public PlaceCustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker), mapView);
		context = mapView.getContext();
		this.setBalloonBottomOffset(53);
		populate();
		paint = initPaint();
		textPaint = getTextPaint();
	}
	
	private Paint initPaint() {
		Paint paint = new Paint();
		paint.setStyle(Style.FILL_AND_STROKE);
		paint.setStrokeWidth(3);
		paint.setColor(Color.GREEN);
		paint.setStrokeCap(Cap.ROUND);
		return paint;
	}
	
	private Paint getTextPaint() {
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setTextSize(25);
		return paint;
	}
	
	public void addTwoPoints(GeoPoint pMe, GeoPoint pPlace, double d) {
		me = pMe;
		place = pPlace;
		distance = d;
	}
	
	public void addOverlay(OverlayItem overlay, Place place) {
		overlays.add(overlay);
		places.add(place);
		populate();
	}

	public void clear() {
		overlays.clear();
		places.clear();
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
		if (places.get(index).getId() != -1) {
			Intent intent = new Intent("com.csun.spotr.PlaceMainActivity");
			Bundle extras = new Bundle();
			extras.putInt("place_id", places.get(index).getId());
			intent.putExtras(extras);
			context.startActivity(intent);
		}
		return true;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (me != null && place != null) {
			Projection projection = mapView.getProjection();
			
			/*
			 * Construct to point on Canvas
			 */
			Point pMe = new Point();
			projection.toPixels(me, pMe);
			
			Point pPlace = new Point();
			projection.toPixels(place, pPlace);
			
			
			Path path = new Path();
			path.moveTo(pMe.x, pMe.y);
		    path.lineTo(pPlace.x, pPlace.y);
		    // draw line
		    canvas.drawPath(path, paint);
		    // draw text
		    canvas.drawText(distance + " m", pPlace.x + 50,  pPlace.y, textPaint);
		}
		super.draw(canvas, mapView, shadow);
	}
}