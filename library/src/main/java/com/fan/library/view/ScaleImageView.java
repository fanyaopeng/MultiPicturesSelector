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

public class ScaleImageView extends ImageView {
    private Matrix matrix;
    private GestureDetector mGestureDetector;
    private float mMaxScale = 4;
    private float mCurScale = 1;
    private float mCenterScale = 2;
    private float mInitScale = 1.0f;
    private ScaleGestureDetector mScaleGestureDetector;

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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCurScale < mInitScale) {
                resetScale();
            }
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

    private class ScaleCallback extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            if (mCurScale * factor < 0.5f) {
                return true;
            }
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

    private float mCenter[] = new float[2];

    private void resetScale() {
        //matrix.reset();
        //matrix.postTranslate(mCenter[0], mCenter[1]);
        matrix.setScale(mInitScale, mInitScale);
        setImageMatrix(matrix);
        mCurScale = mInitScale;
    }

    private boolean isNeedCheckBorder;

    private void checkBorder() {
        if (!isNeedCheckBorder) return;
        RectF rectF = new RectF();
        matrix.mapRect(rectF);

        Log.e("main", rectF.toString());
//        if (rectF.left < 0) {
//            if (rectF.top < 0) {
//                matrix.postTranslate(-rectF.left, -rectF.top);
//            }
//        }
        isNeedCheckBorder = false;
    }

    private class TapCallback extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            isDoubleTap = true;
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
            isNeedCheckBorder = true;
            if (mCurScale == mInitScale) {
                distanceY = 0;
            }
            matrix.postTranslate(-distanceX, -distanceY);
            setImageMatrix(matrix);
            return true;
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
    private boolean isInit;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                if (isInit) return;
                Drawable d = getDrawable();
                if (d == null) return;
                int width = getWidth();
                int height = getHeight();
                int dw = d.getIntrinsicWidth();
                int dh = d.getIntrinsicHeight();
                float scaleW = (float) width / (float) dw;
                float scaleH = (float) height / (float) dh;
                mInitScale = Math.min(scaleH, scaleW);
                mCurScale = mInitScale;
                mMaxScale = mInitScale * 4;
                mCenterScale = mInitScale * 2;
                matrix.postTranslate((width - dw) / 2, (height - dh) / 2);
                mCenter[0] = (width - dw) / 2;
                mCenter[1] = (height - dh) / 2;
                matrix.postScale(mInitScale, mInitScale, getWidth() / 2, getHeight() / 2);
                setImageMatrix(matrix);
                isInit = true;
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(listener);

    }
}
