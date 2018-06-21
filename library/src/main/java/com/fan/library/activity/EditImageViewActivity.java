package com.fan.library.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fan.library.R;
import com.fan.library.utils.Config;
import com.fan.library.view.EditableLayout;
import com.fan.library.view.ColorSelectView;

public class EditImageViewActivity extends Activity implements View.OnClickListener, EditableLayout.OnProcessListener, EditableLayout.OnImagePosChangeListener {
    String mPath;

    private RelativeLayout mAllOperation;
    private EditableLayout mEditLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_imageview);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        initView();
    }

    private void initView() {
        mEditLayout = findViewById(R.id.edit_layout);

        mAllOperation = findViewById(R.id.rel_all_operation);
        mPath = getIntent().getStringExtra("path");
        mEditLayout.setPath(mPath);
        TextView tvClip = findViewById(R.id.tv_clip);
        tvClip.setOnClickListener(this);
        TextView tvEdit = findViewById(R.id.tv_edit);
        tvEdit.setOnClickListener(this);
        mEditLayout.setOnImagePosChangeListener(this);
        mEditLayout.setOnProcessListener(this);
        Config config = Config.get();
        if (!config.isOpenClip) {
            tvClip.setVisibility(View.GONE);
        }
        if (!config.isOpenEdit) {
            tvEdit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_clip) {
            //img.rotate(-90);
            mEditLayout.togglePos();
            mEditLayout.setStatus(EditableLayout.Status.clip);
        }
        if (v.getId() == R.id.tv_edit) {
            mEditLayout.setStatus(EditableLayout.Status.edit);
        }
    }

    private Dialog process;

    @Override
    public void onProcessStart() {
        process = new Dialog(this);
        process.setContentView(R.layout.dialog_process);
        process.show();
    }

    @Override
    public void onProcessEnd(String path) {
        process.dismiss();
        Intent intent = new Intent();
        intent.putExtra("path", path);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onPosChange(boolean isIn) {
        if (isIn) {
            mAllOperation.setVisibility(View.GONE);
        } else {
            mAllOperation.setVisibility(View.VISIBLE);
        }
    }
}
