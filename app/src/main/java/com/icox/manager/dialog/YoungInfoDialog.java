package com.icox.manager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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
public class YoungInfoDialog extends Dialog implements View.OnClickListener {

    private final Context mContext;
    private final String mInfo;

    public YoungInfoDialog(Context context, String msg) {
        super(context, R.style.CustomDialog);
        mContext = context;
        mInfo = msg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_young_info);
        findViewById(R.id.dialog_young_info_but_close).setOnClickListener(this);

        TextView tvTitle = (TextView) findViewById(R.id.dialog_young_info_tv_title);
        TextView tvTiShi = (TextView) findViewById(R.id.dialog_young_info_tv_detail);
        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_young_info_layout);
        tvTiShi.setText(mInfo);

        float[] size = SizeUtil.getScreenScale(mContext);
        SizeUtil.setLinearParams(layout, size[0] * 640, size[0] * 300);
        tvTitle.setTextSize(size[1] * 18);
        tvTiShi.setTextSize(size[1] * 15);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_young_info_but_close:
                break;

            default:
                break;
        }

        dismiss();
    }
}
