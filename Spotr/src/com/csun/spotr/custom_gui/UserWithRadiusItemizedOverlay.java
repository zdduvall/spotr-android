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
import android.util.Log;

import com.csun.spotr.core.Place;
import com.csun.spotr.core.User;
import com.csun.spotr.util.GooglePlaceHelper;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

/**
 * This is user item overlay with 
 * a given radius. Radius is taken from
 * GooglePlaceHelper.GOOGLE_RADIUS_IN_METER
 * 
 * @author chan
 */
public class UserWithRadiusItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private User user;
	private Context context;
	private GeoPoint geopoint;
	private int myCircleRadius;
	private Point point;
	private Paint circle;

	/**
	 * Constructor 
	 * 
	 * @param defaultMarker
	 * 			the icon on map
	 * 
	 * @param mapView
	 * 			the map that is drawn on 
	 */
	public UserWithRadiusItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker), mapView);
		context = mapView.getContext();
	
		// set the pop-up offset so that it's 
		// in the center
		this.setBalloonBottomOffset(53);
		
		// create a new point to draw
		point = new Point();
		// initialize paint component
		circle = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		// let the map populate, so that even without
		// data, it still can display
		populate();
	}
	
	/**
	 * Change the location of the overlay item
	 * to a new GeoPoint on map
	 * 
	 * @param point
	 * 				new overlay item's location
	 */
	public void changePoint(GeoPoint point) {
		geopoint = point;
	}
	
	/**
	 * Add an overlay item with user data
	 * 
	 * @param overlay
	 * 				overlay item
	 * @param u
	 * 				user data
	 */
	public void addOverlay(OverlayItem overlay, User u) {
		overlays.add(overlay);
		user = u;
		populate();
	}
	
	/**
	 * Clear all overlays
	 */
	public void clear() {
		overlays.clear();
	}

	/**
	 * Generated stub from BalloonItemizedOverlay
	 */
	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	/**
	 * The total number of overlays added
	 * 
	 * @return the size
	 *
	 */
	@Override
	public int size() {
		return overlays.size();
	}
	
	/**
	 * Handle on ballon tap if needed
	 */
	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {
		return true;
	}
	
	
	/**
	 * Draw the radius with overlay as center
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (geopoint != null) {
			Projection projection = mapView.getProjection();
			projection.toPixels(geopoint, point);
	
			// the circle to mark the spot
			circle.setColor(Color.parseColor("#A2D8F2"));
			circle.setAlpha(100);
	
			myCircleRadius = metersToRadius(Float.parseFloat(GooglePlaceHelper.GOOGLE_RADIUS_IN_METER), mapView, (double) geopoint.getLatitudeE6() / 1000000);
			canvas.drawCircle(point.x, point.y, myCircleRadius, circle);
		}
		super.draw(canvas, mapView, shadow);
	}
	
	/**
	 * Convert from meters on map to distance on
	 * canvas to draw
	 * 
	 * @param meters
	 * 			actual radius distance
	 * 
	 * @param map
	 * 			the map
	 * 
	 * @param latitude
	 * 			the center
	 * 
	 * @return distance on canvas
	 */
	private int metersToRadius(float meters, MapView map, double latitude) {
		return (int) (map.getProjection().metersToEquatorPixels(meters) * (1.0 / Math.cos(Math.toRadians(latitude))));
	}
}