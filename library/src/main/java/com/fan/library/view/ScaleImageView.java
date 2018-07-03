package com.fan.library.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * 遗留问题 是否可以解决惯性滑动  是否可以支持旋转手势
 */
public class ScaleImageView extends ImageView {
    protected Matrix matrix;
    private GestureDetector mGestureDetector;
    protected float mMaxScale = 4;
    protected float mCenterScale = 2;
    protected float mInitScale = 1.0f;
    private ScaleGestureDetector mScaleGestureDetector;
    private Scroller mScroller;
    protected float[] mScaleFocus = new float[2];
    private float mTouchSlop;

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
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        mScaleGestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (checkScrollBorder) {
                checkBorder();
                checkScrollBorder = false;
            }
        }
        return true;
    }
//
//    private boolean isInEidt;
//
//    public void setInEditStatus(boolean isInEdit) {
//        this.isInEidt = isInEdit;
//    }
//    private int mEditColor;
//
//    public void setEditColor(@ColorInt int color) {
//        mEditColor = color;
//    }

    private OnGestureListener mGestureListener;

    public Bitmap clipImage(Rect rect) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        //经过缩放的图片的范围 可能是小于0的
        int left = rect.left;
        int top = rect.top;
        int width = rect.width();
        int height = rect.height();
        if (left < 0) left = 0;
        if (top < 0) top = 0;
        if (width > getWidth()) width = getWidth();
        if (height > getHeight()) height = getHeight();
        return Bitmap.createBitmap(bitmap, left, top, width, height);
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
                slowScale(mMaxScale);
            }
            if (getCurScale() < mInitScale) {
                resetScale();
            }
        }
    }

    private void resetScale() {
        slowScale(mInitScale);
    }

    protected void checkBorder() {
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
        //slowTranslate(dx,dy);
    }

    protected float[] checkScroll(float distanceX, float distanceY) {
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

    protected float getCurScale() {
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
                slowScale(mCenterScale);
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
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (Math.abs(distanceX) < mTouchSlop && Math.abs(distanceY) < mTouchSlop) {
                return true;
            }
            if (isScale) return true;//滑动的时候 如果在缩放 则 无操作
            ScaleImageView.this.onScroll(distanceX, distanceY);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //fling(e1, e2, velocityX, velocityY);
            return true;
        }

    }

    private void fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        RectF rectF = getMatrixRectF();
        if (rectF.width() <= getWidth() && rectF.height() <= getHeight()) return;
        int startX = (int) e1.getX();
        int startY = (int) e1.getY();
        mLastFlingX = startX;
        mLastFlingY = startY;
        int minX, minY, maxX, maxY;

        minX = 0;
        minY = 0;

        maxX = (int) (rectF.right - getWidth());
        maxY = (int) (rectF.bottom - getHeight());
        mScroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY);
    }

    private int mLastFlingX, mLastFlingY;

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int curX = mScroller.getCurrX();
            //Log.e("main", "cur  " + curX);
            int curY = mScroller.getCurrY();
            int dx = curX - mLastFlingX;
            int dy = curY - mLastFlingY;
            //Log.e("main", "dx  " + dx + "  dy " + dy);
            matrix.postTranslate(dx, dy);
            mLastFlingX = curX;
            mLastFlingY = curY;
            setImageMatrix(matrix);
        }
    }

    private boolean isInitScale;

    protected void onScroll(float distanceX, float distanceY) {
        checkIntercept(distanceX);
        if (getCurScale() == mInitScale) {
            distanceY = 0;
        }
        float[] target = checkScroll(distanceX, distanceY);
        matrix.postTranslate(-target[0], -target[1]);
        setImageMatrix(matrix);
        checkScrollBorder = true;
    }

    private boolean checkScrollBorder;

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

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawColor(getContext().getResources().);
        super.onDraw(canvas);
    }


    protected void slowScale(float target) {

        ValueAnimator animator = ValueAnimator.ofFloat(getCurScale(), target);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curTarget = (float) animation.getAnimatedValue();
                float factor = curTarget / getCurScale();
                matrix.postScale(factor, factor, mScaleFocus[0], mScaleFocus[1]);
                setImageMatrix(matrix);
                if (animation.getAnimatedFraction() == 1) {
                    checkBorder();
                    if (mScaleEndListener != null) mScaleEndListener.onScaleEnd();
                    mScaleEndListener = null;
                }
            }
        });
        animator.setDuration(200).start();
    }

    public OnScaleEndListener mScaleEndListener;

    public void setOnScaleEndListener(OnScaleEndListener listener) {
        mScaleEndListener = listener;
    }

    public interface OnScaleEndListener {
        void onScaleEnd();
    }

    private void slowTranslate(final float targetX, final float targetY) {

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float curTarget = (float) animation.getAnimatedValue();
                float curX = getCurTranslate()[0];
                float curY = getCurTranslate()[1];
                float curTargetX = targetX * curTarget;
                float curTargetY = targetY * curTarget;
                matrix.postTranslate(curTargetX - curX, curTargetY - curY);
                setImageMatrix(matrix);
            }
        });
        animator.setDuration(200).start();
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
//        if (height > dh && width > dw) {
//            float scaleW = (float) width / (float) dw;
//            float scaleH = (float) height / (float) dh;
//            mInitScale = Math.min(scaleH, scaleW);
//        }
        float scaleW = (float) width / (float) dw;
        float scaleH = (float) height / (float) dh;
        if (dh > height && dw > width) {

        }
        mInitScale = Math.min(scaleH, scaleW);
        mMaxScale = mInitScale * 4;
        mCenterScale = mInitScale * 2;
        matrix.postTranslate((width - dw) / 2, (height - dh) / 2);
        matrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
        setImageMatrix(matrix);
        isInitScale = true;

        mScaleFocus[0] = width / 2;
        mScaleFocus[1] = height / 2;
    }

    public void rotate(float degree) {
        matrix.postRotate(degree, getWidth() / 2, getHeight() / 2);
        setImageMatrix(matrix);
    }

}
