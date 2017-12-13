package com.icox.manager.dialog;

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
import android.widget.TextView;

import com.icox.manager.R;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-9-28 12:01
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class FileDetailDialog extends Dialog implements View.OnClickListener {
    private final String mTitle;
    private final String mPath;
    private final String mSize;
    private final String mTime;

    public FileDetailDialog(Context context, String title, String path, String size, String time) {
        super(context, R.style.CustomDialog);
        mTitle = title;
        mPath = path;
        mSize = size;
        mTime = time;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        setContentView(R.layout.dialog_file_detail);
        ImageButton butClose = (ImageButton) findViewById(R.id.dialog_file_detail_close);
        butClose.setOnClickListener(this);
        TextView tvTitle = (TextView) findViewById(R.id.dialog_file_detail_title);
        TextView tvTime = (TextView) findViewById(R.id.dialog_file_detail_time);
        TextView tvSize = (TextView) findViewById(R.id.dialog_file_detail_size);
        TextView tvPath = (TextView) findViewById(R.id.dialog_file_detail_path);

        tvTitle.setText(mTitle);
        tvTime.setText(mTime);
        tvSize.setText(mSize);
        tvPath.setText(mPath);
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


    @Override
    public void onClick(View v) {
        dismiss();
    }
}
