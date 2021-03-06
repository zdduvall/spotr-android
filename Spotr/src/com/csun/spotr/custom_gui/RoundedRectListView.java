package com.csun.spotr.custom_gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.ListView;

public class RoundedRectListView extends ListView {
	 
    private static final float RADIUS = 7;
    private Path mClip;
 
    public RoundedRectListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
 
    private void init() {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(RADIUS); 
    }
   
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mClip = new Path();
        RectF rect = new RectF(0, 0, w, h);
        mClip.addRoundRect(rect, RADIUS, RADIUS, Direction.CW);
    }
 
    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mClip);
        super.dispatchDraw(canvas);
        canvas.restore();
    } 
}