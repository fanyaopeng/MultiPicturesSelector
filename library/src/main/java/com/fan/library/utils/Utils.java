package com.fan.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    public static boolean isLongImage(int width, int height, String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //款或者高 大于1.5倍 我们认为 这是一个大图
        return (options.outWidth > width || options.outHeight > height) &&
                (0.5f > width / height || width / height > 2.0f);
    }
}
