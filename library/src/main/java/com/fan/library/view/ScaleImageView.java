package com.fan.library.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Scroller;

public class ScaleImageView extends ImageView {
    protected Matrix matrix;
    private GestureDetector mGestureDetector;
    private float mMaxScale = 4;
    protected float mCurScale = 1;
    private float mCenterScale = 2;
    private float mInitScale = 1.0f;
    private ScaleGestureDetector mScaleGestureDetector;
    private Scroller mScroller;

    public ScaleImageView(Context context) {
        this(context, null);
    }

    public ScaleImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        if (getScaleType() != ScaleType.MATRIX) {
            setScaleType(ScaleType.MATRIX);
        }
        mGestureDetector = new GestureDetector(context, new TapCallback());
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleCallback());
        matrix = getImageMatrix();
        mScroller = new Scroller(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCurScale < mInitScale) {
                resetScale();
            }
            if (isScale) isScale = false;
            checkBorder();
        }
        return true;
    }

    private OnGestureListener mGestureListener;

    public interface OnGestureListener {
        void onSingleTapUp();
    }

    public void setOnGestureListener(OnGestureListener listener) {
        mGestureListener = listener;
    }

    private boolean isDoubleTap;
    private boolean isScale;

    private class ScaleCallback extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            if (mCurScale * factor < 0.5f) {
                return true;
            }
            isNeedCheckBorder = true;
            isScale = true;
            mCurScale = mCurScale * factor;
            matrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
            setImageMatrix(matrix);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if (mCurScale > mMaxScale) {
                matrix.postScale(mMaxScale / mCurScale, mMaxScale / mCurScale, getWidth() / 2, getHeight() / 2);
                setImageMatrix(matrix);
                mCurScale = mMaxScale;
            }
        }
    }


    private void resetScale() {
        matrix.postScale(mInitScale / mCurScale, mInitScale / mCurScale, getWidth() / 2, getHeight() / 2);
        setImageMatrix(matrix);
        mCurScale = mInitScale;
    }

    private boolean isNeedCheckBorder;

    protected void checkBorder() {
        if (!isNeedCheckBorder) return;
        RectF rectF = getMatrixRectF();

//        Log.e("main", rectF.toString());
//        Log.e("main", "width " + rectF.width());
        float dx = 0;
        float dy = 0;
        float width = getWidth();
        float height = getHeight();
        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                dx = -rectF.left;
            }
            if (rectF.right < width) {
                dx = width - rectF.right;
            }
        }
        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                dy = -rectF.top;
            }
            if (rectF.bottom < height) {
                dy = height - rectF.bottom;
            }
        }

        if (rectF.width() < width) {
            dx = width / 2f + rectF.width() / 2f - rectF.right;
        }
        if (rectF.height() < height) {
            dy = height / 2f + rectF.height() / 2f - rectF.bottom;
        }

        matrix.postTranslate(dx, dy);
        setImageMatrix(matrix);
        isNeedCheckBorder = false;
    }

    private RectF getMatrixRectF() {
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        matrix.mapRect(rectF);
        return rectF;
    }


    private class TapCallback extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isDoubleTap = true;
            isNeedCheckBorder = true;
            if (mCurScale < mCenterScale) {
                float factor = mCenterScale / mCurScale;
                matrix.postScale(factor, factor, getWidth() / 2, getHeight() / 2);
                setImageMatrix(matrix);
                mCurScale = mCenterScale;
            } else {
                resetScale();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            isDoubleTap = false;
            handler.sendEmptyMessageDelayed(1, 1000);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScale) return true;
            ScaleImageView.this.onScroll(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    private void fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {


    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {

        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                if (!isDoubleTap) {
                    if (mGestureListener != null) {
                        mGestureListener.onSingleTapUp();
                    }
                }
            }
            return false;
        }
    });
    private boolean isInitAttach;

    protected void onScroll(float distanceX, float distanceY) {
        isNeedCheckBorder = true;
        if (mCurScale == mInitScale) {
            distanceY = 0;
        }
        matrix.postTranslate(-distanceX, -distanceY);
        setImageMatrix(matrix);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (isInitAttach) return;
                initAttach();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(listener);
    }


    private void initAttach() {
        Drawable d = getDrawable();
        if (d == null) return;
        int width = getWidth();
        int height = getHeight();
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        Log.e("main", "dw  " + dw + "dh  " + dh);
        if (height > dh && width > dw) {
            float scaleW = (float) width / (float) dw;
            float scaleH = (float) height / (float) dh;
            mInitScale = Math.min(scaleH, scaleW);
            mCurScale = mInitScale;
            mMaxScale = mInitScale * 4;
            mCenterScale = mInitScale * 2;
            matrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            matrix.postScale(mInitScale, mInitScale, getWidth() / 2, getHeight() / 2);
            setImageMatrix(matrix);
        }
        isInitAttach = true;
    }
}
