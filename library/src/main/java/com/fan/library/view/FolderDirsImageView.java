package com.fan.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.fan.library.R;
import com.fan.library.Utils;

public class FolderDirsImageView extends ImageView {
    private Paint mPaint;

    public FolderDirsImageView(Context context) {
        super(context);
    }

    public FolderDirsImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(Utils.dp2px(context, 0.5f));
        mPaint.setColor(getResources().getColor(R.color.line));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int margin = Utils.dp2px(getContext(), 2f);
        Path path1 = new Path();
        path1.moveTo(width, margin);
        path1.lineTo(width, height);
        canvas.drawPath(path1, mPaint);


        Path path2 = new Path();
        path2.moveTo(margin , 0);
        path1.lineTo(width, height - margin);
    }
}
