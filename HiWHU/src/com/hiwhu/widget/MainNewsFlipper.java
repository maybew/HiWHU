package com.hiwhu.widget;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.hiwhu.UI.NewsPassageActivity;
import com.hiwhu.UI.R;
import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainNewsFlipper extends HentaiScrollLayout implements
		HentaiScrollLayout.OnViewChangeListener {

	Context context;
	
	HttpHelper helper;
	ImageLoader mImageLoader;
	ArrayList<String[]> mContentList;

	Timer mTimer;
	TimerTask mTTask;
	boolean isTimerRun = true;
	boolean onClickState = false;

	final int mNewsNum = 5;
	int mCurView = 0;
	int mViewIndex;

	// 动画
	Animation mPopenter, mPopexit, mFadein;

	HentaiScrollLayout mHentaiScrollLayout;

	RelativeLayout[] mRelativeContent;
	String[] mNewsInfo;
	boolean[] isNewsInited;
	TextView[] mTitleContent;
	TextView[] mSubContent;
	ImageView[] mImageContent;
	LinearLayout[] mLinearContent;

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if (msg.what == 0) {
				if (mCurView == mNewsNum - 1)
					setCurScreen(0);
				else
					setCurScreen(mCurView + 1);
			}
			if (msg.what == 9) {
				ConnectivityManager webManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() == null) return;
				if (mContentList.size() == 0 || mContentList.get(mCurView)[5] == null || mContentList.get(mCurView)[5] == "") return;
				Intent intent = new Intent(context,
						 NewsPassageActivity.class).putExtra("info", mContentList.get(mCurView))
							.putExtra("url", mContentList.get(mCurView)[5])
							.putExtra("choice", 2);
				 context.startActivity(intent);
				 
				
			}
			if (msg.what == 1) {
				//mImageContent[msg.arg1].setImageResource(msg.arg2);
				//mImageContent[msg.arg1].startAnimation(mFadein);
				String temp = (String)msg.obj;
				Bitmap bm = mImageLoader.loadDrawable(temp, new MyImageLoaderCallback(mImageContent[msg.arg1]));
				if(bm != null) {
					mImageContent[msg.arg1].setImageBitmap(bm);
					mImageContent[msg.arg1].startAnimation(mFadein);
				}
			}
		}

	};

	public MainNewsFlipper(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}

	public MainNewsFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}

	public MainNewsFlipper(Context context) {
		super(context);
		this.context = context;
		init();
		// TODO Auto-generated constructor stub
	}

	private void init() {
		mContentList = new ArrayList<String[]>();
		helper = new HttpHelper();
		mImageLoader = new ImageLoader();
		
		mRelativeContent = new RelativeLayout[mNewsNum];
		mLinearContent = new LinearLayout[mNewsNum];
		mImageContent = new ImageView[mNewsNum];
		mNewsInfo = new String[mNewsNum];
		mTitleContent = new TextView[mNewsNum];
		mSubContent = new TextView[mNewsNum];
		isNewsInited = new boolean[mNewsNum];

		mPopenter = AnimationUtils.loadAnimation(context, R.anim.popup_enter);
		mPopexit = AnimationUtils.loadAnimation(context, R.anim.popup_exit);
		mFadein = AnimationUtils.loadAnimation(context, R.anim.fade_in);
		mPopenter.setStartOffset(600);

		// 初始化新闻图片Flipper
		mHentaiScrollLayout = (HentaiScrollLayout) this.findViewById(R.id.main_flipper);
		mHentaiScrollLayout.setOnViewChangeListener(this);
		for (int i = 0; i < mNewsNum; i++) {

			LayoutInflater mInflater = LayoutInflater.from(context);
			View mView = mInflater.inflate(R.layout.main_news_item, null);

			RelativeLayout mNewsRelative = (RelativeLayout) mView
					.findViewById(R.id.main_news_relativelayout);
			ImageView mNewsImage = (ImageView) mView
					.findViewById(R.id.main_news_image);
			LinearLayout mNewsLinear = (LinearLayout) mView
					.findViewById(R.id.main_news_linearLayout);
			TextView mNewsTitle = (TextView) mView
					.findViewById(R.id.main_news_title);
			TextView mNewsSubtitle = (TextView) mView
					.findViewById(R.id.main_news_subtitle);

			mRelativeContent[i] = mNewsRelative;
			mLinearContent[i] = mNewsLinear;
			mImageContent[i] = mNewsImage;
			mTitleContent[i] = mNewsTitle;
			mSubContent[i] = mNewsSubtitle;
			isNewsInited[i] = false;

			mNewsTitle.getPaint().setFakeBoldText(true);
			mNewsLinear.setVisibility(LinearLayout.INVISIBLE);

			mHentaiScrollLayout.addView(mNewsRelative);

		}
		new Thread() {
			@Override
			public void run() {
				initContent();
			}
		}.start();
		startTimer();

		popupNewsTitle(0);
	}

	public void OnViewChange(int view) {
		// TODO Auto-generated method stub
		if (mCurView == view)
			return;

		popupNewsTitle(view);
		mCurView = view;

	}

	private void popupNewsTitle(int index) {
		if (!isNewsInited[index]) {
			if (mNewsInfo[index] == null || mNewsInfo[index].equals(""))
				return;
			mTitleContent[index].setText(mNewsInfo[index].split("@@")[0]);
			mSubContent[index].setText(mNewsInfo[index].split("@@")[1]);
			isNewsInited[index] = true;
		}
		mLinearContent[index].startAnimation(mPopenter);
		mLinearContent[index].setVisibility(LinearLayout.VISIBLE);
		if (index == mCurView)
			return;
		mLinearContent[mCurView].clearAnimation();
		mLinearContent[mCurView].setVisibility(LinearLayout.INVISIBLE);
	}

	private void setCurScreen(int index) {
		this.setScrollerInterpolator(null);
		this.snapToScreen(index);
	}

	private void startTimer() {
		mTimer = new Timer();
		mTTask = new TimerTask() {
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 0;
				handler.sendMessage(msg);
			}
		};

		mTimer.schedule(mTTask, 3000, 7000);
		isTimerRun = true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		final int action = event.getAction();
		final float x = event.getX();

		mTimer.cancel();
		isTimerRun = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			onClickState = true;
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			if (x < mLastMotionX - ViewConfiguration.getTouchSlop() || x > mLastMotionX + ViewConfiguration.getTouchSlop())
				onClickState = false;
			break;
		case MotionEvent.ACTION_UP:
			if (onClickState) {
				Message msg = new Message();
				msg.what = 9;
				handler.sendMessage(msg);
			}
			onClickState = false;
			startTimer();
			break;
		}

		return super.onTouchEvent(event);
	}

	public void initContent() {
		boolean isEnough = false;
		int page = 1;
		int curNum = 0;
		
		ConnectivityManager webManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (webManager.getActiveNetworkInfo() == null) return;
		
		ArrayList<String[]> mContent = new ArrayList<String[]>();
		while (!isEnough) {
			mContent = helper.getNews(ConfigurationUtil.URL_NEWS
					+ "/a/xiaoyuandongtai/list_17_" + page + ".html", "gb2312");
			if(mContent == null) return;
			for(int i=0;i<mContent.size();i++) {
				String[] temp = mContent.get(i);
				if(temp[4] == null || temp[4].equals("")) continue;
				mContentList.add(temp);
				
				mNewsInfo[curNum] = temp[0]+"@@"+temp[3];
				mImageContent[curNum].setTag(temp[4]);
				Message msg = new Message();
				msg.arg1 = curNum;
				msg.obj = temp[4];
				msg.what = 1;
				handler.sendMessage(msg);
				curNum++;
				if(curNum == mNewsNum) {
					isEnough = true;
					break; 
				}
				
			}
			page++;
		}
		
	}
	
	class MyImageLoaderCallback implements ImageLoader.ImageCallback {
		ImageView tv;
		MyImageLoaderCallback(ImageView tv) {
			this.tv = tv;
		}

		@Override
		public void imageLoaded(Bitmap imageDrawable, String imageUrl) {
			// TODO Auto-generated method stub
			if(imageDrawable != null) tv.setImageBitmap(imageDrawable);
		}
		
	}

}
