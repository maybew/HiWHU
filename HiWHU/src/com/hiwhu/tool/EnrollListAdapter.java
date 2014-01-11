package com.hiwhu.tool;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwhu.UI.LessonDetailActivity;
import com.hiwhu.UI.R;

public class EnrollListAdapter extends BaseAdapter {

	private ArrayList<HashMap<String, Object>> listItem;
	private Context context;
	private int choice;
	Handler handler;

	public EnrollListAdapter(Context c, ArrayList<HashMap<String, Object>> l,
			int choice, Handler handler) {
		this.context = c;
		this.listItem = l;
		this.choice = choice;
		this.handler = handler;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listItem.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return listItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = (RelativeLayout) LayoutInflater.from(context)
					.inflate(R.layout.search_list_item, null);
			holder.name = (TextView) convertView
					.findViewById(R.id.searchItemName);
			holder.operate = (Button) convertView
					.findViewById(R.id.enroll_operate);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.name.setText(Html.fromHtml(listItem.get(position)
				.get("lessonName").toString()));

		switch(choice){
		case 1:
			holder.operate.setBackgroundResource(R.drawable.plus);
			break;
		case 2:
			holder.operate.setBackgroundResource(R.drawable.minus);
			break;
		}
		
		holder.operate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final HashMap<String, Object> map = listItem.get(position);
				switch (choice) {
				case 1:
					
					if (ConfigurationUtil.chooseList.size() == 6) {
						Toast.makeText(context, "最多选取6个公共选修课程",
								Toast.LENGTH_SHORT).show();
						return;
					}
					double point = Double.parseDouble((String) map
							.get("lessonPoint"));
					if (ConfigurationUtil.totalPoint + point > 6) {
						Toast.makeText(context, "最多选取6个学分的课程",
								Toast.LENGTH_SHORT).show();
						return;
					}
					for (int i = 0; i < ConfigurationUtil.chooseList.size(); i++) {
						if (ConfigurationUtil.chooseList.get(i).equals(map)) {
							Toast.makeText(context, "已选择相同课程",
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
					ConfigurationUtil.chooseList.add(map);
					ConfigurationUtil.totalPoint += point;
					Toast.makeText(context, "成功添加至选课单",
							Toast.LENGTH_SHORT).show();
					break;
				case 2:
					AlertDialog.Builder builder = new Builder(context);
					builder.setMessage("确认要取消选课 "+map.get("lessonName"));
					builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							ConfigurationUtil.chooseList.remove(map);
							ConfigurationUtil.totalPoint -= Double.parseDouble((String) map.get("lessonPoint"));
							System.out.println("point====="+ConfigurationUtil.totalPoint);
							Message msg = new Message();
							msg.what = 4;
							handler.sendMessage(msg);
						}
					});
					builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
					builder.create().show();
					break;
				}
			}

		});
		return convertView;
	}

	final class ViewHolder {
		public TextView name;
		public Button operate;
	}

}
