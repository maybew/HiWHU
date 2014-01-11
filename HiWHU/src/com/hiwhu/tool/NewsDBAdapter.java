package com.hiwhu.tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NewsDBAdapter {

	private static final String TAG = "DBAdapter";
	public static final String KEY_ID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DATE = "date";
	public static final String KEY_TYPE = "type";
	public static final String KEY_SUBTITLE = "subtitle";
	public static final String KEY_IMG_URL = "img_url";
	public static final String KEY_URL = "url";

	public static final String TABLE_LECTURE = "lecture";
	public static final String TABLE_WANTED = "wanted";
	public static final String TABLE_INFORMATION = "informaiton";
	public static final String TABLE_SURROUNDING = "srrounding"; 
	public static final String DB_NAME = "news";
	private static final int DB_VERSION = 1;

	private DatabaseHelper DBHelper;
	private SQLiteDatabase db;

	private String[] columns;

	public NewsDBAdapter(Context context) {
		DBHelper = new DatabaseHelper(context);
		columns = new String[] { KEY_ID, KEY_TITLE, KEY_DATE, KEY_TYPE,
				KEY_SUBTITLE, KEY_IMG_URL, KEY_URL };
		// open();
	}

	public NewsDBAdapter open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		DBHelper.close();
		db = null;
	}

	public long insert(String tableName, String title, String date,
			String type, String subtitle, String img, String url) {
		ContentValues v = new ContentValues();
		v.put(KEY_TITLE, title);
		v.put(KEY_DATE, date);
		v.put(KEY_TYPE, type);
		v.put(KEY_SUBTITLE, subtitle);
		v.put(KEY_IMG_URL, img);
		v.put(KEY_URL, url);
		return db.insert(tableName, null, v);

	}

	public boolean update(String table, long id, String title, String date,
			String type, String subtitle, String img, String url) {
		ContentValues v = new ContentValues();
		v.put(KEY_TITLE, title);
		v.put(KEY_DATE, date);
		v.put(KEY_TYPE, type);
		v.put(KEY_SUBTITLE, subtitle);
		v.put(KEY_IMG_URL, img);
		v.put(KEY_URL, url);
		return db.update(table, v, KEY_ID + "=?",
				new String[] { String.valueOf(id) }) > 0;

	}

	public boolean deleteOne(String tableName, long rowId) {
		return db.delete(tableName, KEY_ID + "=?",
				new String[] { String.valueOf(rowId) }) > 0;

	}

	public Cursor queryAllOrderByAsc(String tableName) {
		Cursor mCursor = db.query(tableName, columns, null, null, null, null,
				KEY_ID + " asc");
		if (mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}

	public Cursor queryAllOrderByDesc(String tableName) {
		Cursor mCursor = db.query(tableName, columns, null, null, null, null,
				KEY_ID + " desc");
		if (mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}

	public Cursor query(String table, long id) {

		Cursor mCursor = db.query(true, table, columns, KEY_ID + "=?",
				new String[] { String.valueOf(id) }, null, null, null, null);
		if (mCursor != null)
			mCursor.moveToFirst();
		return mCursor;
	}
	
	public void deleteAll(String table){
		db.delete(table, null, null);
	}
	public void dropAndCreate(String table) {
		drop(table);
		create(table);
		Log.d(TAG, "dropAndCreate " + table + " successfully");

	}
	
	private void drop(String table) {
		String drop = "drop table if exists " + table;
		db.execSQL(drop);

	}

	private void create(String table) {
		String create = "create table if not exists " + table + " ( " + KEY_ID
				+ " integer primary key autoincrement, " 
				+ KEY_TITLE + " VARCHAR , " 
				+ KEY_DATE + " VARCHAR , " 
				+ KEY_TYPE + " VARCHAR , " 
				+ KEY_SUBTITLE + " VARCHAR, " 
				+ KEY_IMG_URL + " VARCHAR, " 
				+ KEY_URL + " VARCHAR );";
		db.execSQL(create);
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

//			String create_lecture = "create table if not exists "
//					+ TABLE_LECTURE + " ( " + KEY_ID
//					+ " integer primary key autoincrement, " + KEY_TITLE
//					+ " VARCHAR , " + KEY_SUBTITLE + " VARCHAR, " + KEY_URL
//					+ " VARCHAR );";
//			String create_wanted = "create table if not exists " + TABLE_WANTED
//					+ "( " + KEY_ID + " integer primary key autoincrement, "
//					+ KEY_TITLE + " VARCHAR, " + KEY_SUBTITLE + " VARCHAR, "
//					+ KEY_URL + " VARCHAR );";
//			String create_information = "create table if not exists "
//					+ TABLE_INFORMATION + " ( " + KEY_ID
//					+ " integer primary key autoincrement, " + KEY_TITLE
//					+ " VARCHAR, " + KEY_SUBTITLE + " VARCHAR, " + KEY_URL
//					+ " VARCHAR );";
//
//			db.execSQL(create_lecture);
//			db.execSQL(create_wanted);
//			db.execSQL(create_information);
			create(db,TABLE_LECTURE);
			create(db,TABLE_WANTED);
			create(db,TABLE_INFORMATION);
			create(db,TABLE_SURROUNDING);
			Log.d(TAG, "Create table successfully");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading DB from version " + oldVersion + " to "
					+ newVersion);
//			String drop1 = "drop table if exists " + TABLE_LECTURE;
//			String drop2 = "drop table if exists " + TABLE_WANTED;
//			String drop3 = "drop table if exists " + TABLE_INFORMATION;
//			db.execSQL(drop1);
//			db.execSQL(drop2);
//			db.execSQL(drop3);
			drop(db,TABLE_LECTURE);
			drop(db,TABLE_WANTED);
			drop(db,TABLE_INFORMATION);
			drop(db,TABLE_SURROUNDING);
			onCreate(db);

		}
		
		private void drop(SQLiteDatabase db, String table) {
			String drop = "drop table if exists " + table;
			db.execSQL(drop);

		}

		private void create(SQLiteDatabase db, String table) {
			String create = "create table if not exists " + table + " ( " + KEY_ID
					+ " integer primary key autoincrement, " 
					+ KEY_TITLE + " VARCHAR , " 
					+ KEY_DATE + " VARCHAR , " 
					+ KEY_TYPE + " VARCHAR , " 
					+ KEY_SUBTITLE + " VARCHAR, " 
					+ KEY_IMG_URL + " VARCHAR, " 
					+ KEY_URL + " VARCHAR );";
			db.execSQL(create);
		}
	}

}
