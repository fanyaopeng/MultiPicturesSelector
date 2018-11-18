package com.fan.MultiImageSelector.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fan.MultiImageSelector.R;

public class TopBar extends RelativeLayout {
    private ImageView back;
    private TextView tvTitle;
    private TextView tvNum;
    private Context mContext;

    public TopBar(Context context) {
        this(context, null);
    }

    public TopBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        inflate(context, R.layout.top_bar, this);
        back = findViewById(R.id.ic_back);
        tvTitle = findViewById(R.id.title);
        tvNum = findViewById(R.id.tv_complete);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TopBar);
        String title = a.getString(R.styleable.TopBar_title);
        a.recycle();
        this.tvTitle.setText(title);
        init();
    }

    private void init() {
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) mContext;
                activity.onBackPressed();
            }
        });
    }

    public void setTitle(String title) {
        this.tvTitle.setText(title);
    }

    public void setNum(int num, int max) {
        if (num != 0) {
            tvNum.setText("完成(" + num + "/" + max + ")");
        } else {
            tvNum.setText("完成");
        }
    }
}
