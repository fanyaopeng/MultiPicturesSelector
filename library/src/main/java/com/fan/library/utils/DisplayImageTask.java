package com.fan.library.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.fan.library.cache.DoubleImageCache;
import com.fan.library.cache.ImageCache;

public class DisplayImageTask implements Runnable {
    private Activity mHost;
    private String mPath;
    private ImageView mTarget;
    private ImageCache mCache;
    private int mWidth;
    private int mHeight;

    public DisplayImageTask(Activity host, String path, ImageView target, int width, int height) {
        this.mHost = host;
        this.mPath = path;
        this.mTarget = target;
        this.mWidth = width;
        this.mHeight = height;
        mTarget.setScaleX(0);
        mCache = new DoubleImageCache(host);
    }

    @Override
    public void run() {
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
        final Bitmap finalBitmap = bitmap;
        mHost.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTarget.setImageBitmap(finalBitmap);
                mTarget.setScaleX(1);
            }
        });
    }
}
