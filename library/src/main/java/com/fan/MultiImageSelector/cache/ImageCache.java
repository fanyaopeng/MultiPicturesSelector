package com.fan.MultiImageSelector.cache;

import android.graphics.Bitmap;

public interface ImageCache {
    void put(String key, Bitmap Bitmap);

    Bitmap get(String key);
}
