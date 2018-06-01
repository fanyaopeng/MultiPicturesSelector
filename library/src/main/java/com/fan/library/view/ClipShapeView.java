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

    public ClipShapeView(Context context) {
        this(context, null);
    }

    public ClipShapeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mDetector = new GestureDetector(context, new ScrollCallback());
    }

    private class ScrollCallback extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            invalidate();
            return true;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mLeft = 0;
        mTop = 0;
        mRight = getMeasuredWidth();
        mBottom = getMeasuredHeight();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //四角
        drawCorner(canvas);

        //框
        int cornerWidth = Utils.dp2px(getContext(), 6.0f) / 2;
        mPaint.setStrokeWidth(Utils.dp2px(getContext(), 2.0f));
        mProfile = new RectF(mLeft + cornerWidth, mTop + cornerWidth, mRight - cornerWidth, mBottom - cornerWidth);
        canvas.drawRect(mProfile, mPaint);

        mPaint.setStrokeWidth(Utils.dp2px(getContext(), 0.5f));

        Path path = new Path();

        //横线1
        path.moveTo(0, mProfile.height() / 3);
        path.lineTo(mProfile.width(), mProfile.height() / 3);
        canvas.drawPath(path, mPaint);

        //横线2
        path.moveTo(0, mProfile.height() * 2 / 3);
        path.lineTo(mProfile.width(), mProfile.height() * 2 / 3);
        canvas.drawPath(path, mPaint);

        //竖线1
        path.moveTo(mProfile.width() / 3, 0);
        path.lineTo(mProfile.width() / 3, mProfile.height());
        canvas.drawPath(path, mPaint);
        //竖线2
        path.moveTo(mProfile.width() * 2 / 3, 0);
        path.lineTo(mProfile.width() * 2 / 3, mProfile.height());
        canvas.drawPath(path, mPaint);
    }

    private void drawCorner(Canvas canvas) {
        int size = Utils.dp2px(getContext(), 40);
        mPaint.setStrokeWidth(Utils.dp2px(getContext(), 6.0f));

        Path path = new Path();

        path.moveTo(mLeft, mTop);
        path.lineTo(mLeft + size, mTop);
        canvas.drawPath(path, mPaint);

        path.moveTo(mLeft, mTop);
        path.lineTo(mLeft, mTop + size);
        canvas.drawPath(path, mPaint);

        path.moveTo(mRight, mTop);
        path.lineTo(mRight - size, mTop);
        canvas.drawPath(path, mPaint);

        path.moveTo(mRight, mTop);
        path.lineTo(mRight, mTop + size);
        canvas.drawPath(path, mPaint);

        path.moveTo(mLeft, mBottom);
        path.lineTo(mLeft + size, mBottom);
        canvas.drawPath(path, mPaint);

        path.moveTo(mLeft, mBottom);
        path.lineTo(mLeft, mBottom - size);
        canvas.drawPath(path, mPaint);
    }
}
