package com.hiwhu.UI;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.hiwhu.UI.R;
import com.hiwhu.tool.MySqliteHelper;
import com.hiwhu.widget.LessonContentLayout;

public class LessonActivity extends Activity {

	LessonContentLayout lcl;
	ArrayList<HashMap<String, Object>> list;
	SQLiteDatabase db = null;
	MySqliteHelper my = null;
	Cursor c = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lesson_activity);
		lcl = (LessonContentLayout) this.findViewById(R.id.lessonContent);

		list = new ArrayList<HashMap<String, Object>>();
		my = new MySqliteHelper(LessonActivity.this, "mydb", null, 1);
		db = my.getReadableDatabase();
		HashMap<String, Object> map = new HashMap<String, Object>();
		c = db.query(MySqliteHelper.TB_LESSON, null, null, null, null, null,
				null);
		int temp1 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_NAME);
		int temp2 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_LOCATION);
		int temp3 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_TIME);
		int temp4 = c.getColumnIndexOrThrow(MySqliteHelper.LESSON_WEEK);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			map = new HashMap<String, Object>();
			map.put("lessonName", c.getString(temp1));
			map.put("lessonLocation", c.getString(temp2));
			map.put("lessonTime", c.getString(temp3).replace("½Ú", ""));
			map.put("lessonWeek", c.getString(temp4));
			System.out.println(map);
			list.add(map);
		}

		lcl.setList(list);
		lcl.invalidate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (db != null)
			if (db.isOpen())
				db.close();
	}
}
