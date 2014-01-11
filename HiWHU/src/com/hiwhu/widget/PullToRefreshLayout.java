package com.hiwhu.widget;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.hiwhu.UI.R;

public class PullToRefreshLayout extends RelativeLayout {
	private static final String TAG = "PullToRefreshLayout";

	private static final int INITIAL_STATE = 0;
	private static final int PULL_TO_REFRESH = 1;
	private static final int RELEASE_TO_REFRESH = 2;
	private static final int REFRESHING = 3;

	private final int SLEEP_TIME = 1000;
	private final int DURATION = 500;

	private LayoutInflater mInflater;
	private RelativeLayout mHeaderLayout;
	private ImageView mArrowImageView;
	private TextView mTipsTextView;
	private TextView mUpdateTimeTextView;
	private ProgressBar mProgressBar;
	private ListView mListView;

	private RotateAnimation mAnimation;;
	private RotateAnimation mReverseAnimation;

	private int mState;
	private boolean isViewChanged;
	private int mLastMotionY;
	private Date mDate;
	private int HEADER_HEIGHT;

	private Scroller mScroller;
	private OnRefreshListener mOnRefreshListener;

	// ========constructed and initial function============
	public PullToRefreshLayout(Context context) {
		super(context);
		init(context);
	}

	public PullToRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {

		mScroller = new Scroller(context,
				new AccelerateDecelerateInterpolator());
		mState = INITIAL_STATE;
		HEADER_HEIGHT = 0;
		isViewChanged = false;

		// --------add header and ListView-------------
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mHeaderLayout = (RelativeLayout) mInflater.inflate(
				R.layout.pull_to_fresh_header_layout, null);
		FrameLayout.LayoutParams lp01 = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		lp01.gravity = Gravity.CENTER_HORIZONTAL;
		mHeaderLayout.setLayoutParams(lp01);

		mListView = new ListView(context);
		RelativeLayout.LayoutParams lp02 = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT,
				RelativeLayout.LayoutParams.FILL_PARENT);

		mListView.setLayoutParams(lp02);
		mListView.setCacheColorHint(0x00000000);
		addView(mHeaderLayout);
		addView(mListView);

		// ---------get other views-------------
		mArrowImageView = (ImageView) mHeaderLayout
				.findViewById(R.id.pull_to_refresh_head_arrow);
		mTipsTextView = (TextView) mHeaderLayout
				.findViewById(R.id.pull_to_refresh_head_tips);
		mUpdateTimeTextView = (TextView) mHeaderLayout
				.findViewById(R.id.pull_to_refresh_head_last_update_time);
		mProgressBar = (ProgressBar) mHeaderLayout
				.findViewById(R.id.pull_to_refresh_head_progress_bar);

		// ---------initialize animation-----------
		mAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mAnimation.setFillAfter(true);
		mAnimation.setDuration(250);

		mReverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		mReverseAnimation.setFillAfter(true);
		mReverseAnimation.setDuration(250);

	}

	// ========== Override ===========
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {

		final int action = event.getAction();
		int y = (int) event.getY();

		// make sure the screen in right position
		if (getScrollY() > 0) {
			scrollBy(0, -getScrollY());
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// Log.d(TAG, "dispatchTouchEvent ACTION_DOWN " + getScrollY());
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:

			int pos = mListView.getFirstVisiblePosition();

			if (pos == 0 && getScrollY() <= 0) {
				int deltaY = mLastMotionY - y;
				mLastMotionY = y;
				int top = mListView.getChildAt(0).getTop();
				if (top == 0 && deltaY != 0) {

					// Log.d(TAG, "dispatchTouchEvent ACTION_MOVE " +
					// getScrollY());
					scrollBy(0, deltaY);

					// make sure the screen in right position
					if (getScrollY() > 0) {
						scrollBy(0, -getScrollY());
						break;
					}
					onMove();
					
					return false;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			// Log.d(TAG, "dispatchTouchEvent ACTION_UP " + getScrollY());
			if (getScrollY() < 0) {
				onRelease();
				return false;
			}
			break;

		}
		invalidate();
		return super.dispatchTouchEvent(event);

	}

	@Override
	public void computeScroll() {
		// Log.i(TAG,"compute Scroll");
		if (mScroller.computeScrollOffset()) {
			Log.i(TAG, "computeScroll not stop");
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate(); // redraw
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// Log.d(TAG, "onLayout changed: " + changed + " ; l: " + left +
		// " ; t: "
		// + top + " ; r: " + right + " ; b: " + bottom);
		// Log.e(TAG, "mHeaderLayout " + mHeaderLayout.getHeight());
		// Log.e(TAG, "mListView " + mListView.getMeasuredHeight());

		if (mHeaderLayout.getVisibility() != View.GONE) {
			mHeaderLayout.layout(0, -mHeaderLayout.getMeasuredHeight(),
					mHeaderLayout.getMeasuredWidth(), 0);
			// HEADER_HEIGHT is confirmed after onMeasure()
			HEADER_HEIGHT = mHeaderLayout.getHeight();
		}
		if (mListView.getVisibility() != View.GONE)
			mListView.layout(0, 0, mListView.getMeasuredWidth(),
					mListView.getMeasuredHeight());

	}

	// ============= custom function ============

	/**
	 * monitoring the change of position, then change the state and update View
	 */
	private void onMove() {
		// Log.d(TAG, "onMove() mState = " + mState);
		if (getScrollY() > 0) {
			Log.e(TAG, "onMove() error getScrollY() > 0");
			return;
		}
		int offset = Math.abs(getScrollY());
		switch (mState) {
		case INITIAL_STATE:
			if (offset < HEADER_HEIGHT) {
				mState = PULL_TO_REFRESH;
			} else if (offset >= HEADER_HEIGHT) {
				mState = RELEASE_TO_REFRESH;
				isViewChanged = true;
				updateView();

			}
			break;
		case PULL_TO_REFRESH:
			if (offset == 0) {
				mState = INITIAL_STATE;
			} else if (offset >= HEADER_HEIGHT) {
				mState = RELEASE_TO_REFRESH;
				isViewChanged = true;
				updateView();
			}
			break;
		case RELEASE_TO_REFRESH:
			if (offset < HEADER_HEIGHT) {
				mState = PULL_TO_REFRESH;
				isViewChanged = true;
				updateView();
			}
			break;
			
		}
		
		invalidate();
		
	}

	/**
	 * Check the current state, confirm whether it needs to be refreshed
	 */
	private void onRelease() {
		// Log.d(TAG, "onRelease() mState = " + mState);
		if (getScrollY() > 0) {
			Log.e(TAG, "onMove() error getScrollY() > 0");
			return;
		}

		switch (mState) {

		case PULL_TO_REFRESH:

			mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), DURATION);
			mState = INITIAL_STATE;

			break;
		case RELEASE_TO_REFRESH:

			int delta = -HEADER_HEIGHT - getScrollY();
			mScroller.startScroll(0, getScrollY(), 0, delta, DURATION);
			mState = REFRESHING;
			updateView();
			if (mOnRefreshListener != null)
				mOnRefreshListener.onRefresh();

			//onRefreshComplete();
			break;

		case REFRESHING:
			int d = -HEADER_HEIGHT - getScrollY();
			mScroller.startScroll(0, getScrollY(), 0, d, DURATION);
			break;
		}
		invalidate();
	}

	/**
	 * Check the current state, and update view
	 */
	private void updateView() {
		// Log.d(TAG, "updateView");
		switch (mState) {
		case INITIAL_STATE:
			mTipsTextView.setText(R.string.pull_to_fresh);
			if (mProgressBar.getVisibility() != View.GONE) {
				mProgressBar.setVisibility(View.GONE);
			}
			if (mArrowImageView.getVisibility() != View.VISIBLE) {
				mArrowImageView.setVisibility(View.VISIBLE);
			}
			mArrowImageView.clearAnimation();
			if (isViewChanged) {
				SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
				String date = formatter.format(mDate);
				String str = getContext().getString(R.string.last_update_time);
				StringBuilder sb = new StringBuilder(str).append(" " + date);
				this.mUpdateTimeTextView.setText(sb.toString());
				mArrowImageView.setImageResource(R.drawable.arrow);
				isViewChanged = false;
			}
			break;
		case PULL_TO_REFRESH:
			mTipsTextView.setText(R.string.pull_to_fresh);
			if (mProgressBar.getVisibility() != View.GONE) {
				mProgressBar.setVisibility(View.GONE);
			}
			if (mArrowImageView.getVisibility() != View.VISIBLE) {
				mArrowImageView.setVisibility(View.VISIBLE);
			}
			if (isViewChanged) {
				mArrowImageView.setAnimation(mReverseAnimation);
				mReverseAnimation.start();
				isViewChanged = false;
			}
			break;
		case RELEASE_TO_REFRESH:
			mTipsTextView.setText(R.string.release_to_fresh);
			if (mProgressBar.getVisibility() != View.GONE) {
				mProgressBar.setVisibility(View.GONE);
			}
			if (mArrowImageView.getVisibility() != View.VISIBLE) {
				mArrowImageView.setVisibility(View.VISIBLE);
			}
			if (isViewChanged) {
				mArrowImageView.setAnimation(mAnimation);
				mAnimation.start();
				isViewChanged = false;
			}
			break;
		case REFRESHING:
			mTipsTextView.setText(R.string.refreshing);
			if (mProgressBar.getVisibility() != View.VISIBLE) {
				mProgressBar.setVisibility(View.VISIBLE);
			}
			if (mArrowImageView.getVisibility() != View.GONE) {
				mArrowImageView.setVisibility(View.GONE);
				mArrowImageView.setImageDrawable(null);
			}

			break;
		}
		invalidate();
	}

	// =========== public custom function =========

	/**
	 * call to reset state and view after refresh
	 */
	public void onRefreshComplete() {
		// must use handler to update view
		final Handler handler = new Handler(Looper.getMainLooper()) {
			public void handleMessage(Message msg) {
				mDate = new Date(System.currentTimeMillis());
				mState = INITIAL_STATE;
				isViewChanged = true;
				updateView();
				mScroller.startScroll(0, getScrollY(), 0, -getScrollY(),
						DURATION);
			}
		};
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				handler.sendMessage(handler.obtainMessage());
			}

		}).start();

	}

	/**
	 * call to reset state and view after refresh
	 */
	public void onRefreshComplete2() {
		// must use handler to update view

				mDate = new Date(System.currentTimeMillis());
				mState = INITIAL_STATE;
				isViewChanged = true;
				updateView();
				mScroller.startScroll(0, getScrollY(), 0, -getScrollY(),
						DURATION);



	}
	/**
	 * call to get the ListView in the layout
	 * 
	 * @return ListView
	 */
	public ListView getListView() {

		return this.mListView;
	}

	/**
	 * call to set onRefreshListener, tell the Layout how to update the data
	 * 
	 * @param onRefreshListener
	 */
	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.mOnRefreshListener = onRefreshListener;
	}

	public interface OnRefreshListener {
		public void onRefresh();
	}
}
