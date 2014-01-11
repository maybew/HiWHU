package com.hiwhu.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.EnrollListAdapter;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.MySqliteHelper;
import com.hiwhu.tool.ScoreListAdapter;
import com.hiwhu.widget.FuncImageButton;

public class EnrollActivity extends Activity {

	private Spinner sp_option, sp_day, sp_location, sp_category;
	private ListView search_list;
	private FuncImageButton get_lesson, enroll_list, back;
	private Button search;
	private EditText search_content;
	private ProgressDialog progress;

	private SharedPreferences userData;
	private HttpHelper helper;
	private Cursor c = null;
	private ArrayList<HashMap<String, Object>> listItem = null;
	private SQLiteDatabase db = null;
	private MySqliteHelper my = null;
	private EnrollListAdapter enrollListAdapter = null;

	private long upload, receive;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.arg2 != 2)
				progress.hide();
			enrollListAdapter.notifyDataSetChanged();
			search_list.setAdapter(enrollListAdapter);
			search_list.invalidate();

			switch (msg.what) {
			case 1:
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
				Toast.makeText(getApplicationContext(), "更新完毕",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "请先获取课程",
						Toast.LENGTH_SHORT).show();
				break;
			case 7:
				if (msg.arg1 == 0 && msg.arg2 == 2) {
					progress.setMessage("正在加载中，请稍后");
				} else {
					progress.setMessage("已完成公选课更新" + 50 * msg.arg1 + "项");
				}
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.enroll_activity);

		sp_option = (Spinner) this.findViewById(R.id.sp_option);
		sp_day = (Spinner) this.findViewById(R.id.sp_day);
		sp_location = (Spinner) this.findViewById(R.id.sp_location);
		sp_category = (Spinner) this.findViewById(R.id.sp_category);

		search_list = (ListView) this.findViewById(R.id.search_list);

		search = (Button) this.findViewById(R.id.search);
		get_lesson = (FuncImageButton) this.findViewById(R.id.get_lesson);
		enroll_list = (FuncImageButton) this.findViewById(R.id.enroll_list);
		back = (FuncImageButton) this.findViewById(R.id.back);

		search_content = (EditText) this.findViewById(R.id.search_content);

		userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
		helper = new HttpHelper();
		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setMessage("加载中，请稍后");
		listItem = new ArrayList<HashMap<String, Object>>();

		ArrayList<String> list = new ArrayList<String>();
		String[] ls = getResources().getStringArray(R.array.option_array);
		for (int i = 0; i < ls.length; i++) {
			list.add(ls[i]);
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.my_spinner, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_option.setAdapter(adapter);
		
		ls = getResources().getStringArray(R.array.enroll_day_array);
		ArrayList<String> list1 = new ArrayList<String>();
		for (int i = 0; i < ls.length; i++) {
			list1.add(ls[i]);
		}
		adapter = new ArrayAdapter<String>(this,
				R.layout.my_spinner, list1);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_day.setAdapter(adapter);
		
		ls = getResources().getStringArray(R.array.location_array);
		ArrayList<String> list2 = new ArrayList<String>();
		list2.clear();
		for (int i = 0; i < ls.length; i++) {
			list2.add(ls[i]);
		}
		adapter = new ArrayAdapter<String>(this,
				R.layout.my_spinner, list2);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_location.setAdapter(adapter);
		
		ls = getResources().getStringArray(R.array.category_array);
		ArrayList<String> list3 = new ArrayList<String>();
		list3.clear();
		for (int i = 0; i < ls.length; i++) {
			list3.add(ls[i]);
		}
		adapter = new ArrayAdapter<String>(this,
				R.layout.my_spinner, list3);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sp_category.setAdapter(adapter);
		
		search_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {

				HashMap<String, Object> map = (HashMap<String, Object>) enrollListAdapter
						.getItem(pos);
				EnrollActivity.this.startActivity(new Intent(
						EnrollActivity.this, LessonDetailActivity.class)
						.putExtra("choice", 2)
						.putExtra("lessonName", (String) map.get("lessonName"))
						.putExtra("lessonWeek", (String) map.get("lessonWeek"))
						.putExtra("lessonTeacher",
								(String) map.get("lessonTeacher"))
						.putExtra("lessonLast", (String) map.get("lessonLast"))
						.putExtra("lessonPoint",
								(String) map.get("lessonPoint")));
			}

		});

		get_lesson.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() != null) {
					progress.setMessage("正在更新公选课");
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
									EnrollActivity.this, handler, userData);
							SimpleDateFormat format = new SimpleDateFormat(
									"yyyy-MM-dd");
							Date date = new Date();
							userData.edit()
									.putString(ConfigurationUtil.PUBCLASS_DATE,
											format.format(date)).commit();

							helper.setSearch("", "");
							helper.getSearchLesson(
									ConfigurationUtil.URL_SEARCHLESSON, "post",
									500200, "gb2312", ConfigurationUtil.cookie,
									EnrollActivity.this, handler, userData);
							if (progress.isShowing()) {
								Message msg = new Message();
								msg.what = 1;
								handler.sendMessage(msg);
							}
						}
					}).start();
				} else
					Toast.makeText(getApplicationContext(), "请检查网络连接",
							Toast.LENGTH_SHORT).show();
			}

		});

		enroll_list.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EnrollActivity.this.startActivity(new Intent(
						EnrollActivity.this, EnrollListActivity.class));
			}

		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EnrollActivity.this.finish();
			}

		});

		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// 隐藏输入法
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(EnrollActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);

				progress.show();
				new Thread(new Runnable() {

					@Override
					public void run() {
						my = new MySqliteHelper(EnrollActivity.this, "mydb",
								null, 1);
						db = my.getReadableDatabase();
						listItem.clear();
						ArrayList<String> selection = new ArrayList<String>();
						ArrayList<String> selectionArgs = new ArrayList<String>();
						int pos = 0;
						String[] ls = null;

						if (sp_option.getSelectedItemPosition() == 0) {
							selection.add(MySqliteHelper.LESSON_NAME
									+ " like ?");
							selectionArgs
									.add("%"
											+ search_content.getText()
													.toString() + "%");
						} else if (sp_option.getSelectedItemPosition() == 1) {
							selection.add(MySqliteHelper.LESSON_TEACHER
									+ " like ?");
							selectionArgs
									.add("%"
											+ search_content.getText()
													.toString() + "%");
						}

						pos = sp_day.getSelectedItemPosition();
						ls = getResources().getStringArray(
								R.array.enroll_day_array);
						ls[0] = "";
						selection.add(MySqliteHelper.LESSON_WEEK + " like ?");
						selectionArgs.add("%" + ls[pos] + "%");

						pos = sp_location.getSelectedItemPosition();
						String[] ls1 = { "", "1区", "2区", "3区", "4区" };
						selection.add(MySqliteHelper.LESSON_LOCATION
								+ " like ?");
						selectionArgs.add("%" + ls1[pos] + "%");

						pos = sp_category.getSelectedItemPosition();
						ls = getResources().getStringArray(
								R.array.category_array);
						ls[0] = "";
						selection.add(MySqliteHelper.LESSON_OTHER + " like ?");
						selectionArgs.add("%" + ls[pos] + "%");

						String finalSelection = "";
						String[] finalSelectionArgs = new String[selection
								.size()];
						for (int i = 0; i < selection.size(); i++) {
							if (i > 0)
								finalSelection += " and ";
							finalSelection += selection.get(i);
							finalSelectionArgs[i] = selectionArgs.get(i);
						}

						c = db.query(MySqliteHelper.TB_SEARCHLESSON, null,
								finalSelection, finalSelectionArgs, null, null,
								null);

						for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
							HashMap<String, Object> map = new HashMap<String, Object>();
							String name = c.getString(c
									.getColumnIndexOrThrow(MySqliteHelper.LESSON_NAME));
							String week = c.getString(c
									.getColumnIndexOrThrow(MySqliteHelper.LESSON_WEEK));
							String teacher = c.getString(c
									.getColumnIndexOrThrow(MySqliteHelper.LESSON_TEACHER));
							String point = c.getString(c
									.getColumnIndexOrThrow(MySqliteHelper.LESSON_POINT));
							String last = c.getString(c
									.getColumnIndexOrThrow(MySqliteHelper.LESSON_LASTING));

							map.put("lessonName", name);
							map.put("lessonWeek", week);
							map.put("lessonTeacher", teacher);
							map.put("lessonPoint", point);
							map.put("lessonLast", last);
							listItem.add(map);
						}

						// 生成适配器的Item和动态数组对应的元素
						enrollListAdapter = new EnrollListAdapter(
								EnrollActivity.this, listItem, 1, null);

						Message msg = new Message();
						handler.sendMessage(msg);
						c.close();
						db.close();
					}

				}).start();
			}

		});

		enrollListAdapter = new EnrollListAdapter(EnrollActivity.this,
				listItem, 1, null);
		// this.search_list.setAdapter(listItemAdapter);

		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				my = new MySqliteHelper(EnrollActivity.this, "mydb", null, 1);
				db = my.getReadableDatabase();
				c = db.query(MySqliteHelper.TB_SEARCHLESSON, null, null, null,
						null, null, null);
				if (!c.moveToFirst()) {
					Message msg = new Message();
					msg.what = 2;
					handler.sendMessage(msg);
				} else {
					for (; !c.isAfterLast(); c.moveToNext()) {
						HashMap<String, Object> map = new HashMap<String, Object>();
						String name = c.getString(c
								.getColumnIndexOrThrow(MySqliteHelper.LESSON_NAME));
						String week = c.getString(c
								.getColumnIndexOrThrow(MySqliteHelper.LESSON_WEEK));
						String teacher = c.getString(c
								.getColumnIndexOrThrow(MySqliteHelper.LESSON_TEACHER));
						String point = c.getString(c
								.getColumnIndexOrThrow(MySqliteHelper.LESSON_POINT));
						String last = c.getString(c
								.getColumnIndexOrThrow(MySqliteHelper.LESSON_LASTING));

						map.put("lessonName", name);
						map.put("lessonWeek", week);
						map.put("lessonTeacher", teacher);
						map.put("lessonPoint", point);
						map.put("lessonLast", last);
						listItem.add(map);
					}

					// 生成适配器的Item和动态数组对应的元素
					enrollListAdapter = new EnrollListAdapter(
							EnrollActivity.this, listItem, 1, null);
					Message msg = new Message();
					handler.sendMessage(msg);
				}
				c.close();
				db.close();
			}

		}).start();

	}

	@Override
	public void onResume() {
		super.onResume();
		if (enrollListAdapter != null)
			enrollListAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		progress.dismiss();
	}
}
