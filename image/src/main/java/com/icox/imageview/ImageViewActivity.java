package com.icox.imageview;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icox.imageview.fragment.ImageFragment;
import com.icox.imageview.fragment.ImageInfo;
import com.icox.imageview.utils.LayoutViewLocation;
import com.icox.imageview.view.ImagePagerView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import static com.icox.imageview.ImageDirActivity.BG_IMAGE_HEIGHT;
import static com.icox.imageview.ImageDirActivity.BG_IMAGE_WIDTH;

//public class ImageViewActivity extends FragmentActivity {
public class ImageViewActivity extends BaseActivity {

    private final int[][] VIEW_LOCATION = new int[][]{
            {1121, 44, 1189, 113, R.drawable.pre_btn_back},
            {-1, 100, -1, 750, -1}
    };

    private final int NUM_COLUMNS = 5;
    private final int NUM_RANK = 3;

    private Context mContext;

    private String mDirName;
    private String mDirPath;

    private ImagePagerView mImagePagerView;

    private List<ImageInfo> mImageInfoList;

    private LayoutViewLocation mViewLocation;
    private RelativeLayout mMainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_view);

        mContext = this;

        mDirName = getIntent().getStringExtra("dir_name");
        mDirPath = getIntent().getStringExtra("dir_path");


        mViewLocation = new LayoutViewLocation(mContext, BG_IMAGE_WIDTH, BG_IMAGE_HEIGHT);
        mMainLayout = (RelativeLayout) findViewById(R.id.activity_image_view);

        initPager();

        initBackBtn();

        setTitleName();
    }

    private void setTitleName(){
        TextView textView = (TextView) findViewById(R.id.tv_title_name);
        textView.setText(mDirName);

        textView.setTextSize(48);
        textView.getPaint().setFakeBoldText(true);

//        AssetManager mgr = getAssets();
//        Typeface tf = Typeface.createFromAsset(mgr, "fonts/fzzyjt.ttf");
//        textView.setTypeface(tf);
    }

    private void initBackBtn(){
        ImageView imageView = new ImageView(mContext);
        imageView.setBackgroundResource(VIEW_LOCATION[0][4]);
        mViewLocation.addViewByLocation(mMainLayout, imageView, VIEW_LOCATION[0]);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initPager(){
        mImagePagerView = (ImagePagerView) findViewById(R.id.ipv_image_view_list);

//        String mDirPath = Environment.getExternalStorageDirectory() + "/Pictures/1/";
        mImageInfoList = new ArrayList<ImageInfo>();
        getLocalImageFiles(new File(mDirPath));

        mImagePagerView.initView(getSupportFragmentManager(), mImageInfoList, NUM_COLUMNS, NUM_RANK,
                ImageDirActivity.getViewWidth(VIEW_LOCATION[1])/NUM_COLUMNS,
                ImageDirActivity.getViewHeight(VIEW_LOCATION[1])/NUM_RANK,
                mImageClickListener);
    }

    private ImageFragment.ImageClickListener mImageClickListener = new ImageFragment.ImageClickListener() {
        @Override
        public void imageClick(ImageInfo imageInfo) {
//            // 传当前的图片路径,自用的图片查看器
//            Intent icoxImageIntent = new Intent(mContext, LocalImageShower.class);
//            icoxImageIntent.putExtra("image_path", imageInfo.filePath);
//            startActivity(icoxImageIntent);

            Intent intent = new Intent(mContext, ImageShowActivity.class);
            intent.putExtra("dir_path", mDirPath);
            intent.putExtra("file_path", imageInfo.filePath);
            startActivity(intent);
        }
    };

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

                        mImageInfoList.add(imageInfo);
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
