package com.csun.spotr.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.csun.spotr.R;

public class PlaceIconUtil {
	public static final int IC_MAP_AGRITOURISM 	= 0;
	public static final int IC_MAP_BIGCITY 		= 1;
	public static final int IC_MAP_CASTLE 		= 2;
	public static final int IC_MAP_ARCH 		= 3;
	public static final int IC_MAP_CAMPFIRE 	= 4;
	public static final int IC_MAP_CABIN 		= 5;
	
	public static final int IC_MAP_PERSON 		= -1;
	
	public static Drawable getMapIconByType(Context c, int type) {
		Drawable icon = null;
		switch (type) {
		
		case -1:
			icon = c.getResources().getDrawable(R.drawable.ic_map_person);
			break;
			
		case 0:
			icon = c.getResources().getDrawable(R.drawable.ic_map_agritourism);
			break;
			
		case 1:
			icon = c.getResources().getDrawable(R.drawable.ic_map_bigcity);
			break;
			
		case 2:
			icon = c.getResources().getDrawable(R.drawable.ic_map_castle);
			break;
			
		case 3:
			icon = c.getResources().getDrawable(R.drawable.ic_map_arch);
			break;
			
		case 4:
			icon = c.getResources().getDrawable(R.drawable.ic_map_campfire);
			break;
	
		case 5:
			icon = c.getResources().getDrawable(R.drawable.ic_map_cabin);
			break;
		
		}
		
		icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
		return icon;
	}
}
