package com.fan.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.fan.library.utils.Utils;

public class ClipShapeView extends View {
    private Paint mPaint;
    private float mLeft, mTop, mRight, mBottom;
    private GestureDetector mDetector;
    private RectF mProfile;

    private int mCornerWidth;
    private float mCornerSize;

    private final int LEFT_TOP = 0;
    private final int RIGHT_TOP = 1;
    private final int RIGHT_BOTTOM = 2;
    private final int LEFT_BOTTOM = 3;
    private final int NO_SCROLL = -1;
    private int mCurScrollRange = NO_SCROLL;
    int mPadding;

    public ClipShapeView(Context context) {
        this(context, null);
    }

    public ClipShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mDetector = new GestureDetector(context, new ScrollCallback());

        mCornerSize = Utils.dp2px(getContext(), 40);
        mCornerWidth = Utils.dp2px(getContext(), 3.0f);
        mPadding = mCornerWidth / 2;
    }

    private class ScrollCallback extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.e("main", "dx  " + distanceX + "  dy  " + distanceY);
            //处理缩小越界
            if (mLeft + mCornerSize > mRight - mCornerSize) {
                mLeft = mRight - mCornerSize - mCornerSize;
            }
            if (mTop + mCornerSize > mBottom - mCornerSize) {
                mTop = mBottom - mCornerSize - mCornerSize;
            }


//            if (mLeft < 0) {
//                mLeft = mPadding;
//            }
//            if (mRight > getWidth() - mPadding) {
//                mRight = getWidth() - mPadding;
//            }
//            if (mTop < 0) {
//                mTop = mPadding;
//            }
//            if (mBottom > getHeight() - mPadding) {
//                mBottom = getHeight() - mPadding;
//            }
            switch (mCurScrollRange) {
                case LEFT_TOP:
                    mLeft -= distanceX;
                    mTop -= distanceY;
                    break;
                case LEFT_BOTTOM:
                    mLeft -= distanceX;
                    mBottom -= distanceY;
                    break;
                case RIGHT_TOP:
                    mRight -= distanceX;
                    mTop -= distanceY;
                    break;
                case RIGHT_BOTTOM:
                    mRight -= distanceX;
                    mBottom -= distanceY;
                    break;
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return shouldMove(e);
        }
    }

    private boolean shouldMove(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        if (x < mProfile.left + mCornerSize && y < mProfile.top + mCornerSize) {
            mCurScrollRange = LEFT_TOP;
            return true;
        }
        if (x > mProfile.right - mCornerSize && y < mProfile.top + mCornerSize) {
            mCurScrollRange = RIGHT_TOP;
            return true;
        }
        if (x < mProfile.left + mCornerSize && y > mProfile.bottom - mCornerSize) {
            mCurScrollRange = LEFT_BOTTOM;
            return true;
        }
        if (x > mProfile.right - mCornerSize && y > mProfile.bottom - mCornerSize) {
            mCurScrollRange = RIGHT_BOTTOM;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mCurScrollRange != NO_SCROLL) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnScrollStopListener != null)
                            mOnScrollStopListener.onScrollStop(mLeft, mTop, mRight, mBottom);
                    }
                }, 1000);
            }
        }
        return mDetector.onTouchEvent(event);
    }

    //
    public interface OnScrollStopListener {
        void onScrollStop(float left, float top, float right, float bottom);
    }

    private OnScrollStopListener mOnScrollStopListener;

    public void setOnScrollStopListener(OnScrollStopListener listener) {
        mOnScrollStopListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLeft = mPadding;
        mTop = mPadding;
        mRight = getMeasuredWidth() - mPadding;
        mBottom = getMeasuredHeight() - mPadding;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //四角
        drawCorner(canvas);

        //框
        int mProfileWidth = Utils.dp2px(getContext(), 2.0f);
        mPaint.setStrokeWidth(mProfileWidth);
        //移动四角线宽
        mProfile = new RectF(mLeft + mCornerWidth / 2, mTop + mCornerWidth / 2, mRight - mCornerWidth / 2, mBottom - mCornerWidth / 2);
        canvas.drawRect(mProfile, mPaint);
        mPaint.setStrokeWidth(Utils.dp2px(getContext(), 0.5f));

        Path path = new Path();

        //横线1
        path.moveTo(mProfile.left, mProfile.top + mProfile.height() / 3);
        path.lineTo(mProfile.right, +mProfile.top + mProfile.height() / 3);
        canvas.drawPath(path, mPaint);

        //横线2
        path.moveTo(mProfile.left, mProfile.top + mProfile.height() * 2 / 3);
        path.lineTo(mProfile.right, mProfile.top + mProfile.height() * 2 / 3);
        canvas.drawPath(path, mPaint);

        //竖线1
        path.moveTo(mProfile.left + mProfile.width() / 3, mProfile.top);
        path.lineTo(mProfile.left + mProfile.width() / 3, mProfile.bottom);
        canvas.drawPath(path, mPaint);
        //竖线2
        path.moveTo(mProfile.left + mProfile.width() * 2 / 3, mProfile.top);
        path.lineTo(mProfile.left + mProfile.width() * 2 / 3, mProfile.bottom);
        canvas.drawPath(path, mPaint);
    }

    private void drawCorner(Canvas canvas) {
        mPaint.setStrokeWidth(mCornerWidth);//只能看到3dp

        Path path = new Path();

        path.moveTo(mLeft, mTop);
        path.lineTo(mLeft + mCornerSize, mTop);
        canvas.drawPath(path, mPaint);

        path.moveTo(mLeft, mTop);
        path.lineTo(mLeft, mTop + mCornerSize);
        canvas.drawPath(path, mPaint);

        path.moveTo(mRight, mTop);
        path.lineTo(mRight - mCornerSize, mTop);
        canvas.drawPath(path, mPaint);

        path.moveTo(mRight, mTop);
        path.lineTo(mRight, mTop + mCornerSize);
        canvas.drawPath(path, mPaint);

        path.moveTo(mLeft, mBottom);
        path.lineTo(mLeft + mCornerSize, mBottom);
        canvas.drawPath(path, mPaint);

        path.moveTo(mLeft, mBottom);
        path.lineTo(mLeft, mBottom - mCornerSize);
        canvas.drawPath(path, mPaint);

        path.moveTo(mRight, mBottom);
        path.lineTo(mRight - mCornerSize, mBottom);
        canvas.drawPath(path, mPaint);

        path.moveTo(mRight, mBottom);
        path.lineTo(mRight, mBottom - mCornerSize);
        canvas.drawPath(path, mPaint);
    }

    public float getPadding() {
        //加上框的线宽
        return mCornerWidth + mPadding;
    }
}
