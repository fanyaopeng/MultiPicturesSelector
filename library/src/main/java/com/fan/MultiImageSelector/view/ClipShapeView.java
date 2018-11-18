package com.fan.MultiImageSelector.view;

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

import com.fan.MultiImageSelector.utils.Utils;

public class ClipShapeView extends View {
    private Paint mPaint;
    private float mLeft, mTop, mRight, mBottom;
    private GestureDetector mDetector;
    private RectF mProfile;

    private int mCornerWidth;
    private float mCornerSize;

    private final int LEFT_TOP = 0;
    private final int LEFT_BOTTOM = 1;
    private final int RIGHT_TOP = 2;
    private final int RIGHT_BOTTOM = 3;
    private final int NO_SCROLL = -1;
    private int mCurScrollRange = NO_SCROLL;
    int mProfileWidth;
    private RectF mRange;//初始的范围

    public ClipShapeView(Context context) {
        this(context, null);
    }

    public ClipShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);

        mDetector = new GestureDetector(context, new ScrollCallback());

        mCornerSize = Utils.dp2px(getContext(), 30);
        mCornerWidth = Utils.dp2px(getContext(), 2.5f);
        mProfileWidth = Utils.dp2px(getContext(), 1.5f);
    }

    private class ScrollCallback extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //Log.e("main", "dx  " + distanceX + "  dy  " + distanceY);
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
            checkBorder(distanceX, distanceY);
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
        if (x > mProfile.right || x < mProfile.left || y > mProfile.bottom || y < mProfile.top) {
            //在矩形的外面
            return false;
        }
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
            return true;
        }
        return false;
    }

    private void checkBorder(float dx, float dy) {
        RectF rectF = new RectF(mRange);
        if (dy < 0) {
            //往下
            if (mBottom > rectF.bottom) {
                mBottom = rectF.bottom;
            }
            if (mProfile.height() < mCornerSize * 2) {
                mTop = mBottom - 2 * mCornerSize;
            }
        } else if (dy > 0) {
            //a往上
            if (mTop < rectF.top) {
                mTop = rectF.top;
            }
            if (mProfile.height() < mCornerSize * 2) {
                mBottom = mTop + 2 * mCornerSize;
            }
        }


        if (dx < 0) {
            //往右
            if (mRight > mRange.right) {
                mRight = mRange.right;
            }
            if (mProfile.width() < mCornerSize * 2) {
                mLeft = mRight - 2 * mCornerSize;
            }
        } else if (dx > 0) {
            //左
            if (mLeft < mRange.left) {
                mLeft = mRange.left;
            }
            if (mProfile.width() < mCornerSize * 2) {
                mRight = mLeft + 2 * mCornerSize;
            }
        }
    }

    public void setRange(RectF rectF) {
        mRange = new RectF(rectF);
        mLeft = rectF.left;
        mTop = rectF.top;
        mRight = rectF.right;
        mBottom = rectF.bottom;
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mRange == null) return;
        //四角
        drawCorner(canvas);
        //框
        drawProfile(canvas);
        //线
        drawLine(canvas);

    }


    private void drawProfile(Canvas canvas) {
        mPaint.setStrokeWidth(mProfileWidth);
        //移动四角线宽
        float left = mLeft;
        float top = mTop;
        float right = mRight;
        float bottom = mBottom;
        mProfile = new RectF(left, top, right, bottom);
        canvas.drawRect(mProfile, mPaint);
    }

    private void drawLine(Canvas canvas) {
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
        mPaint.setStrokeWidth(mCornerWidth);

        Path path = new Path();

        int offset = mProfileWidth / 2;
        canvas.drawPath(path, mPaint);
        float left = mLeft - offset;
        float top = mTop - offset;
        float right = mRight + offset;
        float bottom = mBottom + offset;

        path.moveTo(left, top + mCornerSize);
        path.lineTo(left, top);
        path.lineTo(left + mCornerSize, top);
        canvas.drawPath(path, mPaint);


        path.moveTo(right - mCornerSize, top);
        path.lineTo(right, top);
        path.lineTo(right, top + mCornerSize);
        canvas.drawPath(path, mPaint);

        path.moveTo(left + mCornerSize, bottom);
        path.lineTo(left, bottom);
        path.lineTo(left, bottom - mCornerSize);
        canvas.drawPath(path, mPaint);

        path.moveTo(right - mCornerSize, bottom);
        path.lineTo(right, bottom);
        path.lineTo(right, bottom - mCornerSize);
        canvas.drawPath(path, mPaint);
    }

    public RectF getCurRange() {
        if (mProfile == null) return null;
        return new RectF(mLeft, mTop, mRight, mBottom);
    }
}
