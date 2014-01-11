package com.hiwhu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class HentaiScrollLayout extends ViewGroup {

	private static final String TAG = "HentaiScrollLayout";
	protected static final int VELOCITY_THRESHOLD = 600;// 600 pixels per second
	protected static final int TOUCH_STATE_REST = 0;
	protected static final int TOUCH_STATE_SCROLLING = 1;
	protected static final float DEF_OVER_EDGE_SPACE = 0.0f;
	protected static final int mDefScreen = 0;

	protected int mCurScreen;
	private float overEdgeSpace;
	protected float mLastMotionX;
	protected float mLastMotionY;
	protected int mTouchState;

	protected Scroller hentaiScroller;
	protected VelocityTracker hentaiVelocityTracker; // judge gesture
	protected OnViewChangeListener hentaiOnViewChangeListener;

	// ========constructed and initial function============
	public HentaiScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public HentaiScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HentaiScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mCurScreen = mDefScreen;
		overEdgeSpace = DEF_OVER_EDGE_SPACE;
		mTouchState = TOUCH_STATE_REST;
		hentaiScroller = new Scroller(context);
	}

	// ==========Override===========
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);

		}
		scrollTo(mCurScreen * width, 0);
		// Log.d(TAG, "onMeasure()");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (changed) {
			int childLeftX = 0;
			final int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				final View childView = getChildAt(i);
				if (childView.getVisibility() != View.GONE) {
					final int childWidth = childView.getMeasuredWidth();
					childView.layout(childLeftX, 0, childLeftX + childWidth,
							childView.getMeasuredHeight());
					childLeftX += childWidth;
				}
			}
		}
		// Log.d(TAG, "onLayout()");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "OnTouchEvent ACTION_DOWN");

			if (!hentaiScroller.isFinished()) {
				hentaiScroller.abortAnimation();
			}
			if (hentaiVelocityTracker == null) {
				hentaiVelocityTracker = VelocityTracker.obtain();
				hentaiVelocityTracker.addMovement(event);
			} else {
				Log.e(TAG,
						"in onTouchEvent(ACTION_DOWN) hentaiVelocityTracker is not recycled");
			}

			mLastMotionX = x;
			mTouchState = TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i(TAG, "OnTouchEvent ACTION_MOVE");
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				// delta > 0 if screen move to right ; delta < 0 if screen move
				// to
				// left
				int deltaX = (int) (mLastMotionX - x);

				if (isCanMove(deltaX)) {
					
						if (hentaiVelocityTracker != null) {
							hentaiVelocityTracker.addMovement(event);
						} else {
							Log.e(TAG,
									"in onTouchEvent(ACTION_MOVE) hentaiVelocityTracker is not initialized");
						}
						mLastMotionX = x;
						scrollBy(deltaX, 0);
					
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "OnTouchEvent ACTION_UP");
			int velocityX = 0;
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				if (hentaiVelocityTracker != null) {
					hentaiVelocityTracker.addMovement(event);
					// 1000 means 1000 provides pixels per second
					hentaiVelocityTracker.computeCurrentVelocity(1000);
					// velocityX > 0 if Slide your finger from right to
					// left,that means screen should scroll to right
					// velocityX < 0 ,otherwise
					velocityX = (int) hentaiVelocityTracker.getXVelocity();
				} else {
					Log.e(TAG,
							"in OnTouchEvent(ACTION_UP) hentaiVelocityTracker is not initialize");
				}
				if (velocityX > VELOCITY_THRESHOLD && mCurScreen > 0) {
					Log.d(TAG, "snap to left");
					snapToScreen(mCurScreen - 1);
				} else if (velocityX < -VELOCITY_THRESHOLD
						&& mCurScreen < getChildCount() - 1) {
					Log.d(TAG, "snap to right");
					snapToScreen(mCurScreen + 1);
				} else {
					Log.d(TAG, "snapToDestination()");
					snapToDestination();
				}
				if (hentaiVelocityTracker != null) {
					hentaiVelocityTracker.recycle();
					hentaiVelocityTracker = null;
				}
			}

			break;
		}
		return true;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		super.dispatchTrackballEvent(event);
		Log.d(TAG, "onTrackballEvent");
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "onTrackballEvent(ACTION_DOWN)");
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "onTrackballEvent(ACTION_MOVE)X = " + x + "; Y = " + y);
			if (x < 0 && mCurScreen > 0) {
				Log.d(TAG, "left");
				snapToScreen(mCurScreen - 1);
			} else if (x > 0 && mCurScreen < getChildCount() - 1) {
				Log.d(TAG, "right");
				snapToScreen(mCurScreen + 1);
			}
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "onTrackballEvent(ACTION_UP)");
			break;
		}
		return true;

	}

	@Override
	public void computeScroll() {
		// Log.i(TAG,"compute Scroll");
		if (hentaiScroller.computeScrollOffset()) {
			// Log.i(TAG, "computeScroll not stop");
			scrollTo(hentaiScroller.getCurrX(), hentaiScroller.getCurrY());
			postInvalidate(); // redraw
		}
	}

	// ==========custom function=========
	/**
	 * compute which view should be slide to slide to destination
	 * 
	 */
	public void snapToDestination() {
		final int screenWidth = getWidth();
		final int destScreen = (getScrollX() + screenWidth / 2) / screenWidth;
		// Log.d(TAG, String.valueOf(getScrollX()));
		snapToScreen(destScreen);

	}

	/**
	 * slide to certain view with resilience
	 * 
	 * @param index
	 */
	public void snapToScreen(int index) {
		Log.i(TAG, "snapToScreen:index = " + index);
		index = Math.max(0, Math.min(index, getChildCount() - 1));
		if (getScrollX() != (index * getWidth())) {
			// delta < 0 if screen scroll to left else delta > 0 if screen
			// scroll to right
			int delta = index * getWidth() - getScrollX();
			hentaiScroller.startScroll(getScrollX(), 0, delta, 0,
					(int) (Math.abs(delta) * 1.5));
			mCurScreen = index;
			invalidate(); // redraw
			if (hentaiOnViewChangeListener != null) {
				// if the view change,then update dot-view
				hentaiOnViewChangeListener.OnViewChange(mCurScreen);
			} else {
				Log.e(TAG, "There is no hentaiOnViewChangeListener");
			}

		}
	}
	
	public void goToScreen(int index) {
		Log.i(TAG, "snapToScreen:index = " + index);
		index = Math.max(0, Math.min(index, getChildCount() - 1));
		if (getScrollX() != (index * getWidth())) {
			// delta < 0 if screen scroll to left else delta > 0 if screen
			// scroll to right
			int delta = index * getWidth() - getScrollX();
			hentaiScroller.startScroll(getScrollX(), 0, delta, 0, 0);
			mCurScreen = index;
			invalidate(); // redraw
			if (hentaiOnViewChangeListener != null) {
				// if the view change,then update dot-view
				hentaiOnViewChangeListener.OnViewChange(mCurScreen);
			} else {
				Log.e(TAG, "There is no hentaiOnViewChangeListener");
			}

		}
	}

	/**
	 * the over edge space allowed to drag the value should be in [0,0.5) the
	 * space will be screenWidth * overEdgeSpace
	 * 
	 * @param overEdgeSpace
	 */
	public void setOverEdgeSpace(float overEdgeSpace) {
		if (overEdgeSpace >= 0.5f || overEdgeSpace < 0f) {
			Log.e(TAG, "the edgeDrag should be in [0,0.5)");
			return;
		}
		this.overEdgeSpace = overEdgeSpace;
	}

	/**
	 * @return the over edge space allowed to drag
	 */
	public double getOverEdgeSpace() {
		return this.overEdgeSpace;
	}

	public View getCurScreen() {
		return getChildAt(mCurScreen);
	}

	/**
	 * get interpolator from xml
	 * 
	 * @param resID
	 *            The resource identifier of the interpolator to load
	 */
	public void setScrollerInterpolator(int resID) {
		setScrollerInterpolator(AnimationUtils.loadInterpolator(getContext(),
				resID));

	}

	/**
	 * set the scroller's interpolator
	 * 
	 * @param interpolator
	 *            defines the rate of change of an animation. This allows the
	 *            basic animation effects (alpha, scale, translate, rotate) to
	 *            be accelerated, decelerated, repeated, etc.
	 */
	public void setScrollerInterpolator(Interpolator interpolator) {
		this.hentaiScroller = new Scroller(getContext(), interpolator);
	}

	/**
	 * set OnViewChangeListener
	 * 
	 * @param listener
	 */
	public void setOnViewChangeListener(OnViewChangeListener listener) {
		hentaiOnViewChangeListener = listener;
	}

	/**
	 * 
	 * @param deltaX
	 * @return True if view can move ,false otherwise
	 */
	protected boolean isCanMove(int deltaX) {
		int delta = (int) (getWidth() * overEdgeSpace);
		if (getScrollX() <= -delta && deltaX < 0) {
			return false;
		}
		if (getScrollX() >= (getChildCount() - 1) * getWidth() + delta
				&& deltaX > 0) {
			return false;
		}
		return true;
	}

	public interface OnViewChangeListener {
		public void OnViewChange(int view);
	}
}
