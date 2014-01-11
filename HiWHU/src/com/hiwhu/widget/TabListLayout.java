package com.hiwhu.widget;

import com.hiwhu.UI.R;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Scroller;

public class TabListLayout extends RelativeLayout {
	private static final String TAG = "TabListLayout";
	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private static final int TOUCH_STATE_DOWN = 2;
	private static final int TOUCH_STATE_TAP = 3;
	private static final int TOUCH_STATE_DONE_WAITING = 4;
	private static final int INVALID_POSITION = -1;
	private static final int INVALID_POINTER = -1;

	private static final float DEF_OVER_EDGE_SPACE = 0.0f;

	private int mLastMotionY;
	private int mMotionY;
	private int mTouchState;
	private float overEdgeSpace;
	private int mFirstVisibleItem;
	private int mCurSelection;

	private int mActivePointerId;
	private int mMotionPosition;
	private int mMotionCorrection;
	// private Rect mTouchFrame;
	private CheckForTap mPendingCheckForTap;
	private PerformClick mPerformClick;
	private LayoutInflater mInflater;
	private TabListView mTabListView;
	private ImageView mSlideBar;
	private Scroller mScroller;

	private OnSelectionChangeListener mOnSelectionChangeListener;

	// ========constructed and initial function============
	public TabListLayout(Context context) {
		super(context);
		init(context);
	}

	public TabListLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TabListLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mScroller = new Scroller(context,
				new AccelerateDecelerateInterpolator());
		mTouchState = TOUCH_STATE_REST;
		overEdgeSpace = DEF_OVER_EDGE_SPACE;
		mActivePointerId = INVALID_POINTER;
		mFirstVisibleItem = 0;
		mCurSelection = 0;

		// --------------add view-----------
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mTabListView = (TabListView) mInflater.inflate(R.layout.tab_list, null);
		// mTabListView.setOnItemClickListener(this);

		mSlideBar = new ImageView(context);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		mSlideBar.setLayoutParams(lp);
		mSlideBar.setImageResource(R.drawable.slide_bar);
		mSlideBar.setScaleType(ScaleType.FIT_CENTER);

		addView(mTabListView);
		addView(mSlideBar);

	}

	// ============ Override =================
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mTabListView.measure(widthMeasureSpec, heightMeasureSpec);		
		mSlideBar.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.makeMeasureSpec(
				mTabListView.getChildHeight(), MeasureSpec.EXACTLY));
		// Log.e(TAG, "onMeasure()");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// Log.e(TAG, "onLayout()" + mSlideBar.getMeasuredWidth() + ";"
		// + mSlideBar.getMeasuredHeight());

		Log.e(TAG, "onLayout()");
		mTabListView.layout(0, 0, mTabListView.getMeasuredWidth(),
				mTabListView.getChildCount() * mTabListView.getChildHeight());
		// int mTop = mCurSelection * mTabListView.getChildHeight();
		mSlideBar.layout(getMeasuredWidth() - mSlideBar.getMeasuredWidth(), 0,
				getMeasuredWidth(), mSlideBar.getMeasuredHeight());

	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled())
			return isClickable() || isLongClickable();

		// Log.i(TAG, "OnTouchEvent");

		final int action = event.getAction();
		int deltaY;
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			 Log.i(TAG, "OnTouchEvent ACTION_DOWN");
			final int y = (int) event.getY();

			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}

			mActivePointerId = event.getPointerId(0);
			int motionPosition = findMotionRow(y);

			if (motionPosition >= 0) {
				mTouchState = TOUCH_STATE_DOWN;
				if (mPendingCheckForTap == null) {
					mPendingCheckForTap = new CheckForTap();
				}
				postDelayed(mPendingCheckForTap,
						ViewConfiguration.getTapTimeout());
			}

			mMotionY = y;
			mLastMotionY = Integer.MIN_VALUE;
			mMotionPosition = motionPosition + mFirstVisibleItem;
			break;
		}
		case MotionEvent.ACTION_MOVE: {
//			 Log.i(TAG, "OnTouchEvent pointerIndex = " + pointerIndex);
			final int pointerIndex = event.findPointerIndex(mActivePointerId);

			 if (pointerIndex == -1)
				 break;
			final int y = (int) event.getY(pointerIndex);
			deltaY = mMotionY - y;
			switch (mTouchState) {
			case TOUCH_STATE_DOWN:
			case TOUCH_STATE_TAP:
			case TOUCH_STATE_DONE_WAITING:
				// if child number <= visible child number
				// there is no need to scroll
				if (mTabListView.getChildCount() > mTabListView
						.getVisibleItemNumOnScreen())
					startScrollIfNeeded(deltaY);
				break;
			case TOUCH_STATE_SCROLLING:

				if (y != mLastMotionY) {
//					Log.d(TAG, "TOUCH_STATE_SCROLLING");
					deltaY -= mMotionCorrection;
					int incrementalDeltaY = mLastMotionY != Integer.MIN_VALUE ? mLastMotionY
							- y
							: deltaY;
					Log.d(TAG, "deltaY = " + deltaY + "; incrementalDeltaY = "
							+ incrementalDeltaY);
					if (isCanMove(incrementalDeltaY)) {
						if (getScrollY() + incrementalDeltaY < 0)
							incrementalDeltaY = -getScrollY();
						else if (getScrollY() + incrementalDeltaY > (mTabListView
								.getChildCount() - mTabListView
								.getVisibleItemNumOnScreen())
								* mTabListView.getChildHeight())
							incrementalDeltaY = (mTabListView.getChildCount() - mTabListView
									.getVisibleItemNumOnScreen())
									* mTabListView.getChildHeight()
									- getScrollY();
						scrollBy(0, incrementalDeltaY);
					}
					mLastMotionY = y;
				}

				break;
			}
			break;

		}
		case MotionEvent.ACTION_UP:
			 Log.i(TAG, "OnTouchEvent ACTION_UP");
			switch (mTouchState) {
			case TOUCH_STATE_DOWN:
			case TOUCH_STATE_TAP:
			case TOUCH_STATE_DONE_WAITING:
				final int motionPosition = mMotionPosition;
				final View child = mTabListView.getChildAt(motionPosition);
				if (child != null && !child.isSelected()) {
					if (mTouchState != TOUCH_STATE_DOWN) {
						child.setPressed(false);
					}
					if (mPerformClick == null) {
						mPerformClick = new PerformClick();
					}

					final PerformClick performClick = mPerformClick;
					performClick.mClickMotionPosition = motionPosition;
					performClick.rememberWindowAttachCount();

					if (mTouchState == TOUCH_STATE_DOWN
							|| mTouchState == TOUCH_STATE_TAP) {
						final Handler handler = getHandler();
						if (handler != null) {
							handler.removeCallbacks(mTouchState == TOUCH_STATE_DOWN ? mPendingCheckForTap
									: null);
						}

						postDelayed(new Runnable() {
							@Override
							public void run() {
								post(performClick);
								mTouchState = TOUCH_STATE_REST;
							}
						}, ViewConfiguration.getPressedStateDuration());
						return true;
					} else {
						post(performClick);
					}
					mTouchState = TOUCH_STATE_REST;
				}

				break;
			case TOUCH_STATE_SCROLLING:
				snapToDestination();
				mTouchState = TOUCH_STATE_REST;
				break;
			}
			mActivePointerId = INVALID_POINTER;
			break;

//		case MotionEvent.ACTION_CANCEL:
//			Log.d(TAG, "ACTION_CANCEL");
//			View v = mTabListView.getChildAt(mMotionPosition);
//			if (v != null) {
//				v.setPressed(false);
//			}
//
//			mActivePointerId = INVALID_POINTER;
//			break;
		}

		return true;
	}

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

	private int findMotionRow(int y) {
		int childCount = mTabListView.getChildCount();
		if (childCount > 0) {
			for (int i = 0; i < childCount; i++) {
				View v = mTabListView.getChildAt(i);
				if (y <= v.getBottom()) {
					return i;
				}
			}
		}
		return INVALID_POSITION;
	}

	private boolean startScrollIfNeeded(int delta) {
		final int distance = Math.abs(delta);
		if (distance > ViewConfiguration.getTouchSlop()) {
			mTouchState = TOUCH_STATE_SCROLLING;
			//save the delta
			mMotionCorrection = delta;

			View motionView = mTabListView.getChildAt(mMotionPosition);
			if (motionView != null) {
				motionView.setPressed(false);
			}
			requestDisallowInterceptTouchEvent(true);
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param delta
	 * @return True if view can move ,false otherwise
	 */
	private boolean isCanMove(int delta) {
		int d = (int) (getHeight() * overEdgeSpace);
		if (getScrollY() <= -d && delta < 0) {
			return false;
		}
		if (getScrollY() >= (mTabListView.getChildCount() - mTabListView
				.getVisibleItemNumOnScreen())
				* mTabListView.getChildHeight()
				+ d && delta > 0) {
			return false;
		}
		return true;
	}
	
	final class CheckForTap implements Runnable {

		@Override
		public void run() {
			if (mTouchState == TOUCH_STATE_DOWN) {
				mTouchState = TOUCH_STATE_TAP;
				final View child = mTabListView.getChildAt(mMotionPosition);
				if (child != null) {

					child.setPressed(true);
					final int longPressTimeout = ViewConfiguration
							.getLongPressTimeout();
					final boolean longClickable = mTabListView
							.isLongClickable();
					if (longClickable) {

					} else {
						mTouchState = TOUCH_STATE_DONE_WAITING;
					}
				} else {
					mTouchState = TOUCH_STATE_DONE_WAITING;
				}
			}

		}
	}

	private class WindowRunnable {
		private int mOriginalAttachCount;

		public void rememberWindowAttachCount() {
			mOriginalAttachCount = getWindowAttachCount();
		}

		public boolean sameWindow() {
			return hasWindowFocus()
					&& getWindowAttachCount() == mOriginalAttachCount;
		}
	}

	private class PerformClick extends WindowRunnable implements Runnable {
		// View mChild;
		int mClickMotionPosition;

		@Override
		public void run() {
			final int motionPosition = mClickMotionPosition;
			if (mTabListView.getVisibleChildCount() > 0
					&& motionPosition != INVALID_POSITION
					&& motionPosition < mTabListView.getVisibleChildCount()
					&& sameWindow()) {
				performItemClick(mClickMotionPosition);
			}
		}
	}

	private void performItemClick(int position) {
		View v = mTabListView.getChildAt(position);
		if (v.getTag() != null) {

			int index = (Integer) (v.getTag());

			// Log.d(TAG, "onClick mCurSelection = " + mCurSelection);
			if (mCurSelection == index) {
				// Log.d(TAG, "onClick mCurSelection == index");
				return;
			}
			if (mOnSelectionChangeListener != null) {
				mOnSelectionChangeListener.onSelectionChange(index);
			}
			setSelection(index);

		}

	}

	/**
	 * compute which view should be slide to slide to destination
	 * 
	 */
	private void snapToDestination() {
		final int itemHeight = mTabListView.getChildHeight();
		final int index = (getScrollY() + itemHeight / 2) / itemHeight;

		// 0<= index <= getChildCount() - TabListView.getVisibleChildNum()
		setFirstVisibleItemOnScreen(index);

	}

	private void setFirstVisibleItemOnScreen(int index) {
		// 0<= index <= getChildCount() - TabListView.getVisibleChildNum()
		index = Math.max(
				0,
				Math.min(
						index,
						mTabListView.getChildCount()
								- mTabListView.getVisibleItemNumOnScreen()));

		if (getScrollY() != index * mTabListView.getChildHeight()) {
			int delta = index * mTabListView.getChildHeight() - getScrollY();
			mScroller.startScroll(0, getScrollY(), 0, delta,
					(int) (Math.abs(delta) * 1.5));
			invalidate();
		}
		mFirstVisibleItem = index;
	}

	private void setItemVisibleOnScreen(int index) {

		if (index < mFirstVisibleItem) {
			setFirstVisibleItemOnScreen(index);
		} else if (index > mFirstVisibleItem
				+ mTabListView.getVisibleItemNumOnScreen() - 1) {
			setFirstVisibleItemOnScreen(index
					- mTabListView.getVisibleItemNumOnScreen() + 1);
		} else {
			Log.d(TAG, "Item:" + index + " is already on screen");
		}

	}

	public void setSelection(int index) {
		Log.d(TAG, "setSelection mCurSelection = " + mCurSelection
				+ " ;index = " + index);
		setItemVisibleOnScreen(index);
		TranslateAnimation mAnimation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, mCurSelection,
				Animation.RELATIVE_TO_SELF, index);

		mAnimation.setFillAfter(true);
		mAnimation.setInterpolator(AnimationUtils
				.loadInterpolator(getContext(),
						android.R.anim.accelerate_decelerate_interpolator));
		mAnimation.setDuration(Math.abs(mCurSelection - index)
				* mSlideBar.getMeasuredHeight() * 2);
		mSlideBar.clearAnimation();
		mSlideBar.startAnimation(mAnimation);

		// Log.e(TAG, "onClick index:" + index + " mCurSelection:" +
		// mCurSelection);

		mTabListView.changeSelection(mCurSelection, index);
		mCurSelection = index;

	}

	public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
		this.mOnSelectionChangeListener = listener;
	}

	public interface OnSelectionChangeListener {
		public void onSelectionChange(int index);
	}



}
