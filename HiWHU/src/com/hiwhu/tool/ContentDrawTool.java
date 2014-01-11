package com.hiwhu.tool;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

/**
 * 页面绘画类，将特定字符串与图片链接排版，并在View上显示 集成了分页方法。
 * 
 * @author Maybe
 * 
 */
public class ContentDrawTool {
	// 页面左右margin与上下margin
	private float marginLR = 30;
	private float marginTop = 50;
	private float marginBottom = 150;
	public int picHeight = 200;
	public int picWidth = 300;
	// 当前X、Y坐标
	private float currX;
	private float currY;

	// 屏幕宽度与高度
	private int screenWidth;
	private int screenHeight;

	// 定义画笔，如需加样式自己添加且在构造函数中初始化
	private Paint normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint picPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	// 当前使用画笔
	private Paint currPaint = normalPaint;

	// 分页输出结果，list中一个元素为一页
	ArrayList<String> result = new ArrayList<String>();
	ImageLoader imageLoader;
	Context context;

	public ContentDrawTool(Context mContext) {
		this.context = mContext;

		// 得到屏幕宽高
		screenWidth = getDeviceScreenWidth(mContext);
		screenHeight = getDeviceScreenHeight(mContext);
		
		marginLR = marginLR*screenHeight/800;
		marginTop = marginTop*screenHeight/800;
		marginBottom = marginBottom*screenHeight/800;
		picHeight = (int) picHeight*screenHeight/800;
		picWidth = (int) picWidth*screenHeight/800;
		
		currX = marginLR;
		currY = marginTop;

		// 初始化画笔
		// normalPaint.setColor(Color.WHITE); // 普通画笔
		normalPaint.setTextSize(25*(screenHeight/800));

		// titlePaint.setColor(Color.WHITE); // 标题画笔
		titlePaint.setTextSize(30*(screenHeight/800));
		titlePaint.setFakeBoldText(true);

		// 图片画笔
		picPaint.setStyle(Style.STROKE);

		// 日期画笔
		datePaint.setTextSize(24*(screenHeight/800));
		datePaint.setColor(Color.rgb(0x33, 0x66, 0xCC));

		


		imageLoader = new ImageLoader();
	}

	/**
	 * 分页方法，用于将长文本分页。
	 * 
	 * @param content
	 * @return
	 */
	public ArrayList<String> dividePage(String content) {

		String pageContent = "";
		String[] contentTemp;
		String arrayTemp;
		int index = 0;
		char type;

		contentTemp = content.split("@@");

		// 对于spilt出的每一个元素进行测量
		for (int i = 1; i < contentTemp.length; i++) {
			arrayTemp = contentTemp[i];
			type = arrayTemp.charAt(0);
			arrayTemp = arrayTemp.substring(1);

			// 如检测到为图片
			if (type == 'p') {
				currY += picHeight;
				if (currY >= screenHeight - marginBottom) {
					currY = marginTop + picHeight;
					result.add(pageContent);
					pageContent = "";

				}
				pageContent += "@@" + type + arrayTemp;
			}
			// 如不为图片，即为文字
			else {

				// 根据文字的样式定义当前画笔
				switch (type) {
				case '0':
					currPaint = normalPaint;
					break;
				case '1':
					currPaint = titlePaint;
					break;
				case '2':
					currPaint = datePaint;
					break;
				}
				// 测量文字开始
				while ((index = currPaint.breakText(arrayTemp, true,
						screenWidth - (2 * marginLR), null)) < arrayTemp
						.length()) {

					pageContent += "@@" + type + arrayTemp.substring(0, index);
					arrayTemp = arrayTemp.substring(index);
					currY += currPaint.getTextSize() + 10;
					if (currY >= screenHeight - marginBottom) {
						currY = marginTop;
						result.add(pageContent);
						pageContent = "";
					}

				}
				pageContent += "@@" + type + arrayTemp;
				currY += currPaint.getTextSize() + 10;

				// 若超出Y方向范围，则保存当页进入下一页
				if (currY >= screenHeight - marginBottom) {
					currY = marginTop;
					result.add(pageContent);
					pageContent = "";
				}
			}
		}
		result.add(pageContent);
		return result;
	}

	/**
	 * 绘图方法
	 * 
	 * @param content
	 * @param canvas
	 */
	public void drawContent(String content, Canvas canvas) {
		String[] drawTemp = content.split("@@");
		String temp;

		for (int i = 1; i < drawTemp.length; i++) {
			temp = drawTemp[i];
			if (temp.charAt(0) == 'p') {
				// Drawable tempDrawable;
				Bitmap tempBitmap;

				tempBitmap = imageLoader.loadDrawable(temp.substring(1), null);
				if (tempBitmap != null) {
					tempBitmap = Bitmap.createScaledBitmap(tempBitmap,picWidth,picHeight, false);
					canvas.drawBitmap(tempBitmap, (screenWidth - picWidth) / 2,
							currY - normalPaint.getTextSize(), null);
					currY += picHeight;
				} else {
					canvas.drawRect((screenWidth - picWidth) / 2, currY
							- normalPaint.getTextSize(),
							(screenWidth + picWidth) / 2,
							currY - normalPaint.getTextSize() + picHeight,
							picPaint);
					currY += picHeight;
				}

			} else {
				switch (temp.charAt(0)) {
				case '0':
					currPaint = normalPaint;
					break;
				case '1':
					currPaint = titlePaint;
					break;
				case '2':
					currPaint = datePaint;
					break;
				}
				canvas.drawText(temp.substring(1), currX, currY, currPaint);
				currY += currPaint.getTextSize() + 10;

			}
		}

	}

	public static int getDeviceScreenWidth(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.widthPixels;
	}

	public static int getDeviceScreenHeight(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		return dm.heightPixels;
	}

	/**
	 * 设置屏幕全屏
	 * 
	 * @param context
	 */
	public static void setFullScreen(Activity context) {
		context.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		context.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// 建立对应 bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

}
