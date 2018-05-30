package com.fan.library.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.fan.library.view.ScaleImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LongScaleImageView extends ScaleImageView {

    private Bitmap mBitmap;

    public LongScaleImageView(Context context) {
        super(context);
    }

    public LongScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void initAttach() {
        initLongImage();
        isInitAttach = true;
    }

    private void initLongImage() {
        mBitmap = getBitmap();
        initDecoder();
    }

    private int mLastBottom;
    private Rect mCurRect;

    @Override
    protected void handleScroll(float distanceX, float distanceY) {
        // super.handleScroll(distanceX, distanceY);
        int top = (int) (mLastBottom - getHeight() + distanceY);
        mCurRect.set(0, top, getRight(), top + getHeight());
        //setImageBitmap(decodeLongImage(mCurRect));
        mLastBottom = top + getHeight();
        invalidate();
    }

    @Override
    protected void onFiling(final float velocityX, final float velocityY) {
        super.onFiling(velocityX, velocityY);

//        ValueAnimator animator = new ValueAnimator();
//        animator.setFloatValues(0, 1);
//        animator.setDuration(1000);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                float fraction = animation.getAnimatedFraction();
//                matrix.postTranslate(velocityX * fraction, velocityY * fraction);
//                setImageMatrix(matrix);
//            }
//        });
//        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        Bitmap bitmap = decodeLongImage(mCurRect);
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    private BitmapRegionDecoder mDecoder;

    private void initDecoder() {
        Bitmap bitmap = getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        try {
            mDecoder = BitmapRegionDecoder.newInstance(b, 0, b.length, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCurRect = new Rect(0, 0, getWidth(), getHeight());
        mLastBottom = getHeight();
    }

    private Bitmap decodeLongImage(Rect rect) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return mDecoder.decodeRegion(rect, options);
    }

    private Bitmap getBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        return bitmap;
    }
}
