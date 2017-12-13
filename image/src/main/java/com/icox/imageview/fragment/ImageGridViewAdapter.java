package com.icox.imageview.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icox.imageview.ImageDirActivity;
import com.icox.imageview.R;
import com.icox.imageview.utils.CircleTransform;
import com.icox.imageview.utils.FileUtil;
import com.icox.imageview.utils.PRoundedCornersTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.util.List;

/**
 * Created by jlfxs on 2016/10/25.
 */

public class ImageGridViewAdapter extends BaseAdapter {

    private Context mContex;
    private List<ImageInfo> mData;
    private LayoutInflater mLayoutInflater;

    private PRoundedCornersTransformation mTransformation;

    private class ClassWidget{
        public ImageView wImage01;
        public TextView wText01;
    }

    private int mImageViewWidth;
    private int mImageViewHeight;

    public ImageGridViewAdapter(Context contex, List<ImageInfo> data) {
        this.mContex = contex;
        this.mData = data;
        this.mLayoutInflater = LayoutInflater.from(contex);

        mTransformation = new PRoundedCornersTransformation(30, 0, PRoundedCornersTransformation.CornerType.ALL);
    }

    public void setInfo(int imageViewWidth, int imageViewHeight){
        this.mImageViewWidth = imageViewWidth;
        this.mImageViewHeight = imageViewHeight;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int mPrePosition = -1;
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ClassWidget classWidget = null;
        if (convertView == null){
            classWidget = new ClassWidget();
            convertView = mLayoutInflater.inflate(R.layout.item_image_gridview, null);

            RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.gv_rl);
            relativeLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, mImageViewHeight));

            classWidget.wImage01 = (ImageView) convertView.findViewById(R.id.iv_image);
            classWidget.wText01 = (TextView) convertView.findViewById(R.id.tv_image_name);
            convertView.setTag(classWidget);
        }else {
            classWidget = (ClassWidget) convertView.getTag();
            if (mPrePosition == position){
                return convertView;
            }
            mPrePosition = position;
        }

        ImageInfo imageInfo = mData.get(position);

        int[] imageSize = FileUtil.getImageWidthHeight(imageInfo.filePath);
        if (imageSize[1] > mImageViewHeight){
            imageSize[0] = mImageViewWidth;
            imageSize[1] = mImageViewHeight;
        }
        if (imageSize[0] <= 0 || imageSize[1] <= 0){
            imageSize[0] = 400;
            imageSize[1] = 800;
        }

        File imageFile = new File(imageInfo.filePath);
        Log.i("test", "position = " + position);
//        classWidget.wImage01.setImageURI(Uri.fromFile(imageFile));
        if (imageFile.exists()) {
//            Picasso.with(mContex)
//                    .load(imageFile)
//                    .resize(ImageDirActivity.mScreenWidth/2, ImageDirActivity.mScreenHeight/2)
////                    .transform(mTransformation)
//                    .error(R.mipmap.ic_launcher)
//                    .into(classWidget.wImage01);
            Picasso.with(mContex)
                    .load(imageFile)
                    .placeholder(R.mipmap.ic_launcher)
                    .transform(FileUtil.getTransformation(classWidget.wImage01))
                    .into(classWidget.wImage01);
        }
        else {
            Picasso.with(mContex)
                    .load(R.mipmap.ic_launcher)
                    .into(classWidget.wImage01);
        }
        classWidget.wText01.setText(imageInfo.fileName);

        return convertView;
    }

    private class ViewLongClickListener implements View.OnLongClickListener{
        private int vPosition;

        public ViewLongClickListener(int position) {
            vPosition = position;
        }

        @Override
        public boolean onLongClick(View view) {
            final ImageInfo imageInfo = mData.get(vPosition);
            if (imageInfo.coverType == 1){
                AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
                builder.setMessage("确认删除该图片吗？");
                builder.setTitle("提示");
                builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        File file = new File(imageInfo.filePath);
                        if (file.exists()){
                            if (file.delete()){
                                mData.remove(vPosition);
                                notifyDataSetChanged();
                            }
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            return false;
        }
    }
//    protected void deleteImageDialog(final String imagePath) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContex);
//        builder.setMessage("确认删除该图片吗？");
//        builder.setTitle("提示");
//        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//                File file = new File(imagePath);
//                if (file.exists()){
//                    file.delete();
//
//                }
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.create().show();
//    }

//    private static Transformation getTransformation(final ImageView view) {
//        return new Transformation() {
//            @Override
//            public Bitmap transform(Bitmap source) {
//                int targetWidth = view.getWidth();
//
//                //返回原图
//                if (source.getWidth() == 0 || source.getWidth() < targetWidth) {
//                    return source;
//                }
//
//                //如果图片大小大于等于设置的宽度，则按照设置的宽度比例来缩放
//                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
//                int targetHeight = (int) (targetWidth * aspectRatio);
//                if (targetHeight == 0 || targetWidth == 0) {
//                    return source;
//                }
//                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
//                if (result != source) {
//                    // Same bitmap is returned if sizes are the same
//                    source.recycle();
//                }
//                return result;
//            }
//
//            @Override
//            public String key() {
//                return "transformation" + " desiredWidth";
//            }
//        };
//    }

}
