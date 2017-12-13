package com.icox.imageview.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.icox.imageview.BaseActivity;
import com.icox.imageview.R;
import com.icox.imageview.fragment.ImageInfo;
import com.polites.android.GestureImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

//import com.icox.manager.R;
//import com.polites.android.GestureImageView;

/**
 * Created by sony on 2016/1/10
 */
public class LocalImageShower extends BaseActivity {

    private android.os.Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    hideVirtualkey(mDecorView);
                    break;
            }
        }
    };

    /**
     * 接收的值
     */
    // 当前路径下的图片集合
//    private ArrayList<String> mFilePathArray;
    private List<ImageInfo> mImageInfoList;

    //
    private String mImagePath;

    // 用户点的位置
    private int clickPosition;

    private ViewPager mViewPager;
    private MyPagerAdapter mMyPagerAdapter;
//    // adapter data
//    private View[] views;

    private View mDecorView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_image_shower);

        mDecorView = getWindow().getDecorView();
        hideVirtualkey(mDecorView);
        mContext = this;

        // 屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 获取数据
        getData();
        // 实例化控件
        initView();
        // 设置适配器
        setImageAdapter();
        // 设置监听事件
        setListener();
    }

    /**
     * 获取图片数据与位置
     */
    private void getData() {
//        // 从本地相册查看多个图片
//        mFilePathArray = getIntent().getStringArrayListExtra("ArrayDirPath");
//        // 从文件管理器，查看单个的图片;
//        if (mFilePathArray == null) {
//            mFilePathArray = new ArrayList<String>();
//            mFilePathArray.add(getIntent().getStringExtra("path"));
//        }
//        clickPosition = getIntent().getIntExtra("clickPosition", 0);

        mImagePath = getIntent().getStringExtra("image_path");
        File file = new File(mImagePath);

        mImageInfoList = new ArrayList<ImageInfo>();
        getLocalImageFiles(file.getParentFile());
    }

    private void getLocalImageFiles(File file) {

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {
                String name = file.getName();
                int i = name.lastIndexOf('.');

                if (file.isDirectory()) {
//                    getLocalImageFiles(file);
                    return false;
                }
                else if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".jpg")
                            || name.equalsIgnoreCase(".jpeg")
//                            || name.equalsIgnoreCase(".gif")
                            || name.equalsIgnoreCase(".png")
                            || name.equalsIgnoreCase(".bmp")
//                            || name.equalsIgnoreCase(".tiff")
//                            || name.equalsIgnoreCase(".raw")
                            ) {
                        ImageInfo imageInfo = new ImageInfo();
                        imageInfo.fileName = file.getName();
                        imageInfo.filePath = file.getAbsolutePath();
                        if (mImagePath.equalsIgnoreCase(imageInfo.filePath)){
                            clickPosition = mImageInfoList.size();
                        }

                        mImageInfoList.add(imageInfo);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * 隐藏虚拟按键
     */
    public void hideVirtualkey(View decorView) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    /**
     * 实例化控件
     */
    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.vp_view);
    }

    /**
     * 设置适配器
     */
    private void setImageAdapter() {
        mMyPagerAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mMyPagerAdapter);
        // 设置视图的位置
        mViewPager.setCurrentItem(clickPosition);
    }

    /**
     * 设置监听事件
     */
    private void setListener() {
        mViewPager.setOnPageChangeListener(new MyPageChangeListener());
    }

    private class MyPagerAdapter extends PagerAdapter {

        public MyPagerAdapter() {
//            views = new View[mImageInfoList.size()];
        }

        @Override
        public int getCount() {
            return mImageInfoList.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            //判断将要显示的图片是否和现在显示的图片是同一个
            //arg0为当前显示的图片，arg1是将要显示的图片
            return arg0 == arg1;
        }

//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            //销毁该图片
//            container.removeView(views[position]);
//            views[position] = null;
//
//            // TODO 处理ViewPager的载入大图Out of Memory问题
////            将 Bitmap bitmap 设置成全局,instantiateItem()里的bitmap也是赋予此值,进行删除
////            Bitmap bitmap = BitmapFactory.decodeFile(mFilePathArray.get(position));
////            bitmap.recycle();
//        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View convertView = LayoutInflater.from(LocalImageShower.this).inflate(R.layout.item_image_shower, null);

//            Bitmap bitmap = BitmapFactory.decodeFile(mImageInfoList.get(position).filePath);
//            views[position] = convertView;
            container.addView(convertView);
            //TODO GestureImageView
            GestureImageView gestureImageview = (GestureImageView) convertView.findViewById(R.id.gestureImageview);
//            ImageView gestureImageview = (ImageView) convertView.findViewById(R.id.gestureImageview);
//            gestureImageview.setImageBitmap(bitmap);
            Picasso.with(mContext)
                    .load(new File(mImageInfoList.get(position).filePath))
//                    .resize(ImageDirActivity.mScreenWidth, ImageDirActivity.mScreenHeight)
                    .error(R.mipmap.ic_launcher)
                    .into(gestureImageview);

//            gestureImageview.setOnClickListener(null);
            gestureImageview.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View view) {
                    //TODO onLongClick
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    String path = mImageInfoList.get(position).filePath;
                    Uri uri = Uri.parse(path);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "分享图片到"));
                    return false;
                }
            });
            return convertView;
        }
    }

    // 监听ViewPager的变化
    private class MyPageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
//            Log.i("PageChange", "onPageScrollStateChanged:" + arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
//            Log.i("PageChange", "onPageScrolled:" + arg0);
//            Log.i("PageChange", "onPageScrolled:" + arg1);
//            Log.i("PageChange", "onPageScrolled:" + arg2);
        }

        @Override
        public void onPageSelected(int position) {
//            Log.i("PageChange", "onPageSelected:" + position);

            handler.removeMessages(1);
            handler.sendEmptyMessageDelayed(1, 9000);
        }
    }

    /**
     * 按键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP){
            return super.onKeyDown(keyCode, event);
        }

        // 菜单键
        if (KeyEvent.KEYCODE_MENU == keyCode) {
            if (getCurrentFocus().getId() == R.id.vp_view){
                int position = mViewPager.getCurrentItem();

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                String path = mImageInfoList.get(position).filePath;
                Uri uri = Uri.parse(path);
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "分享图片到"));

                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    public void initWindow() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
