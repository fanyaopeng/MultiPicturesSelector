package com.fan.library.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.fan.library.R;
import com.fan.library.utils.Utils;
import com.fan.library.view.ScaleImageView;

public class EditImageViewActivity extends Activity {
    private ScaleImageView img;
    String mPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_imageview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        img = findViewById(R.id.image);
        mPath = getIntent().getStringExtra("path");

        findViewById(R.id.rotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img.rotate(-90);
            }
        });
    }

    private boolean isInit;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isInit) {
            img.setImageBitmap(Utils.compress(mPath, img.getWidth(), img.getHeight()));
            isInit = true;
        }
    }
}
