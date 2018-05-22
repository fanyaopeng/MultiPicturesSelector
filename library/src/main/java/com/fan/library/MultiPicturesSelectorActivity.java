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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private int mItemMargin;

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
        mItemMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
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
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            ImageView img = holder.img;
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.root.getLayoutParams();
            params.width = mItemSize;
            params.height = mItemSize;
            params.topMargin = mItemMargin;
            if ((position + 1) % 3 == 1) {
                params.leftMargin = mItemMargin;
            } else if ((position + 1) % 3 == 2) {
                params.leftMargin = mItemMargin;
                params.rightMargin = mItemMargin;
            } else {
                params.rightMargin = mItemMargin;
            }
            Future<Bitmap> future = mService.submit(new CompressTask(mAllPictures.get(position)));
            try {
                img.setImageBitmap(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mAllPictures.size();
        }

        public class VH extends RecyclerView.ViewHolder {
            ImageView img;
            CheckBox ck;
            FrameLayout root;

            public VH(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.image);
                ck = itemView.findViewById(R.id.ck);
                root = itemView.findViewById(R.id.root);
            }
        }
    }

    private class CompressTask implements Callable<Bitmap> {
        private String path;

        public CompressTask(String path) {
            this.path = path;
        }

        @Override
        public Bitmap call() throws Exception {
            return compress(path);
        }
    }

    private Bitmap compress(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int sample = Math.max(options.outWidth / mItemSize, options.outHeight / mItemSize);
        options.inSampleSize = sample;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}
