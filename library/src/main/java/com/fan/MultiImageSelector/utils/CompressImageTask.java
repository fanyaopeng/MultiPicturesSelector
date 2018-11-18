package com.fan.MultiImageSelector.utils;

import android.graphics.Bitmap;

import com.fan.MultiImageSelector.cache.MemoryImageCache;
import com.fan.MultiImageSelector.cache.ImageCache;

public class CompressImageTask implements Runnable {
    private String mPath;
    private ImageCache mCache;
    private int mWidth;
    private int mHeight;
    private OnCompressListener mListener;

    public CompressImageTask(String path, int width, int height, OnCompressListener listener) {
        this.mPath = path;
        this.mWidth = width;
        this.mHeight = height;
        mCache = new MemoryImageCache();
        mListener = listener;
    }

    @Override
    public void run() {
        if (mListener != null) mListener.onStart();
        Bitmap bitmap = mCache.get(mPath);
        boolean isSize = false;
        if (bitmap != null) {
            int widthDif = bitmap.getWidth() / mWidth;
            int heightDif = bitmap.getHeight() / mHeight;
            //如果 宽高的相差在0.5倍 到2倍之间  我们就认为 不需要再次压缩
            isSize = widthDif > 0.5f && widthDif < 2f || heightDif > 0.5f && heightDif < 2f;
        }
        if (bitmap == null || !isSize) {
            bitmap = Utils.compress(mPath, mWidth, mHeight);
            mCache.put(mPath, bitmap);
        }
        if (mListener != null) mListener.onComplete(bitmap);
    }

    public interface OnCompressListener {
        void onStart();

        void onComplete(Bitmap bitmap);
    }
}
