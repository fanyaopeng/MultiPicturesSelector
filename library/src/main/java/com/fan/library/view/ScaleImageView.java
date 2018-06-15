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
import android.widget.ImageView;
import android.widget.Scroller;

public class ScaleImageView extends ImageView {
    protected Matrix matrix;
    private GestureDetector mGestureDetector;
    private float mMaxScale = 4;
    private float mCenterScale = 2;
    private float mInitScale = 1.0f;
    private ScaleGestureDetector mScaleGestureDetector;
    private Scroller mScroller;
    private float[] mScaleFocus = new float[2];

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
            if (isScale) isScale = false;
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
        RectF rectF = getMatrixRectF();
        if (rect.left < rectF.left) {
            rect.left = (int) rectF.left;
        }
        if (rect.top < rectF.top) {
            rect.top = (int) rectF.top;
        }
        if (rect.right > rectF.right) {
            rect.right = (int) rectF.right;
        }
        if (rect.bottom > rectF.bottom) {
            rect.bottom = (int) rectF.bottom;
        }
        return Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
    }

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
            if (getCurScale() * factor < 0.5f * mInitScale) {
                return true;
            }
            isScale = true;
            mScaleFocus[0] = detector.getFocusX();
            mScaleFocus[1] = detector.getFocusY();

            matrix.postScale(factor, factor, detector.getFocusX(), detector.getFocusY());
            setImageMatrix(matrix);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
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
            isDoubleTap = true;
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
        public boolean onSingleTapUp(MotionEvent e) {
            isDoubleTap = false;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!isDoubleTap) {
                        if (mGestureListener != null) {
                            mGestureListener.onSingleTapUp();
                        }
                    }
                }
            }, 1000);
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isScale) return true;//滑动的时候 如果在缩放 则 无操作
            ScaleImageView.this.onScroll(distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //fling(e1, e2, velocityX, velocityY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

    }

    private void fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        RectF rectF = getMatrixRectF();
        if (rectF.width() < getWidth() && rectF.height() < getHeight()) return;
        int startX = -Math.round(rectF.left);
        int startY = -Math.round(rectF.top);
        int minX, minY, maxX, maxY;
        maxX = Math.round(rectF.width() - getWidth());
        minX = 0;
        minY = 0;
        maxY = Math.round(rectF.height() - getHeight());
        mScroller.fling(startX, startY, (int) velocityX, (int) velocityY, minX, maxX, minY, maxY);
        invalidate();
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
            if (rectF.right == width && dx > 0) {
                getParent().requestDisallowInterceptTouchEvent(false);
            } else if (rectF.left == 0 && dx < 0) {
                getParent().requestDisallowInterceptTouchEvent(false);
            } else {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawColor(getContext().getResources().);
        super.onDraw(canvas);
    }


    private void slowScale(float target) {

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
                }
            }
        });
        animator.setDuration(200).start();
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


    private void init() {
        if (isInitScale) return;
        Drawable d = getDrawable();
        if (d == null) return;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        int dw = d.getIntrinsicWidth();
        int dh = d.getIntrinsicHeight();
        Log.e("main", "dw  " + dw + "dh  " + dh);
        if (height > dh && width > dw) {
            float scaleW = (float) width / (float) dw;
            float scaleH = (float) height / (float) dh;
            mInitScale = Math.min(scaleH, scaleW);
        }
        if (dh > height && dw > width) {
            float scaleW = (float) width / (float) dw;
            float scaleH = (float) height / (float) dh;
            mInitScale = Math.min(scaleH, scaleW);
        }
        mMaxScale = mInitScale * 4;
        mCenterScale = mInitScale * 2;
        matrix.postTranslate((width - dw) / 2, (height - dh) / 2);
        matrix.postScale(mInitScale, mInitScale, width / 2, height / 2);
        setImageMatrix(matrix);
        isInitScale = true;
    }

    public void rotate(float degree) {
        matrix.postRotate(degree, getWidth() / 2, getHeight() / 2);
        setImageMatrix(matrix);
    }

    //摆正剪切的位置
    public void setClipPosition(float scale) {
        mScaleFocus[0] = getWidth() / 2;
        mScaleFocus[1] = getHeight() / 2;
        slowScale(scale);
    }
}
