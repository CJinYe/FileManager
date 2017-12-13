package com.icox.manager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icox.manager.R;
import com.icox.share.SizeUtil;


/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-12-9 17:26
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class YoungRenameDialog extends Dialog implements View.OnClickListener {

    private final Context mContext;
    private final String mTitle;
    private EditText mEdt;

    protected YoungRenameDialog(Context context, String title) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mTitle = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_young_rename);

        ImageButton butSure = (ImageButton) findViewById(R.id.dialog_young_rename_but_sure);
        ImageButton butCancel = (ImageButton) findViewById(R.id.dialog_young_rename_but_cancel);
        TextView tvTitle = (TextView) findViewById(R.id.dialog_young_rename_tv_title);
        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_young_rename_layout);

        tvTitle.setText(mTitle);
        mEdt = (EditText) findViewById(R.id.dialog_young_rename_edt);

        float[] size = SizeUtil.getScreenScale(mContext);
        SizeUtil.setLinearParams(layout, size[0] * 436, size[0] * 233);
        tvTitle.setTextSize(size[1] * 18);
        mEdt.setTextSize(size[1] * 15);

        butSure.setOnClickListener(this);
        butCancel.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_young_rename_but_sure:
                clickSure(mEdt.getText().toString().trim());
                break;

            default:
                break;
        }

        dismiss();
    }

    public abstract void clickSure(String trim);
}
