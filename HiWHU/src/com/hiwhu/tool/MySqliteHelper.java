package com.hiwhu.tool;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySqliteHelper extends SQLiteOpenHelper{
	//查看成绩
	public static final String TB_SCORE = "score";
	public static final String LESSON_NAME = "lesson_name";
	public static final String LESSON_COLLEGE = "lesson_college";
	public static final String LESSON_TEACHER = "lesson_teacher";
	public static final String LESSON_YEAR = "lesson_year";
	public static final String LESSON_TERM = "lesson_term";
	public static final String LESSON_TYPE = "lesson_type";
	public static final String LESSON_POINT = "lesson_point";
	public static final String LESSON_SCORE = "lesson_score";
	public static final String LEARN_TYPE = "learn_type";
	
	//查看课程

	public static final String TB_LESSON = "lesson";
	public static final String LESSON_STATE = "lesson_state";
	public static final String LESSON_TIME = "lesson_time";
	public static final String LESSON_WEEK = "lesson_week";
	public static final String LESSON_LOCATION = "lesson_location";
	public static final String LESSON_LASTING = "lesson_lasting";
	public static final String LESSON_LEARNTIME = "lesson_learntime";
	public static final String LESSON_OTHER = "lesson_other";
	
	//搜索课程
	public static final String TB_SEARCHLESSON = "searchlesson";
	public static final String LESSON_REMAINNUM = "lesson_remainnum";
	public static final String LESSON_ID = "lesson_id";
	public static final String LESSON_NUM = "lesson_num";
	
	public MySqliteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		//Attention:注意SQL语法，每个变量后需要有空格，否则不认识。
		db.execSQL("CREATE TABLE IF NOT EXISTS "+TB_SCORE+" ("
				+LESSON_NAME+" VARCHAR,"
				+LESSON_COLLEGE+" VARCHAR,"
				+LESSON_TEACHER+" VARCHAR,"
				+LESSON_YEAR+" VARCHAR,"
				+LESSON_TERM+" VARCHAR,"
				+LESSON_POINT+" VARCHAR,"
				+LESSON_TYPE+" VARCHAR,"
				+LEARN_TYPE+" VARCHAR,"
				+LESSON_SCORE+" VARCHAR )");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "+TB_LESSON+" ("
				+LESSON_STATE+" VARCHAR,"
				+LESSON_NAME+" VARCHAR,"
				+LESSON_TYPE+" VARCHAR,"
				+LESSON_POINT+" VARCHAR,"
				+LESSON_LEARNTIME+" VARCHAR,"
				+LESSON_COLLEGE+" VARCHAR,"
				+LESSON_TEACHER+" VARCHAR,"
				+LESSON_TIME+" VARCHAR,"
				+LESSON_OTHER+" VARCHAR,"
				+LESSON_LOCATION+" VARCHAR,"
				+LESSON_LASTING+" VARCHAR,"
				+LESSON_WEEK+"  VARCHAR )");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS "+TB_SEARCHLESSON+" ("
				+LESSON_ID+" VARCHAR,"
				+LESSON_NAME+" VARCHAR,"
				+LESSON_TYPE+" VARCHAR,"
				+LESSON_POINT+" VARCHAR,"
				+LESSON_LEARNTIME+" VARCHAR,"
				+LESSON_COLLEGE+" VARCHAR,"
				+LESSON_TEACHER+" VARCHAR,"
				+LESSON_TIME+" VARCHAR,"
				+LESSON_OTHER+" VARCHAR,"
				+LESSON_LOCATION+" VARCHAR,"
				+LESSON_LASTING+" VARCHAR,"
				+LESSON_REMAINNUM+" VARCHAR,"
				+LESSON_NUM+" VARCHAR,"
				+LESSON_WEEK+"  VARCHAR )");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		//更新的时候删除表重建，这个方法应该被修改，暂时不可用
		db.execSQL("DROP TABLE IF EXISTS "+TB_SCORE);
		db.execSQL("DROP TABLE IF EXISTS "+TB_LESSON);
		db.execSQL("DROP TABLE IF EXISTS "+TB_SEARCHLESSON);
		onCreate(db);
	}
	
	public void createSearchLessonTB(SQLiteDatabase db){
		db.execSQL("DROP TABLE IF EXISTS "+TB_SEARCHLESSON);
		db.execSQL("CREATE TABLE IF NOT EXISTS "+TB_SEARCHLESSON+" ("
				+LESSON_ID+" VARCHAR,"
				+LESSON_NAME+" VARCHAR,"
				+LESSON_TYPE+" VARCHAR,"
				+LESSON_POINT+" VARCHAR,"
				+LESSON_LEARNTIME+" VARCHAR,"
				+LESSON_COLLEGE+" VARCHAR,"
				+LESSON_TEACHER+" VARCHAR,"
				+LESSON_TIME+" VARCHAR,"
				+LESSON_OTHER+" VARCHAR,"
				+LESSON_LOCATION+" VARCHAR,"
				+LESSON_LASTING+" VARCHAR,"
				+LESSON_REMAINNUM+" VARCHAR,"
				+LESSON_WEEK+"  VARCHAR )");
	}
}
