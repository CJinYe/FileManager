package com.icox.imageview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.icox.imageview.fragment.ImageFragment;
import com.icox.imageview.fragment.ImageInfo;
import com.icox.imageview.utils.FileUtil;
import com.icox.imageview.utils.LayoutViewLocation;
import com.icox.imageview.view.ImagePagerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jlfxs on 2016/10/28.
 */

public class ImageDirActivity extends BaseActivity {
    private final int[][] VIEW_LOCATION = new int[][]{
            {576, 33, 740, 100, R.mipmap.baby_main_title},      // 标题
            {334, 160, 1201, 651, -1},      // viewpager
            {64, 180, 300, 287, R.drawable.sel_btn_bendi},
            {64, 355, 300, 462, R.drawable.sel_btn_tfka},
            {64, 530, 300, 637, R.drawable.sel_btn_upan},
            {1121, 44, 1189, 113, R.drawable.pre_btn_back}
    };

    public final static int BG_IMAGE_WIDTH = 1280;
    public final static int BG_IMAGE_HEIGHT = 750;

    private final int NUM_COLUMNS = 4;
    private final int NUM_RANK = 2;

//    private Context mContext;
    public static Context mContext;

    public static int mScreenWidth;
    public static int mScreenHeight;

//    private ImagePagerView mImagePagerView;
    private List<ImageInfo> mImageInfoList;

    private LayoutViewLocation mViewLocation;
    private RelativeLayout mMainLayout;

    private ImageView mBtnBendi;
    private ImageView mBtnTfka;
    private ImageView mBtnUpan;

    private List<ImageInfo> mBendiImageInfoList;
    private List<ImageInfo> mTfkaImageInfoList;
    private List<ImageInfo> mUpanImageInfoList;

    private ImagePagerView mBendiImagePagerView;
    private ImagePagerView mTfkaImagePagerView;
    private ImagePagerView mUpanImagePagerView;

    private ImageView mNoFiles;

    private String[] mVolumePaths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_dir);

        mContext = this;

        mVolumePaths = FileUtil.getVolumePaths(mContext);
        if (mVolumePaths == null){
            Toast.makeText(mContext, "无法识别存储设备", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mViewLocation = new LayoutViewLocation(mContext, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);
        mMainLayout = (RelativeLayout) findViewById(R.id.activity_image_dir);

        getScreenInfo();

        initLocationView();



        mBendiImageInfoList = new ArrayList<ImageInfo>();
        mTfkaImageInfoList = new ArrayList<ImageInfo>();
        mUpanImageInfoList = new ArrayList<ImageInfo>();
        initPager();
    }

    private void initLocationView(){
        ImageView imageView = new ImageView(mContext);
        imageView.setBackgroundResource(VIEW_LOCATION[0][4]);
        mViewLocation.addViewByLocation(mMainLayout, imageView, VIEW_LOCATION[0]);

        mBtnBendi = new ImageView(mContext);
        mBtnBendi.setBackgroundResource(VIEW_LOCATION[2][4]);
        mBtnBendi.setTag(2);
        mViewLocation.addViewByLocation(mMainLayout, mBtnBendi, VIEW_LOCATION[2]);
        mBtnBendi.setOnClickListener(mBtnClickListener);

        mBtnTfka = new ImageView(mContext);
        mBtnTfka.setBackgroundResource(VIEW_LOCATION[3][4]);
        mBtnTfka.setTag(3);
        mViewLocation.addViewByLocation(mMainLayout, mBtnTfka, VIEW_LOCATION[3]);
        mBtnTfka.setOnClickListener(mBtnClickListener);

        mBtnUpan = new ImageView(mContext);
        mBtnUpan.setBackgroundResource(VIEW_LOCATION[4][4]);
        mBtnUpan.setTag(4);
        mViewLocation.addViewByLocation(mMainLayout, mBtnUpan, VIEW_LOCATION[4]);
        mBtnUpan.setOnClickListener(mBtnClickListener);

        initBackBtn();
    }

    private void initBackBtn(){
        ImageView imageView = new ImageView(mContext);
        imageView.setBackgroundResource(VIEW_LOCATION[5][4]);
        mViewLocation.addViewByLocation(mMainLayout, imageView, VIEW_LOCATION[5]);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mBtnBendi.setSelected(false);
            mBtnTfka.setSelected(false);
            mBtnUpan.setSelected(false);

            hideImagePagerView();

            int tag = (Integer) v.getTag();
            if (tag == 2){
                mBtnBendi.setSelected(true);
                clickBendiEvent();
            }
            else if (tag == 3){
                mBtnTfka.setSelected(true);
                clickTfkaEvent();
            }
            else if (tag == 4){
                mBtnUpan.setSelected(true);
                clickUpanEvent();
            }
        }
    };

    private void getScreenInfo(){
        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
    }

    private void initPager(){
        mBendiImagePagerView = (ImagePagerView) findViewById(R.id.ipv_bendi_dir_list);
        mTfkaImagePagerView = (ImagePagerView) findViewById(R.id.ipv_tfka_dir_list);
        mUpanImagePagerView = (ImagePagerView) findViewById(R.id.ipv_upan_dir_list);

        mNoFiles = (ImageView) findViewById(R.id.iv_no_files);

        hideImagePagerView();

        mBtnBendi.setSelected(true);
        clickBendiEvent();
    }

    private void hideImagePagerView(){
        mBendiImagePagerView.setVisibility(View.GONE);
        mTfkaImagePagerView.setVisibility(View.GONE);
        mUpanImagePagerView.setVisibility(View.GONE);

        mNoFiles.setVisibility(View.GONE);
    }

    private void initImagePagerView(ImagePagerView pagerView, List<ImageInfo> list){
        pagerView.initView(getSupportFragmentManager(), list, NUM_COLUMNS, NUM_RANK,
                getViewWidth(VIEW_LOCATION[1])/NUM_COLUMNS,
                getViewHeight(VIEW_LOCATION[1])/NUM_RANK,
                mImageClickListener);
    }

    public static int getViewWidth(int[] location){
        int width = mScreenWidth * (location[2]-location[0]) / BG_IMAGE_WIDTH;
        return width;
    }

    public static int getViewHeight(int[] location){
        int height = mScreenHeight * (location[3]-location[1]) / BG_IMAGE_HEIGHT;
        return height;
    }

    private void clickBendiEvent(){
        if (mBendiImageInfoList.size() == 0){
//            String mDirPath = Environment.getExternalStorageDirectory() + "/Pictures/切图/";
            mImageInfoList = new ArrayList<ImageInfo>();
            getLocalImageFiles(new File(mVolumePaths[0]));

            mBendiImageInfoList.addAll(mImageInfoList);

            initImagePagerView(mBendiImagePagerView, mBendiImageInfoList);
        }

        if (mBendiImageInfoList.size() == 0){
            mNoFiles.setVisibility(View.VISIBLE);
        }else {
            mBendiImagePagerView.setVisibility(View.VISIBLE);
        }
    }

    private void clickTfkaEvent(){
        if (mTfkaImageInfoList.size() == 0){
            if (mVolumePaths.length >= 2) {
                mImageInfoList = new ArrayList<ImageInfo>();
                getLocalImageFiles(new File(mVolumePaths[1]));

                mTfkaImageInfoList.addAll(mImageInfoList);

                initImagePagerView(mTfkaImagePagerView, mTfkaImageInfoList);
            }
        }

        if (mTfkaImageInfoList.size() == 0){
            mNoFiles.setVisibility(View.VISIBLE);
        }else {
            mTfkaImagePagerView.setVisibility(View.VISIBLE);
        }
    }

    private void clickUpanEvent() {
        if (mUpanImageInfoList.size() == 0) {
            if (mVolumePaths.length >= 3) {
                mImageInfoList = new ArrayList<ImageInfo>();
                for (int i = 2; i < mVolumePaths.length; i++) {
                    getLocalImageFiles(new File(mVolumePaths[i]));
                }

                mUpanImageInfoList.addAll(mImageInfoList);

                initImagePagerView(mUpanImagePagerView, mUpanImageInfoList);
            }
        }

        if (mUpanImageInfoList.size() == 0) {
            mNoFiles.setVisibility(View.VISIBLE);
        } else{
            mUpanImagePagerView.setVisibility(View.VISIBLE);
        }
    }

    private ImageFragment.ImageClickListener mImageClickListener = new ImageFragment.ImageClickListener() {
        @Override
        public void imageClick(ImageInfo imageInfo) {
            String dir_path = imageInfo.filePath;
            int index = dir_path.lastIndexOf("/");
            if (index != -1) {
                dir_path = dir_path.substring(0, index);
            }

            Intent intent = new Intent(mContext, ImageViewActivity.class);
            intent.putExtra("dir_path", dir_path);
            intent.putExtra("dir_name", imageInfo.fileName);
            startActivity(intent);
        }
    };

    private void getLocalImageFiles(File dirFile) {
        boolean getDirImage = false;

        File[] fileList = dirFile.listFiles();
        if (fileList == null){
            return;
        }
        for (int i = 0; i < fileList.length; i++){
            File file = fileList[i];
            String filePath = file.getAbsolutePath();
//            Log.i("test", "filePath = " + filePath);

//            String name = file.getName();
            if (filePath.contains("/freenote/")
                    ||filePath.contains("/freenote_temp/")){
                // 屏蔽记事本文件夹
                continue;
            }

            if (i == 0) {
                if (!filePath.contains("家庭宝")
                        && !filePath.contains("e家亲")
                        && !filePath.contains("相册")
                        && !filePath.contains("DCIM")
                        && !filePath.contains("Pictures")) {
                    continue;
                }
            }

            if (file.isFile() && !getDirImage){
                if (filePath.endsWith(".jpg")
                        || filePath.endsWith(".jpeg")
//                    || name.endsWith(".gif")
                        || filePath.endsWith(".png")
                        || filePath.endsWith(".bmp")
//                    || name.endsWith(".tiff")
//                    || name.endsWith(".raw")
                        ) {
                    getDirImage = true;

                    String dir_name = file.getParent();
                    int index = dir_name.lastIndexOf("/");
                    if (index != -1) {
                        dir_name = dir_name.substring(index + 1);
                    }

                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.fileName = dir_name;
                    imageInfo.filePath = filePath;

                    mImageInfoList.add(imageInfo);
                }
            }
            else if (file.isDirectory()){
                if (!file.getName().contains(".")) {
                    getLocalImageFiles(file);
                }
            }
        }

//        file.listFiles(new FileFilter() {
//
//            @Override
//            public boolean accept(final File file) {
//                String name = file.getName();
//                int i = name.lastIndexOf('.');
//
//                if (file.isDirectory()) {
//                    getLocalImageFiles(file);
//                    return false;
//                }
//                else if (i != -1) {
//                    name = name.substring(i);
//                    if (name.equalsIgnoreCase(".jpg")
//                            || name.equalsIgnoreCase(".jpeg")
////                            || name.equalsIgnoreCase(".gif")
//                            || name.equalsIgnoreCase(".png")
//                            || name.equalsIgnoreCase(".bmp")
////                            || name.equalsIgnoreCase(".tiff")
////                            || name.equalsIgnoreCase(".raw")
//                            ) {
//                        ImageInfo imageInfo = new ImageInfo();
//                        imageInfo.fileName = file.getName();
//                        imageInfo.filePath = file.getAbsolutePath();
//
//                        mImageInfoList.add(imageInfo);
//                        return true;
//                    }
//                }
//                return false;
//            }
//        });
    }
}
