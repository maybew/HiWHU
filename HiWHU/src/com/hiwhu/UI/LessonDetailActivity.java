package com.hiwhu.UI;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.MySqliteHelper;
import com.hiwhu.tool.HttpHelper;

public class LessonDetailActivity extends Activity {

	private static final String TAG = "LessonDetailActivity";

	private TextView lessonName = null, lessonInfo = null,
			lessonSpecial = null;
	private Button operate = null;
	private ProgressDialog progress;

	private int choice;
	private String name, lessonWeek, result, lessonTeacher, lessonLast;
	private SQLiteDatabase db = null;
	private MySqliteHelper my = null;
	private SharedPreferences userData;
	private Cursor c = null;
	private HashMap<String, Object> map;
	private HttpHelper helper;

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			progress.hide();
			switch (msg.what) {
			case 5:
				Toast.makeText(getApplicationContext(), "ѡ��ϵͳ��δ����", Toast.LENGTH_SHORT).show();
				break;
			case 6:
				Toast.makeText(getApplicationContext(), "�����ɹ�", Toast.LENGTH_SHORT).show();
				break;
			case 9:
				progress.setMessage("�����������У����Ժ�");
				progress.show();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.lesson_detail_activity);

		lessonName = (TextView) this.findViewById(R.id.lessonName);
		lessonInfo = (TextView) this.findViewById(R.id.lessonInfo);
		lessonSpecial = (TextView) this.findViewById(R.id.lessonSpecial);
		operate = (Button) this.findViewById(R.id.operate);

		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setMessage("�����У����Ժ�");

		helper = new HttpHelper();
		userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels; // ��Ļ��ȣ����أ�
		lessonInfo.setMaxWidth((int) (width * 2.7 / 4));

		choice = this.getIntent().getIntExtra("choice", 0);
		switch (choice) {
		case 1:
			map = new HashMap<String, Object>();
			name = this.getIntent().getStringExtra("lessonName");
			lessonWeek = this.getIntent().getStringExtra("lessonWeek");
			lessonName.setText(name);
			//operate.setText("����");
			operate.setBackgroundResource(R.drawable.minus);
			if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {// ǿ�ƺ���
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			}
			break;
		case 2:
			map = new HashMap<String, Object>();
			name = this.getIntent().getStringExtra("lessonName");
			lessonWeek = this.getIntent().getStringExtra("lessonWeek");
			lessonTeacher = this.getIntent().getStringExtra("lessonTeacher");
			lessonLast = this.getIntent().getStringExtra("lessonLast");
			lessonName.setText(name);
			//operate.setText("ѡ��");
			operate.setBackgroundResource(R.drawable.plus);
			break;
		}
		handleChoice();

		operate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (choice) {
				case 1:
					AlertDialog.Builder builder = new Builder(
							LessonDetailActivity.this);
					builder.setMessage("ȷ�϶�" + name + "������");
					builder.setTitle("��ʾ");
					builder.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									ConfigurationUtil.deletenum = (String) map
											.get("deletenum");
									handlerDeleteLesson();
								}
							});
					builder.setNegativeButton("ȡ��",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.create().show();
					break;
				case 2:
					if (ConfigurationUtil.chooseList.size() == 6) {
						Toast.makeText(getApplicationContext(), "���ѡȡ6������ѡ�޿γ�",
								Toast.LENGTH_SHORT).show();
						return;
					}
					double point = Double.parseDouble((String) map
							.get("lessonPoint"));
					if (ConfigurationUtil.totalPoint + point > 6) {
						Toast.makeText(getApplicationContext(), "���ѡȡ6��ѧ�ֵĿγ�",
								Toast.LENGTH_SHORT).show();
						return;
					}
					for (int i = 0; i < ConfigurationUtil.chooseList.size(); i++) {
						if (ConfigurationUtil.chooseList.get(i).equals(map)) {
							Toast.makeText(getApplicationContext(), "��ѡ����ͬ�γ�",
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
					ConfigurationUtil.chooseList.add(map);
					ConfigurationUtil.totalPoint += point;
					Toast.makeText(getApplicationContext(), "�ɹ������ѡ�ε�",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}

		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null)
			if (db.isOpen())
				db.close();
	}

	private void handleChoice() {
		my = new MySqliteHelper(this, "mydb", null, 1);
		db = my.getReadableDatabase();
		switch (choice) {
		case 1:
			c = db.query(MySqliteHelper.TB_LESSON, null,
					MySqliteHelper.LESSON_NAME + "=" + "'" + name + "'", null,
					null, null, null);
			break;
		case 2:
			c = db.query(MySqliteHelper.TB_SEARCHLESSON, null,
					MySqliteHelper.LESSON_NAME + "=" + "'" + name + "'"
							+ " and " + MySqliteHelper.LESSON_WEEK + "=" + "'"
							+ lessonWeek + "'" + " and "
							+ MySqliteHelper.LESSON_TEACHER + "=" + "'"
							+ lessonTeacher + "'" + " and "
							+ MySqliteHelper.LESSON_LASTING + "=" + "'"
							+ lessonLast + "'", null, null, null, null);
			map.put("lessonName", name);
			map.put("lessonWeek", lessonWeek);
			map.put("lessonTeacher", lessonTeacher);
			map.put("lessonLast", lessonLast);
			break;
		}

		if (c != null) {
			if (!c.moveToFirst())
				return;

			int week = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_WEEK);
			int temp1 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_LASTING);
			int temp2 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TIME);
			int temp3 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_LOCATION);

			String w = c.getString(week);

			String lasting = c.getString(temp1);
			String time = c.getString(temp2);
			String location = c.getString(temp3);

			int pos = 0;
			String[] xtemp = w.split("@");
			for (int i = 0; i < xtemp.length; i++) {
				if (xtemp[i].equals(lessonWeek)) {
					pos = i;
					break;
				}
			}
			xtemp = time.split("@");
			time = xtemp[pos];

			xtemp = location.split("@");
			location = xtemp[pos];

			xtemp = lasting.split("@");
			lasting = xtemp[pos];

			result = "";
			result += "��ʱ: " + lasting;
			result += "\nѧ��: "
					+ c.getString(c.getColumnIndex(MySqliteHelper.LESSON_POINT));
			if (choice == 2)
				map.put("lessonPoint", c.getString(c
						.getColumnIndex(MySqliteHelper.LESSON_POINT)));
			result += "\n�ڿ���ʦ: "
					+ c.getString(c
							.getColumnIndex(MySqliteHelper.LESSON_TEACHER));
			w = w.replace("@", "");
			if (choice == 1)
				w = this.getResources().getStringArray(R.array.weekday_array)[Integer
						.parseInt(lessonWeek) - 1];
			result += "\n�ڿ�ʱ��: " + w + " " + time;
			result += "\n�ڿεص�: " + location;
			result += "\n��ע: "
					+ c.getString(c.getColumnIndex(MySqliteHelper.LESSON_OTHER));
			lessonInfo.setText(result);
			switch (choice) {
			case 1:
				lessonSpecial.setText("�γ�����\n"
						+ c.getString(c
								.getColumnIndex(MySqliteHelper.LESSON_TYPE)));
				if (c.getString(c.getColumnIndex(MySqliteHelper.LESSON_TYPE))
						.contains("����")) {// ���޿γ̲��ܳ���
					operate.setVisibility(View.INVISIBLE);
				}
				map.put("deletenum", c.getString(c.getColumnIndex(MySqliteHelper.LESSON_STATE)));
				Log.d(TAG, c.getString(c.getColumnIndex(MySqliteHelper.LESSON_STATE)));
				break;
			case 2:
				lessonSpecial
						.setText("ʣ��/�������\n"
								+ c.getString(c
										.getColumnIndex(MySqliteHelper.LESSON_REMAINNUM)));
				break;
			}
		}
		c.close();
		db.close();
	}

	public void handlerDeleteLesson() {
		ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (webManager.getActiveNetworkInfo() != null) {

			progress.setMessage("�����������У����Ժ�");
			progress.show();
			new Thread(new Runnable() {

				@Override
				public void run() {

					ConfigurationUtil.cookie = helper
							.getCookie(ConfigurationUtil.URL_INDEX);
					userData.edit()
							.putString(ConfigurationUtil.USER_COOKIE,
									ConfigurationUtil.cookie).commit();

					helper.setInformation(
							userData.getString(ConfigurationUtil.USER_NAME, ""),
							userData.getString(ConfigurationUtil.USER_PWD, ""));
					helper.getPageContent(ConfigurationUtil.URL_LOGIN, "post",
							100500, "gb2312", ConfigurationUtil.cookie);
					ConfigurationUtil.cookie = helper
							.getCookie(ConfigurationUtil.URL_STU)
							+ ";"
							+ ConfigurationUtil.cookie.split(";")[1];
					helper.getStuIndex(ConfigurationUtil.URL_STUINDEX, "post",
							500200, "gb2312", ConfigurationUtil.cookie,
							LessonDetailActivity.this, handler, userData);

					// ���¿α�
					helper.getLessonStart(ConfigurationUtil.URL_LESSON,
							ConfigurationUtil.cookie, "gb2312", 500200,
							LessonDetailActivity.this, userData, 3);
					if (ConfigurationUtil.ifdelete) {// �ύѡ�ε�
						Message msg = new Message();
						msg.what = 9;
						handler.sendMessage(msg);
						helper.cancelLesson(ConfigurationUtil.URL_CANCELLESSON
								+ ConfigurationUtil.deletenum, "post", 500200,
								"gb2312", ConfigurationUtil.cookie,
								LessonDetailActivity.this);
						helper.getLessonStart(ConfigurationUtil.URL_LESSON,
								ConfigurationUtil.cookie, "gb2312", 500200,
								LessonDetailActivity.this, userData, 3);
						Message msg1 = new Message();
						msg1.what = 6;
						handler.sendMessage(msg1);
					} else {
						Message msg = new Message();
						msg.what = 5;
						handler.sendMessage(msg);
					}
				}

			}).start();

		} else {
			Toast.makeText(getApplicationContext(), "������������",
					Toast.LENGTH_SHORT).show();
		}
	}
}
