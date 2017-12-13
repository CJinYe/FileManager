package com.icox.share;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2017/9/23 0023.
 */

public class SizeUtil {

    /**
     * 获取屏幕适配
     * float[0]: 控件大小适配
     * float[1]: 字体大小适配
     */
    public static float[] getScreenScale(Context context) {
        float[] scale = new float[2];
        // 获取屏幕信息
        DisplayMetrics metric = new DisplayMetrics();
        Activity activity = (Activity) context;
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int screenWidth = metric.widthPixels;  // 屏幕宽度（像素）
        int densityDpi = metric.densityDpi;  // 屏幕密度DPI（120 / 160 / 240）
        if (metric.heightPixels > screenWidth)
            screenWidth = metric.heightPixels;
        scale[0] = (float) screenWidth / 1280;
        scale[1] = 6 * ((float) screenWidth / densityDpi) / 30;
        return scale;
    }

    /**
     * 设置View的宽高-LinearLayout
     */
    public static void setViewGroupParams(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
    }

    /**
     * 设置View的宽高-LinearLayout
     */
    public static void setLinearParams(View view, int width, int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
    }

    public static void setLinearParams(View view, float width, float height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = (int) width;
        params.height = (int) height;
    }

    /**
     * 设置View的宽-LinearLayout
     */
    public static void setLinearWidth(View view, int width) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
    }

    /**
     * 设置View的高-LinearLayout
     */
    public static void setLinearHeight(View view, int height) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height = height;
    }

    /**
     * 设置外边框-LinearLayout
     */
    public static void setLinearMargin(View view, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(left, top, right, bottom);
    }

    /**
     * 设置View的宽高-RelativeLayout
     */
    public static void setRelativeParams(View view, int width, int height) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
    }

    /**
     * 设置View的宽-RelativeLayout
     */
    public static void setRelativeWidth(View view, int width) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
    }

    /**
     * 设置View的高-RelativeLayout
     */
    public static void setRelativeHeight(View view, int height) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.height = height;
    }

    /**
     * 设置外边框-RelativeLayout
     */
    public static void setRelativeMargin(View view, int left, int top, int right, int bottom) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.setMargins(left, top, right, bottom);
    }

}
