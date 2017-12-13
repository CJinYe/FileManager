package com.icox.manager.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.icox.manager.R;
import com.icox.manager.util.ApkUtil;
import com.icox.manager.util.AsyncLoadImage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by icox-XiuChou  on 2016/1/4
 */
public class GridShowAdapter extends BaseAdapter {

    // 运行上下文
    private Context context;
    // 文件数组
    private File[] listFiles;
    private LayoutInflater inflater;
    private AsyncLoadImage asyncLoadImage;
    // 记录(checkBox显示不显示)
    private boolean isShow;
    private List<ImageView> imageViewList;

    // 使用checkBox选中效果
    private Map<Integer, Boolean> isSelected;

    private boolean isLoadImage = true;

    // GridShowAdapter的状态
    public Parcelable state;

    /**
     * 菜单选择的id
     */
    private int menuSelectedId = 0;

    /**
     * GridShowAdapter 构造方法
     *
     * @param context   运行上下文
     * @param listFiles 数据
     * @param gridView  相关控件
     * @param isShow    checkbos的显示与否
     */
    public GridShowAdapter(Context context, File[] listFiles, GridView gridView, boolean isShow) {

        this.context = context;
        this.listFiles = listFiles;
        this.inflater = LayoutInflater.from(context);
        this.asyncLoadImage = new AsyncLoadImage(context, new Handler());
        // gridView.setOnScrollListener(this);
        initGridView();
        this.isShow = isShow;
        //imageViewList = new ArrayList<ImageView>();
    }

    // 初始化
    private void initGridView() {

        // 定义isSelected这个map记录每个listitem的状态,初始状态全部为false未勾选状态;
        isSelected = new HashMap<Integer, Boolean>();
        // 当前ye,文件的个数
        for (int position = 0; position < listFiles.length; position++) {
            isSelected.put(position, false);
        }
    }

    // 刷新数据
    public void updateFiles(File[] listFiles) {
        this.listFiles = listFiles;
    }

    // 获取菜单选择时候的id
    public int getMenuSelectedId() {

        return menuSelectedId;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return listFiles == null ? 0 : this.listFiles.length;
    }

    @Override
    public View getView(final int position, View itemView, ViewGroup viewGroup) {

        ViewHolder holder = null;
        if (itemView == null) {
            holder= new ViewHolder();
            // 找到Item布局
            itemView = inflater.inflate(R.layout.file_grid_item, null);
            // 实例化控件
            holder.item_imageView = (ImageView) itemView.findViewById(R.id.item_imageView);
            holder.item_floatableTextView = (TextView) itemView.findViewById(R.id.item_floatableTextView);
            itemView.setTag(holder);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }

        // TODO 绘制图标
        //如果是文件夹
        if (listFiles[position].isDirectory()) {
            File[] listFiles2 = listFiles[position].listFiles();
            if (listFiles2 == null || listFiles2.length == 0) {// 空文件夹
                holder.item_imageView.setImageResource(R.drawable.folder);
            } else {// 长度大于0就是有文件的文件夹
                holder.item_imageView.setImageResource(R.drawable.folder_);
            }
        } else {// 那么就是文件,则需要处理各种后缀的文件
            String fileName = listFiles[position].getName().toLowerCase();

            if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                holder.item_imageView.setImageResource(R.drawable.word);
            } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png") || fileName.endsWith(".bmp")) {
                final String path = listFiles[position].getAbsolutePath();
                holder.item_imageView.setTag(path);
                asyncLoadImage.loadImage2(holder.item_imageView, 100);
            } else if (fileName.endsWith(".apk")) {
                holder.item_imageView.setImageDrawable(ApkUtil.getApkIcon3(context, listFiles[position].getAbsolutePath()));
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".avi") || fileName.endsWith(".flv")) {
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(listFiles[position].getAbsolutePath(), MediaStore.Images.Thumbnails.MICRO_KIND);
                holder.item_imageView.setImageBitmap(bitmap);
            } else if (fileName.endsWith(".mp3") || fileName.endsWith(".wma")|| fileName.endsWith(".wmv")|| fileName.endsWith(".ape")
                    || fileName.endsWith(".wav")|| fileName.endsWith(".ogg")|| fileName.endsWith(".midi")|| fileName.endsWith(".flac")
                    || fileName.endsWith(".mid")|| fileName.endsWith(".amr")) {
                holder.item_imageView.setImageResource(R.drawable.format_music);
            } else if (fileName.endsWith(".lrc")) {
                holder.item_imageView.setImageResource(R.drawable.lrc);
            } else if (fileName.endsWith(".html")) {
                holder.item_imageView.setImageResource(R.drawable.html);
            } else if (fileName.endsWith(".xml")) {
                holder.item_imageView.setImageResource(R.drawable.xml);
            } else if (fileName.endsWith(".txt")) {
                holder.item_imageView.setImageResource(R.drawable.txt);
            } else if (fileName.endsWith(".jtb")) {
                holder.item_imageView.setImageResource(R.drawable.format_media1);
            } else {
                holder.item_imageView.setImageResource(R.drawable.file);
            }
        }

        // TODO 字体过长的滚动效果
//        holder.item_floatableTextView.init(listFiles[position].getName(), 0.6f, holder.item_imageView.getWidth());
//        holder.item_floatableTextView.startFloating();
        holder.item_floatableTextView.setText(listFiles[position].getName());
        holder.item_floatableTextView.setSelected(true);
//        holder.item_floatableTextView.setOnFocusChangeListener(mFocusChangeListener);

//        // test
//        itemView.setFocusable(true);
//        itemView.setBackgroundResource(com.icox.administrator.icoxmp3player.R.drawable.sel_focus);
//        itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // 替换listview OnItemClick
//                FileActivity fileActivity = (FileActivity) context;
//                fileActivity.gridViewItemClickEvent(position);
//            }
//        });

        return itemView;
    }

    /**
     * GridView的优化
     */
    private static final class ViewHolder {
        private ImageView item_imageView;
        private TextView item_floatableTextView;
    }

    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
//            Toast.makeText(FileActivity.this, v.getId() + "", Toast.LENGTH_SHORT).show();
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
        } else {
            focus = R.anim.decrease;
        }

        //如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(context, focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }
}


