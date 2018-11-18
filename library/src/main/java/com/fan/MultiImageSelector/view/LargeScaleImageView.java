package com.fan.MultiImageSelector.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LargeScaleImageView extends ScaleImageView {
    private BitmapRegionDecoder mDecoder;
    private Rect mImageRect;
    private BitmapFactory.Options mOptions;
    private int mImageWidth;
    private int mImageHeight;

    public LargeScaleImageView(Context context) {
        super(context);
    }

    public LargeScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }


    public void setImagePath(String path) {
        initDecoder(path);
    }


    private void initDecoder(String path) {
        try {
            mDecoder = BitmapRegionDecoder.newInstance(path, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOptions = new BitmapFactory.Options();
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;


        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, mOptions);
        mImageHeight = mOptions.outHeight;
        mImageWidth = mOptions.outWidth;
        mOptions.inJustDecodeBounds = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mImageRect == null)
            mImageRect = new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight());
        if (getDrawable() == null) {
            setImageBitmap(mDecoder.decodeRegion(mImageRect, mOptions));
        }
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        canvas.drawBitmap(mDecoder.decodeRegion(mImageRect, mOptions), 0, 0, null);
//    }

    @Override
    protected void onScroll(float distanceX, float distanceY) {
        //super.onScroll();
//        Log.e("main", "range  " + mImageRect);
//
//        Log.e("main", "width  " + mImageWidth + "  height  " + mImageHeight);
        mImageRect.offset((int) distanceX, (int) distanceY);
        checkSelf();
        setImageBitmap(mDecoder.decodeRegion(mImageRect, mOptions));
       // invalidate();
    }

    private void checkSelf() {
        if (mImageRect.left < 0) {
            //左越界
            mImageRect.offset(-mImageRect.left, 0);
        }

        if (mImageRect.right > mImageWidth) {
            //右越界
            mImageRect.offset(mImageWidth - mImageRect.right, 0);
        }
        if (mImageRect.top < 0) {
            //上越界
            mImageRect.offset(0, -mImageRect.top);
        }
        if (mImageRect.bottom > mImageHeight) {
            //下越界
            mImageRect.offset(0, mImageHeight - mImageRect.bottom);
        }
    }
}
