package com.hiwhu.widget;

import com.hiwhu.UI.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class LessonTitleLayout extends View{

	private int width, height, singleWidth, textSize;
	private Paint paint;
	
	public LessonTitleLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		paint = new Paint();
		paint.setAntiAlias(true);// 设置画笔的锯齿效果  
		paint.setStrokeWidth(0);
		this.width = this.getWidth();
		this.height = this.getHeight();
		this.singleWidth = (int) (this.width / 7.5);
		this.textSize = (int) (1*this.height/2.5);
		//底部线条
		paint.setColor(0xffd5d5d5);
		canvas.drawLine(0, this.height-3, this.width , this.height-3, paint);
		//星期间隔
		String[] date = this.getResources().getStringArray(R.array.weekday_array);
		int currentWidth = 0;
		for(int i =  0; i < date.length; i++){
			if(i == 0)
				currentWidth += singleWidth/2;
			else
				currentWidth += singleWidth;
			//间隔短线
			paint.setColor(0xffd5d5d5);
			canvas.drawLine(currentWidth, this.height-3, currentWidth, this.height/2, paint);
			//星期文字
			paint.setColor(Color.BLACK);
			paint.setTextSize(textSize);
			canvas.drawText(date[i], currentWidth+(singleWidth-2*textSize)/2, (this.height+textSize)/2-3, paint);
		}
	}
	
}
