package com.hiwhu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class MattsLayout extends RelativeLayout {
	private static final String TAG = "MattsLayout";

	// ========constructed and initial function============
	public MattsLayout(Context context) {
		super(context);

	}

	public MattsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public MattsLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {

		final int childCount = getChildCount();
		if (childCount > 4) {
			Log.e(TAG, "child number out of bound");
			return;
		}

		int childLeft = 0;
		int childTop = 0;
		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);
			final int childWidth = childView.getMeasuredWidth();
			final int childHeight = childView.getMeasuredHeight();
			int paddingX = (getMeasuredWidth() - 2 * childWidth) / 3;
			int paddingY = (getMeasuredHeight() - 2 * childHeight) / 3;
			final int d = paddingX < paddingY ? paddingX : paddingY;
			paddingX = (getMeasuredWidth() - d - 2 * childWidth) / 2;
			paddingY = (getMeasuredHeight() - d - 2 * childHeight) / 2;
//			Log.d(TAG, "onLayout()" + childWidth + ";" + childHeight + ";"
//					+ paddingX + ";" + paddingY + ";");
			if (childView.getVisibility() != View.GONE) {
				switch (i) {
				case 0:
					childLeft = paddingX;
					childTop = paddingY;
					break;
				case 1:
					childLeft += childWidth + d;
					// childTop = paddingY;
					break;
				case 2:
					childLeft = paddingX;
					childTop += childHeight + d;
					break;
				case 3:
					childLeft += childWidth + d;
					// childTop = paddingY + childHeight + d
					break;
				}
				childView.layout(childLeft, childTop, childLeft + childWidth,
						childTop + childHeight);
			}
		}

//		Log.d(TAG, "onLayout()");
	}
}
