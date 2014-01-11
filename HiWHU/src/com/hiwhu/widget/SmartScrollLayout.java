package com.hiwhu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * 
 * @author Jim
 *
 */
public abstract class SmartScrollLayout extends ViewGroup {

	private static final String TAG = "SmartScrollLayout";
	protected static final int VELOCITY_THRESHOLD = 600;// 600 pixels per second
	protected static final int TOUCH_STATE_REST = 0;
	protected static final int TOUCH_STATE_SCROLLING = 1;
	protected static final float DEF_OVER_EDGE_SPACE = 0.0f;
	protected static final int mDefScreen = 0;

	protected int mCurScreen;
	protected float overEdgeSpace;
	protected int mLastX;
	protected int mLastY;
	protected int mTouchState;
	protected int[] mScrollRange;

	protected Scroller mScroller;
	protected VelocityTracker mVelocityTracker; // judge gesture
	protected OnViewChangeListener mOnViewChangeListener;

	// ========constructed and initial function============
	public SmartScrollLayout(Context context) {
		super(context);
		init(context);
	}

	public SmartScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SmartScrollLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mCurScreen = mDefScreen;
		overEdgeSpace = DEF_OVER_EDGE_SPACE;
		mTouchState = TOUCH_STATE_REST;
		mScrollRange = new int[2];
		mScroller = new Scroller(context);
		
	}

	// ==========Override===========
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// final int width = MeasureSpec.getSize(widthMeasureSpec);
		// final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);

		}
		setScrollRange();
		// scrollTo(mCurScreen * width, 0);
		// Log.d(TAG, "onMeasure()");
	}

	@Override
	protected abstract void onLayout(boolean changed, int left, int top,
			int right, int bottom);
	
	@Override
	public abstract boolean onTouchEvent(MotionEvent event);

	@Override
	public void computeScroll() {
		// Log.i(TAG,"compute Scroll");
		if (mScroller.computeScrollOffset()) {
			// Log.i(TAG, "computeScroll not stop");
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate(); // redraw
		}
	}

	// ==========custom function=========
	/**
	 * compute which view should be slide to
	 * and slide to destination
	 * 
	 */
	public abstract void snapToDestination();

	/**
	 * slide to certain view
	 * 
	 * @param index
	 */
	public abstract void snapToScreen(int index);

	/**
	 * the over edge space allowed to drag the value should be in [0,0.5) the
	 * space will be (screenWidth or screenHeight) * overEdgeSpace
	 * 
	 * @param overEdgeSpace
	 */
	public void setOverEdgeSpace(float overEdgeSpace) {
		if (overEdgeSpace >= 0.5f || overEdgeSpace < 0f) {
			Log.e(TAG, "the edgeDrag should be in [0,0.5)");
			return;
		}
		setScrollRange();
		this.overEdgeSpace = overEdgeSpace;
	}

	/**
	 * @return the over edge space allowed to drag
	 */
	public double getOverEdgeSpace() {
		return this.overEdgeSpace;
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
		this.mScroller = new Scroller(getContext(), interpolator);
	}

	/**
	 * set OnViewChangeListener
	 * 
	 * @param listener
	 */
	public void setOnViewChangeListener(OnViewChangeListener listener) {
		mOnViewChangeListener = listener;
	}

	/**
	 * 
	 * @param delta
	 * @return True if view can move ,false otherwise
	 */
	protected abstract boolean isCanMove(int delta);
	
	/**
	 * set scroll range 
	 */
	protected abstract void setScrollRange();
	
	/**
	 * 
	 * @author Jim
	 *
	 */
	public interface OnViewChangeListener {
		
		public abstract void onViewChange(int view);
	}
}
