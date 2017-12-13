package com.icox.imageview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.icox.imageview.fragment.ImageInfo;
import com.icox.imageview.fragment.ImagePageFragment;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liujian on 2017/2/25.
 */

public class ImageShowActivity extends BaseActivity {

    private final int NUM_COLUMNS = 5;
    private final int NUM_RANK = 3;

    private Context mContext;
    private View mDecorView;

    private String mDirPath;
    private String mFilePath;
    private ViewPager mViewPager;
    private List<ImageInfo> mImageInfoList;
    private MyAdapter mMyAdapter;

    private int mCurPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_show);
        mContext = this;
        mDecorView = getWindow().getDecorView();

        mDirPath = getIntent().getStringExtra("dir_path");
        mFilePath = getIntent().getStringExtra("file_path");

        hideSystemUI(mDecorView);

        initPager();
    }

    /**
     * 沉浸模式
     */
    public static void hideSystemUI(View decorView) {
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void initPager(){
        mViewPager = (ViewPager) findViewById(R.id.vp_image_show);

        mImageInfoList = new ArrayList<ImageInfo>();
        getLocalImageFiles(new File(mDirPath));

        mMyAdapter = new MyAdapter(getSupportFragmentManager(), mImageInfoList);
        mViewPager.setAdapter(mMyAdapter);

        mViewPager.setCurrentItem(mCurPosition);
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
                        imageInfo.coverType = 1;
                        imageInfo.fileName = file.getName();
                        imageInfo.filePath = file.getAbsolutePath();

                        if (mFilePath.equals(imageInfo.filePath)){
                            mCurPosition = mImageInfoList.size();
                        }

                        mImageInfoList.add(imageInfo);
                        return true;
                    }
                }
                return false;
            }
        });
    }


    public class MyAdapter extends FragmentStatePagerAdapter {
        private List<ImageInfo> mImageInfoList;

        public MyAdapter(FragmentManager fm, List<ImageInfo> imageInfoList) {
            super(fm);

            this.mImageInfoList = imageInfoList;
        }

        @Override
        public Fragment getItem(int position) {
            ImagePageFragment imagePageFragment = ImagePageFragment.newInstance(mImageInfoList.get(position).filePath, position);
            return imagePageFragment;
        }

        @Override
        public int getCount() {
            return mImageInfoList.size();
        }
    }
}
