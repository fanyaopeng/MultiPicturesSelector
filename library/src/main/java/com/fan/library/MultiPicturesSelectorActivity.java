package com.fan.library;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.io.File;
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
    public int mItemSize;
    private int mItemMargin;
    private List<String> mCheckPaths = new ArrayList<>();
    private TextView tvPreviewNum;
    private LruCache<String, Bitmap> cache = new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_pictures_selector);
        mList = findViewById(R.id.list);
        mList.setLayoutManager(new GridLayoutManager(this, 4));
        mList.addItemDecoration(new Decoration());
        mService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mItemMargin = Utils.dp2px(this, 0.5f);
        mItemSize = (getWidth() - mItemMargin * 5) / 4;
        init();
        tvPreviewNum = findViewById(R.id.tv_preview_num);
    }

    private int getWidth() {
        Point point = new Point();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(point);
        return point.x;
    }

    private void init() {
        mService.submit(new ReadTask());
    }

    private SelectDialog mPop;

    public void SelectType(final View view) {
        if (mPop == null) mPop = new SelectDialog(this, mAllDirs);
        mPop.setOnItemClickListener(new SelectDialog.OnItemClickListener() {
            @Override
            public void onItemClick(String path) {
                int index = path.lastIndexOf(File.separator);
                TextView tv = (TextView) view;
                String selectPath = path.substring(index + 1);
                if (tv.getText().equals(selectPath)) {
                    return;
                }
                tv.setText(selectPath);
                mAllPictures.clear();
                setSelectPictures(path);
                mAdapter.notifyDataSetChanged();
            }
        });
        mPop.showAtLocation(findViewById(R.id.bottom), Gravity.BOTTOM, 0, Utils.dp2px(this, 44));
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.3f;
        getWindow().setAttributes(params);
        mPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1;
                getWindow().setAttributes(params);
            }
        });
    }

    private void setSelectPictures(String path) {
        File files[] = new File(path).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (Utils.isPicture(file)) {
                    mAllPictures.add(file.getAbsolutePath());
                }
            } else {
                setSelectPictures(file.getAbsolutePath());
            }
        }
    }

    private class ReadTask implements Runnable {
        @Override
        public void run() {
            Cursor cursor = getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            while (cursor.moveToNext()) {
                mAllPictures.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            }
            cursor.close();
            for (String path : mAllPictures) {
                int end = path.lastIndexOf(File.separator);
                String dir = path.substring(0, end);
                if (!mAllDirs.contains(dir)) {
                    mAllDirs.add(dir);
                }
            }
            handler.sendEmptyMessage(1);
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                mAdapter = new PicturesAdapter();
                mList.setAdapter(mAdapter);
                return true;
            }
            return false;
        }
    });

    private class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, final int position) {
            ImageView img = holder.img;
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.root.getLayoutParams();
            params.width = mItemSize;
            params.height = mItemSize;
            mService.submit(new CompressTask(mAllPictures.get(position), img));
            holder.ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mCheckPaths.add(mAllPictures.get(position));
                    } else {
                        mCheckPaths.remove(mAllPictures.get(position));
                    }
                    tvPreviewNum.setText("(" + mCheckPaths.size() + ")");
                }
            });
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


    private class CompressTask implements Runnable {
        private String path;
        private ImageView img;

        public CompressTask(String path, ImageView img) {
            this.path = path;
            this.img = img;
        }

        @Override
        public void run() {
            int index = path.lastIndexOf(".");
            String key = path.substring(0, index);
            Bitmap bitmap = cache.get(key);
            if (bitmap == null) {
                bitmap = compress(path);
                cache.put(key, bitmap);
            }
            final Bitmap finalBitmap = bitmap;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img.setImageBitmap(finalBitmap);
                }
            });
        }
    }

    private Bitmap compress(String path) {
        return Utils.compress(path, mItemSize, mItemSize);
    }

    private class Decoration extends RecyclerView.ItemDecoration {
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, mItemMargin, mItemMargin);
        }
    }
}
