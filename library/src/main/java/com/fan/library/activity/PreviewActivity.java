package com.fan.library.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fan.library.R;
import com.fan.library.utils.Utils;
import com.fan.library.view.GifImageView;
import com.fan.library.view.LargeScaleImageView;
import com.fan.library.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends Activity {
    private ViewPager vp;
    private List<String> paths;
    private RelativeLayout mTopBar;
    private RelativeLayout mBottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        paths = getIntent().getStringArrayListExtra("paths");
        initView();
    }

    private void initView() {
        vp = findViewById(R.id.vp);
        mTopBar = findViewById(R.id.top_bar);
        mBottomBar = findViewById(R.id.bottom_bar);
        mBottomBar.setAlpha(0.8f);
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
                ScaleImageView imageView = new ScaleImageView(this);
                if (Utils.isLongImage(PreviewActivity.this, p)) {
                    // Bitmap result = Utils.compress(p, vp.getWidth(), Integer.MAX_VALUE);
                    imageView = new LargeScaleImageView(this);
                    LargeScaleImageView img = (LargeScaleImageView) imageView;
                    img.setImagePath(p);
                } else {
                    imageView = new ScaleImageView(this);
                    Bitmap result = Utils.compress(p, vp.getWidth(), vp.getHeight());
                    imageView.setImageBitmap(result);
                }
                imageViews.add(imageView);
            } else {
                GifImageView gifImageView = new GifImageView(this);
                gifImageView.setResource(p);
                imageViews.add(gifImageView);
            }
        }
        if (imageViews.size() != 0)
            vp.setAdapter(new ImageAdapter());
        initTop();
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
            if (!Utils.isGif(paths.get(position))) {
                ScaleImageView scaleImageView = (ScaleImageView) imageViews.get(position);
                scaleImageView.setOnGestureListener(new HandleSingleTap());
            }
            return imageViews.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(imageViews.get(position));
        }
    }

    private boolean isShow = true;

    private class HandleSingleTap implements ScaleImageView.OnGestureListener {
        @Override
        public void onSingleTapUp() {
            if (isShow) {
                isShow = false;
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mTopBar.animate().translationYBy(-mTopBar.getHeight()).setDuration(200).start();
                mBottomBar.animate().alpha(0).setDuration(200).start();
            } else {
                isShow = true;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mTopBar.animate().translationYBy(mTopBar.getHeight()).setDuration(200).start();
                mBottomBar.animate().alpha(0.8f).setDuration(200).start();
            }
            initTop();
        }
    }

    private void initTop() {
        int statusBarHeight = -1;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) vp.getLayoutParams();
        if (isShow)
            params.topMargin = -statusBarHeight;
        else
            params.topMargin = 0;
        vp.setLayoutParams(params);
    }

}
