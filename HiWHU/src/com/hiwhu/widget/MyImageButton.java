package com.hiwhu.widget;

import com.hiwhu.UI.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyImageButton extends RelativeLayout {

	private ImageView my_icon;
	private TextView my_text;
	private boolean longPressable;


	public MyImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		longPressable = false;
		LayoutInflater.from(context).inflate(R.layout.my_imagebutton, this,
				true);
		my_text = (TextView) this.findViewById(R.id.my_text);
		my_icon = (ImageView) this.findViewById(R.id.my_icon);
		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.MyImageButton);
		int src = ta.getResourceId(R.styleable.MyImageButton_src,
				R.drawable.btn_default);
		int text = ta.getResourceId(R.styleable.MyImageButton_text,
				R.string.btn_default);
		my_icon.setImageResource(src);
		my_text.setText(text);
		setTag(text);
		ta.recycle();

	}
	
	public void setText(int id) {
		my_text.setText(id);
		setTag(id);
	}

	public void setImage(int id) {
		my_icon.setImageResource(id);
	}

	public void setLongPressable(boolean b) {
		this.longPressable = b;
	}
	
	public boolean isLongPressable (){
		return this.longPressable;
	}

}