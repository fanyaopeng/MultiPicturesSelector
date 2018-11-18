package com.fan.MultiImageSelector.cache;

import android.graphics.Bitmap;
import android.util.LruCache;

public class MemoryImageCache implements ImageCache {
    public MemoryImageCache() {
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
    }

    @Override
    public Bitmap get(String key) {
        Bitmap result;
        if ((result = cache.get(key)) != null) {
            return result;
        }
        return null;
    }
}
