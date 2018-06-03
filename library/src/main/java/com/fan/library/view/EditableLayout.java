package com.fan.library.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fan.library.R;
import com.fan.library.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class EditableLayout extends FrameLayout implements ClipShapeView.OnScrollStopListener, View.OnClickListener {
    private EditImageView mImage;
    private ClipShapeView mShape;
    private boolean isIn;

    private int mOperationHeight;
    private float mScale = 0.8f;

    private LinearLayout mClipRoot;
    private RelativeLayout mTop;
    private ColorSelectView mColorSelector;

    public EditableLayout(@NonNull Context context) {
        this(context, null);
    }

    public EditableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.editable_layout, this);
        mImage = findViewById(R.id.image);
        mShape = findViewById(R.id.shape);


        mOperationHeight = Utils.dp2px(context, 44);
        mClipRoot = findViewById(R.id.clip_root);
        mTop = findViewById(R.id.rel_top);
        mTop.setAlpha(0);
        mColorSelector = findViewById(R.id.color_selector);
        findViewById(R.id.img_clip_complete).setOnClickListener(this);
        findViewById(R.id.img_clip_close).setOnClickListener(this);
        findViewById(R.id.img_rotate).setOnClickListener(this);
        findViewById(R.id.tv_complete).setOnClickListener(this);

        setEditColor(mColorSelector.getColor());
        mColorSelector.setOnColorChangeListener(new ColorSelectView.OnColorChangeListener() {
            @Override
            public void onColorChange(int color) {
                setEditColor(color);
            }
        });
    }

    private String mPath;

    public void setPath(String p) {
        mPath = p;
    }

    private boolean isInit;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isInit) return;
        mImage.setImageBitmap(Utils.compress(mPath, mImage.getWidth(), mImage.getHeight()));
        RectF rectF = mImage.getMatrixRectF();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mShape.getLayoutParams();
        params.width = (int) (rectF.width() * mScale + mShape.getPadding());
        params.height = (int) (rectF.height() * mScale + mShape.getPadding());
        params.gravity = Gravity.CENTER;
        mShape.requestLayout();
        mShape.setOnScrollStopListener(this);
        isInit = true;
    }

    public void in() {
        mImage.animate().scaleX(0.8f).scaleY(0.8f).translationYBy(-mOperationHeight).setDuration(200).start();

        FrameLayout.LayoutParams shapeParams = (FrameLayout.LayoutParams) mShape.getLayoutParams();

        shapeParams.bottomMargin = mOperationHeight;
        mShape.requestLayout();
        mShape.setVisibility(View.VISIBLE);

        isIn = true;
        mClipRoot.setVisibility(VISIBLE);
        if (mPosChangeListener != null) {
            mPosChangeListener.onPosChange(isIn);
        }
        mTop.animate().alpha(0).setDuration(100).start();
    }

    public void out() {
        mImage.animate().scaleX(1.0f).scaleY(1.0f).translationYBy(mOperationHeight).setDuration(200).start();

        FrameLayout.LayoutParams shapeParams = (FrameLayout.LayoutParams) mShape.getLayoutParams();
        shapeParams.bottomMargin = 0;
        mShape.requestLayout();
        mShape.setVisibility(View.GONE);

        isIn = false;
        mClipRoot.setVisibility(GONE);

        if (mPosChangeListener != null) {
            mPosChangeListener.onPosChange(isIn);
        }
    }

    @Override
    public void onScrollStop(RectF rect) {

    }

    public void togglePos() {
        if (isIn) {
            out();
        } else {
            in();
        }
    }


    public void process() {
        if (mOnProcessListener != null) mOnProcessListener.onProcessStart();
        Thread thread = Executors.defaultThreadFactory().newThread(new Runnable() {
            @Override
            public void run() {
                clip();
            }
        });
        thread.start();
    }

    private void clip() {
        RectF rectF = mShape.getCurRange();
        if (rectF == null) {
            rectF = mImage.getMatrixRectF();
        }
        int left = (int) (rectF.left / mScale);
        int top = (int) (rectF.top / mScale);
        int right = (int) (rectF.right / mScale);
        int bottom = (int) (rectF.bottom / mScale);
        Rect rect = new Rect(left, top, right, bottom);
        Bitmap bitmap = mImage.clipImage(rect);
        int fileNameStart = mPath.lastIndexOf(File.separator);
        int fileNameEnd = mPath.lastIndexOf(".");
        File dest = new File(getContext().getExternalCacheDir() + File.separator + mPath.substring(fileNameStart + 1, fileNameEnd) + "-clip.png");
        try {
            FileOutputStream os = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mOnProcessListener != null) mOnProcessListener.onProcessEnd(dest.getAbsolutePath());
    }

    public interface OnProcessListener {
        void onProcessStart();

        void onProcessEnd(String path);
    }

    public void setOnProcessListener(OnProcessListener listerer) {
        mOnProcessListener = listerer;
    }

    private OnImagePosChangeListener mPosChangeListener;
    private OnProcessListener mOnProcessListener;

    public interface OnImagePosChangeListener {
        void onPosChange(boolean isIn);
    }

    public void setOnImagePosChangeListener(OnImagePosChangeListener listener) {
        mPosChangeListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mStatus == Status.edit && ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mTop.getAlpha() == 1.0f) {
                mTop.animate().alpha(0).setDuration(100).start();
            } else {
                mTop.animate().alpha(1.0f).setDuration(100).start();
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.img_clip_complete) {
            process();
        }
        if (v.getId() == R.id.img_clip_close) {
            out();
        }
        if (v.getId() == R.id.img_rotate) {
//            img.rotate(-90);
//            mShape.rotate(90);
        }
        if (v.getId() == R.id.tv_complete) {
            process();
        }
    }

    public void setStatus(Status status) {
        if (status == Status.edit) {
            mStatus = Status.edit;
            mImage.setInEditStatus(true);
            mClipRoot.setVisibility(GONE);
            if (mColorSelector.getVisibility() == VISIBLE) {
                mColorSelector.setVisibility(GONE);
            } else {
                mColorSelector.setVisibility(VISIBLE);
            }
        } else {
            mStatus = Status.clip;
            mImage.setInEditStatus(false);
            mClipRoot.setVisibility(VISIBLE);
            mColorSelector.setVisibility(GONE);
        }
    }

    public void setEditColor(int color) {
        mImage.setEditColor(color);
    }

    private Status mStatus;

    public enum Status {
        edit, clip
    }
}
