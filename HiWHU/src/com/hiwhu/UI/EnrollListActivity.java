package com.hiwhu.UI;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.hiwhu.tool.EnrollListAdapter;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.ConfigurationUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class EnrollListActivity extends Activity{
	
	private Button cancel, upload;
	private ListView enroll_list;
	private TextView info;
	private ProgressDialog progress;
	
	private ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>();
	private EnrollListAdapter listItemAdapter;
	
	private SharedPreferences userData;
	private HttpHelper helper;
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			progress.hide();
			switch(msg.what){
			case 1:
				progress.setMessage("正在提交选课单中，请稍后");
				progress.show();
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "提交成功", Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "选课系统尚未开放", Toast.LENGTH_SHORT).show();
				break;
			case 4:
				check();
				break;
			case 7:
				progress.setMessage("完成公选课更新"+50*msg.arg1+"项");
				progress.show();
				break;
			}
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enroll_list_activity);
        
        cancel = (Button)this.findViewById(R.id.cancel);
        upload = (Button)this.findViewById(R.id.upload);
        info = (TextView)this.findViewById(R.id.info);
        enroll_list = (ListView)this.findViewById(R.id.enroll_list);
        
        helper = new HttpHelper();
		userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
		
		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		check();
		
        enroll_list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				HashMap<String, Object> map = (HashMap<String, Object>) listItemAdapter.getItem(pos);
				EnrollListActivity.this.startActivity(new Intent(EnrollListActivity.this, LessonDetailActivity.class)
				.putExtra("choice", 2)
				.putExtra("lessonName", (String)map.get("lessonName"))
				.putExtra("lessonWeek", (String)map.get("lessonWeek"))
				.putExtra("lessonTeacher", (String)map.get("lessonTeacher"))
				.putExtra("lessonLast", (String)map.get("lessonLast")));
			}
        	
        });
        
        cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				EnrollListActivity.this.finish();
			}
        	
        });
        
        upload.setOnClickListener(new OnClickListener(){

   			@Override
   			public void onClick(View v) {
   				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
   				if (webManager.getActiveNetworkInfo() != null) {
   					
   						progress.setMessage("选课单提交中，请稍后");
   						progress.show();
   				    	new Thread(new Runnable(){

   							@Override
   							public void run() {

   									ConfigurationUtil.cookie = helper.getCookie(ConfigurationUtil.URL_INDEX);
   									userData.edit().putString(ConfigurationUtil.USER_COOKIE, ConfigurationUtil.cookie).commit();
   									helper.setInformation(userData.getString(ConfigurationUtil.USER_NAME, ""), userData.getString(ConfigurationUtil.USER_PWD, ""));
   									helper.getPageContent(ConfigurationUtil.URL_LOGIN, "post", 100500, "gb2312", ConfigurationUtil.cookie);
   									ConfigurationUtil.cookie = helper.getCookie(ConfigurationUtil.URL_STU) + ";"+ConfigurationUtil.cookie.split(";")[1];
   									helper.getStuIndex(ConfigurationUtil.URL_STUINDEX, "post", 500200, "gb2312", ConfigurationUtil.cookie, EnrollListActivity.this, handler, userData);

   								//更新课表
   								helper.setSearch("", "");
   								helper.getSearchLesson(ConfigurationUtil.URL_SEARCHLESSON, "post", 500200, "gb2312", ConfigurationUtil.cookie, EnrollListActivity.this, handler, userData);
   								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
   								Date date = new Date();
   								userData.edit().putString(ConfigurationUtil.PUBCLASS_DATE, format.format(date)).commit();
   								if(ConfigurationUtil.ifchoose){//提交选课单
   									Message msg = new Message();
   									msg.what = 1;
   									handler.sendMessage(msg);
   									helper.uploadLesson(ConfigurationUtil.URL_UPLOADLESSON+userData.getString(ConfigurationUtil.USER_NAME, ""), "post", 500200, "gb2312", ConfigurationUtil.cookie, EnrollListActivity.this);
   									Message msg1 = new Message();
   									msg1.what = 2;
   									handler.sendMessage(msg1);
   								}else{
   									Message msg = new Message();
   									msg.what = 3;
   									handler.sendMessage(msg);
   								}
   							}
   				    		
   				    	}).start();
   				    	
   				}else{
   					Toast.makeText(getApplicationContext(), "请检查网络连接", Toast.LENGTH_SHORT).show();
   				}
   			}
           	
           });
	}
	
	private void check() {
        if(ConfigurationUtil.chooseList.size() == 0)
        	info.setVisibility(View.VISIBLE);
        listItem = new ArrayList<HashMap<String, Object>>();
		
        for(int i = 0; i < ConfigurationUtil.chooseList.size(); i++){
        	listItem.add(ConfigurationUtil.chooseList.get(i));
        }
        
        //生成适配器的Item和动态数组对应的元素  
        listItemAdapter = new EnrollListAdapter(EnrollListActivity.this,
				listItem, 2, handler);
        
        this.enroll_list.setAdapter(listItemAdapter);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		check();
	}
	
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	progress.dismiss();
    }
}
