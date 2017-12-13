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
import android.widget.EditText;
import android.widget.ImageButton;

import com.icox.manager.R;


/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-9-14 11:55
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class NewFileDialog extends Dialog implements View.OnClickListener {
    private final Context mContext;
    private EditText mEditText;

    public NewFileDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.dialog_new_file);

        ImageButton butNo = (ImageButton) findViewById(R.id.dialog_new_file_cancel);
        ImageButton butYes = (ImageButton) findViewById(R.id.dialog_new_file_sure);
        mEditText = (EditText) findViewById(R.id.dialog_new_file_edt);

        butNo.setOnClickListener(this);
        butYes.setOnClickListener(this);
    }

    protected void applyCompat() {
        if (Build.VERSION.SDK_INT < 19) {
            return;
        }
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

    public String getEdtText(){
        return mEditText.getText().toString().trim();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dialog_new_file_cancel) {
            clickCancel();
        } else if (i == R.id.dialog_new_file_sure) {
            clickSure();
        }
        dismiss();
    }
}
