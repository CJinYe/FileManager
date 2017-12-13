package com.icox.mediafilemanager;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-9-28 12:01
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class FileDeleteDialog extends Dialog implements View.OnClickListener {
    public FileDeleteDialog(Context context) {
        super(context, R.style.CustomDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        setContentView(R.layout.dialog_file_delete);
        ImageButton butCancel = (ImageButton) findViewById(R.id.dialog_file_delete_cancel);
        ImageButton butSure = (ImageButton) findViewById(R.id.dialog_file_delete_sure);
        butCancel.setOnClickListener(this);
        butSure.setOnClickListener(this);
    }

    protected void applyCompat() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        Window window = this.getWindow();
        if (window != null) {
            WindowManager.LayoutParams attr = window.getAttributes();
            if (attr != null) {
                attr.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                attr.gravity = Gravity.CENTER;//设置dialog 在布局中的位置
            }
        }
    }

    public abstract void clickCancel();

    public abstract void clickSure();

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dialog_file_delete_cancel) {
            clickCancel();

        } else if (i == R.id.dialog_file_delete_sure) {
            clickSure();

        }

        dismiss();
    }
}
