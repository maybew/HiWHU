package com.hiwhu.widget;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hiwhu.UI.LessonDetailActivity;

public class LessonContentLayout extends View {

	// 1~14节课
	private int singleWidth, singleHeight, textSize;
	private double scale = 0;
	private Paint paint;
	private ArrayList<HashMap<String, Object>> list = null, position = null;

	public void setList(ArrayList<HashMap<String, Object>> list) {
		this.list = list;
		position = new ArrayList<HashMap<String, Object>>();
	}

	public LessonContentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

/*	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		
	}
	*/
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(VIEW_LOG_TAG, "width=="+MeasureSpec.getSize(widthMeasureSpec)+"height===="+MeasureSpec.getSize(heightMeasureSpec));//854  1108
		//scale  = Math.min(w/854.0, h/1108);
		this.setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
				MeasureSpec.getSize(widthMeasureSpec)*77 * 14/854 + 30);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		this.singleHeight = (int) ((this.getHeight() - 30) / 14);
		this.singleWidth = (int) (this.getWidth() / 7.5);
		this.textSize = (int) (this.singleHeight / 3.5);
		paint = new Paint();
		paint.setAntiAlias(true);// 设置画笔的锯齿效果
		paint.setTextSize(textSize);
		paint.setStrokeWidth(0);
		paint.setColor(0xffE6E3E3);
		int currentHeight = this.singleHeight / 2 + this.textSize / 2;
		int currentLineHeight = this.singleHeight;
		int currentLineWidth = this.singleWidth / 2;
		//绘制表格线
		for (int i = 0; i < 14; i++) {

			paint.setColor(Color.BLACK);
			canvas.drawText(i + 1 + "", this.singleWidth / 4 - this.textSize
					/ 2, currentHeight, paint);
			paint.setColor(0xffd5d5d5);
			canvas.drawLine(this.singleWidth / 2, currentLineHeight, 1000,
					currentLineHeight, paint);
			currentHeight += this.singleHeight;
			currentLineHeight += this.singleHeight;
			if (i % 2 == 0) {
				canvas.drawLine(currentLineWidth, 0, currentLineWidth,
						this.singleHeight * 14, paint);
				currentLineWidth += this.singleWidth;
			}
		}

		// 画圆角矩形
		paint.setStyle(Paint.Style.FILL);// 充满
		paint.setColor(0xffff6600);
		paint.setAntiAlias(true);// 设置画笔的锯齿效果
		RectF rect;
		HashMap<String, Object> map;
		String temp;
		String[] array, array1, array2;// 星期 时间 地点
		int currentWidth = this.singleWidth / 2, lessonLong = 0;
		currentHeight = 0;
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {// 取出每一门课
				currentWidth = this.singleWidth / 2;
				lessonLong = 0;
				currentHeight = 0;
				map = list.get(i);
				temp = (String) map.get("lessonWeek");
				array = temp.split("@");
				temp = (String) map.get("lessonTime");
				array1 = temp.split("@");
				temp = (String) map.get("lessonLocation");
				array2 = temp.split("@");
				temp = (String) map.get("lessonName");

				for (int j = 0; j < array.length; j++) {// 绘制每门课的色块
					currentWidth = this.singleWidth / 2;
					lessonLong = 0;
					currentHeight = 0;
					currentWidth += (Integer.parseInt(array[j]) - 1)
							* this.singleWidth;

					currentHeight += (Integer.parseInt(array1[j].split("-")[0]) - 1)
							* this.singleHeight;
					lessonLong = Integer.parseInt(array1[j].split("-")[1])
							- Integer.parseInt(array1[j].split("-")[0]) + 1;
					rect = new RectF(currentWidth + 5, currentHeight + 4,
							currentWidth + this.singleWidth - 5, currentHeight
									+ lessonLong * this.singleHeight - 5);// 设置个新的长方形
					paint.setColor(0xffff6600);
					canvas.drawRoundRect(rect, 7, 7, paint);// 第二个参数是x半径，第三个参数是y半径
					
					map = new HashMap<String, Object>();
					map.put("lessonName", temp);
					map.put("left", currentWidth + 5);
					map.put("top", currentHeight + 4);
					map.put("right", currentWidth + this.singleWidth - 5);
					map.put("bottom", currentHeight + lessonLong * this.singleHeight - 5);
					map.put("week", array[j]);
					position.add(map);
					
					paint.setColor(Color.WHITE);
					float[] widths = new float[temp.length()];
					paint.getTextWidths(temp, widths);
					float tempTextWidth = 0;
					for (int k = 0; k < widths.length; k++) {
						canvas.drawText(temp.substring(k, k + 1),
								currentWidth + 7, currentHeight + textSize + 5,
								paint);
						tempTextWidth += widths[k];
						currentWidth += widths[k];
						if (k < (widths.length - 1)
								&& (tempTextWidth + widths[k + 1]) > (this.singleWidth - 14)) {
							tempTextWidth = 0;
							currentWidth = (Integer.parseInt(array[j]) - 1)
									* this.singleWidth + this.singleWidth / 2;
							currentHeight += textSize;
						}
					}

					widths = new float[array2[j].length()];
					paint.getTextWidths(array2[j], widths);
					currentWidth = (Integer.parseInt(array[j]) - 1)
							* this.singleWidth + this.singleWidth / 2;
					tempTextWidth = 0;

					int offset = 0;
					for (int k = 0; k < widths.length; k++) {
						offset += widths[k];
					}
					offset = (int) Math.ceil((double) offset
							/ (double) this.singleWidth);
					currentHeight = (Integer.parseInt(array1[j].split("-")[1]))
							* this.singleHeight - this.textSize * offset;

					for (int k = 0; k < widths.length; k++) {
						canvas.drawText(array2[j].substring(k, k + 1).trim(),
								currentWidth + 7, currentHeight, paint);
						tempTextWidth += widths[k];
						currentWidth += widths[k];
						if (k < (widths.length - 1)
								&& (tempTextWidth + widths[k + 1]) > (this.singleWidth - 7)) {
							tempTextWidth = 0;
							currentWidth = (Integer.parseInt(array[j]) - 1)
									* this.singleWidth + this.singleWidth / 2;
							currentHeight += textSize;
						}
					}

				}
			}
		}

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		float x, y;
		HashMap<String, Object> m;
		
		switch(action){
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			x = event.getX();
			y = event.getY();
			if(position == null)
				return true;
			else{
				for(int i = 0; i < position.size(); i++){
					m = position.get(i);
					if(x > (Integer)m.get("left") && x < (Integer)m.get("right") &&
							y > (Integer)m.get("top") && y < (Integer)m.get("bottom")){
						//new DetailDialog(this.getContext(), 2, (String)m.get("lessonName"), Integer.parseInt((String)m.get("week")), null, null).show();
						this.getContext().startActivity(new Intent(this.getContext(), LessonDetailActivity.class)
						.putExtra("choice", 1)
						.putExtra("lessonName", (String)m.get("lessonName"))
						.putExtra("lessonWeek", (String)m.get("week")));
						break;
					}
				}
			}
			break;
		}
		return true;
	}
	
}
