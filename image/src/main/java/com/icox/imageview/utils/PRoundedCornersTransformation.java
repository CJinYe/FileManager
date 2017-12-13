package com.icox.imageview.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.icox.imageview.ImageDirActivity;
import com.icox.imageview.R;
import com.squareup.picasso.Transformation;

/**
 * Created by jlfxs on 2016/10/29.
 */

public class PRoundedCornersTransformation implements Transformation {

    public enum CornerType{
        ALL,
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT,
        TOP, BOTTOM, LEFT, RIGHT,
        OTHER_TOP_LEFT, OTHER_TOP_RIGHT, OTHER_BOTTOM_LEFT, OTHER_BOTTOM_RIGHT,
        DIAGONAL_FROM_TOP_LEFT, DIAGONAL_FROM_TOP_RIGHT
    }

    private int mRadius;
    private int mDiameter;
    private int mMargin;
    private CornerType mCornerType;

    public PRoundedCornersTransformation(int radius, int margin, CornerType cornerType) {
        this.mRadius = radius;
        this.mMargin = margin;
        this.mCornerType = cornerType;

        mDiameter = radius * 2;
    }

    public PRoundedCornersTransformation(int radius, int margin) {
        this.mRadius = radius;
        this.mMargin = margin;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (bitmap == null){
            source.recycle();
            return null;
        }
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
        drawRoundRect(canvas, paint, width, height);
        source.recycle();

//        if (bitmap != null) {
//            Canvas canvas = new Canvas(bitmap);
//            Paint paint = new Paint();
//            paint.setAntiAlias(true);
//            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
//            drawRoundRect(canvas, paint, width, height);
//            source.recycle();
//        }else {
//            bitmap = BitmapFactory.decodeResource(ImageDirActivity.mContext.getResources(), R.mipmap.ic_launcher);
//        }

        return bitmap;
    }

    private void drawRoundRect(Canvas canvas, Paint paint, float width, float height){
        float right = width - mMargin;
        float bottom = height - mMargin;

        switch (mCornerType){
            case ALL:
                canvas.drawRoundRect(new RectF(mMargin, mMargin, right, bottom), mRadius, mRadius, paint);
                break;
        }
    }

    @Override
    public String key() {
        return "PRoundedCorners";
    }
}
