package com.fan.library;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends Activity {
    private ViewPager vp;
    private List<String> paths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
//        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        vp = findViewById(R.id.vp);
        paths = getIntent().getStringArrayListExtra("paths");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (vp.getAdapter() == null)
            init();
    }

    private void init() {
        imageViews.clear();
        for (String p : paths) {
            if (!Utils.isGif(p)) {
                ImageView imageView = new ImageView(PreviewActivity.this);
                Bitmap result = Utils.compress(p, vp.getWidth(), vp.getHeight());
                imageView.setImageBitmap(result);
                imageViews.add(imageView);
            } else {
                GifImageView gifImageView = new GifImageView(this);
                gifImageView.setResource(p);
                imageViews.add(gifImageView);
            }
        }
        if (imageViews.size() != 0)
            vp.setAdapter(new ImageAdapter());
    }


    private List<View> imageViews = new ArrayList<>();

    private class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return paths.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(imageViews.get(position));
            return imageViews.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(imageViews.get(position));
        }
    }
}
