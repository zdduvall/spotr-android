package com.csun.spotr.custom_gui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.csun.spotr.core.Place;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CustomQuestItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private List<Place> places = new ArrayList<Place>();
	private Context context;

	public CustomQuestItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		context = mapView.getContext();
		populate();
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
			
		}
		return true;
	}
}