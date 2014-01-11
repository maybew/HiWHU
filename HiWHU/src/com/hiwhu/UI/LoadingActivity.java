package com.hiwhu.UI;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;

import com.hiwhu.tool.ConfigurationUtil;
import com.hiwhu.tool.ImageLoader;

public class LoadingActivity extends Activity{
	
	private SharedPreferences userData;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);
        ImageLoader.checkSDDir();
        Timer timer = new Timer();
        timer.schedule(new MyTimerTask(), 1200);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			//½ûÖ¹·µ»Ø
    		return true;
		}
		return false;
	}
	
	class MyTimerTask extends TimerTask{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			userData = LoadingActivity.this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
			if (!userData.getBoolean(ConfigurationUtil.IFSIGNED, false)) {
				LoadingActivity.this.startActivity(new Intent(LoadingActivity.this,
						LoginActivity.class).putExtra("choice", 0));
			}else{
				LoadingActivity.this.startActivity(new Intent(LoadingActivity.this,
						MainActivity.class));
			}
			finish();
		}
		
	}
}
