package com.hiwhu.tool;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;

public class ImageLoader {
	// 为了加快速度，在内存中开启缓存（主要应用于重复图片较多时，或者同一个图片要多次被访问，比如在ListView时来回滚动）
	// public Map<String, SoftReference<bitmap>> imageCache = new
	// HashMap<String, SoftReference<bitmap>>();
	String TAG = "TAG";
	final public static String imgCachePath = "/HIWHU/imageCache";
	final public static int MB = 1024 * 1024;
	final public static int IMAGECACHE_MAX_SIZE = 20;
	final public static int FREE_SD_SPACE_NEEDED_TO_CACHE = 5;

	public static Map<String, Bitmap> imageCache = new LinkedHashMap<String, Bitmap>() {
		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Bitmap> eldest) {
			// TODO Auto-generated method stub
			if (size() > IMAGECACHE_MAX_SIZE) {
				return true;
			} else
				return false;
		}
	};
	public static ArrayList<String> sdImageCache = new ArrayList<String>();
	public static ArrayList<String> loadingCache = new ArrayList<String>();

	/**
	 * 
	 * @param imageUrl
	 *            图像url地址
	 * @param callback
	 *            回调接口
	 * @return 返回内存中缓存的图像，第一次加载返回null
	 */
	public Bitmap loadDrawable(final String imageUrl,
			final ImageCallback callback) {
		// 如果缓存过就从缓存中取出数据
		Log.v("SIZE", String.valueOf(imageCache.size()));
		if (imageCache.containsKey(imageUrl)) {
			Bitmap bm = imageCache.get(imageUrl);
			imageCache.remove(imageUrl);
			imageCache.put(imageUrl, bm);
			return bm;
		} else if (sdImageCache.contains(imageUrl)) {
			Bitmap bm = getBmpFromSd(imageUrl);
			if (bm != null)
				imageCache.put(imageUrl, bm);
			return bm;
		} else if (loadingCache.contains(imageUrl))
			return null;

		// 缓存中没有图像，则从网络上取出数据，并将取出的数据缓存到内存中
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (callback != null)
					callback.imageLoaded((Bitmap) msg.obj, imageUrl);
			}
		};
		new Thread() {
			public void run() {
				Log.e("imageDown", imageUrl);
				loadingCache.add(imageUrl);
				Bitmap bitmap = loadImageFromUrl(imageUrl);
				loadingCache.remove(imageUrl);
				if (bitmap != null) {
					if (checkSd())
						sdImageCache.add(imageUrl);
					imageCache.put(imageUrl, bitmap);
				}

				Message message = handler.obtainMessage(0, bitmap);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}

	// 从网络上取数据方法
	protected Bitmap loadImageFromUrl(String url) {
		/*
		 * try {
		 * 
		 * Bitmap bitmap = null; HttpURLConnection conn = (HttpURLConnection)
		 * new URL(url) .openConnection(); conn.setDoInput(true);
		 * conn.connect();
		 * 
		 * InputStream is = conn.getInputStream();
		 * 
		 * int length = (int) conn.getContentLength(); if (length != -1) {
		 * byte[] imgData = new byte[length]; byte[] temp = new byte[512]; int
		 * readLen = 0; int destPos = 0; while ((readLen = is.read(temp)) > 0) {
		 * System.arraycopy(temp, 0, imgData, destPos, readLen); destPos +=
		 * readLen; } bitmap = BitmapFactory.decodeByteArray(imgData, 0,
		 * imgData.length); } return bitmap;
		 * 
		 * } catch (Exception e) { throw new RuntimeException(e); }
		 */

		final AndroidHttpClient client = AndroidHttpClient
				.newInstance("Android");
		final HttpGet getRequest = new HttpGet(url);
		try {
			HttpResponse response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				Log.w(TAG, "从" + url + "中下载图片时出错!,错误码:" + statusCode);
				return null;
			}
			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				OutputStream outputStream = null;
				try {
					inputStream = entity.getContent();
					int length = (int) entity.getContentLength();
					if (length != -1) {
						byte[] imgData = new byte[length];
						byte[] temp = new byte[512];
						int readLen = 0;
						int destPos = 0;
						while ((readLen = inputStream.read(temp)) > 0) {
							System.arraycopy(temp, 0, imgData, destPos, readLen);
							destPos += readLen;
						}
						Bitmap bitmap = BitmapFactory.decodeByteArray(imgData,
								0, imgData.length);
						saveBmpToSd(bitmap, url);
						return bitmap;
					}

				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					if (outputStream != null) {
						outputStream.close();
					}
					entity.consumeContent();
				}
			}
		} catch (IOException e) {
			getRequest.abort();
			Log.w(TAG, "I/O errorwhile retrieving bitmap from " + url, e);
		} catch (IllegalStateException e) {
			getRequest.abort();
			Log.w(TAG, "Incorrect URL:" + url);
		} catch (Exception e) {
			getRequest.abort();
			Log.w(TAG, "Error whileretrieving bitmap from " + url, e);
		} finally {
			if (client != null) {
				client.close();
			}
		}
		return null;
	}

	private void saveBmpToSd(Bitmap bm, String url) {
		if (!checkSd())
			return;
		if (bm == null) {
			Log.w(TAG, " trying to savenull bitmap");
			return;
		}
		// 判断sdcard上的空间
		if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
			Log.w(TAG, "Low free space onsd, do not cache");
			return;
		}

		String filename = convertUrlToFileName(url);
		String dir = getDirectory();
		File file = new File(dir + imgCachePath, filename);
		try {
			file.createNewFile();
			OutputStream outStream = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();
			outStream.close();
			Log.i(TAG, "Image saved tosd");
		} catch (FileNotFoundException e) {
			Log.w(TAG, "FileNotFoundException");
		} catch (IOException e) {
			Log.w(TAG, "IOException");
		}
	}

	private Bitmap getBmpFromSd(String url) {
		if (!checkSd())
			return null;
		Bitmap bm = BitmapFactory.decodeFile(getDirectory() + imgCachePath
				+ "/" + convertUrlToFileName(url));
		return bm;
	}

	private static String convertUrlToFileName(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	public static String convertFileNameToUrl(String file) {
		try {
			return URLDecoder.decode(file, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	public static String getDirectory() {
		File sdDir = null;
		if (checkSd()) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			return sdDir.toString();
		}
		return null;
	}

	public static boolean checkSd() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static void checkSDDir() {
		String path = getDirectory();
		if (path == null)
			return;
		File sdDir = new File(getDirectory() + imgCachePath);
		if (!sdDir.exists()) {
			sdDir.mkdirs();
		}
		String[] imgCachedKey = sdDir.list();
		for (int i = 0; i < imgCachedKey.length; i++) {
			String temp = convertFileNameToUrl(imgCachedKey[i]);
			if (temp != null)
				sdImageCache.add(temp);
		}
	}

	private int freeSpaceOnSd() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
				.getPath());
		double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat
				.getBlockSize()) / MB;
		return (int) sdFreeMB;
	}

	public static boolean clearSdImageCache() {
		String path = getDirectory();
		if (path == null)
			return false;
		File sdDir = new File(getDirectory() + imgCachePath);
		if (!sdDir.exists()) {
			return false;
		}
		File[] fileList = sdDir.listFiles();
		for (int i = 0; i < fileList.length; i++) {
			fileList[i].delete();
		}
		sdImageCache.clear();
		return true;
	}

	// 对外界开放的回调接口
	public interface ImageCallback {
		// 注意 此方法是用来设置目标对象的图像资源
		public void imageLoaded(Bitmap imagebitmap, String imageUrl);
	}
}