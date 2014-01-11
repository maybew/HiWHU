package com.hiwhu.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.MySqliteHelper;
import com.hiwhu.widget.BtnsLayout;
import com.hiwhu.widget.HentaiScrollLayout;
import com.hiwhu.widget.ListFlipper;
import com.hiwhu.widget.SmartScrollLayout;
import com.hiwhu.widget.TabListLayout;

public class MainActivity extends Activity implements
		TabListLayout.OnSelectionChangeListener,
		SmartScrollLayout.OnViewChangeListener, BtnsLayout.OnBtnClickListener {
	private BtnsLayout btnsLayout;
	private TabListLayout tabList;
	private ListFlipper sdDayList = null;
	private SlidingDrawer sd = null;
	private Button sdHandler = null, op = null, change_user, log_out,
			main_update;
	private TextView name = null, week = null;
	private LinearLayout op_layout;
	private ProgressDialog progress;

	private ArrayList<HashMap<String, Object>> listItem = null;
	private SQLiteDatabase db = null;
	private MySqliteHelper my = null;
	private Cursor c = null;
	private SharedPreferences userData;
	private HttpHelper helper;
	private long upload, receive;

	private ListView[] mDayList;
	private TextView[] mNoLesson;
	private ArrayList<HashMap<String, Object>>[] mData;
	private int mToday = new Date().getDay();

	private int mLastMotionY;
	private int mLastMotionX;
	private boolean isInSlidingDrawer;
	private boolean isReadyToCloseSlidingDrawer;
	private Rect mTouchFrame;
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			progress.hide();

			switch (msg.what) {
			case 1:
				for (int i = 0; i < 7; i++) {
					if (mData[i].size() != 0) {
						mNoLesson[i].setVisibility(View.GONE);
						mDayList[i].setVisibility(View.VISIBLE);

						//mDayAdapter[i].notifyDataSetChanged();
					} else {
						mNoLesson[i].setVisibility(View.VISIBLE);
						mDayList[i].setVisibility(View.GONE);
					}
				}
				sdDayList.post(new Runnable() {

					@Override
					public void run() {
						sdDayList.goToScreen(mToday);
					}

				});
				break;
			case 8:
				upload = TrafficStats.getUidTxBytes(Process.myUid()) - upload;
				receive = TrafficStats.getUidRxBytes(Process.myUid()) - receive;
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date();
				String time = format.format(date);
				if (time.equals(userData.getString(
						ConfigurationUtil.FLOW_1_TIME, "2000-01-01"))) {
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_1_DAY_UPLOAD,
									userData.getLong(
											ConfigurationUtil.FLOW_1_DAY_UPLOAD,
											0)
											+ upload).commit();
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_1_DAY_RECEIVE,
									userData.getLong(
											ConfigurationUtil.FLOW_1_DAY_RECEIVE,
											0)
											+ receive).commit();
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_1_MONTH_UPLOAD,
									userData.getLong(
											ConfigurationUtil.FLOW_1_MONTH_UPLOAD,
											0)
											+ upload).commit();
					userData.edit()
							.putLong(
									ConfigurationUtil.FLOW_1_MONTH_RECEIVE,
									userData.getLong(
											ConfigurationUtil.FLOW_1_MONTH_RECEIVE,
											0)
											+ receive).commit();
				} else {
					userData.edit()
							.putLong(ConfigurationUtil.FLOW_1_DAY_UPLOAD,
									upload).commit();
					userData.edit()
							.putLong(ConfigurationUtil.FLOW_1_DAY_RECEIVE,
									receive).commit();
					int month = 0;
					try {
						month = format.parse(
								userData.getString(
										ConfigurationUtil.FLOW_1_TIME,
										"2000-01-01")).getMonth();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (month == date.getMonth()) {
						userData.edit()
								.putLong(
										ConfigurationUtil.FLOW_1_MONTH_UPLOAD,
										userData.getLong(
												ConfigurationUtil.FLOW_1_MONTH_UPLOAD,
												0)
												+ upload).commit();
						userData.edit()
								.putLong(
										ConfigurationUtil.FLOW_1_MONTH_RECEIVE,
										userData.getLong(
												ConfigurationUtil.FLOW_1_MONTH_RECEIVE,
												0)
												+ receive).commit();
					} else {
						userData.edit()
								.putLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD,
										upload).commit();
						userData.edit()
								.putLong(
										ConfigurationUtil.FLOW_1_MONTH_RECEIVE,
										receive).commit();
					}
					userData.edit()
							.putString(ConfigurationUtil.FLOW_1_TIME, time)
							.commit();
				}
				Toast.makeText(getApplicationContext(), "更新完成",
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		if (mToday == 0)
			mToday = 6;
		else
			mToday--;

		helper = new HttpHelper();

		btnsLayout = (BtnsLayout) findViewById(R.id.btns_layout);
		tabList = (TabListLayout) findViewById(R.id.tab_list_layout);
		op_layout = (LinearLayout) findViewById(R.id.op_layout);
		btnsLayout.setOnViewChangeListener(this);
		btnsLayout.setOnBtnClickListener(this);
		tabList.setOnSelectionChangeListener(this);

		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setMessage("加载中，请稍后");

		op = (Button) findViewById(R.id.op);
		change_user = (Button) findViewById(R.id.change_user);
		log_out = (Button) findViewById(R.id.log_out);
		main_update = (Button) findViewById(R.id.main_update);
		name = (TextView) findViewById(R.id.name);
		week = (TextView) findViewById(R.id.week);
		name.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (op_layout.getVisibility() == View.INVISIBLE)
					op_layout.setVisibility(View.VISIBLE);
				else
					op_layout.setVisibility(View.INVISIBLE);
			}
		});

		op.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (op_layout.getVisibility() == View.INVISIBLE)
					op_layout.setVisibility(View.VISIBLE);
				else
					op_layout.setVisibility(View.INVISIBLE);
			}

		});

		change_user.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				op_layout.setVisibility(View.INVISIBLE);
				MainActivity.this.startActivity(new Intent(MainActivity.this,
						LoginActivity.class));
			}

		});

		log_out.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				op_layout.setVisibility(View.INVISIBLE);
				userData.edit().putBoolean(ConfigurationUtil.IFSIGNED, false)
						.commit();
				MainActivity.this.startActivity(new Intent(MainActivity.this,
						LoginActivity.class).putExtra("choice", 0));
				MainActivity.this.finish();
			}

		});

		main_update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				op_layout.setVisibility(View.INVISIBLE);
				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() != null) {

					progress.setMessage("正在更新成绩");
					progress.show();
					upload = TrafficStats.getUidTxBytes(Process.myUid());
					receive = TrafficStats.getUidRxBytes(Process.myUid());
					new Thread(new Runnable() {

						@Override
						public void run() {

							ConfigurationUtil.cookie = helper
									.getCookie(ConfigurationUtil.URL_INDEX);
							userData.edit()
									.putString(ConfigurationUtil.USER_COOKIE,
											ConfigurationUtil.cookie).commit();
							helper.setInformation(userData.getString(
									ConfigurationUtil.USER_NAME, ""), userData
									.getString(ConfigurationUtil.USER_PWD, ""));
							helper.getPageContent(ConfigurationUtil.URL_LOGIN,
									"post", 100500, "gb2312",
									ConfigurationUtil.cookie);
							ConfigurationUtil.cookie = helper
									.getCookie(ConfigurationUtil.URL_STU)
									+ ";"
									+ ConfigurationUtil.cookie.split(";")[1];
							helper.getStuIndex(ConfigurationUtil.URL_STUINDEX,
									"post", 500200, "gb2312",
									ConfigurationUtil.cookie,
									MainActivity.this, handler, userData);

							helper.getScoreStart(ConfigurationUtil.URL_SCORE,
									ConfigurationUtil.cookie, "gb2312", 500200,
									MainActivity.this, userData, 0);
							if (!progress.isShowing())
								return;
							Message msg1 = new Message();
							msg1.what = 7;
							msg1.arg1 = 1;
							msg1.arg2 = 1;
							handler.sendMessage(msg1);
							helper.getLessonStart(ConfigurationUtil.URL_LESSON,
									ConfigurationUtil.cookie, "gb2312", 500200,
									MainActivity.this, userData, 0);
							update(new Date().getDay());
							if (progress.isShowing()) {
								Message msg2 = new Message();
								msg2.what = 8;
								handler.sendMessage(msg2);
							}
						}

					}).start();
				} else {
					Toast.makeText(getApplicationContext(), "请检查网络连接",
							Toast.LENGTH_SHORT).show();
				}
			}

		});

		sd = (SlidingDrawer) findViewById(R.id.slidingdrawer);
		sdDayList = (ListFlipper) findViewById(R.id.sdDayList);
		sdHandler = (Button) findViewById(R.id.handle);

		mDayList = new ListView[7];
		mNoLesson = new TextView[7];
		mData = new ArrayList[7];

		for (int i = 0; i < 7; i++) {
			FrameLayout mTempLinear = new FrameLayout(this);

			mData[i] = new ArrayList<HashMap<String, Object>>();

			SimpleAdapter tempAdapter = new SimpleAdapter(
					MainActivity.this,
					mData[i],// 数据源
					R.layout.day_list_item,// ListItem的XML实现
					new String[] { "LessonName", "LessonTime", "LessonLocation" },
					new int[] { R.id.lessonName, R.id.lessonTime,
							R.id.lessonLocation });

			mDayList[i] = new ListView(this);
			mDayList[i].setAdapter(tempAdapter);
			mDayList[i].setCacheColorHint(0x00000000);
			mDayList[i].setVisibility(View.GONE);

			mNoLesson[i] = new TextView(this);
			mNoLesson[i].setText("今日无课");
			mNoLesson[i].setTextSize(28);
			mNoLesson[i].setTextColor(Color.BLACK);
			mNoLesson[i].setGravity(Gravity.CENTER);

			mTempLinear.addView(mDayList[i]);
			mTempLinear.addView(mNoLesson[i]);

			sdDayList.addView(mTempLinear);

		}

		sdDayList.setOnViewChangeListener(new MyOnViewChangeListener());
		sd.setOnDrawerCloseListener(new MyOnDrawerCloseListener());
		userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);

	}

	@Override
	public void onViewChange(int index) {
		tabList.setSelection(index);
	}

	@Override
	public void onSelectionChange(int index) {
		btnsLayout.changeView(index);
	}

	@Override
	public void onResume() {
		super.onResume();
		// if (canUpdate)
		update(new Date().getDay());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null)
			if (db.isOpen())
				db.close();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (sd.isOpened()) {
				sd.animateClose();
			} else if (op_layout.getVisibility() == View.VISIBLE) {
				op_layout.setVisibility(View.INVISIBLE);
			} else {// 退出
				dialog();
			}
		}
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final int y = (int) event.getY();
		final int x = (int) event.getX();
		if (sd.isOpened()) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:

				mLastMotionY = y;
				mLastMotionX = x;
				Rect frame = mTouchFrame;
				if (frame == null) {
					mTouchFrame = new Rect();
					frame = mTouchFrame;
				}
				sd.getHitRect(frame);
				isInSlidingDrawer = frame.contains(x, y);
				isReadyToCloseSlidingDrawer = false;
				break;
			case MotionEvent.ACTION_MOVE:
				if (isInSlidingDrawer) {

					int deltaY = (int) (mLastMotionY - y);
					int deltaX = (int) (mLastMotionX - x);

					if (Math.abs(deltaY) > Math.abs(deltaX)
							&& deltaY < -ViewConfiguration.getTouchSlop() * 3) {
						View curView = sdDayList.getCurScreen();
						if (!(curView instanceof ListView)) {
							isReadyToCloseSlidingDrawer = true;
						} else {
							ListView curListView = (ListView) curView;
							int pos = curListView.getFirstVisiblePosition();
							int top = curListView.getChildAt(0).getTop();

							if (pos == 0 && top == 0) {
								isReadyToCloseSlidingDrawer = true;
							}
						}
					} else if (deltaY > 0)
						isReadyToCloseSlidingDrawer = false;

					mLastMotionY = y;
				}
				break;
			case MotionEvent.ACTION_UP:
				if (isReadyToCloseSlidingDrawer) {
					sd.animateClose();
					isReadyToCloseSlidingDrawer = false;
					return true;
				}

			}
			return sd.dispatchTouchEvent(event);
		}

		return super.dispatchTouchEvent(event);

	}

	// 以下是自定义方法
	protected void dialog() {
		AlertDialog.Builder builder = new Builder(this);
		builder.setMessage("确认退出掌上武大吗？");
		builder.setTitle("提示");
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				MainActivity.this.finish();
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}

	// 更新界面信息
	public void update(int id) {

		try {
			// sdDayList初始化
			Log.e("TAG", "Update");
			Date date = new Date();
			sdHandler.setText(getResources().getStringArray(
					R.array.weekday_array)[mToday]);

			listItem = new ArrayList<HashMap<String, Object>>();

			my = new MySqliteHelper(MainActivity.this, "mydb", null, 1);
			db = my.getReadableDatabase();

			for (int d = 1; d < 8; d++) {
				c = db.query(MySqliteHelper.TB_LESSON, null,
						MySqliteHelper.LESSON_WEEK + " like ?",
						new String[] { "%" + d + "%" }, null, null, null);

				listItem = new ArrayList<HashMap<String, Object>>();
				int week = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_WEEK);
				int temp1 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_NAME);
				int temp2 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TIME);
				int temp3 = c
						.getColumnIndexOrThrow(MySqliteHelper.LESSON_LOCATION);

				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					String w = c.getString(week);
					String name = c.getString(temp1);
					String time = c.getString(temp2);
					String location = c.getString(temp3);

					int pos = 0;

					String[] xtemp = w.split("@");
					for (int i = 0; i < xtemp.length; i++) {
						if (xtemp[i].equals(id + "")) {
							pos = i;
							break;
						}
					}

					xtemp = time.split("@");
					time = xtemp[pos];

					xtemp = location.split("@");
					location = xtemp[pos];

					map.put("LessonName", name);
					map.put("LessonTime", time);
					map.put("LessonLocation", location);
					map.put("weekday", id);

					if (listItem.size() == 0)
						listItem.add(map);
					else
						for (int i = 0; i < listItem.size(); i++) {// 获取时间，按上课时间顺序排列
							String tt = (String) listItem.get(i).get(
									"LessonTime");
							int time1 = Integer.parseInt(time.split("-")[0]);
							int time2 = Integer.parseInt(tt.split("-")[0]);
							if (time1 <= time2) {
								listItem.add(i, map);
								break;
							} else {
								if (i + 1 == listItem.size()) {
									listItem.add(i + 1, map);
									break;
								}
								tt = (String) listItem.get(i + 1).get(
										"LessonTime");
								time2 = Integer.parseInt(tt.split("-")[0]);
								if (time1 <= time2) {
									listItem.add(i + 1, map);
									break;
								}
							}
						}
				}

				if (listItem.size() != 0) {
					mData[d - 1].clear();
					mData[d - 1].addAll(listItem);
				} else
					mData[d - 1].clear();
				c.close();

			}
			db.close();
			Message msg = new Message();
			msg.what = 1;
			handler.sendMessage(msg);

			// 更新用户名name
			name.setText(userData.getString(ConfigurationUtil.REAL_NAME, ""));

			// 更新week
			int temp = getWeekOfYear(date) - 6;
			if (temp < 1) {
				this.week.setText("本学期尚未开始");
			} else if (temp / 10 == 0) {
				temp = temp % 10 - 1;
				this.week.setText("第"
						+ getResources().getStringArray(
								R.array.semesterweek_array)[temp] + "周");
			} else if (temp / 10 == 1) {
				temp = temp % 10 - 1;
				if (temp < 0)
					this.week.setText("第十周");
				else
					this.week.setText("第十"
							+ getResources().getStringArray(
									R.array.semesterweek_array)[temp] + "周");
			} else if (temp == 20) {
				this.week.setText("第二十周");
			} else {
				this.week.setText("本学期已结束");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public int getWeekOfYear(Date date) {
		Calendar c = new GregorianCalendar();
		c.setFirstDayOfWeek(Calendar.SUNDAY);
		c.setMinimalDaysInFirstWeek(7);
		c.setTime(date);
		return c.get(Calendar.WEEK_OF_YEAR);
	}

	@Override
	public void performItemClick(int btnId) {
		switch (btnId) {
		case R.string.btn_default:
			btnsLayout.showDialog();
			break;
		case R.string.cheng_ji_dan:
			verify(R.string.cheng_ji_dan);
			break;
		case R.string.ke_cheng_biao:
			this.startActivity(new Intent(this, LessonActivity.class));
			break;
		case R.string.xuan_ke:
			this.startActivity(new Intent(this, EnrollActivity.class));
			break;
		case R.string.xiao_yuan_ka:
			verify(R.string.xiao_yuan_ka);
			break;
		case R.string.xin_wen:
			this.startActivity(new Intent(this, NewsListActivity.class)
					.putExtra("choice", 2));
			break;
		case R.string.jiang_zuo:
			this.startActivity(new Intent(this, NewsListActivity.class)
					.putExtra("choice", 1));
			break;
		case R.string.zhao_pin:
			this.startActivity(new Intent(this, NewsListActivity.class)
					.putExtra("choice", 3));
			break;
		case R.string.zhou_bian:
			this.startActivity(new Intent(this, NewsListActivity.class)
					.putExtra("choice", 4));
			break;
		case R.string.yin_si_kong_zhi:
			this.startActivity(new Intent(this, PrivacyControlActivity.class));
			break;
		case R.string.liu_liang_tong_ji:
			this.startActivity(new Intent(this, NetFlowActivity.class));
			break;
		case R.string.gong_neng_yin_dao:
			this.startActivity(new Intent(this, NewFeatureActivity.class));
			break;
		case R.string.ban_ben_xin_xi:
			this.startActivity(new Intent(this, InfoActivity.class));
			break;
		}

	}

	private void verify(final int id) {
		if (!userData.getBoolean(ConfigurationUtil.IFCONTROLED, false)) {
			switch (id) {
			case R.string.cheng_ji_dan:
				MainActivity.this.startActivity(new Intent(MainActivity.this,
						ScoreActivity.class));
				break;
			case R.string.xiao_yuan_ka:
				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() != null) {
					MainActivity.this.startActivity(new Intent(
							MainActivity.this, CampusCardActivity.class));
				} else
					Toast.makeText(getApplicationContext(), "无网络连接",
							Toast.LENGTH_SHORT).show();
				break;
			}
			return;
		}

		Builder dialog = new AlertDialog.Builder(this);
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(MainActivity.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.privacy_control_dialog, null);
		dialog.setView(layout);
		dialog.setMessage("身份验证");
		final EditText pwd = (EditText) layout.findViewById(R.id.input);
		dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				/*
				 * // 隐藏输入法 InputMethodManager inputMethodManager =
				 * (InputMethodManager)
				 * getSystemService(Context.INPUT_METHOD_SERVICE);
				 * inputMethodManager.hideSoftInputFromWindow(MainActivity.this
				 * .getCurrentFocus().getWindowToken(),
				 * InputMethodManager.HIDE_NOT_ALWAYS);
				 */

				if (pwd.getText()
						.toString()
						.toUpperCase()
						.equals(userData.getString(ConfigurationUtil.USER_PWD,
								""))) {
					switch (id) {
					case R.string.cheng_ji_dan:
						MainActivity.this.startActivity(new Intent(
								MainActivity.this, ScoreActivity.class));
						break;
					case R.string.xiao_yuan_ka:
						ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
						if (webManager.getActiveNetworkInfo() != null) {
							MainActivity.this
									.startActivity(new Intent(
											MainActivity.this,
											CampusCardActivity.class));
						} else
							Toast.makeText(getApplicationContext(), "无网络连接",
									Toast.LENGTH_SHORT).show();
						break;
					}
				} else {
					Toast.makeText(getApplicationContext(), "密码错误",
							Toast.LENGTH_SHORT).show();
				}
				dialog.dismiss();
			}
		});

		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				/*
				 * // 隐藏输入法 InputMethodManager inputMethodManager =
				 * (InputMethodManager)
				 * getSystemService(Context.INPUT_METHOD_SERVICE);
				 * inputMethodManager.hideSoftInputFromWindow(MainActivity.this
				 * .getCurrentFocus().getWindowToken(),
				 * InputMethodManager.HIDE_NOT_ALWAYS);
				 */

				dialog.dismiss();
			}

		});
		dialog.show();
	}

	class MyOnDrawerCloseListener implements OnDrawerCloseListener {

		public void onDrawerClosed() {
			// TODO Auto-generated method stub
			sdHandler.setText(getResources().getStringArray(
					R.array.weekday_array)[mToday]);
			sdDayList.goToScreen(mToday);
		}

	}

	class MyOnViewChangeListener implements
			HentaiScrollLayout.OnViewChangeListener {

		public void OnViewChange(int view) {
			// TODO Auto-generated method stub
			sdHandler.setText(getResources().getStringArray(
					R.array.weekday_array)[view]);
		}

	}

}