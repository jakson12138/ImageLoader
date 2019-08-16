package com.example.imageloader.MyView;

import android.content.Context;
import android.util.AttributeSet;

import android.support.v7.widget.AppCompatImageView;

import java.util.jar.Attributes;


//通过重现onMeasure来使view测量时宽高一致
public class SquareImageView extends AppCompatImageView {

    public SquareImageView(Context context){
        super(context,null,0);
    }

    public SquareImageView(Context context, AttributeSet attrs){
        super(context,attrs,0);
    }

    public SquareImageView(Context context,AttributeSet attrs,int defStyleAttr){
        super(context,attrs,defStyleAttr);
    }

    @Override
    public void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,widthMeasureSpec);
    }

}
