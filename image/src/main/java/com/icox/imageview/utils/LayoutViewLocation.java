package com.icox.imageview.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by jlfxs on 2016/10/19.
 */

public class LayoutViewLocation {
    private Context mContext;
    private int mImgWidth;
    private int mImgHeight;

    public LayoutViewLocation(Context context, int imgWidth, int imgHeight) {
        this.mContext = context;
        this.mImgWidth = imgWidth;
        this.mImgHeight = imgHeight;
    }

    public void setRelativeLayoutMatchParent(View setView){
        RelativeLayout.LayoutParams matchParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        setView.setLayoutParams(matchParams);
    }

    public void setRelativeLayoutWidth(View setView){
        RelativeLayout.LayoutParams matchParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        setView.setLayoutParams(matchParams);
    }

    public void setRelativeLayoutCenter(View setView, int verb){
        RelativeLayout.LayoutParams matchParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        matchParams.addRule(verb);
        setView.setLayoutParams(matchParams);
    }

    /**
     * 根据坐标位置布局控件
     * @param mainView
     * @param addView
     * @param location
     */
    public void addViewByLocation(ViewGroup mainView, View addView, int[] location){
        RelativeLayout topRelativeLayout = new RelativeLayout(mContext);
        RelativeLayout.LayoutParams matchParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        topRelativeLayout.setLayoutParams(matchParams);

        // 竖向
        addViewByWeights(mainView, topRelativeLayout,
                getWeights(location, LinearLayout.VERTICAL), LinearLayout.VERTICAL);
        // 横向
        addViewByWeights(topRelativeLayout, addView,
                getWeights(location, LinearLayout.HORIZONTAL), LinearLayout.HORIZONTAL);
    }

    /**
     * 根据比例线性布局中间位置控件
     * @param mainView
     * @param addView
     * @param weights
     * @param orientation
     */
    public void addViewByWeights(ViewGroup mainView, View addView, int[] weights, int orientation){
        LinearLayout topLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams matchParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        topLayout.setLayoutParams(matchParams);
        topLayout.setOrientation(orientation);

        int width, height;
        if (orientation == LinearLayout.VERTICAL){
            width = LinearLayout.LayoutParams.MATCH_PARENT;
            height = 0;
        }else {
            width = 0;
            height = LinearLayout.LayoutParams.MATCH_PARENT;
        }

        RelativeLayout addViewRelativeLayout = new RelativeLayout(mContext);
//        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addViewRelativeLayout.setLayoutParams(matchParams);

        for (int i = 0; i < 3; i++){
            LinearLayout linearLayout = new LinearLayout(mContext);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    width, height, weights[i]));
            topLayout.addView(linearLayout);

            if (i == 1){
//                linearLayout.addView(addView);
//                addView.setLayoutParams(matchParams);
                linearLayout.addView(addViewRelativeLayout);
            }
        }
        addViewRelativeLayout.addView(addView);

        mainView.addView(topLayout);
    }

    /**
     * 通过控件坐标取得布局比例
     * @param location
     * @param orientation
     * @return
     */
    public int[] getWeights(int[] location, int orientation){
        int[] weights = new int[3];
        if (orientation == LinearLayout.VERTICAL) {
            weights[0] = location[1];
            weights[1] = location[3] - location[1];
            weights[2] = mImgHeight - location[3];
        }else {
            weights[0] = location[0];
            weights[1] = location[2] - location[0];
            weights[2] = mImgWidth - location[2];
        }
        return weights;
    }

}
