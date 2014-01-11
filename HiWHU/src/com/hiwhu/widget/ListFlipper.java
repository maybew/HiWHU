package com.hiwhu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;

public class ListFlipper extends HentaiScrollLayout {

	boolean isFlip = false;
	float initX = 0;
	float initY = 0;

	public ListFlipper(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ListFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		// return super.onInterceptTouchEvent(ev);
		final float x = ev.getX();
		final float y = ev.getY();
		int actionType = ev.getAction();

		switch (actionType) {
		case MotionEvent.ACTION_DOWN:
			initX = ev.getX();
			initY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			if ((y - initY) / (x - initX) < 1 && (y - initY) / (x - initX) > -1) {
				if ((ev.getX() > initX + 0.5) || (ev.getX() < initX - 0.5)) {
					Log.v("TAG", "flip");
					ev.setAction(MotionEvent.ACTION_DOWN);
					isFlip = true;
					return true;
				}
			}
			Log.v("TAG", "Normal");
			break;
		case MotionEvent.ACTION_UP:
			break;
		}
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		float moveX = event.getX();
		if (isFlip) {
			isFlip = false;
			if (!hentaiScroller.isFinished()) {
				hentaiScroller.abortAnimation();
			}
			if (hentaiVelocityTracker == null) {
				hentaiVelocityTracker = VelocityTracker.obtain();
				hentaiVelocityTracker.addMovement(event);
			} else {
				Log.e("TAG",
						"in onTouchEvent(ACTION_DOWN) hentaiVelocityTracker is not recycled");
			}

			mLastMotionX = moveX;
			mTouchState = TOUCH_STATE_SCROLLING;
		}
		return super.onTouchEvent(event);
	}
	
	public void setCurView(int index) {
		this.setScrollerInterpolator(null);
		this.snapToScreen(index);
	}

}
