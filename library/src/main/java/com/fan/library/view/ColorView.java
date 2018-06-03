package com.fan.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import com.fan.library.utils.Utils;

public class ColorView extends View implements Checkable {
    private int mColor;
    private Paint mPaint;
    private int mNormalSize;
    private int mSelectSize;
    private boolean isChecked;
    int offset;

    public ColorView(Context context) {
        this(context, null);
    }

    public ColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mNormalSize = Utils.dp2px(context, 20);
        mSelectSize = Utils.dp2px(context, 25);
        offset = Utils.dp2px(context, 2);
    }

    public void setColor(@ColorInt int color) {
        mColor = color;
    }

    public int getColor() {
        return mColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getMeasuredWidth() / 2;

        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(centerX, centerX, centerX, mPaint);

        mPaint.setColor(mColor);
        canvas.drawCircle(centerX, centerX, centerX - offset, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = 0;
        if (isChecked) {
            size = MeasureSpec.makeMeasureSpec(mSelectSize, MeasureSpec.EXACTLY);
        } else {
            size = MeasureSpec.makeMeasureSpec(mNormalSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(size, size);
    }

    @Override
    public void setChecked(boolean checked) {
        this.isChecked = checked;
        requestLayout();
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    @Override
    public void toggle() {
        isChecked = !isChecked;
        requestLayout();
    }
}
