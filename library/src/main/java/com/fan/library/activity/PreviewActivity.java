package com.fan.library.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fan.library.R;
import com.fan.library.utils.Config;
import com.fan.library.utils.DisplayImageTask;
import com.fan.library.utils.Utils;
import com.fan.library.view.CheckImageView;
import com.fan.library.view.GifImageView;
import com.fan.library.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 已知  编辑后  原来的图片 不会消失
 * 已知  剪切的时候的  经过缩放后 越界处理 不正确
 */
public class PreviewActivity extends Activity {
    private ViewPager mPreviewVp;
    private List<String> paths;
    private RelativeLayout mTopBar;
    private LinearLayout mBottomBar;
    private TextView tvTitle;
    private RecyclerView mThumbList;
    private TextView tvEdit;
    private List<String> mCheckPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        paths = getIntent().getStringArrayListExtra("paths");
        mCheckPath = new ArrayList<>();
        mCheckPath.addAll(paths);
        initView();
        initThumb();
        mPreviewVp.post(new Runnable() {
            @Override
            public void run() {
                initPreview();
            }
        });
    }

    private void initView() {
        mPreviewVp = findViewById(R.id.vp_preview);
        mTopBar = findViewById(R.id.top_bar);
        mBottomBar = findViewById(R.id.bottom_bar);
        mBottomBar.setAlpha(0.8f);
        mThumbList = findViewById(R.id.thumb_list);
        tvTitle = findViewById(R.id.title);
        tvEdit = findViewById(R.id.tv_edit);
        Config config = Config.get();
        if (!config.isOpenClip && !config.isOpenEdit) {
            mBottomBar.setVisibility(View.GONE);
        }
    }

    public void complete(View view) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("paths", (ArrayList<String>) mCheckPath);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void edit(View view) {
        Intent intent = new Intent(PreviewActivity.this, EditImageViewActivity.class);
        intent.putExtra("path", paths.get(mPreviewVp.getCurrentItem()));
        startActivityForResult(intent, requestEdit);
    }

    private int requestEdit = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestEdit) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                paths.set(mPreviewVp.getCurrentItem(), path);
                ScaleImageView image = (ScaleImageView) mPreViewImages.get(mPreviewVp.getCurrentItem());
                Bitmap result = Utils.compress(path, mPreviewVp.getWidth(), mPreviewVp.getHeight());
                image.setImageBitmap(result);
                //mPreViewImages.set(mPreviewVp.getCurrentItem(), image);
                mPreviewVp.getAdapter().notifyDataSetChanged();
                mThumbList.getAdapter().notifyDataSetChanged();
            }
        }
    }

    private void initPreview() {
        mPreViewImages.clear();
        for (String p : paths) {
            if (!Utils.isGif(p)) {
                ScaleImageView imageView = new ScaleImageView(this);
                Bitmap result = Utils.compress(p, mPreviewVp.getWidth(), mPreviewVp.getHeight());
                imageView.setImageBitmap(result);
                mPreViewImages.add(imageView);
            } else {
                GifImageView gifImageView = new GifImageView(this);
                gifImageView.setResource(p);
                mPreViewImages.add(gifImageView);
            }
        }
        if (mPreViewImages.size() != 0)
            mPreviewVp.setAdapter(new ImageAdapter());
        tvTitle.setText(mPreviewVp.getCurrentItem() + 1 + "/" + paths.size());
        initTop();
        mPreviewVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                tvTitle.setText(position + 1 + "/" + paths.size());
                ThumbAdapter adapter = (ThumbAdapter) mThumbList.getAdapter();
                adapter.setSelectPos(position);
                if (Utils.isGif(paths.get(position))) {
                    tvEdit.setVisibility(View.GONE);
                } else {
                    tvEdit.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void onCheckClick(View view) {
        CheckImageView ck = (CheckImageView) ((ViewGroup) view).getChildAt(0);
        ck.toggle();
        if (!ck.isChecked()) {
            mCheckPath.remove(mPreviewVp.getCurrentItem());
        } else {
            if (!mCheckPath.contains(paths.get(mPreviewVp.getCurrentItem()))) {
                mCheckPath.add(paths.get(mPreviewVp.getCurrentItem()));
            }
        }
        ThumbAdapter adapter = (ThumbAdapter) mThumbList.getAdapter();
        adapter.setUncheckPos(mPreviewVp.getCurrentItem());
    }

    private List<View> mPreViewImages = new ArrayList<>();

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
            container.addView(mPreViewImages.get(position));
            if (!Utils.isGif(paths.get(position))) {
                ScaleImageView scaleImageView = (ScaleImageView) mPreViewImages.get(position);
                scaleImageView.setOnGestureListener(new HandleSingleTap());
            }
            return mPreViewImages.get(position);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mPreViewImages.get(position));
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }


    private void initThumb() {
        mThumbList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mThumbList.setAdapter(new ThumbAdapter());
    }


    private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private class ThumbAdapter extends RecyclerView.Adapter<ThumbAdapter.VH> {
        private int mSelectPos;
        private int mUncheckedPos = -1;

        public void setSelectPos(int pos) {
            mSelectPos = pos;
            notifyDataSetChanged();
        }

        public void setUncheckPos(int pos) {
            mUncheckedPos = pos;
            notifyItemChanged(pos);
        }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_thumb_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            int size = Utils.dp2px(PreviewActivity.this, 70);
            service.execute(new DisplayImageTask(PreviewActivity.this, paths.get(position), holder.image, size, size));
            final RelativeLayout root = (RelativeLayout) holder.itemView;
            if (position == mSelectPos) holder.border.setVisibility(View.VISIBLE);
            else holder.border.setVisibility(View.INVISIBLE);
            if (position == mUncheckedPos) holder.shadow.setVisibility(View.VISIBLE);
            else holder.shadow.setVisibility(View.INVISIBLE);
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getAdapterPosition();
                    mPreviewVp.setCurrentItem(pos);
                }
            });
        }

        @Override
        public int getItemCount() {
            return paths.size();
        }

        class VH extends RecyclerView.ViewHolder {
            ImageView image;
            View border;
            View shadow;

            public VH(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
                border = itemView.findViewById(R.id.border);
                shadow = itemView.findViewById(R.id.shadow);
            }
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
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mPreviewVp.getLayoutParams();
        if (isShow)
            params.topMargin = -statusBarHeight;
        else
            params.topMargin = 0;
        mPreviewVp.setLayoutParams(params);
    }

}
