package com.fan.library;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class CheckImageView extends ImageView {
    private boolean isChecked;

    public CheckImageView(Context context) {
        super(context);
    }

    public CheckImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        if (isChecked) {
            setImageResource(R.mipmap.ic_checked);
        } else {
            setImageResource(R.mipmap.ic_no_check);
        }
    }

    public boolean getChecked() {
        return isChecked;
    }
}
