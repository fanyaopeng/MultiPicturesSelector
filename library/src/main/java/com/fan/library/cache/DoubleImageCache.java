package com.fan.library.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;

public class DoubleImageCache implements ImageCache {
    private Context mContext;
    private String mCachePath;

    public DoubleImageCache(Context context) {
        mContext = context;
        //disk 缓存 应该是没必要的
//        mCachePath = context.getCacheDir().getAbsolutePath() + "/images/";
//        File cache = new File(mCachePath);
//        if (!cache.exists()) cache.mkdir();
    }

    private static LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    @Override
    public void put(String key, Bitmap bitmap) {
        cache.put(key, bitmap);
//        try {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(new File(mCachePath + encode(key))));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public Bitmap get(String key) {
        Bitmap result;
        if ((result = cache.get(key)) != null) {
            return result;
        }
//        File cachePath = new File(mCachePath + encode(key));
//        if (cachePath.exists()) {
//            result = BitmapFactory.decodeFile(cachePath.getAbsolutePath());
//            cache.put(key, result);
//            return result;
//        }
        return null;
    }

    private String encode(String path) {
        int index = path.lastIndexOf(".");
        String key = path.substring(0, index);
        try {
            String result = Base64.encodeToString(key.getBytes("utf-8"), Base64.DEFAULT);
            return result.concat(path.substring(index, path.length()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
