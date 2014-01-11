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
 * ҳ��滭�࣬���ض��ַ�����ͼƬ�����Ű棬����View����ʾ �����˷�ҳ������
 * 
 * @author Maybe
 * 
 */
public class ContentDrawTool {
	// ҳ������margin������margin
	private float marginLR = 30;
	private float marginTop = 50;
	private float marginBottom = 150;
	public int picHeight = 200;
	public int picWidth = 300;
	// ��ǰX��Y����
	private float currX;
	private float currY;

	// ��Ļ�����߶�
	private int screenWidth;
	private int screenHeight;

	// ���廭�ʣ��������ʽ�Լ�������ڹ��캯���г�ʼ��
	private Paint normalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint picPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint datePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	// ��ǰʹ�û���
	private Paint currPaint = normalPaint;

	// ��ҳ��������list��һ��Ԫ��Ϊһҳ
	ArrayList<String> result = new ArrayList<String>();
	ImageLoader imageLoader;
	Context context;

	public ContentDrawTool(Context mContext) {
		this.context = mContext;

		// �õ���Ļ���
		screenWidth = getDeviceScreenWidth(mContext);
		screenHeight = getDeviceScreenHeight(mContext);
		
		marginLR = marginLR*screenHeight/800;
		marginTop = marginTop*screenHeight/800;
		marginBottom = marginBottom*screenHeight/800;
		picHeight = (int) picHeight*screenHeight/800;
		picWidth = (int) picWidth*screenHeight/800;
		
		currX = marginLR;
		currY = marginTop;

		// ��ʼ������
		// normalPaint.setColor(Color.WHITE); // ��ͨ����
		normalPaint.setTextSize(25*(screenHeight/800));

		// titlePaint.setColor(Color.WHITE); // ���⻭��
		titlePaint.setTextSize(30*(screenHeight/800));
		titlePaint.setFakeBoldText(true);

		// ͼƬ����
		picPaint.setStyle(Style.STROKE);

		// ���ڻ���
		datePaint.setTextSize(24*(screenHeight/800));
		datePaint.setColor(Color.rgb(0x33, 0x66, 0xCC));

		


		imageLoader = new ImageLoader();
	}

	/**
	 * ��ҳ���������ڽ����ı���ҳ��
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

		// ����spilt����ÿһ��Ԫ�ؽ��в���
		for (int i = 1; i < contentTemp.length; i++) {
			arrayTemp = contentTemp[i];
			type = arrayTemp.charAt(0);
			arrayTemp = arrayTemp.substring(1);

			// ���⵽ΪͼƬ
			if (type == 'p') {
				currY += picHeight;
				if (currY >= screenHeight - marginBottom) {
					currY = marginTop + picHeight;
					result.add(pageContent);
					pageContent = "";

				}
				pageContent += "@@" + type + arrayTemp;
			}
			// �粻ΪͼƬ����Ϊ����
			else {

				// �������ֵ���ʽ���嵱ǰ����
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
				// �������ֿ�ʼ
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

				// ������Y����Χ���򱣴浱ҳ������һҳ
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
	 * ��ͼ����
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
	 * ������Ļȫ��
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
		// ȡ drawable �ĳ���
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();

		// ȡ drawable ����ɫ��ʽ
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		// ������Ӧ bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// ������Ӧ bitmap �Ļ���
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// �� drawable ���ݻ���������
		drawable.draw(canvas);
		return bitmap;
	}

}
