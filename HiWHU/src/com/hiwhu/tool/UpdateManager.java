package com.hiwhu.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.hiwhu.UI.R;

public class UpdateManager {

	private Context mContext;
	private static final String TAG = "updateManager";
	private static final String UPDATE_PAGE = "http://hiwhu.sinaapp.com/";
	// ��ʾ��
	private String updateMsg = "�����µ������Ŷ���׿����ذ�~";

	// ���صİ�װ��url
	// private String apkUrl =
	// <a class="downapk" href="http://hiwhustore-hiwhu.stor.sinaapp.com/hiwhu_2.1.6.apk"></a>
	private String apkUrl = "";

	private String lastestVersion = "";

	private void setLastestVersion(String lastestVersion) {
		this.lastestVersion = lastestVersion;
	}

	private Dialog noticeDialog;

	private Dialog downloadDialog;
	/* ���ذ���װ·�� */
	private static final String savePath = "/sdcard/hiwhu/";

	private static final String saveFileName = savePath
			+ "WHUinhand.apk";

	/* ��������֪ͨuiˢ�µ�handler��msg���� */
	private ProgressBar mProgress;

	private static final int DOWN_UPDATE = 1;

	private static final int DOWN_OVER = 2;

	private int progress;

	private Thread downLoadThread;

	private boolean interceptFlag = false;

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				break;
			case DOWN_OVER:

				installApk();
				break;
			default:
				break;
			}
		};
	};

	public UpdateManager(Context context) {
		this.mContext = context;
	}

	// �ⲿ�ӿ�����Activity����
	public boolean checkUpdateInfo() {
		// System.out.println("ininin");
		if (isNeedUpdate()){
			//showNoticeDialog();
			return true;
		}
		else
			return false;
	}

	private boolean isNeedUpdate() {
		// TODO Auto-generated method stub
		HttpHelper httpHelper = new HttpHelper();
		String temp = httpHelper.getContent(this.UPDATE_PAGE, "utf-8");
		if(temp == null)return false;
		Pattern p = Pattern.compile("<a href=\"([^>])+");
		Matcher m = p.matcher(temp);
		if (m.find()) {
			String link = m.group();
			this.setApkUrl(link.split("\"")[1]);
			this.setLastestVersion(this.apkUrl
					.substring(this.apkUrl.indexOf("_V")).replace("_V", "")
					.replace(".apk", ""));
		
			Log.d(TAG, "lastestVersion=======" + this.lastestVersion);
		}
		// ��ȡ��ǰ�����
		String currentVersion = "";
		try {
			PackageInfo info = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0);

			if (info.versionName.equals(lastestVersion))
				return false;
			else
				return true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}

	}

	public void setApkUrl(String url) {
		this.apkUrl = url;
	}

	public void showNoticeDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("����汾����");
		builder.setMessage(updateMsg);
		builder.setPositiveButton("����", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showDownloadDialog();
			}
		});
		builder.setNegativeButton("�Ժ���˵", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		noticeDialog = builder.create();
		noticeDialog.show();
	}

	private void showDownloadDialog() {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("����汾����");

		final LayoutInflater inflater = LayoutInflater.from(mContext);
		View v = inflater.inflate(R.layout.update_progress, null);
		mProgress = (ProgressBar) v.findViewById(R.id.update_progress);

		builder.setView(v);
		builder.setNegativeButton("ȡ��", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadDialog = builder.create();
		downloadDialog.show();

		downloadApk();
	}

	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				if (apkUrl.equals("")) {
					Log.d(TAG, "���ص�ַΪ��");
					return;
				}
				URL url = new URL(apkUrl);

				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				InputStream is = conn.getInputStream();

				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdir();
				}
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);

				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					progress = (int) (((float) count / length) * 100);
					// ���½���
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// �������֪ͨ��װ
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// ���ȡ����ֹͣ����.

				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	};

	/**
	 * ����apk
	 * 
	 * @param url
	 */

	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * ��װapk
	 * 
	 * @param url
	 */
	private void installApk() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + saveFileName),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
		downloadDialog.dismiss();
	}
}