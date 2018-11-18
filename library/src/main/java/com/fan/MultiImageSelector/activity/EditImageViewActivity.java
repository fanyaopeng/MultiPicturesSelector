package com.fan.MultiImageSelector.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import com.fan.MultiImageSelector.R;
import com.fan.MultiImageSelector.view.EditableLayout;

public class EditImageViewActivity extends Activity implements  EditableLayout.OnProcessListener{
    String mPath;

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
        mPath = getIntent().getStringExtra("path");
        mEditLayout.setPath(mPath);
        mEditLayout.setOnProcessListener(this);
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
}
