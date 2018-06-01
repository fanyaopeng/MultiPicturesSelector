package com.fan.library.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;

import com.fan.library.R;
import com.fan.library.utils.Utils;
import com.fan.library.view.ClipShapeView;
import com.fan.library.view.ScaleImageView;

public class EditImageViewActivity extends Activity {
    private ScaleImageView img;
    String mPath;
    private ClipShapeView mShape;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_imageview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
    }

    private void initView() {
        img = findViewById(R.id.image);
        mShape = findViewById(R.id.shape);
        mShape.setScaleX(0.8f);
        mShape.setScaleY(0.8f);
        mPath = getIntent().getStringExtra("path");
        img.post(new Runnable() {
            @Override
            public void run() {
                img.setImageBitmap(Utils.compress(mPath, img.getWidth(), img.getHeight()));
            }
        });
        findViewById(R.id.img_clip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //img.rotate(-90);
                float cur = img.getScaleX();
                if (cur == 1.0f) {
                    img.animate().scaleX(0.8f).scaleY(0.8f).setDuration(200).start();
                    mShape.setVisibility(View.VISIBLE);
                    //img.animate().translationYBy(co)
                } else {
                    img.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    mShape.setVisibility(View.GONE);
                }
            }
        });
    }
}
