package com.hiwhu.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiwhu.UI.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.SimpleAdapter;

public class BtnsLayout extends VerticalScrollLayout {
	private static final String TAG = "BtnsLayout";
	private static final String btnCustom = "btn_custom_";

	private static final int TOUCH_STATE_DOWN = 2;
	private static final int TOUCH_STATE_TAP = 3;
	private static final int TOUCH_STATE_DONE_WAITING = 4;
	private static final int INVALID_POSITION = -1;
	private static final int INVALID_POINTER = -1;

	private int mMotionY;
	private int mActivePointerId;
	private int mMotionPosition;
	private int mMotionCorrection;

	private int[] btnStrIdArray;
	private int[] btnImageIdArray;
	private String[] btnStrArray;
	private boolean isLongPress;

	private MattsLayout mCurBtnGroup;

	private CheckForTap mPendingCheckForTap;
	private CheckForLongPress mPendingCheckForLongPress;
	private PerformClick mPerformClick;
	private Rect mTouchFrame;
	

	private OnBtnClickListener mOnBtnClickListener;


	// ========constructed and initial function============
	public BtnsLayout(Context context) {
		super(context);
		init();

	}

	public BtnsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BtnsLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mActivePointerId = INVALID_POINTER;
		isLongPress = false;
		btnImageIdArray = new int[] { R.drawable.btn_default,
				R.drawable.btn_cheng_ji_dan, R.drawable.btn_ke_cheng_biao,
				R.drawable.btn_xuan_ke, R.drawable.btn_xiao_yuan_ka,
				R.drawable.btn_xin_wen, R.drawable.btn_jiang_zuo,
				R.drawable.btn_zhao_pin, R.drawable.btn_zhou_bian,
				R.drawable.btn_yin_si_kong_zhi, R.drawable.liu_liang_tong_ji, };
		btnStrIdArray = new int[] { R.string.btn_default,
				R.string.cheng_ji_dan, R.string.ke_cheng_biao,
				R.string.xuan_ke, R.string.xiao_yuan_ka, R.string.xin_wen,
				R.string.jiang_zuo, R.string.zhao_pin, R.string.zhou_bian,
				R.string.yin_si_kong_zhi, R.string.liu_liang_tong_ji };
		btnStrArray = new String[btnStrIdArray.length];
		btnStrArray[0] = "取消关联";
		for (int i = 1; i < btnStrArray.length; i++) {
			btnStrArray[i] = getContext().getString(btnStrIdArray[i]);
		}

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mCurBtnGroup != null)
			return;
		mCurBtnGroup = (MattsLayout) getChildAt(0);
		SharedPreferences set = getContext().getSharedPreferences("user_data",
				Context.MODE_PRIVATE);
		for (int i = 0; i < 4; i++) {
			int which = set.getInt(btnCustom + i, -1);
			if (which <= 0)
				continue;
			MyImageButton btn = (MyImageButton) mCurBtnGroup.getChildAt(i);
			btn.setImage(btnImageIdArray[which]);
			btn.setText(btnStrIdArray[which]);
			btn.setLongPressable(true);
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// Log.i(TAG, "OnTouchEvent");
		final int action = event.getAction();
		int deltaY;
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();

		}
		mVelocityTracker.addMovement(event);

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			Log.i(TAG, "OnTouchEvent ACTION_DOWN");
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();

			}

			mActivePointerId = event.getPointerId(0);
			final int x = (int) event.getX();
			final int y = (int) event.getY();

			if (getScrollY() != (mCurScreen * getHeight())) {
				mTouchState = TOUCH_STATE_SCROLLING;
				mMotionCorrection = 0;
			} else {
				int motionPosition = pointToPosition(x, y);
				if (motionPosition >= 0) {
					// Log.d(TAG, "motionPosition = " + motionPosition);
					mTouchState = TOUCH_STATE_DOWN;
					if (mPendingCheckForTap == null) {
						mPendingCheckForTap = new CheckForTap();
					}
					postDelayed(mPendingCheckForTap,
							ViewConfiguration.getTapTimeout());
				} else {
					mTouchState = TOUCH_STATE_SCROLLING;
					mMotionCorrection = 0;
				}
				mMotionPosition = motionPosition;
			}

			mMotionY = y;
			mLastY = Integer.MIN_VALUE;

			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = event.findPointerIndex(mActivePointerId);
			if (pointerIndex == -1)
				break;
			final int y = (int) event.getY(pointerIndex);

			deltaY = mMotionY - y;
			switch (mTouchState) {
			case TOUCH_STATE_DOWN:
			case TOUCH_STATE_TAP:
			case TOUCH_STATE_DONE_WAITING:
				startScrollIfNeeded(deltaY);
				break;
			case TOUCH_STATE_SCROLLING:

				if (y != mLastY) {

					deltaY -= mMotionCorrection;
					int correctionY = mLastY != Integer.MIN_VALUE ? mLastY - y
							: deltaY;
					if (isCanMove(correctionY)) {
						if (getScrollY() + correctionY < mScrollRange[0])
							correctionY = mScrollRange[0] - getScrollY();
						else if (getScrollY() + correctionY > mScrollRange[1])
							correctionY = mScrollRange[1] - getScrollY();
						// delta > 0 if screen move to below ; delta < 0 if
						// screen move
						// to above
						scrollBy(0, correctionY);
					}
					mLastY = y;
				}
				break;
			}

			break;
		}
		case MotionEvent.ACTION_UP:

			switch (mTouchState) {
			case TOUCH_STATE_DOWN:
			case TOUCH_STATE_TAP:
			case TOUCH_STATE_DONE_WAITING:
				final int motionPosition = mMotionPosition;
				if (mCurBtnGroup == null)
					mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);
				final View child = mCurBtnGroup.getChildAt(motionPosition);
				if (child != null) {
					if (mTouchState != TOUCH_STATE_DOWN)
						child.setPressed(false);

					if (mPerformClick == null)
						mPerformClick = new PerformClick();

					final PerformClick performClick = mPerformClick;
					performClick.mClickMotionPosition = motionPosition;
					performClick.rememberWindowAttachCount();

					if (mTouchState == TOUCH_STATE_DOWN
							|| mTouchState == TOUCH_STATE_TAP) {
						final Handler handler = getHandler();
						if (handler != null) {
							handler.removeCallbacks(mTouchState == TOUCH_STATE_DOWN ? mPendingCheckForTap
									: mPendingCheckForLongPress);
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
				int velocityY = 0;
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
				break;
			}
			mActivePointerId = INVALID_POINTER;
			break;
		}
		invalidate();
		return true;
	}

	// ======= custom function ===========
	private int pointToPosition(int x, int y) {
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}
		final MattsLayout group = (MattsLayout) getChildAt(mCurScreen);
		final int count = group.getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View button = group.getChildAt(i);
			if (button.getVisibility() == View.VISIBLE) {
				button.getHitRect(frame);
				if (frame.contains(x, y)) {
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
			// save the delta
			mMotionCorrection = delta;
			
			final Handler handler = getHandler();
			if (handler != null) {
				handler.removeCallbacks(mPendingCheckForLongPress);
			}
			if (mCurBtnGroup == null)
				mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);

			View motionView = mCurBtnGroup.getChildAt(mMotionPosition);
			if (motionView != null) {
				motionView.setPressed(false);
			}
			requestDisallowInterceptTouchEvent(true);
			return true;
		}
		return false;
	}

	final class CheckForTap implements Runnable {

		@Override
		public void run() {
			if (mTouchState == TOUCH_STATE_DOWN) {
				mTouchState = TOUCH_STATE_TAP;
				if (mCurBtnGroup == null)
					mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);

				MyImageButton child = (MyImageButton) mCurBtnGroup
						.getChildAt(mMotionPosition);
				if (child != null) {

					child.setPressed(true);
					final int longPressTimeout = ViewConfiguration
							.getLongPressTimeout();

					final boolean longClickable = child.isLongPressable();
					Log.d(TAG, "longClickable" + longClickable);
					if (longClickable) {
						if (mPendingCheckForLongPress == null) {
							mPendingCheckForLongPress = new CheckForLongPress();
						}
						mPendingCheckForLongPress.rememberWindowAttachCount();
						postDelayed(mPendingCheckForLongPress, longPressTimeout);

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

			if (mCurBtnGroup == null)
				mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);

			if (mCurBtnGroup.getChildCount() > 0
					&& motionPosition != INVALID_POSITION
					&& motionPosition < mCurBtnGroup.getChildCount()
					&& sameWindow()) {
				View v = mCurBtnGroup.getChildAt(motionPosition);
				if (mOnBtnClickListener != null) {
					isLongPress = false;
					mOnBtnClickListener.performItemClick((Integer) v.getTag());
				}
			}
		}
	}

	private class CheckForLongPress extends WindowRunnable implements Runnable {
		@Override
		public void run() {
			final int motionPosition = mMotionPosition;
			final View child = mCurBtnGroup.getChildAt(mMotionPosition);

			if (child != null) {
				boolean handled = false;
				if (sameWindow())
					handled = performItemLongPress(motionPosition);
				if (handled) {
					mTouchState = TOUCH_STATE_REST;
					child.setPressed(false);
				} else {
					mTouchState = TOUCH_STATE_DONE_WAITING;
				}
			}
		}
	}

	private boolean performItemLongPress(int position) {
		boolean handled = false;
		isLongPress = true;
		showDialog();
		return true;
	}

//	private void performItemClick(int position) {
//		if (mCurBtnGroup == null)
//			mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);
//
//		View v = mCurBtnGroup.getChildAt(position);
//		switch ((Integer) v.getTag()) {
//		case R.string.btn_default:
//			isLongPress = false;
//			showDialog();
//			Log.d(TAG, "btn_default");
//			break;
//		case R.string.cheng_ji_dan:
//			Log.d(TAG, "cheng_ji_dan");
//			break;
//		case R.string.ke_cheng_biao:
//			Log.d(TAG, "ke_cheng_biao");
//			break;
//		}
//	}

	public void showDialog() {
		SimpleAdapter adapter = new SimpleAdapter(getContext(),
				getDialogData(), R.layout.btn_dialog_item, new String[] {
						"img", "text" }, new int[] {
						R.id.btn_dialog_item_image, R.id.btn_dialog_itme_text });
		new AlertDialog.Builder(getContext())
				.setTitle("请选择功能")
				.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						MyImageButton btn = (MyImageButton) mCurBtnGroup
								.getChildAt(mMotionPosition);
						if (which == 0) {
							if (!isLongPress)
								return;
							btn.setLongPressable(false);
						}

						btn.setImage(btnImageIdArray[which]);
						btn.setText(btnStrIdArray[which]);
						if (!isLongPress)
							btn.setLongPressable(true);
						SharedPreferences.Editor editor = getContext()
								.getSharedPreferences("user_data",
										Context.MODE_PRIVATE).edit();
						editor.putInt(btnCustom + mMotionPosition, which);
						editor.commit();

					}
				}).create().show();

	}

	private List<Map<String, Object>> getDialogData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		for (int i = 0; i < btnStrArray.length; i++) {
			map = new HashMap<String, Object>();
			map.put("img", btnImageIdArray[i]);
			map.put("text", btnStrArray[i]);
			list.add(map);
		}

		return list;
	}

	@Override
	public void snapToScreen(int index) {
		int temp = mCurScreen;
		super.snapToScreen(index);

		if (temp != mCurScreen) {
			mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);
		}
	}

	@Override
	public void changeView(int index) {
		super.changeView(index);
		mCurBtnGroup = (MattsLayout) getChildAt(mCurScreen);
	}

	
	public void setOnBtnClickListener(OnBtnClickListener l){
		this.mOnBtnClickListener = l;
	}
	
	public interface OnBtnClickListener{
		public abstract void performItemClick(int btnId);
	}
}
