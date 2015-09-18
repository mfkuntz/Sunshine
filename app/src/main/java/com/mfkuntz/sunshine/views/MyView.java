package com.mfkuntz.sunshine.views;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by mkuntz on 9/18/15.
 */
public class MyView extends View {
    public MyView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec){
        int hSpecMode = MeasureSpec.getMode(hSpec);
        int hSpecSize = MeasureSpec.getSize(hSpec);

        int height = hSpecSize;

//        if (hSpecMode == MeasureSpec.EXACTLY){
//
//        }

        int wSpecSize = MeasureSpec.getSize(wSpec);

        int width = wSpecSize;

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
