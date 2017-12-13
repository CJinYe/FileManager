package com.icox.manager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
public abstract class YoungDeleteDialog extends Dialog implements View.OnClickListener {

    private final Context mContext;

    protected YoungDeleteDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_young_delete);

        ImageButton butSure = (ImageButton) findViewById(R.id.dialog_young_delete_but_sure);
        ImageButton butCancel = (ImageButton) findViewById(R.id.dialog_young_delete_but_cancel);
        TextView tvTitle = (TextView) findViewById(R.id.dialog_young_delete_tv_title);
        TextView tvTiShi = (TextView) findViewById(R.id.dialog_young_delete_tv_tishi);
        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_young_delete_layout);

        float[] size = SizeUtil.getScreenScale(mContext);
        SizeUtil.setLinearParams(layout,size[0]*436,size[0]*233);
        tvTitle.setTextSize(size[1]*18);
        tvTiShi.setTextSize(size[1]*16);

        butSure.setOnClickListener(this);
        butCancel.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_young_delete_but_sure:
                clickSure();
                break;

            default:
                break;
        }

        dismiss();
    }

    public abstract void clickSure();
}
