package com.fan.MultiImageSelector.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import android.widget.OverScroller;

import com.fan.MultiImageSelector.utils.Utils;

/**
 * 遗留问题  是否可以支持旋转手势
 */
public class ScaleImageView extends ImageView {
    private Matrix matrix;
    private GestureDetector mGestureDetector;
    private float mMaxScale = 4;
    private float mCenterScale = 2;
    private float mInitScale = -1;
    private ScaleGestureDetector mScaleGestureDetector;
    private float[] mScaleFocus = new float[2];
    private boolean isNeedCheckBorder;

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
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (isNeedCheckBorder) {
                checkBorder(false);
                isNeedCheckBorder = false;
            }
        }
        return true;
    }

    private OnGestureListener mGestureListener;

    public Bitmap clipImage(Rect rect) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }

    public interface OnGestureListener {
        void onSingleTapUp();
    }

    public void setOnGestureListener(OnGestureListener listener) {
        mGestureListener = listener;
    }

    private boolean isScale;

    private class ScaleCallback extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor();
            if (getCurScale() * factor < 0.5f * mInitScale) {
                return true;
            }
            if (!isScale)
                isScale = true;
            mScaleFocus[0] = detector.getFocusX();
            mScaleFocus[1] = detector.getFocusY();

            matrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
            setImageMatrix(matrix);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            isScale = false;
            super.onScaleEnd(detector);
            if (getCurScale() > mMaxScale) {
                smoothScale(mMaxScale);
            }
            if (getCurScale() < mInitScale) {
                resetScale();
            }
        }
    }

    private void resetScale() {
        smoothScale(mInitScale);
    }


    private void checkBorder(boolean isImmediately) {
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
        if (isImmediately) {
            matrix.postTranslate(dx, dy);
        } else {
            smoothTranslate(dx, dy);
        }
    }

    private float[] checkScroll(float distanceX, float distanceY) {
        RectF rectF = getMatrixRectF();
        float width = getWidth();
        float height = getHeight();
        float damp = 4;
        if (rectF.width() >= width) {
            if (rectF.left > 0) {
                distanceX = distanceX / damp;
            }
            if (rectF.right < width) {
                distanceX = distanceX / damp;
            }
        }
        if (rectF.height() >= height) {
            if (rectF.top > 0) {
                distanceY = distanceY / damp;
            }
            if (rectF.bottom < height) {
                distanceY = distanceY / damp;
            }
        }
        if (rectF.width() < width) {
            distanceX = distanceX / damp;
        }
        if (rectF.height() < height) {
            distanceY = distanceY / damp;
        }
        float[] result = new float[2];
        result[0] = distanceX;
        result[1] = distanceY;
        return result;
    }

    public RectF getMatrixRectF() {
        RectF rectF = new RectF();
        Drawable d = getDrawable();
        rectF.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        matrix.mapRect(rectF);
        return rectF;
    }

    private float getCurScale() {
        float src[] = new float[9];
        matrix.getValues(src);
        return src[Matrix.MSCALE_X];
    }

    private float[] getCurTranslate() {
        float src[] = new float[9];
        float[] result = new float[2];
        matrix.getValues(src);
        result[0] = src[Matrix.MTRANS_X];
        result[1] = src[Matrix.MTRANS_Y];
        return result;
    }

    private class TapCallback extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float curScale = getCurScale();
            if (curScale < mCenterScale) {
                mScaleFocus[0] = e.getX();
                mScaleFocus[1] = e.getY();
                smoothScale(mCenterScale);
            } else {
                resetScale();
            }
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mGestureListener != null) {
                mGestureListener.onSingleTapUp();
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {

            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScale) return true;//滑动的时候 如果在缩放 则 无操作
            ScaleImageView.this.onScroll(distanceX, distanceY);
            isNeedCheckBorder = true;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            fling(velocityX, velocityY);
            return true;
        }
    }

    protected void fling(float velocityX, float velocityY) {
        mFlingTask.fling((int) velocityX, (int) velocityY);
    }


    FlingTask mFlingTask = new FlingTask();

    private class FlingTask implements Runnable {
        private OverScroller mScroller;
        private int mLastFlingX;
        private int mLastFlingY;

        public FlingTask() {
            mScroller = new OverScroller(getContext());
        }

        void fling(int velocityX, int velocityY) {
            RectF rectF = getMatrixRectF();
            if (rectF.width() < getWidth() && rectF.height() < getHeight()) {
                return;
            }
            int startX = (int) -rectF.left;
            int startY = (int) -rectF.top;
            int minX, maxX, minY, maxY;
            maxX = (int) (rectF.width() - getWidth());
            maxY = (int) (rectF.height() - getHeight());
            minX = 0;
            minY = 0;
            mLastFlingX = startX;
            mLastFlingY = startY;

            mScroller.fling(startX, startY, -velocityX, -velocityY,
                    minX, maxX, minY, maxY);
            post(this);
        }

        @Override
        public void run() {
            if (mScroller.computeScrollOffset()) {
                int curX = mScroller.getCurrX();
                int curY = mScroller.getCurrY();
                float dx = mLastFlingX - curX;
                float dy = mLastFlingY - curY;
                matrix.postTranslate(dx, dy);
                checkBorder(true);
                setImageMatrix(matrix);
                mLastFlingY = curY;
                mLastFlingX = curX;
                if (!mScroller.isFinished()) {
                    removeCallbacks(this);
                    post(this);
                }
            }
        }
    }

    private boolean isInitScale;

    protected void onScroll(float distanceX, float distanceY) {
        checkIntercept(distanceX);
        float[] target = checkScroll(distanceX, distanceY);
        matrix.postTranslate(-target[0], -target[1]);
        setImageMatrix(matrix);
    }

    private void checkIntercept(float dx) {
        RectF rectF = getMatrixRectF();
        float width = getWidth();
        float height = getHeight();
        if (rectF.height() > height || rectF.width() >= width) {
            if (rectF.right >= width && dx > 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
            } else if (rectF.left <= 0 && dx < 0) {
                getParent().requestDisallowInterceptTouchEvent(true);
            } else {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
    }

    private RectF mRange;

    public void setInitScale(float scale) {
        mInitScale = scale;
        float factor = scale / getCurScale();
        matrix.postScale(factor, factor, getWidth() / 2, getHeight() / 2);
        setImageMatrix(matrix);
//        mRange = getMatrixRectF();
//        Log.e("main", "range  " + mRange);
    }

    private void smoothScale(float target) {
        post(new SmoothScaleTask(target));
    }


    private class SmoothScaleTask implements Runnable {
        private float target;
        private float makeBigger = 1.04f;
        private float makeSmaller = 0.96f;
        float factor;
        boolean isScaleFinish;

        public SmoothScaleTask(float target) {
            this.target = target;
            if (getCurScale() < target) {
                factor = makeBigger;
            } else {
                factor = makeSmaller;
            }
        }

        @Override
        public void run() {
            float curScale = getCurScale();
            if (factor > 1 && curScale < target) {
                factor = makeBigger;
            } else if (factor < 1 && curScale > target) {
                factor = makeSmaller;
            } else {
                factor = target / curScale;
                isScaleFinish = true;
            }
            matrix.postScale(factor, factor, mScaleFocus[0], mScaleFocus[1]);
            setImageMatrix(matrix);
            if (!isScaleFinish) {
                isScale = true;
                postDelayed(this, 10);
            } else {
                isScale = false;
                checkBorder(false);
            }
        }
    }

    private class SmoothTranslateTask implements Runnable {
        private float targetX;
        private float targetY;
        boolean isXFinish;
        boolean isYFinish;
        private float perTranslateValue = 20;
        private float mTotalX;
        private float mTotalY;
        private int mXSymbol;
        private int mYSymbol;

        public SmoothTranslateTask(float targetX, float targetY) {
            this.targetX = targetX;
            this.targetY = targetY;

            if (targetX > 0) {
                mXSymbol = 1;
            } else {
                mXSymbol = -1;
            }
            if (targetY > 0) {
                mYSymbol = 1;
            } else {
                mYSymbol = -1;
            }
        }

        @Override
        public void run() {

            float dx = 0;
            float dy = 0;
            if (mTotalX < Math.abs(targetX) && Math.abs(mTotalX - Math.abs(targetX)) > perTranslateValue) {
                dx = perTranslateValue * mXSymbol;
            } else {
                dx = (Math.abs(targetX) - mTotalX) * mXSymbol;
                isXFinish = true;
            }
            if (mTotalY < Math.abs(targetY) && Math.abs(mTotalY - Math.abs(targetY)) > perTranslateValue) {
                dy = perTranslateValue * mYSymbol;
            } else {
                dy = (Math.abs(targetY) - mTotalY) * mYSymbol;
                isYFinish = true;
            }
            mTotalX += Math.abs(dx);
            mTotalY += Math.abs(dy);
            matrix.postTranslate(dx, dy);
            setImageMatrix(matrix);

            if (!isXFinish || !isYFinish) {
                postDelayed(this, 10);
            } else {
//                Log.e("main", "targetX  " + targetX + " targetY " + targetY);
//                Log.e("main", "totalX   " + mTotalX + " totalY " + mTotalY);
            }
        }
    }

    private void smoothTranslate(float targetX, float targetY) {
        post(new SmoothTranslateTask(targetX, targetY));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        init();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        matrix.reset();
        isInitScale = false;
        super.setImageBitmap(bm);
    }

    private void init() {
        if (isInitScale) return;
        Drawable d = getDrawable();
        if (d == null) return;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        Log.e("main", "dw  " + dw + "dh  " + dh);
        float scaleW = (float) width / (float) dw;
        float scaleH = (float) height / (float) dh;
        boolean isLongImage = Utils.isLongImage(dw, dh);
        if (isLongImage) {
            scaleW = (float) width / (float) dw;
            scaleH = scaleW;
        }
        if (mInitScale == -1) {
            mInitScale = Math.min(scaleH, scaleW);
            mMaxScale = mInitScale * 4;
            mCenterScale = mInitScale * 2;
        }
        if (!isLongImage || mInitScale != -1) {
            matrix.postTranslate((width - dw) / 2, (height - dh) / 2);
            matrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
        } else {
            matrix.postTranslate((width - dw) / 2, 0);
            matrix.postScale(mInitScale, mInitScale, width / 2, 0);
        }

        setImageMatrix(matrix);
        isInitScale = true;

        mScaleFocus[0] = width / 2;
        mScaleFocus[1] = height / 2;
    }
}
