package com.hiwhu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class VerticalScrollLayout extends SmartScrollLayout {

	private static final String TAG = "VerticalScrollLayout";
	
	// ========constructed and initial function============
	public VerticalScrollLayout(Context context) {
		super(context);

	}

	public VerticalScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public VerticalScrollLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	// ==========Override===========

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (changed) {
			int childTopY = 0;
			final int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View childView = getChildAt(i);
				if (childView.getVisibility() != View.GONE) {
					final int childHeight = childView.getMeasuredHeight();
					childView.layout(0, childTopY,
							childView.getMeasuredWidth(), childTopY
									+ childHeight);
					childTopY += childHeight;
				}
			}
		}
		// Log.d(TAG, "onLayout()");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final int y = (int) event.getY();

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();

		}
		mVelocityTracker.addMovement(event);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "OnTouchEvent ACTION_DOWN");

			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			mLastY = y;
			if (mTouchState == TOUCH_STATE_REST)
				mTouchState = TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i(TAG, "OnTouchEvent ACTION_MOVE");
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// delta > 0 if screen move to below ; delta < 0 if screen move
				// to above
				int delta = (int) (mLastY - y);

				if (isCanMove(delta)) {

					if (getScrollY() + delta < mScrollRange[0])
						delta = mScrollRange[0] - getScrollY();
					else if (getScrollY() + delta > mScrollRange[1])
						delta = mScrollRange[1] - getScrollY();
					scrollBy(0, delta);

					mLastY = y;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "OnTouchEvent ACTION_UP");
			int velocityY = 0;
			if (mTouchState == TOUCH_STATE_SCROLLING) {

				// 1000 means 1000 provides pixels per second
				mVelocityTracker.computeCurrentVelocity(1000);
				// velocityY > 0 if Slide your finger from above to
				// below,that means screen should scroll to above
				// velocityY < 0 ,otherwise
				velocityY = (int) mVelocityTracker.getYVelocity();

				if (velocityY > VELOCITY_THRESHOLD && mCurScreen > 0) {
					Log.d(TAG, "snap to left");
					snapToScreen(mCurScreen - 1);
				} else if (velocityY < -VELOCITY_THRESHOLD
						&& mCurScreen < getChildCount() - 1) {
					Log.d(TAG, "snap to right");
					snapToScreen(mCurScreen + 1);
				} else {
					Log.d(TAG, "snapToDestination()");
					snapToDestination();
				}
				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}

				mTouchState = TOUCH_STATE_REST;
			}

			break;
		}
		return true;
	}

	// ==========custom function=========
	@Override
	public void snapToDestination() {
		final int screenHeight = getHeight();
		final int destScreen = (getScrollY() + screenHeight / 2) / screenHeight;
		// Log.d(TAG, String.valueOf(getScrollX()));
		snapToScreen(destScreen);

	}

	@Override
	public void snapToScreen(int index) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		index = Math.max(0, Math.min(index, getChildCount() - 1));
		Log.i(TAG, "snapToScreen:index = " + index);
		
		if (mCurScreen != index) {
			mCurScreen = index;
			if (mOnViewChangeListener != null) {
				// if the view change,then update dot-view
				mOnViewChangeListener.onViewChange(index);
			} else {
				Log.e(TAG, "There is no OnViewChangeListener");
			}
		}

		if (getScrollY() != (index * getHeight())) {
			// delta < 0 if screen scroll to above else delta > 0 if screen
			// scroll to below
			int delta = index * getHeight() - getScrollY();
			mScroller.startScroll(0, getScrollY(), 0, delta,
					(int) (Math.abs(delta) * 1.5));
			invalidate(); // redraw
		}

	}

	/**
	 * called by the OnViewChangeListener
	 * 
	 * @param index
	 */
	public void changeView(int index) {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}

		index = Math.max(0, Math.min(index, getChildCount() - 1));

		int delta = index * getHeight() - getScrollY();
		mScroller.startScroll(0, getScrollY(), 0, delta,
				(int) (Math.abs(delta) * 1.5));
		mCurScreen = index;
		invalidate(); // redraw

	}

	@Override
	protected boolean isCanMove(int delta) {

		if (getScrollY() <= mScrollRange[0] && delta < 0) {
			return false;
		}
		if (getScrollY() >= mScrollRange[1] && delta > 0) {

			return false;
		}
		return true;
	}

	@Override
	protected void setScrollRange() {
		int d = (int) (getHeight() * overEdgeSpace);
		mScrollRange[0] = -d;
		mScrollRange[1] = (getChildCount() - 1) * getHeight() + d;

	}

	
}
