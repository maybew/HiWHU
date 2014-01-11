package com.hiwhu.widget;

import com.hiwhu.UI.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TabListView extends LinearLayout {
	private static final String TAG = "TabList";
	private static final int VISIBLE_ITEM_NUM = 4;
	// private int mCurSelection;
	private int mVisibleChildCount;
	ColorStateList mColorStateList;

	// private OnItemClickListener mOnItemClickListener;

	// ========constructed and initial function============
	public TabListView(Context context) {
		super(context);
		init(context);
	}

	public TabListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		this.setClickable(true);
		// mCurSelection = 0;
		mVisibleChildCount = 0;
		mColorStateList = (ColorStateList) getContext().getResources()
				.getColorStateList(R.color.color_selector);
	}

	// ==========Override===========
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// final int width = MeasureSpec.getSize(widthMeasureSpec);
		// final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(
					widthMeasureSpec,
					MeasureSpec.makeMeasureSpec(getMeasuredHeight()
							/ VISIBLE_ITEM_NUM, MeasureSpec.EXACTLY));

		}

		// Log.d(TAG, "onMeasure()");
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (changed) {
			int childTopY = 0;
			final int childCount = getChildCount();
			for (int i = 0; i < childCount; i++) {
				final TextView childView = (TextView) getChildAt(i);
				if (childView.getVisibility() != View.GONE) {
					final int childHeight = childView.getMeasuredHeight();
					childView.layout(0, childTopY,
							childView.getMeasuredWidth(), childTopY
									+ childHeight);
					childView.setTag(mVisibleChildCount);
					childView.setTextColor(mColorStateList);
					childView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20.0f);
					mVisibleChildCount++;
					childTopY += childHeight;
				}
			}
			getChildAt(0).setSelected(true);
		}
		// Log.d(TAG, "onLayout()");
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return false;

	}

	public int getVisibleItemNumOnScreen() {
		return VISIBLE_ITEM_NUM;
	}

	public int getChildHeight() {
		return getMeasuredHeight() / VISIBLE_ITEM_NUM;
	}

	public int getChildWidth() {
		return getMeasuredWidth();
	}

	public int getVisibleChildCount() {
		return mVisibleChildCount;
	}

	public void changeSelection(int oldSelection, int newSelection) {

		getChildAt(oldSelection).setSelected(false);
		getChildAt(newSelection).setSelected(true);

	}

}
