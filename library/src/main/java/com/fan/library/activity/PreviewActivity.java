package com.fan.library.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fan.library.R;
import com.fan.library.utils.Utils;
import com.fan.library.view.EditImageView;
import com.fan.library.view.GifImageView;
import com.fan.library.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;

public class PreviewActivity extends Activity {
    private ViewPager vp;
    private List<String> paths;
    private RelativeLayout mTopBar;
    private RelativeLayout mBottomBar;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        paths = getIntent().getStringArrayListExtra("paths");
        initView();
        vp.post(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void initView() {
        vp = findViewById(R.id.vp);
        mTopBar = findViewById(R.id.top_bar);
        mBottomBar = findViewById(R.id.bottom_bar);
        mBottomBar.setAlpha(0.8f);
        tvTitle = findViewById(R.id.title);
        findViewById(R.id.tv_edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PreviewActivity.this, EditImageViewActivity.class);
                intent.putExtra("path", paths.get(vp.getCurrentItem()));
                startActivityForResult(intent, requestEdit);
            }
        });
    }

    private int requestEdit = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestEdit) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                paths.set(vp.getCurrentItem(), path);
                ScaleImageView image = (ScaleImageView) imageViews.get(vp.getCurrentItem());
                Bitmap result = Utils.compress(path, vp.getWidth(), vp.getHeight());
                image.setImageBitmap(result);
                vp.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void init() {
        imageViews.clear();
        for (String p : paths) {
            if (!Utils.isGif(p)) {
                ScaleImageView imageView;
//                if (Utils.isLongImage(PreviewActivity.this, p)) {
//                    // Bitmap result = Utils.compress(p, vp.getWidth(), Integer.MAX_VALUE);
//                    imageView = new LargeScaleImageView(this);
//                    LargeScaleImageView img = (LargeScaleImageView) imageView;
//                    img.setImagePath(p);
//                } else {
//                    imageView = new ScaleImageView(this);
//                    Bitmap result = Utils.compress(p, vp.getWidth(), vp.getHeight());
//                    imageView.setImageBitmap(result);
//                }
                imageView = new ScaleImageView(this);
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
        tvTitle.setText(vp.getCurrentItem() + 1 + "/" + paths.size());
        initTop();
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvTitle.setText(position + 1 + "/" + paths.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
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
