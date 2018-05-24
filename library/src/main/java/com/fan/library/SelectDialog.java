package com.fan.library;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class SelectDialog extends PopupWindow {
    private Context mContext;
    private View root;
    private List<String> path;
    private RecyclerView mList;
    private int mPreViewSize;

    public SelectDialog(Context context, List<String> path) {
        super(context);
        this.mContext = context;
        root = View.inflate(mContext, R.layout.pop, null);
        setContentView(root);
        int height = context.getResources().getDisplayMetrics().heightPixels * 3 / 4;
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        setWidth(width);
        setHeight(height);
        setFocusable(true);
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#cccccc")));
        MultiPicturesSelectorActivity activity = (MultiPicturesSelectorActivity) context;
        mPreViewSize = activity.mItemSize;

        mList = root.findViewById(R.id.list);
        mList.setLayoutManager(new LinearLayoutManager(context));
        mList.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        this.path = path;
        mList.setAdapter(new DirsAdapter());
        setAnimationStyle(R.style.pop_anim);
    }

    private class DirsAdapter extends RecyclerView.Adapter<DirsAdapter.VH> {

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.pop_item, parent, false));
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            final String parentPath = path.get(position);
            int index = parentPath.lastIndexOf(File.separator);
            holder.tvName.setText(parentPath.substring(index + 1));
            ImageView preview = holder.preview;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) preview.getLayoutParams();
            params.width = mPreViewSize;
            params.height = mPreViewSize;
            String prePath = getFirstPic(parentPath);
            preview.setImageBitmap(Utils.compress(prePath, mPreViewSize, mPreViewSize));
            int len[] = new int[1];
            getFileNum(parentPath, len);
            holder.tvNum.setText(len[0] + "张");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        dismiss();
                        listener.onItemClick(parentPath);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return path.size();
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
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String path);
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
}
