package com.hiwhu.tool;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiwhu.UI.R;

public class ScoreListAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, Object>> listItem;
	private Context context;

	public ScoreListAdapter(Context c, ArrayList<HashMap<String, Object>> l) {
		this.context = c;
		this.listItem = l;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItem.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView t1;
		TextView t2;
		TextView t3;
		convertView = (RelativeLayout) LayoutInflater.from(context).inflate(
				R.layout.score_list_item, null);
		t1 = (TextView) convertView.findViewById(R.id.lessonName);
		t2 = (TextView) convertView.findViewById(R.id.lessonScore);
		t3 = (TextView) convertView.findViewById(R.id.semester);

		t1.setText(Html.fromHtml(listItem.get(position).get("LessonName")
				.toString()));
		t2.setText(Html.fromHtml(listItem.get(position).get("LessonScore")
				.toString()));
		t3.setText(listItem.get(position).get("Semester").toString());
		return convertView;

	}

}
