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

import com.icox.manager.R;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-9-28 10:53
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class FileMenuDialog extends Dialog implements View.OnClickListener {
    private Context mContext;

    public FileMenuDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    public FileMenuDialog(Context context, int theme) {
        super(context, theme);
    }

    protected FileMenuDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        setContentView(R.layout.dialog_file_menu);

        ImageButton butCopy = (ImageButton) findViewById(R.id.dialog_file_menu_copy);
        ImageButton butCut = (ImageButton) findViewById(R.id.dialog_file_menu_cut);
        ImageButton butRename = (ImageButton) findViewById(R.id.dialog_file_menu_rename);
        ImageButton butDelete = (ImageButton) findViewById(R.id.dialog_file_menu_delete);
        ImageButton butDetail = (ImageButton) findViewById(R.id.dialog_file_menu_detail);

        butCopy.setOnClickListener(this);
        butCut.setOnClickListener(this);
        butRename.setOnClickListener(this);
        butDelete.setOnClickListener(this);
        butDetail.setOnClickListener(this);

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

    public abstract void clickCopy();

    public abstract void clickCut();

    public abstract void clickRename();

    public abstract void clickDelete();

    public abstract void clickDetail();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_file_menu_copy:
                clickCopy();
                break;
            case R.id.dialog_file_menu_cut:
                clickCut();
                break;
            case R.id.dialog_file_menu_rename:
                clickRename();
                break;
            case R.id.dialog_file_menu_delete:
                clickDelete();
                break;
            case R.id.dialog_file_menu_detail:
                clickDetail();
                break;

            default:
                break;
        }

        dismiss();
    }
}
