package com.hiwhu.UI;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hiwhu.widget.OvershootScrollLayout;
import com.hiwhu.widget.HentaiScrollLayout;

public class NewFeatureActivity extends Activity implements
		HentaiScrollLayout.OnViewChangeListener, OnClickListener {
	private final float OverEdgeSpace = 0.2f;
	private int mViewCount;
	private int mCurSel;// current dot index
	private ImageView[] mImageViews;
	private OvershootScrollLayout guideScroller;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_feature_activity);
		init();
	}

	// initialize
	private void init() {

		guideScroller = (OvershootScrollLayout) findViewById(R.id.ScrollLayout);
		guideScroller.setOverEdgeSpace(OverEdgeSpace);
		guideScroller.setOnViewChangeListener(this);
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llayout);
		for (int i = 0; i < 6; i++) {
			FrameLayout temp = new FrameLayout(this);
			switch (i) {
			case 0:
				temp.setBackgroundResource(R.drawable.guide01);
				break;
			case 1:
				temp.setBackgroundResource(R.drawable.guide02);
				break;
			case 2:
				temp.setBackgroundResource(R.drawable.guide03);
				break;
			case 3:
				temp.setBackgroundResource(R.drawable.guide04);
				break;
			case 4:
				temp.setBackgroundResource(R.drawable.guide05);
				break;
			case 5:
				temp.setBackgroundResource(R.drawable.guide06);
				break;
			}
			guideScroller.addView(temp);
		}
		mViewCount = guideScroller.getChildCount();
		mImageViews = new ImageView[mViewCount];
		for (int i = 0; i < mViewCount; i++) {
			ImageView temp = new ImageView(this);
			temp.setImageResource(R.drawable.guide_round);
			temp.setPadding(10, 10, 10, 10);
			linearLayout.addView(temp);
			mImageViews[i] = temp;
			mImageViews[i].setEnabled(true);
			mImageViews[i].setOnClickListener(this);
			mImageViews[i].setTag(i);
		}
		mCurSel = 0;
		// make the first dot white
		mImageViews[mCurSel].setEnabled(false);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!guideScroller.onTouchEvent(event)) {
			this.finish();
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		int pos = (Integer) (v.getTag());
		setCurPoint(pos);
		guideScroller.setScrollerInterpolator(null);
		guideScroller.snapToScreen(pos);

	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		Log.d("@@@", "onTrackballEvent");
		return guideScroller.onTrackballEvent(event);

	}

	@Override
	public void OnViewChange(int view) {
		setCurPoint(view);
	}

	private void setCurPoint(int index) {
		if (index < 0 || index > mViewCount - 1 || mCurSel == index) {
			// if the clicked dot index is out of range or equals current dot
			// index
			return;
		}
		mImageViews[mCurSel].setEnabled(true);
		mImageViews[index].setEnabled(false);
		mCurSel = index;
	}
}