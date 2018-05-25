package com.fan.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

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
    private PicturesAdapter mPictureAdapter;
    private ExecutorService mService;
    public int mItemSize;
    private int mItemMargin;
    private List<String> mCheckPaths = new ArrayList<>();
    private TextView tvPreviewNum;
    private RecyclerView mDirList;
    private TextView mCurDir;
    private LinearLayout mShadow;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#333333"));
        }
        initView();
        init();
    }

    private void initView() {
        mList = findViewById(R.id.list);
        mDirList = findViewById(R.id.dir_list);
        mList.setLayoutManager(new GridLayoutManager(this, 4));
        mList.addItemDecoration(new Decoration());
        mService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        mItemMargin = Utils.dp2px(this, 0.5f);
        mItemSize = (getWidth() - mItemMargin * 5) / 4;
        mCurDir = findViewById(R.id.tv_type);
        tvPreviewNum = findViewById(R.id.tv_preview_num);
        mShadow = findViewById(R.id.shadow);
        mContainer = findViewById(R.id.rel_container);

        mShadow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow()) {
                    animHide();
                }
            }
        });
        findViewById(R.id.ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findViewById(R.id.tv_complete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra("paths", (ArrayList<String>) mCheckPaths);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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

    private RelativeLayout mContainer;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDirList.getLayoutParams();
        params.height = mContainer.getHeight() * 5 / 6;
    }

    public void SelectType(View view) {
        if (mDirList.getAdapter() == null) {
            mDirList.setLayoutManager(new LinearLayoutManager(MultiPicturesSelectorActivity.this));
            mDirList.setAdapter(new DirsAdapter());
            mDirList.setTranslationY(mDirList.getHeight());
        }
        if (mDirList.getTranslationY() == 0) {
            //目前显示中
            animHide();
        }
        if (mDirList.getTranslationY() == mDirList.getHeight()) {
            //现在隐藏中
            animEnter();
        }
    }

    @Override
    public void onBackPressed() {
        if (isShow()) {
            animHide();
            return;
        }
        super.onBackPressed();
    }

    private boolean isShow() {
        return mDirList.getTranslationY() == 0;
    }

    private void animEnter() {
        mShadow.setVisibility(View.VISIBLE);
        mDirList.animate().translationYBy(-mDirList.getHeight()).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        }).start();
    }

    private void animHide() {
        mDirList.animate().translationYBy(mDirList.getHeight()).setDuration(300).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mShadow.setVisibility(View.GONE);
            }
        }).start();
    }

    private class DirsAdapter extends RecyclerView.Adapter<DirsAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_item, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, final int position) {
            final String parentPath = mAllDirs.get(position);
            int index = parentPath.lastIndexOf(File.separator);
            holder.tvName.setText(parentPath.substring(index + 1));
            ImageView preview = holder.preview;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) preview.getLayoutParams();
            params.width = mItemSize;
            params.height = mItemSize;
            String prePath = getFirstPic(parentPath);
            mService.submit(new CompressTask(prePath, preview));
            int len[] = new int[1];
            getFileNum(parentPath, len);
            holder.tvNum.setText(len[0] + "张");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animHide();
                    String path = mAllDirs.get(position);
                    int index = path.lastIndexOf(File.separator);
                    TextView tv = mCurDir;
                    String selectPath = path.substring(index + 1);
                    if (tv.getText().equals(selectPath)) {
                        return;
                    }
                    tv.setText(selectPath);
                    mAllPictures.clear();
                    setSelectPictures(path);
                    mPictureAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAllDirs.size();
        }

        public class VH extends RecyclerView.ViewHolder {
            ImageView preview;
            TextView tvName;
            TextView tvNum;

            public VH(View itemView) {
                super(itemView);
                preview = itemView.findViewById(R.id.img_dir_preview);
                tvName = itemView.findViewById(R.id.tv_name);
                tvNum = itemView.findViewById(R.id.tv_num);
            }
        }
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

    private String traversalFile(String path) {
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (Utils.isPicture(file)) {
                    return file.getAbsolutePath();
                }
            } else {
                return traversalFile(file.getAbsolutePath());
            }
        }
        return null;
    }

    private String getFirstPic(String path) {
        File[] files = new File(path).listFiles();
        String result;
        for (File file : files) {
            if (file.isFile()) {
                if (Utils.isPicture(file)) {
                    return file.getAbsolutePath();
                }
            } else {
                result = traversalFile(file.getAbsolutePath());
                if (result != null) {
                    return result;
                }
            }
        }
        //遍历了所有的目录
        return null;
    }

    private void getFileNum(String path, int[] result) {
        File[] files = new File(path).listFiles();
        for (File file : files) {
            if (file.isFile()) {
                if (Utils.isPicture(file)) {
                    result[0]++;
                }
            } else {
                getFileNum(file.getAbsolutePath(), result);
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
                mPictureAdapter = new PicturesAdapter();
                mList.setAdapter(mPictureAdapter);


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
