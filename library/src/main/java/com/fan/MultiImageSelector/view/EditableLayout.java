package com.fan.MultiImageSelector.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.fan.MultiImageSelector.R;
import com.fan.MultiImageSelector.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class EditableLayout extends FrameLayout implements View.OnClickListener {
    private ScaleImageView mImage;
    private ClipShapeView mShape;

    private RelativeLayout mTop;

    public EditableLayout(@NonNull Context context) {
        this(context, null);
    }

    public EditableLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.editable_layout, this);
        mImage = findViewById(R.id.image);
        mShape = findViewById(R.id.shape);
        mTop = findViewById(R.id.rel_top);
        mTop.setAlpha(0);
        findViewById(R.id.tv_complete).setOnClickListener(this);
        findViewById(R.id.tv_cancel).setOnClickListener(this);
    }

    private String mPath;

    public void setPath(String p) {
        mPath = p;
        post(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = Utils.compress(mPath, mImage.getWidth(), mImage.getHeight());
                mImage.setImageBitmap(bitmap);
                scaleToFitPosition(bitmap);
            }
        });
    }

    private void scaleToFitPosition(final Bitmap bitmap) {
        mImage.post(new Runnable() {
            @Override
            public void run() {
                float desireWidth = mImage.getWidth() * 0.8f;
                float desireHeight = mImage.getHeight() * 0.8f;
                RectF rectF = mImage.getMatrixRectF();
                float realWidth = rectF.width();
                float realHeight = rectF.height();
                float bitmapWidth = bitmap.getWidth();
                float bitmapHeight = bitmap.getHeight();
                if (bitmapWidth > desireWidth || bitmapHeight > desireHeight) {
                    float desireScale = Math.min(desireWidth / bitmapWidth, desireHeight / bitmapHeight);
                    mImage.setInitScale(desireScale);
                } else {
                    float desireScale = Math.max(realWidth / desireWidth, realHeight / desireHeight);
                    mImage.setInitScale(desireScale);
                }
                mShape.setRange(mImage.getMatrixRectF());
            }
        });
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
        Rect rect = new Rect();
        rect.set((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        Bitmap bitmap = mImage.clipImage(rect);
        File file = new File(mPath);
        File dir = new File(getContext().getExternalCacheDir() + File.separator + "image");
        if (!dir.exists()) dir.mkdir();
        File dest = new File(dir + File.separator + file.getName() + "-clip.png");
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

    private OnProcessListener mOnProcessListener;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (mTop.getAlpha() == 1.0f) {
                mTop.animate().alpha(0).setDuration(100).start();
            } else {
                mTop.animate().alpha(1.0f).setDuration(100).start();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_complete) {
            process();
        }
        if (v.getId() == R.id.tv_cancel) {
            Activity activity = (Activity) getContext();
            activity.finish();
        }
    }
}
