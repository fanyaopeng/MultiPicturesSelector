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
import android.widget.Scroller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LargeScaleImageView extends ScaleImageView {
    private BitmapRegionDecoder mDecoder;
    private Rect mImageRect;
    private BitmapFactory.Options mOptions;
    private int mImageWidth;
    private int mImageHeight;
    private Scroller mScroller;
    private int mScrollX;
    private int mScrollY;

    public LargeScaleImageView(Context context) {
        this(context, null);
    }

    public LargeScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    @Override
    protected void fling(float velocityX, float velocityY) {
        //super.fling(velocityX, velocityY);
        mLastX = mScrollX;
        mLastY = mScrollY;
        mScroller.fling(mScrollX, mScrollY, (int) velocityX, (int) velocityY, Integer.MIN_VALUE, mImageWidth - getWidth(), Integer.MIN_VALUE,
                mImageHeight - getHeight());
    }

    private int mLastX;
    private int mLastY;

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int curX = mScroller.getCurrX();
            int curY = mScroller.getCurrY();
            scrollBy(mLastX - curX, mLastY - curY);
            mLastX = curX;
            mLastY = curY;
        }
    }

    @Override
    public void scrollBy(int x, int y) {
        //super.scrollBy(x, y);
        mImageRect.offset(x, y);
        Log.e("main", "dx  " + x + "  dy  " + y);
        checkSelf();
        setImageBitmap(mDecoder.decodeRegion(mImageRect, mOptions));
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
        if (getDrawable() == null) {
            setImageBitmap(mDecoder.decodeRegion(mImageRect, mOptions));
        } else {
            super.onDraw(canvas);
        }
    }

    @Override
    protected void onScroll(float distanceX, float distanceY) {
        int dx = (int) distanceX;
        int dy = (int) distanceY;
        mScrollX += dx;
        mScrollY += dy;
        mImageRect.offset(dx, dy);
        checkSelf();
        setImageBitmap(mDecoder.decodeRegion(mImageRect, mOptions));
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
