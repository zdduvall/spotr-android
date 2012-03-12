package com.csun.spotr.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.csun.spotr.R;

public class PlaceIconUtil {
	
	public static final int ic_map_person 			= -1;
	public static final int IC_MAP_DEFAULT	 		= 0;
	public static final int IC_MAP_COFFEE	 		= 1;
	public static final int IC_MAP_LIBRARY	 		= 2;
	public static final int IC_MAP_GYM		 		= 3;
	public static final int IC_MAP_PARK		 		= 4;
	public static final int IC_MAP_PLAZA	 		= 5;
	public static final int IC_MAP_MARKET	 		= 6;
	public static final int IC_MAP_BAR		 		= 7;
	public static final int IC_MAP_BANK		 		= 8;
	public static final int IC_MAP_ACADEMIC	 		= 9;
	public static final int IC_MAP_ADMINISTRATIVE	= 10;
	public static final int IC_MAP_AUDITORIUM		= 11;
	public static final int IC_MAP_FOUNTAIN	 		= 12;
	public static final int IC_MAP_SANDWICH	 		= 13;
	public static final int IC_MAP_AGRICULTURE		= 14;
	public static final int IC_MAP_RESTAURANT		= 15;
	public static final int IC_MAP_COURT	 		= 16;
	public static final int IC_MAP_MONUMENT	 		= 17;
	public static final int IC_MAP_MEMORIAL	 		= 18;

	
	public static Drawable getMapIconByType(Context c, int type) {
		Drawable icon = null;
		switch (type) {
		
			case -1:
				icon = c.getResources().getDrawable(R.drawable.ic_map_person);
				break;
			case 0:
				icon = c.getResources().getDrawable(R.drawable.ic_map_default);
				break;
			case 1:
				icon = c.getResources().getDrawable(R.drawable.ic_map_coffee);
				break;
			case 2:
				icon = c.getResources().getDrawable(R.drawable.ic_map_library);
				break;
			case 3:
				icon = c.getResources().getDrawable(R.drawable.ic_map_gym);
				break;
			case 4:
				icon = c.getResources().getDrawable(R.drawable.ic_map_default);
				break;
			case 5:
				icon = c.getResources().getDrawable(R.drawable.ic_map_plaza);
				break;
			case 6:
				icon = c.getResources().getDrawable(R.drawable.ic_map_default);
				break;
			case 7:
				icon = c.getResources().getDrawable(R.drawable.ic_map_bar);
				break;
			case 8:
				icon = c.getResources().getDrawable(R.drawable.ic_map_default);
				break;
			case 9:
				icon = c.getResources().getDrawable(R.drawable.ic_map_academic);
				break;
			case 10:
				icon = c.getResources().getDrawable(R.drawable.ic_map_administrative);
				break;
			case 11:
				icon = c.getResources().getDrawable(R.drawable.ic_map_auditorium);
				break;
			case 12:
				icon = c.getResources().getDrawable(R.drawable.ic_map_fountain);
				break;
			case 13:
				icon = c.getResources().getDrawable(R.drawable.ic_map_sandwich);
				break;
			case 14:
				icon = c.getResources().getDrawable(R.drawable.ic_map_agriculture);
				break;
			case 15:
				icon = c.getResources().getDrawable(R.drawable.ic_map_restaurant);
				break;
			case 16:
				icon = c.getResources().getDrawable(R.drawable.ic_map_default);
				break;
			case 17:
				icon = c.getResources().getDrawable(R.drawable.ic_map_monument);
				break;
			case 18:
				icon = c.getResources().getDrawable(R.drawable.ic_map_memorial);
				break;
			default:
				icon = c.getResources().getDrawable(R.drawable.ic_map_default);
				break;	
		}
		
	//	icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
		icon.setBounds(-icon.getIntrinsicWidth()/2, -icon.getIntrinsicHeight(), icon.getIntrinsicWidth() /2, 0);
		return icon;
	}
}
