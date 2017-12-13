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
public abstract class YoungMenuDialog extends Dialog implements View.OnClickListener {

    private final Context mContext;

    protected YoungMenuDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_young_menu);

        ImageButton butCopy = (ImageButton) findViewById(R.id.dialog_young_menu_but_copy);
        ImageButton butMove = (ImageButton) findViewById(R.id.dialog_young_menu_but_move);
        ImageButton butRename = (ImageButton) findViewById(R.id.dialog_young_menu_but_rename);
        ImageButton butDelete = (ImageButton) findViewById(R.id.dialog_young_menu_but_delete);
        ImageButton butInfo = (ImageButton) findViewById(R.id.dialog_young_menu_but_info);
        TextView tvTitle = (TextView) findViewById(R.id.dialog_young_menu_tv_title);
        LinearLayout layout = (LinearLayout) findViewById(R.id.dialog_young_menu_layout);

        float[] size = SizeUtil.getScreenScale(mContext);
        SizeUtil.setLinearParams(layout, size[0] * 640, size[0] * 300);
        tvTitle.setTextSize(size[1] * 18);

        butCopy.setOnClickListener(this);
        butMove.setOnClickListener(this);
        butRename.setOnClickListener(this);
        butDelete.setOnClickListener(this);
        butInfo.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_young_menu_but_copy:
                clickMenuCopy();
                break;
            case R.id.dialog_young_menu_but_move:
                clickMenuMove();
                break;
            case R.id.dialog_young_menu_but_rename:
                clickMenuRename();
                break;
            case R.id.dialog_young_menu_but_delete:
                clickMenuDelete();
                break;
            case R.id.dialog_young_menu_but_info:
                clickMenuInfo();
                break;

            default:
                break;
        }

        dismiss();
    }

    protected abstract void clickMenuInfo();

    protected abstract void clickMenuDelete();

    protected abstract void clickMenuRename();

    protected abstract void clickMenuMove();

    public abstract void clickMenuCopy();
}
