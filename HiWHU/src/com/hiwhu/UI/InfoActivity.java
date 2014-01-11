package com.hiwhu.UI;

import com.hiwhu.tool.UpdateManager;
import com.hiwhu.widget.FuncImageButton;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class InfoActivity extends Activity {

	private TextView info_content;
	private FuncImageButton info, back, update;
	private ProgressDialog progress;
	final UpdateManager updateManager = new UpdateManager(InfoActivity.this);

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			progress.hide();
			switch (msg.what) {
			case 1:
				Toast.makeText(getApplicationContext(), "您的版本已是最新版本",
						Toast.LENGTH_SHORT).show();
				break;
			case 2:
				updateManager.showNoticeDialog();
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_activity);

		info_content = (TextView) this.findViewById(R.id.info_content);
		info = (FuncImageButton) this.findViewById(R.id.info);
		back = (FuncImageButton) this.findViewById(R.id.back);
		update = (FuncImageButton) this.findViewById(R.id.update);

		progress = new ProgressDialog(this);
		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progress.setMessage("加载中，请稍后");

		info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				info_content.setText(R.string.info1);
			}

		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				InfoActivity.this.finish();
			}

		});

		update.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 版本更新！！！！！！！！
				ConnectivityManager webManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				if (webManager.getActiveNetworkInfo() != null) {
					progress.show();
					new Thread(new Runnable(){

						@Override
						public void run() {
							if (updateManager.checkUpdateInfo()) {//更新
								Message msg = new Message();
								msg.what = 2;
								handler.sendMessage(msg);
							}else{//无需更新
								Message msg = new Message();
								msg.what = 1;
								handler.sendMessage(msg);
							}
						}
						
					}).start();
						
				} else {
					Toast.makeText(getApplicationContext(), "无网络连接",
							Toast.LENGTH_SHORT).show();
				}
			}

		});
	}
}
