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

    private boolean isInitAttach;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (isInitAttach) return;
                initDecoder();
                isInitAttach = true;
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }

    private void initDecoder() {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        try {
            mDecoder = BitmapRegionDecoder.newInstance(b, 0, b.length, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOptions = new BitmapFactory.Options();
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        mImageRect = new Rect(0, 0, getWidth(), getHeight());

        mOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(b, 0, b.length, mOptions);
        mImageHeight = mOptions.outHeight;
        mImageWidth = mOptions.outWidth;
        mOptions.inJustDecodeBounds = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Bitmap bitmap = decodeLongImage(mImageRect);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    protected void onScroll(float dx, float dy) {
        //super.onScroll(dx, dy);
        if (getWidth() >= mImageWidth) {
            //竖图
        }
        mImageRect.offset((int) dx, (int) dy);
        invalidate();
    }

    private Bitmap decodeLongImage(Rect rect) {
        return mDecoder.decodeRegion(rect, mOptions);
    }

}
