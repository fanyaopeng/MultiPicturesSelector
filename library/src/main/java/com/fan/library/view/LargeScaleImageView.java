package com.fan.library.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Bitmap bitmap = decodeLongImage(mImageRect);
        canvas.drawBitmap(bitmap, 0, 0, null);
        if (getDrawable() == null) setImageBitmap(bitmap);
    }

    @Override
    protected void onScroll(float dx, float dy) {
        //super.onScroll(dx, dy);
        isNeedCheckBorder = true;
        if (getWidth() >= mImageWidth) {
            //竖图
        }
        if (mImageRect.left + dx <= 0) {
            mImageRect.offsetTo(0, mImageRect.top);
            super.onScroll(dx, dy);
            return;
        }
        mImageRect.offset((int) dx, (int) dy);
        invalidate();
    }

    private boolean isNeedCheckBorder;

    private void checkBorder() {
        Log.e("main", "rect  " + mImageRect.toString());
        if (!isNeedCheckBorder) return;
        isNeedCheckBorder = false;
        int dx = 0;
        int dy = 0;

        if (mImageRect.left < 0) {
            dx = -mImageRect.left;
        }
        if (mImageRect.top < 0) {
            dy = -mImageRect.top;
        }
        mImageRect.offset(dx, dy);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            //checkBorder();
        }
        return super.onTouchEvent(event);
    }

    private Bitmap decodeLongImage(Rect rect) {
        return mDecoder.decodeRegion(rect, mOptions);
    }

}
