package com.hiwhu.UI;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiwhu.tool.ConfigurationUtil;

public class PrivacyControlActivity extends Activity{
	
	private static final String TAG = "PrivacyControlActivity";
	
	private SharedPreferences userData;
	private TextView msg = null;
	private EditText input = null;
	private Button button1 = null, button2 = null;
	private int status = 0;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_control_activity);
        
        msg = (TextView)this.findViewById(R.id.msg);
        input = (EditText)this.findViewById(R.id.input);
        button1 = (Button)this.findViewById(R.id.button1);
        button2 = (Button)this.findViewById(R.id.button2);
        
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        
        userData = this.getSharedPreferences(ConfigurationUtil.USER_DATA, 0);
        
        if(!userData.getBoolean(ConfigurationUtil.IFCONTROLED, false)){
        	status = 1;
        	input.setVisibility(View.INVISIBLE);
        	button2.setVisibility(View.INVISIBLE);
        }else{
        	status = 3;
        	button1.setText(R.string.cancel);
        	input.setVisibility(View.INVISIBLE);
        	button2.setVisibility(View.INVISIBLE);
        }
        
        button1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				if(status == 1 || status == 3){
					msg.setText(R.string.authentication);
					input.setVisibility(View.VISIBLE);
					button1.setVisibility(View.INVISIBLE);
					button2.setVisibility(View.VISIBLE);
					if(status == 1)
						status = 2;
					else
						status = 4;
				}else{
					PrivacyControlActivity.this.finish();
				}
			}
        	
        });
        
        button2.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				//“˛≤ÿ ‰»Î∑®
				InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.hideSoftInputFromWindow(PrivacyControlActivity.this.getCurrentFocus().getWindowToken(), 
						InputMethodManager.HIDE_NOT_ALWAYS);
				
				//±»∂‘√‹¬Î
				String password = input.getText().toString();
				if(userData.getString(ConfigurationUtil.USER_PWD, "").equals(password.toUpperCase())){
					if(status == 2)
						userData.edit().putBoolean(ConfigurationUtil.IFCONTROLED, true).commit();
					else
						userData.edit().putBoolean(ConfigurationUtil.IFCONTROLED, false).commit();
					input.setVisibility(View.INVISIBLE);
					button2.setVisibility(View.INVISIBLE);
					button1.setVisibility(View.VISIBLE);
					button1.setText(R.string.confirm);
					if(status == 2)
						msg.setText(R.string.start_msg);
					else
						msg.setText(R.string.cancel_msg);
				}else{
					Toast.makeText(getApplicationContext(), "√‹¬Î¥ÌŒÛ", Toast.LENGTH_SHORT).show();
				}
			}
        	
        });
	}

}
