package com.hiwhu.UI;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import com.hiwhu.tool.ConfigurationUtil;

public class NetFlowActivity extends Activity{
	
	SharedPreferences userData;
	long flow1, flow2, flow3;
	String unit1 = "KB", unit2 = "KB", unit3 = "KB";
	TextView info_1_2, info_1_3, info_2_2, info_2_3;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netflow_activity);
        
        info_1_2 = (TextView)this.findViewById(R.id.info_1_2);
        info_1_3 = (TextView)this.findViewById(R.id.info_1_3);
        info_2_2 = (TextView)this.findViewById(R.id.info_2_2);
        info_2_3 = (TextView)this.findViewById(R.id.info_2_3);
        
        userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
        
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String time = format.format(date);
		
		if(!time.equals(userData.getString(ConfigurationUtil.FLOW_1_TIME, "2000-01-01"))){
			userData.edit().putLong(ConfigurationUtil.FLOW_1_DAY_UPLOAD,  0).commit();
			userData.edit().putLong(ConfigurationUtil.FLOW_1_DAY_RECEIVE, 0).commit();
			int month = 0;
			try {
				month = format.parse(userData.getString(ConfigurationUtil.FLOW_1_TIME, "2000-01-01")).getMonth();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(month != date.getMonth()){
				userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, 0).commit();
				userData.edit().putLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, 0).commit();
			}
			userData.edit().putString(ConfigurationUtil.FLOW_1_TIME, time).commit();
		}
		
		if(!time.equals(userData.getString(ConfigurationUtil.FLOW_2_TIME, "2000-01-01"))){
			userData.edit().putLong(ConfigurationUtil.FLOW_2_DAY_UPLOAD,  0).commit();
			userData.edit().putLong(ConfigurationUtil.FLOW_2_DAY_RECEIVE, 0).commit();
			int month = 0;
			try {
				month = format.parse(userData.getString(ConfigurationUtil.FLOW_2_TIME, "2000-01-01")).getMonth();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(month != date.getMonth()){
				userData.edit().putLong(ConfigurationUtil.FLOW_2_MONTH_UPLOAD, 0).commit();
				userData.edit().putLong(ConfigurationUtil.FLOW_2_MONTH_RECEIVE, 0).commit();
			}
			userData.edit().putString(ConfigurationUtil.FLOW_2_TIME, time).commit();
		}
        
        
        //每日流量
        flow1 = userData.getLong(ConfigurationUtil.FLOW_1_DAY_UPLOAD, 0);
        flow2 = userData.getLong(ConfigurationUtil.FLOW_2_DAY_UPLOAD, 0);
        setData();
        info_1_2.setText("发送\n"+flow1+unit1+"\n"+flow2+unit2+"\n"+flow3+unit3);
        flow1 = userData.getLong(ConfigurationUtil.FLOW_1_DAY_RECEIVE, 0);
        flow2 = userData.getLong(ConfigurationUtil.FLOW_2_DAY_RECEIVE, 0);
        setData();
        info_1_3.setText("接收\n"+flow1+unit1+"\n"+flow2+unit2+"\n"+flow3+unit3);
        //每月流量
        flow1 = userData.getLong(ConfigurationUtil.FLOW_1_MONTH_UPLOAD, 0);
        flow2 = userData.getLong(ConfigurationUtil.FLOW_2_MONTH_UPLOAD, 0);
        setData();
        info_2_2.setText("发送\n"+flow1+unit1+"\n"+flow2+unit2+"\n"+flow3+unit3);
        flow1 = userData.getLong(ConfigurationUtil.FLOW_1_MONTH_RECEIVE, 0);
        flow2 = userData.getLong(ConfigurationUtil.FLOW_2_MONTH_RECEIVE, 0);
        setData();
        info_2_3.setText("接收\n"+flow1+unit1+"\n"+flow2+unit2+"\n"+flow3+unit3);
	}
	
	private void setData(){
        flow3 = flow1 + flow2;
        
        flow1 = flow1/1024;
        flow2 = flow2/1024;
        flow3 = flow3/1024;
        
        unit1 = unit2 = unit3 = "KB";
        
        //转化为MB
        if(flow1 > 1024){
        	flow1 = flow1/1024;
        	unit1 = "MB";
        	//转化为GB
        	if(flow1 > 1024){
            	flow1 = flow1/1024;
            	unit1 = "GB";
        	}
        }
        if(flow2 > 1024){
        	flow2 = flow2/1024;
        	unit2 = "MB";
        	if(flow2 > 1024){
            	flow2 = flow2/1024;
            	unit2 = "GB";
        	}
        }
        if(flow3 > 1024){
        	flow3 = flow3/1024;
        	unit3 = "MB";
        	if(flow3 > 1024){
            	flow3 = flow3/1024;
            	unit3 = "GB";
        	}
        }
	}
	
}
