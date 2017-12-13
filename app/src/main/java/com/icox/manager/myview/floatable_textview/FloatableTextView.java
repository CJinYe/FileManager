package com.icox.manager.myview.floatable_textview;

/**
 * Created by XiuChou on 2015/12/11
 */

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 此文件为自定义TextView,功能是实现:单行显示,当文版内容过长的时候则向左滚动显示文本内容
 */
public class FloatableTextView extends TextView {

    public boolean mIsFloating = false; //是否开始滚动
    private float mSpeed = 0.5f;//初始化速度
    private float mStep = 0f;
    private String mStr = ""; //文本内容
    public float mTextLength = 0f; //文本长度
    public float mViewWidth = 0f;//控件的宽度
    private float mY = 0f; //文字的纵坐标

    public FloatableTextView(Context context) {
        super(context);
    }

    public FloatableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FloatableTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 設置滾動的相關
     *
     * @param str   内容
     * @param speed 速度
     * @param width 宽度
     */
    public void init(String str, float speed, float width) {

        //直接设置文本内容
        setText(str);
        mSpeed = speed;
        mViewWidth = width;

        //获取文本内容
        mStr = getText().toString();
        //获取文本的长度
        mTextLength = getPaint().measureText(mStr);

        mStep = mTextLength + mViewWidth;
        //padding+textSize
        mY = getTextSize() + getPaddingTop();
        //设置文本的颜色

        getPaint().setColor(0xffffffff);
//        getPaint().setColor(getResources().getColor(R.color.baby_text_color));

    }

    @Override
    public void onDraw(Canvas canvas) {
        //drawText(String text, int start, int end, float x, float y, Paint paint)
        canvas.drawText(mStr, 0, mStr.length(), mViewWidth + mTextLength - mStep, mY, getPaint());
        //没有滚动则不需要画
        if (!mIsFloating) {
            return;
        }
        mStep = mStep + mSpeed;
        if (mStep > mViewWidth + mTextLength * 2) {
            mStep = mTextLength;
        }
        invalidate();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        mStep = savedState.mStep;
        mIsFloating = savedState.mIsFloating;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.mStep = mStep;
        savedState.mIsFloating = mIsFloating;
        return savedState;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public void startFloating() {
        mIsFloating = mTextLength > mViewWidth;
        // mIsFloating = true;
        invalidate();
    }

    public void stopFloating() {
        mIsFloating = false;
        invalidate();
    }

    /**
     * 状态的保存
     */
    public static class SavedState extends BaseSavedState {

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public boolean mIsFloating = false;
        public float mStep = 0.0f;

        private SavedState(Parcel in) {
            super(in);
            boolean[] b = new boolean[1];
            in.readBooleanArray(b);
            mIsFloating = b[0];
            mStep = in.readFloat();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeBooleanArray(new boolean[]{
                    mIsFloating
            });
            out.writeFloat(mStep);
        }
    }
}