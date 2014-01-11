package com.hiwhu.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.TextHelper;
import com.hiwhu.tool.ContentDrawTool;
import com.hiwhu.widget.FuncImageButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.Process;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class NewsPassageActivity extends Activity implements OnClickListener {

	private int mViewCount;
	private int mCurSel;// current dot index
	private Button[] mButtonViews;
	private ContentDrawTool mContentDrawTool;
	private ArrayList<String> dividedPageContent;
	private Timer mTime;
	private FuncImageButton back;
	private ViewPager mViewPager;
	private ArrayList<ContentView> mContentViews;
	private MyAdapter mAdapter;
	private String mUrl;
	private String[] mInfo;
	String start = "", temp = "", contentResult = "";
	private int choice = 0;
	private long upload, receive;;
	private SharedPreferences userData;

	private int screenWidth;

	ProgressDialog mProgress;
	HttpHelper helper;
	TextHelper textHelper;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				mProgress.hide();
				mAdapter.notifyDataSetChanged();

				upload = TrafficStats.getUidTxBytes(Process.myUid()) - upload;
				receive = TrafficStats.getUidRxBytes(Process.myUid()) - receive;
				upload /= 2;
				receive /= 2;
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date();
				String time = format.format(date);
				if (time.equals(userData.getString(
						ConfigurationUtil.FLOW_2_TIME, "2000-01-01"))) {
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_2_DAY_UPLOAD,
									userData.getLong(
											ConfigurationUtil.FLOW_2_DAY_UPLOAD,
											0)
											+ upload).commit();
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_2_DAY_RECEIVE,
									userData.getLong(
											ConfigurationUtil.FLOW_2_DAY_RECEIVE,
											0)
											+ receive).commit();
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_2_MONTH_UPLOAD,
									userData.getLong(
											ConfigurationUtil.FLOW_2_MONTH_UPLOAD,
											0)
											+ upload).commit();
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_2_MONTH_RECEIVE,
									userData.getLong(
											ConfigurationUtil.FLOW_2_MONTH_RECEIVE,
											0)
											+ receive).commit();
				} else {
					userData.edit()
							.putLong(ConfigurationUtil.FLOW_2_DAY_UPLOAD,
									upload).commit();
					userData.edit()
							.putLong(ConfigurationUtil.FLOW_2_DAY_RECEIVE,
									receive).commit();
					int month = 0;
					try {
						month = format.parse(
								userData.getString(
										ConfigurationUtil.FLOW_2_TIME,
										"2000-01-01")).getMonth();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (month == date.getMonth()) {
						userData.edit()
								.putLong(
										ConfigurationUtil.FLOW_2_MONTH_UPLOAD,
										userData.getLong(
												ConfigurationUtil.FLOW_2_MONTH_UPLOAD,
												0)
												+ upload).commit();
						userData.edit()
								.putLong(
										ConfigurationUtil.FLOW_2_MONTH_RECEIVE,
										userData.getLong(
												ConfigurationUtil.FLOW_2_MONTH_RECEIVE,
												0)
												+ receive).commit();
					} else {
						userData.edit()
								.putLong(ConfigurationUtil.FLOW_2_MONTH_UPLOAD,
										upload).commit();
						userData.edit()
								.putLong(
										ConfigurationUtil.FLOW_2_MONTH_RECEIVE,
										receive).commit();
					}
					userData.edit()
							.putString(ConfigurationUtil.FLOW_2_TIME, time)
							.commit();
				}

			}
			// guideScroller.postInvalidate();
			if (msg.what == 0) {
				if(msg.arg1 == 0) Toast.makeText(getApplicationContext(), "读取内容失败，请稍后重试",
						Toast.LENGTH_SHORT).show();
				init();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_passage_layout);

		screenWidth = ContentDrawTool.getDeviceScreenWidth(this);

		userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
		// progress
		mProgress = new ProgressDialog(this);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setMessage("加载中，请稍后");
		mProgress.show();

		back = (FuncImageButton) this.findViewById(R.id.back);

		mViewPager = (ViewPager) findViewById(R.id.info_content_pager);
		mContentViews = new ArrayList<ContentView>();
		mAdapter = new MyAdapter();
		mViewPager.setAdapter(mAdapter);

		mContentDrawTool = new ContentDrawTool(this);

		mInfo = this.getIntent().getStringArrayExtra("info");
		mUrl = this.getIntent().getStringExtra("url");
		choice = this.getIntent().getIntExtra("choice", 0);

		helper = new HttpHelper();
		textHelper = new TextHelper();

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NewsPassageActivity.this.finish();
			}

		});

		loadContent();
		mTime = new Timer();
		mTime.schedule(new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = 9;
				handler.sendMessage(msg);
			}

		}, 1000, 1000);

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				setCurPoint(arg0);
			}

		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mProgress.dismiss();
		mTime.cancel();
		this.finish();
	}

	private void init() {

		// content = contentResult;

		dividedPageContent = mContentDrawTool.dividePage(contentResult);
		for (int i = 0; i < dividedPageContent.size(); i++) {
			addContentView(dividedPageContent.get(i));
		}

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llayout);
		mViewCount = dividedPageContent.size();

		mButtonViews = new Button[mViewCount];
		for (int i = 0; i < mViewCount; i++) {

			Button temp = new Button(this);
			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
					24 * screenWidth / 480, 56 * screenWidth / 480);
			// temp.setBackgroundResource(R.drawable.guide_round);
			temp.setBackgroundResource(R.drawable.dot_black);
			temp.setPadding(0, 0, 0, 0);
			temp.setGravity(Gravity.CENTER);
			linearLayout.addView(temp, p);

			mButtonViews[i] = temp;
			mButtonViews[i].setEnabled(true);
			mButtonViews[i].setOnClickListener(this);
			mButtonViews[i].setTag(i);
		}
		mCurSel = 0;
		// make the first dot white

		mButtonViews[mCurSel].setEnabled(false);
		mButtonViews[mCurSel].setBackgroundResource(R.drawable.dot_white);
		mButtonViews[mCurSel].setText(String.valueOf(mCurSel + 1));

		Message msg = new Message();
		msg.what = 1;
		handler.sendMessage(msg);
	}

	private void loadContent() {
		upload = TrafficStats.getUidTxBytes(Process.myUid());
		receive = TrafficStats.getUidRxBytes(Process.myUid());
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				String temp;
				try {
					switch (choice) {
					case 1:
						temp = helper.getContent(mUrl, "utf-8");
						temp = temp.replaceAll("<div class=\"clear\">", "@@")
								.replaceAll("责任编辑", "@@");
						temp = temp.split("@@")[1]; // +
													// temp.split("@@")[2];
						temp = textHelper.contentDeal(temp, choice);
						temp = temp.replaceAll("\\&[a-zA-Z]{1,10};", "")
								.replaceAll("<[^>]*>", "");
						contentResult += temp;
						break;
					case 2:
						temp = helper.getContent(mUrl, "gb2312");
						temp = temp.replaceAll("<div class=\"entrycontent\">",
								"@@");
						temp = temp.split("@@")[1].replaceAll("\\(责任", "@@")
								.split("@@")[0];
						temp = textHelper.contentDeal(temp, choice);
						temp = temp.replaceAll("\\&[a-zA-Z]{1,10};", "")
								.replaceAll("<[^>]*>", "");
						contentResult += temp;
						break;
					case 3:
						temp = helper.getContent(mUrl, "gb2312");
						temp = temp.replaceAll("<div class=\"showb\">", "@@")
								.split("@@")[1];
						temp = temp
								.replaceAll("<div class=\"sb_rinfo\">", "@@")
								.split("@@")[0];
						temp = temp.replaceAll("</script>", "></script>");
						temp = temp.replaceAll("<script([^>])*>", "<script><");
						temp = temp.replaceAll("&#8226;", "·");
						Pattern p = Pattern.compile("<script([^>])*>");
						Matcher m = p.matcher(temp);
						while (m.find()) {
							temp = temp.replaceAll(m.group(), "");
						}
						temp = temp.replaceAll("\\&[a-zA-Z]{1,10};", "")
								.replaceAll("<[^>]*>", "");
						temp = temp.replaceAll("\r\n", "@@0");
						temp = temp.replaceAll("[\\s]+", "").trim();
						temp = temp.replaceAll("(@@0)+", "@@0");
						contentResult += temp;
						break;
					case 4:
						temp = helper.getContent(mUrl, "utf-8");
						if (!temp.contains("收起")) {
							p = Pattern.compile("<div class=\"wr\">.+?</div>");
							m = p.matcher(temp);
							if (m.find())
								temp = m.group();
						} else {
							temp = temp
									.replaceAll(
											"<div id=\"edesc_f\" class=\"wr\" style=\"display:none\">",
											"@@")
									.replaceAll("收起</a></div>", "@@")
									.split("@@")[1];
						}
						temp = temp.replaceAll("(<br/>)+", "<br/>").replaceAll(
								"<br/>", "@@0");

						temp = temp.replaceAll("&.+?;", "")
								.replaceAll("<.+?>", "").replaceAll("\\r+", "");
						contentResult = "@@0" + temp;
						break;
					}
				} catch (Exception e) {
					Log.v("TAG", "error");
					contentResult = "";
				} finally {
					Message msg = new Message();
					msg.what = 0;
					if(contentResult == "" || contentResult == null) msg.arg1 = 0;
					else msg.arg1 = 1;
					
					contentResult = "@@1" + mInfo[0] + "@@2" + mInfo[1]
							+ contentResult;
					
					handler.sendMessage(msg);
				}
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		int pos = (Integer) (v.getTag());
		mViewPager.setCurrentItem(pos);
		setCurPoint(pos);
	}

	private void setCurPoint(int index) {
		mButtonViews[mCurSel].setEnabled(true);
		mButtonViews[mCurSel].setBackgroundResource(R.drawable.dot_black);
		mButtonViews[mCurSel].setText("");
		mButtonViews[index].setEnabled(false);
		mButtonViews[index].setBackgroundResource(R.drawable.dot_white);
		mButtonViews[index].setText(String.valueOf(index + 1));
		mCurSel = index;
	}

	private void addContentView(String content) {

		ContentView tempContentView = new ContentView(this, content);
		tempContentView.setBackgroundColor(Color.WHITE);
		mContentViews.add(tempContentView);
	}

	private class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mContentViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mContentViews.get(arg1));
			return mContentViews.get(arg1);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView((View) arg2);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}

	/**
	 * 内容显示页面View，实现了ContentDrawTool的回调接口
	 * 
	 * @author Maybe
	 * 
	 */
	public class ContentView extends View {

		String content;
		Context context;

		public ContentView(Context context, AttributeSet attrs) {
			super(context, attrs);
			this.context = context;
			// TODO Auto-generated constructor stub
		}

		public ContentView(Context context, String content) {
			super(context);
			this.context = context;
			this.content = content;
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub

			super.onDraw(canvas);
			ContentDrawTool cdt = new ContentDrawTool(context);
			cdt.drawContent(content, canvas);

		}

	}
}
