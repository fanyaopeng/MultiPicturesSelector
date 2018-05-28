package com.fan.library.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class GifImageView extends View {
    private Movie mMovie;
    private long mMovieStart = 0;

    public GifImageView(Context context) {
        this(context, null);
    }

    public GifImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setResource(String path) {
        mMovie = Movie.decodeFile(path);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        long now = SystemClock.uptimeMillis();
        if (mMovieStart == 0) {
            mMovieStart = now;
        }
        if (mMovie != null) {
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }

            int relTime = (int) ((now - mMovieStart) % dur);
            mMovie.setTime(relTime);
            mMovie.draw(canvas, (getWidth()-mMovie.width())/2, (getHeight()-mMovie.height())/2);

            invalidate();
        }
    }
}
