package com.icox.manager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icox.manager.R;
import com.icox.manager.util.ApkUtil;
import com.icox.manager.util.AsyncLoadImage;

import java.io.File;

import static com.icox.manager.R.drawable.file;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-10-9 10:24
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class FileViewPagerAdapter extends PagerAdapter {

    private File[] mFiles;
    private int count;
    public final int itemMaxSize = 6;
    private LayoutInflater mLayoutInflater;
    private final AsyncLoadImage mAsyncLoadImage;
    private final Context mContext;
    private View[] mViews;

    public FileViewPagerAdapter(Context context, File[] files) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mAsyncLoadImage = new AsyncLoadImage(mContext, new Handler());
        mFiles = files;
        count = (mFiles.length + itemMaxSize - 1) / itemMaxSize;
        mViews = new View[count];
    }

    public void updateFilesPager(File[] files){
        mFiles = files;
        count = (mFiles.length + itemMaxSize - 1) / itemMaxSize;
        mViews = new View[count];
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return object == view;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mLayoutInflater.inflate(R.layout.item_filelist_viewpager, null);
        mViews[position] = view;
        for (int i = 0; i < itemMaxSize; i++) {
            RelativeLayout layout = (RelativeLayout)
                    view.findViewById(R.id.item_file_manager_rl_01 + i * 3);
            layout.setVisibility(View.VISIBLE);
            final int itemTag = position * itemMaxSize + i;
            if (itemTag >= mFiles.length) {
                layout.setVisibility(View.GONE);
                continue;
            }
            ImageView imageView =
                    (ImageView) view.findViewById(R.id.item_file_manager_iv_01 + i * 3);
            TextView textView =
                    (TextView) view.findViewById(R.id.item_file_manager_tv_01 + i * 3);
            imageView.setTag(itemTag);
            textView.setText(mFiles[itemTag].getName());
            textView.setSelected(true);

            if (mFiles[itemTag].isDirectory()) {
                if (mFiles[itemTag].listFiles() == null ||
                        mFiles[itemTag].listFiles().length == 0) {
                    imageView.setImageResource(R.drawable.folder);
                } else {
                    imageView.setImageResource(R.drawable.folder_);
                }
            } else {
                String fileName = mFiles[itemTag].getName().toLowerCase();

                if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                    imageView.setImageResource(R.drawable.word);
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp")) {
                    final String path = mFiles[itemTag].getAbsolutePath();
                    imageView.setTag(path);
                    mAsyncLoadImage.loadImage2(imageView, 100);
                } else if (fileName.endsWith(".apk")) {
                    Drawable drawable = ApkUtil.getUninstallApkIcon(mContext, mFiles[itemTag].getAbsolutePath());
                    imageView.setImageDrawable(drawable);
                } else if (fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".avi") || fileName.endsWith(".flv")) {
                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mFiles[itemTag].getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND);
                    imageView.setImageBitmap(bitmap);
                } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wma") || fileName.endsWith(".wmv") || fileName.endsWith(".ape")
                        || fileName.endsWith(".wav") || fileName.endsWith(".ogg") || fileName.endsWith(".midi") || fileName.endsWith(".flac")
                        || fileName.endsWith(".mid") || fileName.endsWith(".amr")) {
                    imageView.setImageResource(R.drawable.format_music);
                } else if (fileName.endsWith(".lrc")) {
                    imageView.setImageResource(R.drawable.lrc);
                } else if (fileName.endsWith(".html")) {
                    imageView.setImageResource(R.drawable.html);
                } else if (fileName.endsWith(".xml")) {
                    imageView.setImageResource(R.drawable.xml);
                } else if (fileName.endsWith(".txt")) {
                    imageView.setImageResource(R.drawable.txt);
                } else if (fileName.endsWith(".jtb")) {
                    imageView.setImageResource(R.drawable.format_media1);
                } else {
                    imageView.setImageResource(file);
                }
            }

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLayoutClickListener!=null)
                        mOnLayoutClickListener.onClickListener(mFiles[itemTag]);
                }
            });

            imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnLayoutClickListener!=null)
                        mOnLayoutClickListener.onLongClickListener(mFiles[itemTag]);
                    return false;
                }
            });
        }

        container.addView(view);
        return view;
    }

    public interface onLayoutClickListener{
        void onClickListener(File file);
        void onLongClickListener(File file);
    }

    public onLayoutClickListener mOnLayoutClickListener;
    public void setOnLayoutClickListener(onLayoutClickListener onLayoutClickListener){
        mOnLayoutClickListener = onLayoutClickListener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        container.removeView((View) object);
        if (position<mViews.length){
            container.removeView(mViews[position]);
            mViews[position] = null;
        }
    }
}
