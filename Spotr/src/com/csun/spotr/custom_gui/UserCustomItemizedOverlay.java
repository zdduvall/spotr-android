package com.csun.spotr.custom_gui;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.csun.spotr.ProfileActivity;
import com.csun.spotr.core.FriendAndLocation;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class UserCustomItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {
	private List<OverlayItem> overlays = new ArrayList<OverlayItem>();
	private List<FriendAndLocation> friendLocationList = new ArrayList<FriendAndLocation>();
	private Context context;

	public UserCustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		context = mapView.getContext();
		populate();
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
}