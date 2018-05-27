package com.fan.library.cache;

import android.graphics.Bitmap;

public interface ImageCache {
    void put(String key, Bitmap Bitmap);

    Bitmap get(String key);
}
