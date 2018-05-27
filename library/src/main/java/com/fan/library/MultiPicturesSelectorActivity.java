package com.fan.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huisoucw on 2018/5/22.
 */

public class MultiPicturesSelectorActivity extends Activity {
    private List<ImageInfo> mAllPictures = new ArrayList<>();
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
    private List<ImageInfo> mSelectDirsPictures = new ArrayList<>();

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
        mItemMargin = Utils.dp2px(this, 2);
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
        findViewById(R.id.ll_preview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MultiPicturesSelectorActivity.this, PreviewActivity.class);
                intent.putStringArrayListExtra("paths", (ArrayList<String>) mCheckPaths);
                startActivity(intent);
            }
        });
        findViewById(R.id.tv_type).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectType();
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


    private void SelectType() {

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
        private int size;

        public DirsAdapter() {
            size = Utils.dp2px(MultiPicturesSelectorActivity.this, 80);
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_item, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return -1;
            }
            return super.getItemViewType(position);
        }

        @Override
        public void onBindViewHolder(final VH holder, final int position) {
            if (holder.getItemViewType() == -1) {
                holder.tvName.setText(type_all);
                holder.tvNum.setText(mAllPictures.size() + "张");

                ImageView preview = holder.preview;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) preview.getLayoutParams();
                params.width = size;
                params.height = size;
                mService.submit(new DisplayImageTask(MultiPicturesSelectorActivity.this,
                        mAllPictures.get(0).getPath(), preview, size, size));
            } else {
                String parentPath = mAllDirs.get(position);
                int index = parentPath.lastIndexOf(File.separator);
                holder.tvName.setText(parentPath.substring(index + 1));
                ImageView preview = holder.preview;
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) preview.getLayoutParams();
                params.width = size;
                params.height = size;
                String prePath = getFirstPic(parentPath);
                mService.submit(new DisplayImageTask(MultiPicturesSelectorActivity.this,
                        prePath, preview, size, size));
                int len[] = new int[1];
                getFileNum(parentPath, len);
                holder.tvNum.setText(len[0] + "张");
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animHide();
                    TextView tv = mCurDir;
                    if (holder.getItemViewType() == -1) {
                        if (tv.getText().equals(type_all)) return;
                        tv.setText(type_all);
                        mSelectDirsPictures.clear();
                        mSelectDirsPictures.addAll(mAllPictures);
                        mPictureAdapter.notifyDataSetChanged();
                    } else {
                        String path = mAllDirs.get(position);
                        int index = path.lastIndexOf(File.separator);
                        String selectPath = path.substring(index + 1);
                        if (tv.getText().equals(selectPath)) {
                            return;
                        }
                        tv.setText(selectPath);
                        mSelectDirsPictures.clear();
                        setSelectPictures(path);
                        mPictureAdapter.notifyDataSetChanged();
                    }
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
                    String p = file.getAbsolutePath();
                    for (ImageInfo info : mAllPictures) {
                        if (p.equals(info.getPath())) {
                            ImageInfo item = new ImageInfo(info.getDate(), p);
                            mSelectDirsPictures.add(item);
                        }
                    }
                }
            } else {
                setSelectPictures(file.getAbsolutePath());
            }
        }
        ImageInfo tempArray[] = new ImageInfo[mSelectDirsPictures.size()];
        mSelectDirsPictures.toArray(tempArray);
        Arrays.sort(tempArray, new Comparator<ImageInfo>() {
            @Override
            public int compare(ImageInfo o1, ImageInfo o2) {
                return o2.getDate() - o1.getDate();
            }
        });
        mSelectDirsPictures.clear();
        mSelectDirsPictures.addAll(Arrays.asList(tempArray));
    }

    private String getFirstPic(String path) {

        File files[] = new File(path).listFiles();
        List<ImageInfo> temp = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                if (Utils.isPicture(file)) {
                    String p = file.getAbsolutePath();
                    for (ImageInfo info : mAllPictures) {
                        if (p.equals(info.getPath())) {
                            temp.add(info);
                            break;
                        }
                    }
                }
            } else {
                setSelectPictures(file.getAbsolutePath());
            }
        }
        // 1 2  3  4  5  6  7  8  9  10
        //  3  5 6 2  7
//        int pos = temp.size();
//        for (int i = 0; i < temp.size(); i++) {
//            for (int j = 0; j < mAllPictures.size(); j++) {
//                if (temp.get(i).equals(mAllPictures.get(j))) {
//                    if (i < pos) {
//                        pos = i;
//                    }
//                }
//            }
//        }
        ImageInfo tempArray[] = new ImageInfo[temp.size()];
        temp.toArray(tempArray);
        Arrays.sort(tempArray, new Comparator<ImageInfo>() {
            @Override
            public int compare(ImageInfo o1, ImageInfo o2) {
                return o2.getDate() - o1.getDate();
            }
        });
        return tempArray[0].getPath();
    }


//    private String traversalFile(String path) {
//        File[] files = new File(path).listFiles();
//        for (File file : files) {
//            if (file.isFile()) {
//                if (Utils.isPicture(file)) {
//                    return file.getAbsolutePath();
//                }
//            } else {
//                return traversalFile(file.getAbsolutePath());
//            }
//        }
//        return null;
//    }
//
//    private String getFirstPic(String path) {
//        File[] files = new File(path).listFiles();
//        String result;
//        for (File file : files) {
//            if (file.isFile()) {
//                if (Utils.isPicture(file)) {
//                    return file.getAbsolutePath();
//                }
//            } else {
//                result = traversalFile(file.getAbsolutePath());
//                if (result != null) {
//                    return result;
//                }
//            }
//        }
//        //遍历了所有的目录
//        return null;
//    }

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
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA,
                                    MediaStore.Images.Media.DATE_ADDED},
                            null, null,
                            MediaStore.Images.Media.DATE_ADDED + " desc");
            while (cursor.moveToNext()) {
                ImageInfo info = new ImageInfo(cursor.getInt(1), cursor.getString(0));
                mAllPictures.add(info);
            }
            cursor.close();
            mAllDirs.add(type_all);
            for (ImageInfo info : mAllPictures) {
                String path = info.getPath();
                int end = path.lastIndexOf(File.separator);
                String dir = path.substring(0, end);
                if (!mAllDirs.contains(dir)) {
                    mAllDirs.add(dir);
                }
            }

            handler.sendEmptyMessage(1);
        }
    }

    private final String type_all = "所有图片";
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                if (mAllPictures.size() == 0) {
                    Toast.makeText(MultiPicturesSelectorActivity.this, "您还没有图片", Toast.LENGTH_SHORT).show();
                    return true;
                }
                mSelectDirsPictures.addAll(mAllPictures);
                mPictureAdapter = new PicturesAdapter();
                mList.setAdapter(mPictureAdapter);

                mDirList.setLayoutManager(new LinearLayoutManager(MultiPicturesSelectorActivity.this));
                mDirList.setAdapter(new DirsAdapter());
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mDirList.getLayoutParams();
                int mTranslateSize = mContainer.getHeight() * 5 / 6;
                params.height = mTranslateSize;
                mDirList.setLayoutParams(params);
                mDirList.setTranslationY(mTranslateSize);
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
        public void onBindViewHolder(final VH holder, final int position) {
            ImageView img = holder.img;
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.root.getLayoutParams();
            params.width = mItemSize;
            params.height = mItemSize;
            final String path = mSelectDirsPictures.get(position).getPath();
            mService.submit(new DisplayImageTask(MultiPicturesSelectorActivity.this,
                    path, img, mItemSize, mItemSize));
            holder.tvImageType.setVisibility(Utils.isGif(path) ? View.VISIBLE : View.GONE);
            holder.ck.setChecked(mCheckPaths.contains(path));
            holder.ck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked && !mCheckPaths.contains(path)) {
                        mCheckPaths.add(path);
                        holder.img.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
                    } else {
                        mCheckPaths.remove(path);
                        holder.img.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                    }
                    tvPreviewNum.setText("(" + mCheckPaths.size() + ")");
                }
            });
        }

        @Override
        public int getItemCount() {
            return mSelectDirsPictures.size();
        }

        public class VH extends RecyclerView.ViewHolder {
            ImageView img;
            CheckBox ck;
            FrameLayout root;
            TextView tvImageType;

            public VH(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.image);
                ck = itemView.findViewById(R.id.ck);
                root = itemView.findViewById(R.id.root);
                tvImageType = itemView.findViewById(R.id.tv_image_type);
            }
        }
    }

    private class Decoration extends RecyclerView.ItemDecoration {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            c.drawColor(Color.parseColor("#000000"));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, mItemMargin, mItemMargin);
        }
    }
}
