package com.fan.library.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fan.library.utils.DisplayImageTask;
import com.fan.library.R;
import com.fan.library.utils.Utils;
import com.fan.library.info.Folder;
import com.fan.library.info.ImageInfo;
import com.fan.library.view.CheckImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by huisoucw on 2018/5/22.
 */

public class MultiPicturesSelectorActivity extends Activity {
    private List<ImageInfo> mAllPictures = new ArrayList<>();
    private List<Folder> mAllDirs = new ArrayList<>();
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
    private TextView tvCurDir;
    private int mMaxNum = 9;
    private TextView tvComplete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_pictures_selector);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
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
        tvComplete = findViewById(R.id.tv_complete);

        tvComplete.setOnClickListener(new View.OnClickListener() {
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
                if (mCheckPaths.size() == 0) return;
                Intent intent = new Intent(MultiPicturesSelectorActivity.this, PreviewActivity.class);
                intent.putStringArrayListExtra("paths", (ArrayList<String>) mCheckPaths);
                startActivity(intent);
            }
        });
        tvCurDir = findViewById(R.id.tv_type);
        tvCurDir.setOnClickListener(new View.OnClickListener() {
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
            ImageView preview = holder.preview;
            FrameLayout imgParent = (FrameLayout) preview.getParent();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imgParent.getLayoutParams();
            params.width = size;
            params.height = size;
            if (holder.getItemViewType() == -1) {
                holder.tvName.setText(type_all);
                holder.tvNum.setText(mAllPictures.size() + "张");
                mService.submit(new DisplayImageTask(MultiPicturesSelectorActivity.this,
                        mAllPictures.get(0).getPath(), preview, size, size));
            } else {
                String parentPath = mAllDirs.get(position).getPath();
                int index = parentPath.lastIndexOf(File.separator);
                holder.tvName.setText(parentPath.substring(index + 1));
                mService.submit(new DisplayImageTask(MultiPicturesSelectorActivity.this,
                        mAllDirs.get(position).getImageInfos().get(0).getPath(), preview, size, size));
                holder.tvNum.setText(mAllDirs.get(position).getImageInfos().size() + "张");
            }
            if (tvCurDir.getText().equals(type_all)) {
                holder.imgIndicator.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            } else {
                holder.imgIndicator.setVisibility(mAllDirs.get(position).getPath().endsWith(tvCurDir.getText().toString()) ? View.VISIBLE : View.GONE);
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
                        String path = mAllDirs.get(position).getPath();
                        int index = path.lastIndexOf(File.separator);
                        String selectPath = path.substring(index + 1);
                        if (tv.getText().equals(selectPath)) {
                            return;
                        }
                        tv.setText(selectPath);
                        mSelectDirsPictures.clear();
                        mSelectDirsPictures.addAll(mAllDirs.get(position).getImageInfos());
                        mPictureAdapter.notifyDataSetChanged();
                    }
                    notifyDataSetChanged();
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
            ImageView imgIndicator;

            public VH(View itemView) {
                super(itemView);
                preview = itemView.findViewById(R.id.img_dir_preview);
                tvName = itemView.findViewById(R.id.tv_name);
                tvNum = itemView.findViewById(R.id.tv_num);
                imgIndicator = itemView.findViewById(R.id.img_indicator);
            }
        }
    }

    //过滤掉重复的folder
    private Folder getFolder(String path) {
        for (Folder folder : mAllDirs) {
            if (folder.getPath().equals(path)) {
                return folder;
            }
        }
        return null;
    }

    private class ReadTask implements Runnable {
        @Override
        public void run() {
            Cursor cursor = getContentResolver().
                    query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media.DATA,
                                    MediaStore.Images.Media.DATE_ADDED},
                            null, null,
                            MediaStore.Images.Media.DATE_ADDED + " desc");
            mAllDirs.add(new Folder("", null, null));
            while (cursor.moveToNext()) {
                int date = cursor.getInt(1);
                String path = cursor.getString(0);
                ImageInfo info = new ImageInfo(date, path);
                mAllPictures.add(info);

                String dir = new File(path).getParentFile().getAbsolutePath();
                Folder f = getFolder(dir);
                if (f == null) {
                    List<ImageInfo> folderImage = new ArrayList<>();
                    folderImage.add(info);
                    Folder folder = new Folder(dir, info, folderImage);
                    mAllDirs.add(folder);
                } else {
                    f.getImageInfos().add(info);
                }
            }
            cursor.close();
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
                int mTranslateSize = mContainer.getHeight() * 7 / 8;
                params.height = mTranslateSize;
                mDirList.setLayoutParams(params);
                mDirList.setTranslationY(mTranslateSize);
                return true;
            }
            return false;
        }
    });

    private class PicturesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int TYPE_CAMERA = 5000;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_CAMERA) {
                return new CameraVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camera, parent, false));
            }
            return new PictureVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false));
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_CAMERA;
            }
            return super.getItemViewType(position);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder vh, final int position) {

            if (vh.getItemViewType() == TYPE_CAMERA) {
                CameraVH holder = (CameraVH) vh;
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.cameraRoot.getLayoutParams();
                params.width = mItemSize;
                params.height = mItemSize;
                holder.camera.setImageResource(R.mipmap.ic_camera);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (MultiPicturesSelectorActivity.this.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                startCamera();
                            } else {
                                requestPermissions(new String[]{Manifest.permission.CAMERA}, 0);
                            }
                        } else {
                            startCamera();
                        }
                    }
                });
            } else {
                final PictureVH holder = (PictureVH) vh;
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.root.getLayoutParams();
                params.width = mItemSize;
                params.height = mItemSize;
                ImageView img = holder.img;
                final String path = mSelectDirsPictures.get(position - 1).getPath();
                mService.submit(new DisplayImageTask(MultiPicturesSelectorActivity.this,
                        path, img, mItemSize, mItemSize));
                holder.tvImageType.setVisibility(Utils.isGif(path) ? View.VISIBLE : View.GONE);
                holder.ck.setChecked(mCheckPaths.contains(path));
                holder.shadow.setVisibility(holder.ck.isChecked() ? View.VISIBLE : View.GONE);
                holder.ckParent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FrameLayout parent = (FrameLayout) v;
                        CheckImageView view = (CheckImageView) parent.getChildAt(0);
                        boolean isChecked = view.isChecked();
                        view.setChecked(!isChecked);
                        if (view.isChecked() && !mCheckPaths.contains(path)) {

                            if (mCheckPaths.size() == mMaxNum) {
                                Toast.makeText(MultiPicturesSelectorActivity.this, "你最多只能选择" + mMaxNum + "张图片", Toast.LENGTH_SHORT).show();
                                view.setChecked(false);
                                return;
                            }
                            holder.shadow.setVisibility(View.VISIBLE);
                            mCheckPaths.add(path);
                        } else {
                            holder.shadow.setVisibility(View.GONE);
                            mCheckPaths.remove(path);
                        }
                        if (mCheckPaths.size() != 0) {
                            tvComplete.setText("完成(" + mCheckPaths.size() + "/" + mMaxNum + ")");
                        } else {
                            tvComplete.setText("完成");
                        }
                        tvPreviewNum.setText("(" + mCheckPaths.size() + ")");
                    }
                });
            }
        }


        @Override
        public int getItemCount() {
            return mSelectDirsPictures.size();
        }

        public class CameraVH extends RecyclerView.ViewHolder {
            FrameLayout cameraRoot;
            ImageView camera;

            public CameraVH(View itemView) {
                super(itemView);
                cameraRoot = itemView.findViewById(R.id.root);
                camera = itemView.findViewById(R.id.ic_camera);
            }
        }

        public class PictureVH extends RecyclerView.ViewHolder {
            ImageView img;
            CheckImageView ck;
            FrameLayout root;
            TextView tvImageType;
            FrameLayout shadow;
            FrameLayout ckParent;

            public PictureVH(View itemView) {
                super(itemView);

                img = itemView.findViewById(R.id.image);
                ck = itemView.findViewById(R.id.ck);
                root = itemView.findViewById(R.id.root);
                tvImageType = itemView.findViewById(R.id.tv_image_type);
                shadow = itemView.findViewById(R.id.shadow);
                ckParent = itemView.findViewById(R.id.ck_parent);
            }
        }
    }

    private String cameraPath;

    private void startCamera() {
        cameraPath = getExternalCacheDir() + File.separator + System.currentTimeMillis() + ".png";
        File target = new File(cameraPath);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(MultiPicturesSelectorActivity.this,
                    getPackageName(), target));
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(target));
        }
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            mCheckPaths.add(cameraPath);
            tvComplete.performClick();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            }
        }
    }

    private class Decoration extends RecyclerView.ItemDecoration {
        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            super.onDraw(c, parent, state);
            c.drawColor(Color.BLACK);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(0, 0, mItemMargin, mItemMargin);
        }
    }
}
