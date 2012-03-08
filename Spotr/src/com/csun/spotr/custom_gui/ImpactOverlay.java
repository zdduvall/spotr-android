package com.csun.spotr.custom_gui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class ImpactOverlay extends Overlay {
	private int radius = 0;
	private GeoPoint geopoint;
	private int myCircleRadius;
	Point point = new Point();
	Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);

	public ImpactOverlay(GeoPoint point, int myRadius) {
		geopoint = point;
		radius = myRadius;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		projection.toPixels(geopoint, point);

		// the circle to mark the spot
		circle.setColor(Color.parseColor("#A2D8F2"));
		circle.setAlpha(100);

		myCircleRadius = metersToRadius(radius, mapView, (double) geopoint.getLatitudeE6() / 1000000);
		canvas.drawCircle(point.x, point.y, myCircleRadius, circle);
	}
	
	private int metersToRadius(float meters, MapView map, double latitude) {
		return (int) (map.getProjection().metersToEquatorPixels(meters) * (1.0 / Math.cos(Math.toRadians(latitude))));
	}
}