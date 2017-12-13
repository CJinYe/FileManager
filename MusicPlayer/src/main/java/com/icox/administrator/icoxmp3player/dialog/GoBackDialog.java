package com.icox.administrator.icoxmp3player.dialog;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;

import com.icox.administrator.icoxmp3player.R;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-9-14 11:55
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public abstract class GoBackDialog extends Dialog implements View.OnClickListener {
    private final Context mContext;

    public GoBackDialog(Context context) {
        super(context, R.style.CustomDialog);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyCompat();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.dialog_go_back);

        ImageButton butNo = (ImageButton) findViewById(R.id.dialog_go_back_but_no);
        ImageButton butYes = (ImageButton) findViewById(R.id.dialog_go_back_but_yes);

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

    public abstract void clickNo();

    public abstract void clickYes();

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.dialog_go_back_but_no) {
            clickNo();
        } else if (i == R.id.dialog_go_back_but_yes) {
            clickYes();
        }
        dismiss();
    }

    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            viewAnimation(v, hasFocus);


        }
    };

    public void viewAnimation(View v, boolean hasFocus) {

        if (v == null) {
            return;
        }

        int focus = 0;
        if (hasFocus) {
            focus = R.anim.enlarge;
            addIcoxAnimator(v);
        } else {
            focus = R.anim.decrease;
        }

        //如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(mContext, focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }

    /**
     * 增加动画
     */
    private void addIcoxAnimator(View view) {
        view.getId();
        ObjectAnimator a3 = null;
        ObjectAnimator a2 = null;
        a3 = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f);
        a2 = ObjectAnimator.ofFloat(view, "translationY", 0f, -50, 0f);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.play(a3).with(a2);
        // 设置插值器。
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }
}
