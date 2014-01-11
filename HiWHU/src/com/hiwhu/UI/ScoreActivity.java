package com.hiwhu.UI;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.hiwhu.tool.MySqliteHelper;
import com.hiwhu.tool.ScoreListAdapter;
import com.hiwhu.widget.FuncImageButton;

public class ScoreActivity extends Activity {

	private static final String TAG = "ScoreActivity";

	private ListView scoreList = null;
	private FuncImageButton all_score, analyse_gpa, back;
	
	private double num1, num2, num3, num4, num5, num6, 
	total_num1, total_num2, total_num3, total_num4, total_num5, total_num6,
	num11, num21, num31, num41,
	total_num11, total_num21, total_num31, total_num41;
	private SQLiteDatabase db = null;
	private MySqliteHelper my = null;
	private Cursor c = null;
	private ArrayList<HashMap<String, Object>> listItem = null;
	private SimpleAdapter listItemAdapter;
	private ScoreListAdapter scoreListAdapter;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case 1:
				scoreList.setAdapter(scoreListAdapter);
				break;
			case 2:
				scoreList.setAdapter(listItemAdapter);
				break;
			}
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.score_activity);
		scoreList = (ListView) findViewById(R.id.scoreList);
		all_score = (FuncImageButton) findViewById(R.id.all_score);
		analyse_gpa = (FuncImageButton) findViewById(R.id.analyse_gpa);
		back = (FuncImageButton) findViewById(R.id.back);

		listItem = new ArrayList<HashMap<String, Object>>();

		all_score.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				getAllScore();
			}
			
		});
		
		analyse_gpa.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				analyse_gpa();
			}
			
		});
		
		back.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				ScoreActivity.this.finish();
			}
			
		});
		
		getAllScore();
	}

	private void getAllScore(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				listItem.clear();
				my = new MySqliteHelper(ScoreActivity.this, "mydb", null, 1);
				db = my.getReadableDatabase();
				c = db.query(MySqliteHelper.TB_SCORE, null, null, null, null,
						null, null);

				int temp1 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_NAME);
				int temp2 = c
						.getColumnIndexOrThrow(MySqliteHelper.LESSON_SCORE);
				int temp3 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_YEAR);
				int temp4 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TERM);
				int temp5 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TYPE);
				int temp6 = c.getColumnIndexOrThrow(MySqliteHelper.LEARN_TYPE);

				String name = "", semester = "", score = "", raw = "";
				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					if (semester.equals("")) {// 初始化
						semester = c.getString(temp3) + "年"
								+ c.getString(temp4) + "学期";
					} else if (!semester.equals(c.getString(temp3) + "年"
							+ c.getString(temp4) + "学期")) {// 不同学期的分开放置
						HashMap<String, Object> map = new HashMap<String, Object>();
						name = name.substring(0, name.length() - 1);
						score = score.substring(0, score.length() - 1);
						map.put("LessonName", name);
						map.put("LessonScore", score);
						map.put("Semester", semester);
						listItem.add(map);
						name = "";
						score = "";
						semester = c.getString(temp3) + "年"
								+ c.getString(temp4) + "学期";
					}
					raw = c.getString(temp1);
					if (raw.length() > 12) {
						raw = raw.substring(0, 12) + "...";
					}
					// 选修换色
					if (c.getString(temp5).contains("选修")) {
						name += "<font color=\"#606060\">" + raw + "</font>"
								+ "<br>";
						score += "<font color=\"#606060\">" + c.getInt(temp2)
								+ "</font>" + "<br>";
					} else if (c.getString(temp6).contains("重修")) {
						name += "<font color=\"#755432\">" + raw + "</font>"
								+ "<br>";
						score += "<font color=\"#755432\">" + c.getInt(temp2)
								+ "</font>" + "<br>";
					} else {
						name += raw + "<br>";
						score += c.getInt(temp2) + "<br>";
					}

				}
				// 最后一个学期数据
				HashMap<String, Object> map = new HashMap<String, Object>();
				name = name.substring(0, name.length() - 1);
				score = score.substring(0, score.length() - 1);
				map.put("LessonName", name);
				map.put("LessonScore", score);
				map.put("Semester", semester);
				System.out.println(name);
				listItem.add(map);

				scoreListAdapter = new ScoreListAdapter(ScoreActivity.this,
						listItem);

				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);

				c.close();
				db.close();
			}

		}).start();
	}
	
	private void analyse_gpa(){
		new Thread(new Runnable() {

			@Override
			public void run() {
				listItem.clear();
				my = new MySqliteHelper(ScoreActivity.this, "mydb", null, 1);
				db = my.getReadableDatabase();
				c = db.query(MySqliteHelper.TB_SCORE, null, null, null, null,
						null, null);
				int temp1 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_SCORE);
				int temp2 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_POINT);
				int temp3 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_YEAR);
				int temp4 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TERM);
				int temp5 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TYPE);

				String semester = "";
				DecimalFormat df =  new DecimalFormat( "0.000"); 
				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					if (semester.equals("")) {// 初始化
						semester = c.getString(temp3) + "年"
								+ c.getString(temp4) + "学期";
					} else if (!semester.equals(c.getString(temp3) + "年"
							+ c.getString(temp4) + "学期")) {// 不同学期的分开放置
						HashMap<String, Object> map = new HashMap<String, Object>();
						map.put("LessonName", "总评GPA\n必修GPA\n专业GPA\n\n公共必修\n公共选修\n专业必修\n专业选修");
						map.put("LessonScore", df.format(num1/num6)+"\n"+df.format(num2/num4)+"\n"+df.format(num3/num5)+"\n\n"+
								num11+"\n"+num21+"\n"+num31+"\n"+num41);
						map.put("Semester", semester);
						listItem.add(map);
						total_num1 += num1;
						total_num2 += num6;
						total_num3 += num2;
						total_num4 += num4;
						total_num5 += num3;
						total_num6 += num5;
						num1 = num2 = num3 = num4 = num5 =num6 =0;
						total_num11 += num11;
						total_num21 += num21;
						total_num31 += num31;
						total_num41 += num41;
						num11 = num21 = num31 = num41 = 0;
						
						semester = c.getString(temp3) + "年"
								+ c.getString(temp4) + "学期";
					}

					double score = c.getDouble(temp1);
					double point = c.getDouble(temp2);
					double gpa;
					if(score >= 90) gpa = 4.0;
    				else if(score >=85) gpa = 3.7;
    				else if(score >=82) gpa = 3.3;
    				else if(score >=78) gpa = 3.0;
    				else if(score >=75) gpa = 2.7;
    				else if(score >=72) gpa = 2.5;
    				else if(score >=68) gpa = 2.0;
    				else if(score >=64) gpa = 1.5;
    				else if(score >=60) gpa = 1.0;
    				else gpa = 0;
					
					if (c.getString(temp5).contains("必修")) {
						num2 += point * gpa;
						num4 += point;
					}
					if (c.getString(temp5).contains("专业")) {
						num3 += point * gpa;
						num5 += point;
					}
					num1 += point * gpa;
					num6 += point;
					
					if (c.getString(temp5).contains("公共选修")) {
						num21 += Double.parseDouble(c.getString(temp2));
					} else if (c.getString(temp5).contains("公共必修")) {
						num11 += Double.parseDouble(c.getString(temp2));
					} else if (c.getString(temp5).contains("专业选修")) {
						num41 += Double.parseDouble(c.getString(temp2));
					} else {
						num31 += Double.parseDouble(c.getString(temp2));
					}

				}
				// 最后一个学期数据
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("LessonName", "总评GPA\n必修GPA\n专业GPA\n\n公共必修\n公共选修\n专业必修\n专业选修");
				map.put("LessonScore", df.format(num1/num6)+"\n"+df.format(num2/num4)+"\n"+df.format(num3/num5)+"\n\n"+
						num11+"\n"+num21+"\n"+num31+"\n"+num41);
				map.put("Semester", semester);
				listItem.add(map);
				total_num1 += num1;
				total_num2 += num6;
				total_num3 += num2;
				total_num4 += num4;
				total_num5 += num3;
				total_num6 += num5;
				total_num11 += num11;
				total_num21 += num21;
				total_num31 += num31;
				total_num41 += num41;
				//总评
				map = new HashMap<String, Object>();
				map.put("LessonName", "总评GPA\n必修GPA\n专业GPA\n\n公共必修\n公共选修\n专业必修\n专业选修\n总计");
				map.put("LessonScore", df.format(total_num1/total_num2)+"\n"+df.format(total_num3/total_num4)+"\n"+df.format(total_num5/total_num6)
						+"\n\n"+total_num11+"\n"+total_num21+"\n"+total_num31+"\n"+total_num41+"\n"+
						(total_num11+total_num21+total_num31+total_num41));
				map.put("Semester", "所有成绩");
				listItem.add(0, map);

				num1 = num2 = num3 = num4 = num5 =num6 =0;
				num11 = num21 = num31 = num41 = 0;
				total_num11 = total_num21 = total_num31 = total_num41 = 0;
				
				listItemAdapter = new SimpleAdapter(
						ScoreActivity.this, listItem,// 数据源
						R.layout.score_list_item,// ListItem的XML实现
						// 动态数组与ImageItem对应的子项
						new String[] { "LessonName", "LessonScore",
								"Semester" },
						// ImageItem的XML文件里面的一个ImageView,两个TextView ID
						new int[] { R.id.lessonName, R.id.lessonScore ,R.id.semester });

				Message msg = new Message();
				msg.what = 2;
				handler.sendMessage(msg);

				c.close();
				db.close();
			}

		}).start();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null)
			if (db.isOpen())
				db.close();
	}

}
