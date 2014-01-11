package com.hiwhu.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hiwhu.UI.R;
import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.HttpHelper;
import com.hiwhu.tool.MySqliteHelper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
    /** Called when the activity is first created. */
	SharedPreferences userData;
	
	Button submit = null;
	EditText userName = null;
	EditText userPwd = null;
	HttpHelper helper = null;
	
	ProgressDialog progress;
	MySqliteHelper my;
	
	String real_name;
	long upload, receive;
	
	boolean noneed = false;
	
	int choice = -1;
	
	Handler handler = new Handler(){
		public void  handleMessage(Message msg){
			if(msg.arg2 != 1 && msg.arg2 != 2 )
			progress.hide();
			switch(msg.what){

			case 2:
/*				String temp = "";
				for(int i = 0; i < helper.getScore().size(); i++){
					temp += helper.getClassName().get(i) + "\t";
					temp += helper.getScore().get(i)+ "\n";
				}*/
				/*result.setText(helper.tempRaw);//第二种方法
				result.invalidate();*/
		        
		        userData.edit().putBoolean(ConfigurationUtil.IFSIGNED, true).commit();
		        userData.edit().putString(ConfigurationUtil.USER_NAME, userName.getText().toString()).commit();
		        userData.edit().putString(ConfigurationUtil.USER_PWD, userPwd.getText().toString().toUpperCase()).commit();
		        userData.edit().putString(ConfigurationUtil.USER_LIB_PWD, userPwd.getText().toString().toUpperCase()).commit();
		        
		        //切换用户以及更新
		        getData();
		        //else if(choice == 2)uploadLesson();//选课
		        //else if(choice == 3)cancelLesson();//撤课
		        	
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
				break;
			case 5:
				Toast.makeText(getApplicationContext(), "请检查您的网络连接", Toast.LENGTH_SHORT).show();
				break;
			case 6:
				real_name = helper.getName();
				userData.edit().putString(ConfigurationUtil.REAL_NAME, real_name).commit();
				upload = TrafficStats.getUidTxBytes(Process.myUid()) - upload;
				receive = TrafficStats.getUidRxBytes(Process.myUid()) - receive;
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date();
				String time = format.format(date);
				if(time.equals(userData.getString(ConfigurationUtil.FLOW_1_TIME, "2000-01-01"))){
					userData.edit().putLong(ConfigurationUtil.FLOW_1_DAY_UPLOAD, 
							userData.getLong(ConfigurationUtil.FLOW_1_DAY_UPLOAD, 0) + upload).commit();
					userData.edit().putLong(ConfigurationUtil.FLOW_1_DAY_RECEIVE, 
							userData.getLong(ConfigurationUtil.FLOW_1_DAY_RECEIVE, 0) + receive).commit();
					userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, 
							userData.getLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, 0) + upload).commit();
					userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, 
							userData.getLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, 0) + receive).commit();
				}else{
					userData.edit().putLong(ConfigurationUtil.FLOW_1_DAY_UPLOAD,  upload).commit();
					userData.edit().putLong(ConfigurationUtil.FLOW_1_DAY_RECEIVE, receive).commit();
					int month = 0;
					try {
						month = format.parse(userData.getString(ConfigurationUtil.FLOW_1_TIME, "2000-01-01")).getMonth();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(month == date.getMonth()){
						userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, 
								userData.getLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, 0) + upload).commit();
						userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, 
								userData.getLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, 0) + receive).commit();
					}else{
						userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, upload).commit();
						userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, receive).commit();
					}
					userData.edit().putString(ConfigurationUtil.FLOW_1_TIME, time).commit();
				}
				Toast.makeText(getApplicationContext(), "更新完毕", Toast.LENGTH_SHORT).show();
				if(choice == 0)
					LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
				LoginActivity.this.finish();
				break;
			case 7:
				if(msg.arg1 == 1 && msg.arg2 == 1)
					progress.setMessage("正在获取成绩信息");
				else if(msg.arg1 == 2 && msg.arg2 == 1)
					progress.setMessage("正在获取课表信息");
				else if(msg.arg1 == 0 && msg.arg2 == 2){
					progress.setMessage("正在加载中，请稍后");
				}else{
					progress.setMessage("已完成公选课更新"+50*msg.arg1+"项");
				}
				break;
			case 8:
				progress.setMessage("正在提交选修课表单");
				progress.show();
				break;
			case 9:
				Toast.makeText(getApplicationContext(), "选课系统尚未开放", Toast.LENGTH_SHORT).show();
				break;
			case 10:
				progress.setMessage("正在处理撤课请求");
				progress.show();
				break;
			case 11:
				Toast.makeText(getApplicationContext(), "处理完成", Toast.LENGTH_SHORT).show();
				LoginActivity.this.finish();
				break;
			}
		}
	};
	
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	progress.dismiss();
    }

	public void getData() {
		// TODO Auto-generated method stub
    	progress.setMessage("正在更新数据");
    	progress.show();
    	
    	new Thread(new Runnable(){

			@Override
			public void run() {

				ConfigurationUtil.cookie = helper.getCookie(ConfigurationUtil.URL_STU) + ";"+ConfigurationUtil.cookie.split(";")[1];
				helper.getStuIndex(ConfigurationUtil.URL_STUINDEX, "post", 500200, "gb2312", ConfigurationUtil.cookie, LoginActivity.this, handler, userData);
				Message msg = new Message();
				msg.what = 7;
				msg.arg1 = 1;
				msg.arg2 = 1;
				handler.sendMessage(msg);
				//0表示更新，1表示切换用户
				helper.getScoreStart(ConfigurationUtil.URL_SCORE, ConfigurationUtil.cookie, "gb2312", 500200, LoginActivity.this, userData, choice);
				Message msg1 = new Message();
				msg1.what = 7;
				msg1.arg1 = 2;
				msg1.arg2 = 1;
				handler.sendMessage(msg1);
				//0表示更新，1表示切换用户
				helper.getLessonStart(ConfigurationUtil.URL_LESSON, ConfigurationUtil.cookie, "gb2312", 500200, LoginActivity.this, userData, choice);
				
/*				helper.setSearch("", "");
				helper.getSearchLesson(ConfigurationUtil.URL_SEARCHLESSON, "post", 500200, "gb2312", ConfigurationUtil.cookie, LoginActivity.this, handler, userData);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date date = new Date();
				userData.edit().putString(ConfigurationUtil.PUBCLASS_DATE, format.format(date)).commit();*/
				Message msg2 = new Message();
				msg2.what = 6;
				handler.sendMessage(msg2);
			}
    		
    	}).start();
    	
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        choice = this.getIntent().getIntExtra("choice", -1);//0表示更新，1表示切换用户，2表示选课，3表示撤课
        userData=LoginActivity.this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
        
        System.setProperty("sun.net.client.defaultConnectTimeout", "9000");
        System.setProperty("sun.net.client.defaultReadTimeout", "9000");
        
        submit = (Button)findViewById(R.id.submit);
        userName = (EditText)findViewById(R.id.userNameIn);
        userPwd = (EditText)findViewById(R.id.userPwdIn);
        userPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        progress = new ProgressDialog(this);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setMessage("关联账户中，请稍后");
        
        helper = new HttpHelper();
        
        userName.setText(userData.getString(ConfigurationUtil.USER_NAME, ""));
        userPwd.setText(userData.getString(ConfigurationUtil.USER_PWD, ""));
        
        submit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(LoginActivity.this.getCurrentFocus().getWindowToken(), 
						InputMethodManager.HIDE_NOT_ALWAYS);
				
				ConnectivityManager webManager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
				if(webManager.getActiveNetworkInfo() == null){
					Toast.makeText(getApplicationContext(), "暂时无法登录，请检查网络连接后重试", Toast.LENGTH_LONG).show();
					return;
				}

				progress.setMessage("关联账户中，请稍后");
				progress.show();
				upload = TrafficStats.getUidTxBytes(Process.myUid());
				receive = TrafficStats.getUidRxBytes(Process.myUid());
				
		        new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						Message msg = new Message();
						
						ConfigurationUtil.cookie = helper.getCookie(ConfigurationUtil.URL_INDEX);
						userData.edit().putString(ConfigurationUtil.USER_COOKIE, ConfigurationUtil.cookie).commit();
						//userData.edit().putLong(ConfigurationUtil.COOKIE_TIME, System.currentTimeMillis()).commit();
						helper.setInformation(userName.getText().toString(), userPwd.getText().toString().toUpperCase());
						//helper.getPageContent(url, "post", 100500, "gb2312");
						
						int i = helper.getPageContent(ConfigurationUtil.URL_LOGIN, "post", 100500, "gb2312", ConfigurationUtil.cookie);

						if(i == 1){
							msg.what = 2;
							handler.sendMessage(msg);
						
						}else if(i == 2){
							msg.what = 3;
							handler.sendMessage(msg);
							
						}else{
							msg.what = 5;
							handler.sendMessage(msg);
							
						}
					}
		        	
		        }).start();

			}
        	
        });
        

    }
}