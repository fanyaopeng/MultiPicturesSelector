package com.fan.library;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huisoucw on 2018/5/22.
 */

public class MultiPicturesSelectorActivity extends Activity {
    private List<String> mAllPictures = new ArrayList<>();
    private List<String> mAllDirs = new ArrayList<>();
    private RecyclerView mList;
    private PicturesAdapter mAdapter;
    private ExecutorService mService;
    private int mItemSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_pictures_selector);
        init();
        mList = findViewById(R.id.list);
        mList.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new PicturesAdapter();
        mList.setAdapter(mAdapter);
        mService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mItemSize = getResources().getDisplayMetrics().widthPixels / 3;
    }

    private void init() {
        Cursor cursor = getContentResolver().
                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        while (cursor.moveToNext()) {
            mAllPictures.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
        }
        cursor.close();
    }

    private class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, null));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {

        }

        @Override
        public int getItemCount() {
            return mAllPictures.size();
        }

        public class VH extends RecyclerView.ViewHolder {

            public VH(View itemView) {
                super(itemView);
            }
        }
    }

    private class CompressTask implements Runnable {

        @Override
        public void run() {

        }
    }

    private Bitmap compress(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap src = BitmapFactory.decodeFile(path);
        return null;
    }
}
