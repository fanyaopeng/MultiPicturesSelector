package com.fan.library.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.fan.library.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class ColorSelectView extends LinearLayout {
    @ColorInt
    private final int WHITE = Color.WHITE;
    @ColorInt
    private final int BLACK = Color.BLACK;
    @ColorInt
    private final int RED = Color.RED;
    @ColorInt
    private final int YELLOW = Color.YELLOW;
    @ColorInt
    private final int GREEN = Color.GREEN;
    @ColorInt
    private final int BLUE = Color.BLUE;
    @ColorInt
    private final int PURPLE = 0xff9400d3;
    @ColorInt
    private final int PINK = 0xffff1493;

    public ColorSelectView(Context context) {
        this(context, null);
    }

    public ColorSelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.leftMargin = Utils.dp2px(getContext(), 15);
        ColorView white = new ColorView(context);
        white.setColor(WHITE);
        addView(white, params);

        ColorView black = new ColorView(context);
        black.setColor(BLACK);
        addView(black, params);

        ColorView red = new ColorView(context);
        red.setColor(RED);
        red.setChecked(true);
        addView(red, params);

        ColorView yellow = new ColorView(context);
        yellow.setColor(YELLOW);
        addView(yellow, params);

        ColorView green = new ColorView(context);
        green.setColor(GREEN);
        addView(green, params);

        ColorView blue = new ColorView(context);
        blue.setColor(BLUE);
        addView(blue, params);

        ColorView purple = new ColorView(context);
        purple.setColor(PURPLE);
        addView(purple, params);

        ColorView pink = new ColorView(context);
        pink.setColor(PINK);
        addView(pink, params);

        for (int i = 0; i < getChildCount(); i++) {
            final ColorView view = (ColorView) getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    view.toggle();
                    toggleOther(view);
                    if (mListener != null) {
                        mListener.onColorChange(getColor());
                    }
                }
            });
        }
    }

    private void toggleOther(ColorView selectView) {

        for (int i = 0; i < getChildCount(); i++) {
            ColorView child = (ColorView) getChildAt(i);
            if (child != selectView) {
                child.setChecked(false);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        setGravity(Gravity.CENTER_VERTICAL);
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Utils.dp2px(getContext(), 44), MeasureSpec.EXACTLY));
    }

    public int getColor() {
        for (int i = 0; i < getChildCount(); i++) {
            ColorView child = (ColorView) getChildAt(i);
            if (child.isChecked()) {
                return child.getColor();
            }
        }
        return RED;
    }

    public interface OnColorChangeListener {
        void onColorChange(int color);
    }

    OnColorChangeListener mListener;

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        mListener = listener;
    }
}
