package com.fan.MultiImageSelector.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;

public class Utils {
    public static int dp2px(Context context, float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    public static Bitmap compress(String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int sample = Math.max(options.outWidth / width, options.outHeight / height);
        options.inSampleSize = sample;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static boolean isPicture(String path) {

        return path.endsWith("gif") || path.endsWith("jpg") || path.endsWith("png");
    }

    public static boolean isGif(String path) {

        return path.endsWith("gif");
    }

    public static boolean isLongImage(float width, float height) {
        float ratio = width / height;
        return ratio < 0.5f;
    }
}
