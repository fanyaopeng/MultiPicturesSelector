package com.fan.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.fan.library.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class EditImageView extends ScaleImageView {
    private Paint mPaint;

    private float mCurX;
    private float mCurY;
    private List<PathInfo> mPaths;
    private RectF mInitRange;

    public EditImageView(Context context) {
        this(context, null);
    }

    public EditImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(Utils.dp2px(context, 5));
        mPaths = new ArrayList<>();
    }

    public void setEditColor(int color) {
        PathInfo info = new PathInfo(new Path(), color);
        mPaths.add(info);

    }

    private boolean isInEditStatus;

    public void setInEditStatus(boolean isInEditStatus) {
        this.isInEditStatus = isInEditStatus;
    }

    @Override
    protected void onScroll(float distanceX, float distanceY) {
        if (!isInEditStatus) {
            super.onScroll(distanceX, distanceY);
            return;
        }
        mCurX -= distanceX;
        mCurY -= distanceY;
        invalidate();
    }

    public void setRange(RectF rectF) {
        mInitRange = new RectF(rectF);
    }

    @Override
    protected void checkBorder() {
        if (!isInEditStatus) {
            super.checkBorder();
            return;
        }
        if (mInitRange == null) return;
        //在剪切的的情况下 我们不能让他超过我们矩形的 范围
        RectF rectF = getMatrixRectF();
        float dx = 0;
        float dy = 0;
        float width = getWidth();
        float height = getHeight();
        if (rectF.width() >= width) {
            if (rectF.left > mInitRange.left) {
                dx = mInitRange.left - rectF.left;
            }
            if (rectF.right < mInitRange.right) {
                dx = mInitRange.right - rectF.right;
            }
        }
        if (rectF.height() >= height) {
            if (rectF.top > mInitRange.top) {
                dy = mInitRange.top - rectF.top;
            }
            if (rectF.bottom < mInitRange.bottom) {
                dy = mInitRange.bottom - rectF.bottom;
            }
        }

        if (rectF.width() < width) {
            dx = (mInitRange.left + mInitRange.right) / 2f + rectF.width() / 2f - rectF.right;
        }
        if (rectF.height() < height) {
            dy = (mInitRange.bottom + mInitRange.top) / 2f + rectF.height() / 2f - rectF.bottom;
        }

        matrix.postTranslate(dx, dy);
        setImageMatrix(matrix);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditStatus) {
            for (PathInfo pathInfo : mPaths) {
                Path path = pathInfo.getPath();
                if (pathInfo == getLastPath()) {
                    path.lineTo(mCurX, mCurY);
                }
                mPaint.setColor(pathInfo.getColor());
                canvas.drawPath(path, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isInEditStatus && event.getAction() == MotionEvent.ACTION_DOWN) {
            Path path = getLastPath().getPath();
            float startX = event.getX();
            float startY = event.getY();
            path.moveTo(startX, startY);
            mCurX = startX;
            mCurY = startY;
        }
        return super.onTouchEvent(event);
    }

    private static class PathInfo {
        Path path;
        int color;

        public PathInfo(Path path, int color) {
            this.path = path;
            this.color = color;
        }

        public Path getPath() {
            return path;
        }

        public int getColor() {
            return color;
        }
    }

    private PathInfo getLastPath() {
        return mPaths.get(mPaths.size() - 1);
    }

    public void resetClipPosition(float scale) {
        slowScale(getCurScale() / scale);
    }

    //剪切的位置
    public void setClipPosition(float scale) {
        slowScale(mInitScale * scale);
    }

}
