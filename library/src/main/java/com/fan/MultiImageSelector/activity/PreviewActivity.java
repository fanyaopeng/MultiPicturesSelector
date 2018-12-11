package com.fan.MultiImageSelector.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.fan.MultiImageSelector.R;
import com.fan.MultiImageSelector.utils.CompressImageTask;
import com.fan.MultiImageSelector.utils.Utils;
import com.fan.MultiImageSelector.view.CheckImageView;
import com.fan.MultiImageSelector.view.GifImageView;
import com.fan.MultiImageSelector.view.ScaleImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PreviewActivity extends Activity {
    private ViewPager mPreviewVp;
    private List<String> paths;
    private RelativeLayout mTopBar;
    private LinearLayout mBottomBar;
    private TextView tvTitle;
    private RecyclerView mThumbList;
    private List<Boolean> mCheckPath;
    private CheckImageView mCheckView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar));
        }
        paths = getIntent().getStringArrayListExtra("paths");
        mCheckPath = new ArrayList<>();
        for (String s : paths) {
            mCheckPath.add(true);
        }
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
        mCheckView = findViewById(R.id.ck);
        mCheckView.setChecked(true);
    }

    public void edit(View view) {
        Intent intent = new Intent(this, EditImageViewActivity.class);
        intent.putExtra("path", paths.get(mPreviewVp.getCurrentItem()));
        startActivityForResult(intent, requestEdit);
    }

    public void complete(View view) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < mCheckPath.size(); i++) {
            if (mCheckPath.get(i)) {
                result.add(paths.get(i));
            }
        }
        Intent intent = new Intent();
        intent.putStringArrayListExtra("paths", (ArrayList<String>) result);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int requestEdit = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestEdit) {
            if (resultCode == RESULT_OK) {
                String path = data.getStringExtra("path");
                paths.set(mPreviewVp.getCurrentItem(), path);
                ScaleImageView image = (ScaleImageView) mPreviewVp.getChildAt(mPreviewVp.getCurrentItem());
                Bitmap result = Utils.compress(path, mPreviewVp.getWidth(), mPreviewVp.getHeight());
                image.setImageBitmap(result);
                mPreviewVp.getAdapter().notifyDataSetChanged();
                mThumbList.getAdapter().notifyItemChanged(mPreviewVp.getCurrentItem());
            }
        }
    }

    private void initPreview() {
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
                mCheckView.setChecked(mCheckPath.get(position));
                mThumbList.scrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void onCheckClick(View view) {
        CheckImageView ck = (CheckImageView) ((ViewGroup) view).getChildAt(0);
        ck.toggle();
        mCheckPath.set(mPreviewVp.getCurrentItem(), ck.isChecked());
        ThumbAdapter adapter = (ThumbAdapter) mThumbList.getAdapter();
        adapter.notifyItemChanged(mPreviewVp.getCurrentItem());
    }

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
            String p = paths.get(position);
            View result;
            if (!Utils.isGif(p)) {
                final ScaleImageView imageView = new ScaleImageView(PreviewActivity.this);

                int compressWidth = mPreviewVp.getWidth();
                int compressHeight = mPreviewVp.getHeight();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(p, options);
                if (Utils.isLongImage(options.outWidth, options.outHeight)) {
                    compressHeight = options.outHeight;
                }
                service.execute(new CompressImageTask(p, compressWidth, compressHeight, new CompressImageTask.OnCompressListener() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onComplete(final Bitmap bitmap) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    }
                }));
                container.addView(imageView);
                result = imageView;
                imageView.setOnGestureListener(new HandleSingleTap());
            } else {
                GifImageView gifImageView = new GifImageView(PreviewActivity.this);
                gifImageView.setResource(p);
                container.addView(gifImageView);
                result = gifImageView;
            }
            return result;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
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

        public void setSelectPos(int pos) {
            mSelectPos = pos;
            notifyItemChanged(pos - 1);
            notifyItemChanged(pos);
            notifyItemChanged(pos + 1);
        }


        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.preview_thumb_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final VH holder, int position) {
            int size = Utils.dp2px(PreviewActivity.this, 70);
            service.execute(new CompressImageTask(paths.get(position), size, size, new CompressImageTask.OnCompressListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete(final Bitmap bitmap) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.image.setImageBitmap(bitmap);
                        }
                    });
                }
            }));
            final RelativeLayout root = (RelativeLayout) holder.itemView;
            if (position == mSelectPos) holder.border.setVisibility(View.VISIBLE);
            else holder.border.setVisibility(View.INVISIBLE);

            if (mCheckPath.get(position))
                holder.shadow.setVisibility(View.INVISIBLE);
            else holder.shadow.setVisibility(View.VISIBLE);
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
                //mBottomBar.animate().alpha(0).setDuration(200).start();
                mBottomBar.setAlpha(0);
                mBottomBar.setVisibility(View.GONE);
            } else {
                isShow = true;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mTopBar.animate().translationYBy(mTopBar.getHeight()).setDuration(200).start();
                mBottomBar.setVisibility(View.VISIBLE);
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
