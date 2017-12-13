package com.icox.mediafilemanager.baby;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.icox.mediafilemanager.R;
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
public abstract class VideoDeleteDialog extends Dialog implements View.OnClickListener {

    private final Context mContext;

    VideoDeleteDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_baby_video_delete);

        ImageButton butSure = (ImageButton) findViewById(R.id.dialog_baby_video_delete_but_sure);
        ImageButton butCancel = (ImageButton) findViewById(R.id.dialog_baby_video_delete_but_cancel);
        TextView tvTitle = (TextView) findViewById(R.id.dialog_baby_video_delete_tv_title);
        TextView tvTiShi = (TextView) findViewById(R.id.dialog_baby_video_delete_tv_tishi);
        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_baby_video_delete_layout);

        float[] size = SizeUtil.getScreenScale(mContext);
        SizeUtil.setLinearParams(layout, size[0] * 415, size[0] * 237);
        tvTitle.setTextSize(size[1] * 16);
        tvTiShi.setTextSize(size[1] * 14);

        butSure.setOnClickListener(this);
        butCancel.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dialog_baby_video_delete_but_sure) {
            clickSure();

        }

        dismiss();
    }

    public abstract void clickSure();
}
