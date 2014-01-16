package com.flightcomputer.utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;

public class VerticalProgressBar_Reverse extends ProgressBar {
	
	public VerticalProgressBar_Reverse(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VerticalProgressBar_Reverse(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalProgressBar_Reverse(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		canvas.rotate(90);
		canvas.translate(0, -getWidth());
		super.onDraw(canvas);
	}

}
