package com.hiwhu.UI;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.HttpHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class CampusCardActivity extends Activity{
	
	private TextView info;
	private Button xujie;
	private ProgressDialog progress;
	
	private HttpHelper helper;
	private String balance, lib;
	private SharedPreferences userData;
	private String xujie_url = "";
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			progress.hide();
			switch(msg.what){
			case 1:
				info.setText(balance + "\n\n" + lib);
				xujie.setVisibility(View.VISIBLE);
				if(lib.equals("当前借阅数: 0"))
					xujie.setVisibility(View.INVISIBLE);
				break;
			case 2:
				Toast.makeText(getApplicationContext(), "密码错误", Toast.LENGTH_SHORT).show();
				AlertDialog.Builder builder = new Builder(CampusCardActivity.this);
				LayoutInflater inflater = LayoutInflater.from(CampusCardActivity.this); 
				final View myView = inflater.inflate(R.layout.privacy_control_dialog, null);  
		        final EditText input=(EditText)myView.findViewById(R.id.input);
		        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
				builder.setMessage("请输入您的图书馆登录密码");
				builder.setTitle("提示");
				builder.setView(myView);  
				builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String password = input.getText().toString();
						userData.edit().putString(ConfigurationUtil.USER_LIB_PWD, password).commit();
						dialog.dismiss();	
						getData();
					}
				});
				builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				builder.create().show();
				break;
			case 3:
				Toast.makeText(getApplicationContext(), "续借成功", Toast.LENGTH_SHORT).show();
				break;
			}
			
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campus_card_activity);
        
        userData= getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
        
        info = (TextView)this.findViewById(R.id.info);
        xujie = (Button)this.findViewById(R.id.xujie);
        
        helper = new HttpHelper();
        
		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setMessage("加载中，请稍后");
		
        getData();
        
        xujie.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				progress.show();
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						helper.getContent(xujie_url, "utf-8");
						Message msg= new Message();
						msg.what = 3;
						handler.sendMessage(msg);
					}
					
				}).start();
				
			}
        	
        });
	}
	
	private void getData(){
		ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (webManager.getActiveNetworkInfo() != null) {
			progress.show();
	        new Thread(new Runnable(){

				@Override
				public void run() {
					helper.setInformation(userData.getString(ConfigurationUtil.USER_NAME, ""),
							userData.getString(ConfigurationUtil.USER_LIB_PWD, userData.getString(ConfigurationUtil.USER_PWD, "")));
					lib = helper.getLibInfo(ConfigurationUtil.URL_LIB, "utf-8");
					if(lib == null){
						Message msg = new Message();
						msg.what = 2;
						handler.sendMessage(msg);
						return;
					}else if(lib.equals("0")){
						lib = "当前借阅数: 0";
					}else{
						String[] temp;
						temp = lib.split("@");
						lib = "";
						for(int i = 0; i < temp.length; i++){
							if(i == 1){
								xujie_url = temp[i];
								continue;
							}
							if(i == temp.length -1)
								lib += temp[i];
							else
								lib += temp[i]+"\n\n";;
						}
					}
					
					ConfigurationUtil.cookie = helper.getCookie(ConfigurationUtil.URL_INDEX);
					helper.setInformation(userData.getString(ConfigurationUtil.USER_NAME, ""), userData.getString(ConfigurationUtil.USER_PWD, ""));
					helper.getPageContent(ConfigurationUtil.URL_LOGIN, "post", 100500, "gb2312", ConfigurationUtil.cookie);
					ConfigurationUtil.cookie = helper.getCookie(ConfigurationUtil.URL_STU) + ";"+ConfigurationUtil.cookie.split(";")[1];
					balance = helper.getCampusCardInfo(ConfigurationUtil.URL_BALANCE, "utf-8", ConfigurationUtil.cookie);
					
					if(progress.isShowing()){
						Message msg = new Message();
						msg.what = 1;
						handler.sendMessage(msg);
					}
				}
	        	
	        }).start();
		}else{
			Toast.makeText(this, "无网络连接", Toast.LENGTH_SHORT).show();
		}
	}
}
