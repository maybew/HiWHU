package com.hiwhu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

public class OvershootScrollLayout extends HentaiScrollLayout {

	private static final String TAG = "OvershootScrollLayout";
	private Interpolator defInterpolator;

	public OvershootScrollLayout(Context context) {
		super(context);
		init();
	}

	public OvershootScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public OvershootScrollLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		this.defInterpolator = new OvershootInterpolator(1.5f);
		setScrollerInterpolator(defInterpolator);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		setScrollerInterpolator(defInterpolator);
		if (mCurScreen == getChildCount() - 1) {
			final int action = event.getAction();
			final float x = event.getX();
			if(action == MotionEvent.ACTION_MOVE){
				// Log.i(TAG, "OnTouchEvent ACTION_MOVE");
				if (mTouchState == HentaiScrollLayout.TOUCH_STATE_SCROLLING) {
					// delta > 0 if screen move to right ; delta < 0 if screen
					// move
					// to
					// left
					int deltaX = (int) (mLastMotionX - x);
					if (deltaX > 0) 
						return false;
				}
			}
		}
		return super.onTouchEvent(event);
	}
}
