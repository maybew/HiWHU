package com.hiwhu.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.NewsDBAdapter;
import com.hiwhu.tool.TextHelper;
import com.hiwhu.widget.FuncImageButton;
import com.hiwhu.widget.PullToRefreshLayout;

public class NewsListActivity extends Activity implements
		PullToRefreshLayout.OnRefreshListener {
	private ListView mListView;
	private PullToRefreshLayout mPullToRefreshLayout;
	private NewsListAdapter mAdapter;
	private List<String[]> mList;
	private List<String[]> mFullList;
	private ArrayList<String[]> mMore;
	private ArrayList<String[]> mLastTmp;

	private ProgressDialog mProgress;

	private View mFooterView;
	private View mFooterLoadingView;
	private Button mFooterMore;
	private FuncImageButton zixun_list, back;

	private int choice;
	private int page = 1;
	private TextHelper textHelper;
	private HttpHelper helper;
	private long upload, receive;;
	private SharedPreferences userData;
	private NewsDBAdapter db;
	private boolean isOnCreate;
	private boolean isRefresh;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			// 数据更新
			if (msg.what == 1) {
				if (mProgress.isShowing())
					mProgress.hide();
				mFooterLoadingView.setVisibility(View.GONE);
				mFooterMore.setVisibility(View.VISIBLE);

				if (mMore == null || mMore.size() == 0) {
					Toast.makeText(getApplicationContext(), "暂无更新",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (page == 1)
					isRefresh = true;

				if (isRefresh) {
					page = 1;
					mFullList.clear();
					mList.clear();
				}

				mLastTmp = mMore;// (ArrayList<String[]>) mMore.clone();
				mFullList.addAll(mMore);
				mList.addAll(textHelper.infoListAbbr(mMore));
				mAdapter.notifyDataSetChanged();
				if (isRefresh)
					mListView.setSelection(0);
				isRefresh = false;
				page++;
				mMore = null;

				upload = TrafficStats.getUidTxBytes(Process.myUid()) - upload;
				receive = TrafficStats.getUidRxBytes(Process.myUid()) - receive;
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

			if (msg.what == 2) {
				mProgress.hide();
				NewsListActivity.this.startActivity(new Intent(
						NewsListActivity.this, NewsPassageActivity.class)
						.putExtra("info", mFullList.get(msg.arg1))
						.putExtra("url", mList.get(msg.arg1)[5])
						.putExtra("choice", choice));
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_list_activity_layout);
		userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
		choice = getIntent().getIntExtra("choice", 0);

		mPullToRefreshLayout = (PullToRefreshLayout) this
				.findViewById(R.id.test_list);
		mPullToRefreshLayout.setOnRefreshListener(this);
		mListView = mPullToRefreshLayout.getListView();
		mListView.setDividerHeight(0);

		textHelper = new TextHelper();
		helper = new HttpHelper();
		mList = new ArrayList<String[]>();
		mFullList = new ArrayList<String[]>();
		mMore = new ArrayList<String[]>();
		mLastTmp = null;
		mProgress = new ProgressDialog(this);

		// progress
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setMessage("加载中，请稍后");
		mProgress.show();

		// Set Footer
		LayoutInflater inflater = LayoutInflater.from(this);
		mFooterView = inflater.inflate(R.layout.info_list_footer_layout, null);
		mFooterMore = (Button) mFooterView
				.findViewById(R.id.info_list_refresh_button);
		mFooterLoadingView = (View) mFooterView
				.findViewById(R.id.info_list_loading_view);
		mFooterLoadingView.setVisibility(View.GONE);
		mFooterMore.setVisibility(View.GONE);
		mFooterMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Log.v("test", "click");
				mFooterMore.setVisibility(View.GONE);
				mFooterLoadingView.setVisibility(View.VISIBLE);
				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() != null) {
					getData();
				} else {
					Toast.makeText(getApplicationContext(), "请检查网络连接",
							Toast.LENGTH_SHORT).show();
					mFooterLoadingView.setVisibility(View.GONE);
					mFooterMore.setVisibility(View.VISIBLE);
				}
			}
		});

		zixun_list = (FuncImageButton) this.findViewById(R.id.zixun_list);
		zixun_list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

			}

		});
		back = (FuncImageButton) this.findViewById(R.id.back);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				NewsListActivity.this.finish();
			}

		});
		// Adapter
		mAdapter = new NewsListAdapter(this, mList, mListView);

		db = new NewsDBAdapter(this);
		db.open();
		isOnCreate = true;
		isRefresh = false;
		getData();

		mListView.addFooterView(mFooterView);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				// 防止点击到FOOTER
				if (pos >= mAdapter.getCount())
					return;
				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() != null) {
					// mProgress.show();
					// loadContent(pos);
					Message msg = new Message();
					msg.what = 2;
					msg.arg1 = pos;
					handler.sendMessage(msg);
				} else
					Toast.makeText(getApplicationContext(), "暂无网络连接，请稍后再试",
							Toast.LENGTH_SHORT).show();
			}

		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mProgress.dismiss();
		if (mLastTmp != null)
			switch (choice) {
			case 1:
				// db.dropAndCreate(NewsDBAdapter.TABLE_LECTURE);
				db.deleteAll(NewsDBAdapter.TABLE_LECTURE);
				for (int i = 0; i < mLastTmp.size(); i++) {
					db.insert(NewsDBAdapter.TABLE_LECTURE, mLastTmp.get(i)[0],
							mLastTmp.get(i)[1], mLastTmp.get(i)[2],
							mLastTmp.get(i)[3], mLastTmp.get(i)[4],
							mLastTmp.get(i)[5]);
					Log.d("NewsActivity", mLastTmp.get(i)[0]
							+ mLastTmp.get(i)[1] + mLastTmp.get(i)[2]
							+ mLastTmp.get(i)[3] + mLastTmp.get(i)[4]
							+ mLastTmp.get(i)[5]);

				}
				break;
			case 2:
				// db.dropAndCreate(NewsDBAdapter.TABLE_INFORMATION);
				db.deleteAll(NewsDBAdapter.TABLE_INFORMATION);
				for (int i = 0; i < mLastTmp.size(); i++) {
					db.insert(NewsDBAdapter.TABLE_INFORMATION,
							mLastTmp.get(i)[0], mLastTmp.get(i)[1],
							mLastTmp.get(i)[2], mLastTmp.get(i)[3],
							mLastTmp.get(i)[4], mLastTmp.get(i)[5]);
					Log.d("NewsActivity", mLastTmp.get(i)[0]
							+ mLastTmp.get(i)[1] + mLastTmp.get(i)[2]
							+ mLastTmp.get(i)[3] + mLastTmp.get(i)[4]
							+ mLastTmp.get(i)[5]);
				}
				break;
			case 3:
				// db.dropAndCreate(NewsDBAdapter.TABLE_WANTED);
				db.deleteAll(NewsDBAdapter.TABLE_WANTED);
				for (int i = 0; i < mLastTmp.size(); i++) {
					db.insert(NewsDBAdapter.TABLE_WANTED, mLastTmp.get(i)[0],
							mLastTmp.get(i)[1], mLastTmp.get(i)[2],
							mLastTmp.get(i)[3], mLastTmp.get(i)[4],
							mLastTmp.get(i)[5]);
					Log.d("NewsActivity", mLastTmp.get(i)[0]
							+ mLastTmp.get(i)[1] + mLastTmp.get(i)[2]
							+ mLastTmp.get(i)[3] + mLastTmp.get(i)[4]
							+ mLastTmp.get(i)[5]);
				}
				break;
			case 4:
				// db.dropAndCreate(NewsDBAdapter.TABLE_SURROUNDING);
				db.deleteAll(NewsDBAdapter.TABLE_SURROUNDING);
				for (int i = 0; i < mLastTmp.size(); i++) {
					db.insert(NewsDBAdapter.TABLE_SURROUNDING,
							mLastTmp.get(i)[0], mLastTmp.get(i)[1],
							mLastTmp.get(i)[2], mLastTmp.get(i)[3],
							mLastTmp.get(i)[4], mLastTmp.get(i)[5]);
					Log.d("NewsActivity", mLastTmp.get(i)[0]
							+ mLastTmp.get(i)[1] + mLastTmp.get(i)[2]
							+ mLastTmp.get(i)[3] + mLastTmp.get(i)[4]
							+ mLastTmp.get(i)[5]);
				}
				break;

			}

		db.close();
		super.onDestroy();
	}

	private void getData() {

		upload = TrafficStats.getUidTxBytes(Process.myUid());
		receive = TrafficStats.getUidRxBytes(Process.myUid());

		new Thread(new Runnable() {

			@Override
			public void run() {
				switch (choice) {
				case 1:
					if (isOnCreate)
						if (getDataFromDB(NewsDBAdapter.TABLE_LECTURE))
							break;

					mMore = helper.getLecture(ConfigurationUtil.URL_LECTURE
							+ "/Lecture/nav/" + page, "utf-8");
					break;
				case 2:
					if (isOnCreate)
						if (getDataFromDB(NewsDBAdapter.TABLE_INFORMATION))
							break;

					mMore = helper.getNews(ConfigurationUtil.URL_NEWS
							+ "/a/xiaoyuandongtai/list_17_" + page + ".html",
							"gb2312");
					break;
				case 3:
					if (isOnCreate)
						if (getDataFromDB(NewsDBAdapter.TABLE_WANTED))
							break;

					mMore = helper.getEmployment(
							ConfigurationUtil.URL_EMPLOYMENT + "list.php?page="
									+ (page - 1), "gb2312");
					// Log.e("招聘", result.get(0));
					break;
				case 4:
					if (isOnCreate)
						if (getDataFromDB(NewsDBAdapter.TABLE_SURROUNDING))
							break;
					mMore = helper.getActivities(
							"http://www.douban.com/location/wuhan/events/week/all?start="
									+ (page - 1) * 10, "utf-8");
					break;
				}
				// TODO Auto-generated method stub
				// mList.addAll(getData());
				isOnCreate = false;
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);

			}
		}).start();
	}

	public boolean getDataFromDB(String table) {
		Log.d("NewsListActivity", "getfrom DB");
		Cursor cursor = db.queryAllOrderByAsc(table);
		if (cursor.moveToFirst()) {
			page = 0;
			int title = cursor.getColumnIndex(NewsDBAdapter.KEY_TITLE);
			int date = cursor.getColumnIndex(NewsDBAdapter.KEY_DATE);
			int type = cursor.getColumnIndex(NewsDBAdapter.KEY_TYPE);
			int subtitle = cursor.getColumnIndex(NewsDBAdapter.KEY_SUBTITLE);
			int img = cursor.getColumnIndex(NewsDBAdapter.KEY_IMG_URL);
			int url = cursor.getColumnIndex(NewsDBAdapter.KEY_URL);
			do {
				if (mMore == null)
					mMore = new ArrayList<String[]>();
				;
				mMore.add(new String[] { cursor.getString(title),
						cursor.getString(date), cursor.getString(type),
						cursor.getString(subtitle), cursor.getString(img),
						cursor.getString(url) });
			} while (cursor.moveToNext());
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}

	@Override
	public void onRefresh() {
		isRefresh = true;
		page = 1;

		upload = TrafficStats.getUidTxBytes(Process.myUid());
		receive = TrafficStats.getUidRxBytes(Process.myUid());

		new Thread(new Runnable() {

			@Override
			public void run() {
				switch (choice) {
				case 1:

					mMore = helper.getLecture(ConfigurationUtil.URL_LECTURE
							+ "/Lecture/nav/" + page, "utf-8");
					break;
				case 2:

					mMore = helper.getNews(ConfigurationUtil.URL_NEWS
							+ "/a/xiaoyuandongtai/list_17_" + page + ".html",
							"gb2312");
					break;
				case 3:

					mMore = helper.getEmployment(
							ConfigurationUtil.URL_EMPLOYMENT + "list.php?page="
									+ (page - 1), "gb2312");
					// Log.e("招聘", result.get(0));
					break;
				case 4:

					mMore = helper.getActivities(
							"http://www.douban.com/location/wuhan/events/week/all?start="
									+ (page - 1) * 10, "utf-8");
					break;
				}

				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
				mPullToRefreshLayout.onRefreshComplete();

			}
		}).start();
	

		
	
		
	}

}
