package com.fan.library.view;

import android.view.MotionEvent;

public class RotateGestureDetector {
    private float mLastX1, mLastY1;
    private float mLastX2, mLastY2;

    public boolean onTouchEvent(MotionEvent event) {
        int count = event.getPointerCount();
        if (count <= 1) return true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX1 = event.getX(event.getPointerId(0));
                mLastY1 = event.getY(event.getPointerId(0));

                mLastX2 = event.getX(event.getPointerId(1));
                mLastY2 = event.getY(event.getPointerId(1));
                break;
            case MotionEvent.ACTION_MOVE:
                float mCurX1 = event.getX(event.getPointerId(0));
                float mCurY1 = event.getY(event.getPointerId(0));

                float mCurX2 = event.getX(event.getPointerId(1));
                float mCurY2 = event.getY(event.getPointerId(1));

                float dx1 = mCurX1 - mLastX1;
                float dy1 = mCurY1 - mLastY1;

                float dx2 = mCurX2 - mLastX2;
                float dy2 = mCurY2 - mLastY2;
               // if (dx1>0&&dx2<0)
                mLastX1 = mCurX1;
                mLastX2 = mCurX2;
                mLastY1 = mCurY1;
                mLastY2 = mCurY2;
                break;
        }
        return false;
    }

    public interface RoteteCallback {

    }
}
