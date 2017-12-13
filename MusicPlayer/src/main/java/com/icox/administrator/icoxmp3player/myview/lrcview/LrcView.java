package com.icox.administrator.icoxmp3player.myview.lrcview;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 此文件为自定义TextView,功能是实现:绘画歌词，产生滚动效果
 */

public class LrcView extends TextView {

    private float width;
    private float high;
    private Paint CurrentPaint;
    private Paint NotCurrentPaint;
    private float TextHigh = 52;
    private float TextSize = 36;
    private int Index = 0;

    private List<LrcProcess.LrcContent> mSentenceEntities = new ArrayList<LrcProcess.LrcContent>();

    public void setSentenceEntities(List<LrcProcess.LrcContent> mSentenceEntities) {
        this.mSentenceEntities = mSentenceEntities;
    }

    public LrcView(Context context) {
        super(context);
        init();
    }

    public LrcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public LrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setFocusable(true);

        // 高亮部分:正在播放的部分
        CurrentPaint = new Paint();
        CurrentPaint.setAntiAlias(true);
        CurrentPaint.setTextAlign(Paint.Align.CENTER);

        // 非高亮部分:已经播放完的部分，还没播放的部分
        NotCurrentPaint = new Paint();
        NotCurrentPaint.setAntiAlias(true);
        NotCurrentPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas == null) {
            return;
        }

//        CurrentPaint.setColor(Color.parseColor("#2d51ab"));
//        NotCurrentPaint.setColor(Color.parseColor("#ffffff"));
        CurrentPaint.setColor(Color.parseColor("#ffffff"));
        NotCurrentPaint.setColor(Color.parseColor("#40ffffff"));

        CurrentPaint.setTextSize(40);
        CurrentPaint.setTypeface(Typeface.SERIF);

        NotCurrentPaint.setTextSize(TextSize);
        NotCurrentPaint.setTypeface(Typeface.DEFAULT);
        try {
            //画出当前播放的本句
            setText("");
            canvas.drawText(mSentenceEntities.get(Index).getLrc(), width / 2,
                    high / 2, CurrentPaint);

            float tempY = high / 2;
            // 画出本句之前的句子
            for (int i = Index - 1; i >= 0; i--) {
                // 向上推移
                tempY = tempY - TextHigh;

                canvas.drawText(mSentenceEntities.get(i).getLrc(), width / 2,
                        tempY, NotCurrentPaint);
            }
            tempY = high / 2;
            // 画出本句之后的句子
            for (int i = Index + 1; i < mSentenceEntities.size(); i++) {
                // 往下推移
                tempY = tempY + TextHigh;
                canvas.drawText(mSentenceEntities.get(i).getLrc(), width / 2,
                        tempY, NotCurrentPaint);
            }
        } catch (Exception e) {
            setText("...未能检索到本首歌的歌词......");
            setTextSize(36);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.high = h;
    }

    public void SetIndex(int index) {
        this.Index = index;
    }

}
