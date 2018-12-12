package com.fan.multipicturesselector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.fan.MultiImageSelector.utils.Config;
import com.fan.MultiImageSelector.activity.MultiPicturesSelectorActivity;
import com.fan.MultiImageSelector.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ViewPager vp;
    private List<String> paths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vp = findViewById(R.id.vp);
    }

    private void config() {
        Config config = Config.get();
        EditText max = findViewById(R.id.et_max);
        EditText min = findViewById(R.id.et_min);
        if (max.length() != 0) {
            config.setMaxNum(Integer.parseInt(max.getText().toString()));
        }
        if (min.length() != 0) {
            config.setMinMum(Integer.parseInt(min.getText().toString()));
        }
        CheckBox camera = findViewById(R.id.ck_open_camera);
        config.isOpenCamera = camera.isChecked();
        CheckBox ratio = findViewById(R.id.ck_open_ratio);
        EditText etRatio = findViewById(R.id.et_ratio);
        if (ratio.isChecked()) {
            config.setRatio(Float.parseFloat(etRatio.getText().toString()));
        }
    }

    public void start(View view) {
        config();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission}, 0);
            } else {
                startActivityForResult(new Intent(this, MultiPicturesSelectorActivity.class), 0);
            }
        } else {
            startActivityForResult(new Intent(this, MultiPicturesSelectorActivity.class), 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(this, MultiPicturesSelectorActivity.class), 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (paths != null) paths.clear();
            paths = data.getStringArrayListExtra("paths");
            imageViews.clear();
            for (String p : paths) {
                ImageView imageView = new ImageView(MainActivity.this);
                Bitmap result = Utils.compress(p, MainActivity.this.getResources().getDisplayMetrics().widthPixels, vp.getHeight());
                imageView.setImageBitmap(result);
                imageViews.add(imageView);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            }
            if (imageViews.size() != 0)
                vp.setAdapter(new ImageAdapter());
        }
    }


    private List<ImageView> imageViews = new ArrayList<>();

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
