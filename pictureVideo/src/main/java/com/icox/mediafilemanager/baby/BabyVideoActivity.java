package com.icox.mediafilemanager.baby;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icox.mediafilemanager.GlobalData;
import com.icox.mediafilemanager.R;
import com.icox.share.BaseActivity;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.icoxedu.app_login.SetupCheck;

//import org.apache.http.util.EncodingUtils;

public class BabyVideoActivity extends BaseActivity {

    private Context mContext;

    //    private ImageButton mBtnBendi;
    //    private ImageButton mBtnTfka;
    //    private ImageButton mBtnUpan;
    private final int[][] BTN_DATA = new int[][]{
            {1121, 44, 1189, 113, R.drawable.pre_back},
            {107, 299, 240, 361, R.drawable.sel_bendi},
            {107, 386, 240, 448, R.drawable.sel_tf},
            {107, 476, 240, 538, R.drawable.sel_upan}
    };
    private ImageView[] mBtnView;

    private final int[] VIEW_PAGER = new int[]{
            395, 195, 1164, 633
    };

    private final String IMAGE = "image";
    private final String VIDEO = "video";
    private String mMediaType = IMAGE;

    private int mMntType = 0;

    private ViewPager mViewPager;//用于显示图片
    private MyPagerAdapter mAdapter;//viewPager的适配器

    private boolean mbGetDir;
    private List<String> mDirPath;
    private List<String> mImagePath;

    private String[] mVolumePaths;

    private LinearLayout mLoadingLayout;

    private ImageView mNullShow;

    private TextView mPageIndex;

    // 背景图宽高
    public final static int BG_WIDTH = 1280;
    public final static int BG_HEIGHT = 750;
    // 屏幕宽高
    public static int mScreenWidth;
    public static int mScreenHeight;
    //    // 背景图实际宽高
    //    private final int mBgWidth = 1024;
    //    private final int mBgHeight = 552;

    //    // ImageLoader - 优化图片加载
    //    private DisplayImageOptions mOptions;
    //    private ImageLoader mImageLoader;
    //    private ImageLoadingListener mAnimateFirstListener;

    //注册检测服务名
    private final String ICOX_SERVICE = "icoxapp.checking";
    //注册程序返回信息标记
    private final String ICOX_RETURN = "ICOX_SETUP";
    //注册状态 1为已注册 其他为未注册
    private static int loginState;
    //绑定服务状态
    private boolean isLink = false;
    //注册检测接口
    private SetupCheck login_interface = null;

    private ServiceConnection serConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            login_interface = SetupCheck.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            login_interface = null;
        }
    };

    private boolean register() {

        if (isLink && login_interface != null) {
            try {
                loginState = login_interface.getState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (loginState == 1) {
                return true;
            }
        }
        return false;
    }

    // 安卓5.0显示调用service
    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_baby_video);
        mContext = this;

        registerHomeKeyReceiver(this);
        new Thread(new Runnable() {

            @Override
            public void run() {

                Intent mIntent = new Intent();
                mIntent.setAction(ICOX_SERVICE);
                Intent eintent = new Intent(getExplicitIntent(mContext, mIntent));
                isLink = bindService(eintent, serConn, BIND_AUTO_CREATE);
            }
        }).start();

        //        mMediaType = getIntent().getStringExtra("MediaType");
        mMediaType = "video";

        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        if (Utils.hasAppInstalled(mContext, "com.icox.home_child_xinan")
                || Utils.hasAppInstalled(mContext, "com.icox.home_child_xinan_key")) {
            mScreenHeight = getDpi(mContext);
        }

        initBtnView();

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.baby_video_rl);
        initItemLocation(relativeLayout, VIEW_PAGER);

        mLoadingLayout = (LinearLayout) findViewById(R.id.main_ll_loading);
        mNullShow = (ImageView) findViewById(R.id.main_iv_null);
        if (mMediaType.equals(VIDEO)) {
            mNullShow.setBackgroundResource(R.drawable.media_9);
        } else if (mMediaType.equals(IMAGE)) {
            mNullShow.setBackgroundResource(R.drawable.media_10);
        }

        mDirPath = new ArrayList<String>();
        mImagePath = new ArrayList<String>();

        mVolumePaths = getVolumePaths(mContext);

        //显示图片的VIew
        mViewPager = (ViewPager) findViewById(R.id.main_vp_dir);
        //为viewPager设置适配器
        mAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mAdapter);

        //
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setPageIndex(position + 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        handler.sendEmptyMessage(INIT_ARRAYLIST);

    }

    private void initBtnView() {
        AbsoluteLayout absoluteLayout = (AbsoluteLayout) findViewById(R.id.baby_video_al);

        mBtnView = new ImageView[BTN_DATA.length];
        for (int i = 1; i < BTN_DATA.length; i++) {
            mBtnView[i] = new ImageView(mContext);
            absoluteLayout.addView(mBtnView[i]);

            initItemLocation(mBtnView[i], BTN_DATA[i]);
            mBtnView[i].setBackgroundResource(BTN_DATA[i][4]);
            mBtnView[i].setTag(i);
            mBtnView[i].setOnClickListener(mBtnClickListener);
        }
        mBtnView[1].setSelected(true);


        findViewById(R.id.btn_key_back_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPageIndex = (TextView) findViewById(R.id.tv_page_index);
    }

    private void setPageIndex(int index) {
        mPageIndex.setText("-" + index + "-");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterHomeKeyReceiver(this);
    }

    public static int getDpi(Context context) {
        int dpi = 0;
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    public boolean hasPermanentKey() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        if (hasBackKey && hasHomeKey) {
            // 没有虚拟按键
            //            Log.i("test", "没有虚拟按键");
            return false;
        } else {
            // 有虚拟按键：99%可能。
            //            Log.i("test", "有虚拟按键：99%可能。");
            return true;
        }
    }

    public void initItemLocation(View view, int[] location) {
        int itemWith = (location[2] - location[0]) * mScreenWidth / BG_WIDTH;
        int itemHeight = (location[3] - location[1]) * mScreenHeight / BG_HEIGHT;
        AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(
                itemWith,
                itemHeight,
                location[0] * mScreenWidth / BG_WIDTH,
                location[1] * mScreenHeight / BG_HEIGHT);

        view.setLayoutParams(lp);
    }

    private View.OnClickListener mBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int tag = (Integer) v.getTag();

            selectBtn(tag);
            setPageIndex(1);

            switch (tag) {
                case 0:
                    finish();
                    break;
                case 1:
                    mMntType = 0;
                    handler.sendEmptyMessage(INIT_ARRAYLIST);
                    break;
                case 2:
                    mMntType = 1;
                    handler.sendEmptyMessage(INIT_ARRAYLIST);
                    break;
                case 3:
                    mMntType = 2;
                    handler.sendEmptyMessage(INIT_ARRAYLIST);
                    break;
            }
        }
    };

    private void selectBtn(int tag) {
        for (int i = 1; i < mBtnView.length; i++) {
            if (i == tag) {
                mBtnView[i].setSelected(true);
            } else {
                mBtnView[i].setSelected(false);
            }
        }
    }

    private static final int INIT_ARRAYLIST = 1;
    private static final int INIT_ADPTER = 2;
    private static final int UPDATE_ADPTER = 3;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case INIT_ARRAYLIST:

                    mNullShow.setVisibility(View.GONE);
                    mLoadingLayout.setVisibility(View.VISIBLE);
                    //                    initImageSource(mMntType, mMediaType);
                    //                    handler.sendEmptyMessage(INIT_ADPTER);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            initImageSource(mMntType, mMediaType);
                            handler.sendEmptyMessage(INIT_ADPTER);
                        }
                    }).start();
                    break;
                case INIT_ADPTER:
                    //为viewPager设置适配器
                    mAdapter = new MyPagerAdapter();
                    mViewPager.setAdapter(mAdapter);
                    mLoadingLayout.setVisibility(View.GONE);

                    if (mImagePath.size() == 0) {
                        mNullShow.setVisibility(View.VISIBLE);
                        mPageIndex.setVisibility(View.GONE);
                    } else {
                        mNullShow.setVisibility(View.GONE);
                        mPageIndex.setVisibility(View.VISIBLE);
                    }
                    break;
                case UPDATE_ADPTER:
                    mAdapter.updateData();
                    mAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private void initImageSource(int index, String mediaType) {

        mDirPath.clear();
        mImagePath.clear();

        if (index == 0) {
            //            String path = "/mnt/media/"
            String path = GlobalData.IAIWAI_FILE_DIR;
            File file = new File(path);
            if (file.exists()) {
                if (mediaType.equals(VIDEO)) {
                    getLocalVideoFiles(file);
                } else if (mediaType.equals(IMAGE)) {
                    getLocalImageFiles(file);
                }
            }
        }

        //        Log.i("test", "index = " + index + ", mVolumePaths.length = " + mVolumePaths.length);
        if (index < mVolumePaths.length && index < 2) {
            //            String path = mVolumePaths[index] + "/Thumbs.ms";
            String path = mVolumePaths[index];
            File file = new File(path);
            if (file.exists()) {
                if (mediaType.equals(VIDEO)) {
                    getLocalVideoFiles(file);
                } else if (mediaType.equals(IMAGE)) {
                    getLocalImageFiles(file);
                }
            }
        } else {
            Log.i("mVolumePaths", "mVolumePaths.length:" + mVolumePaths.length);
            for (int i = 2; i < mVolumePaths.length; i++) {
                String path = mVolumePaths[i];
                Log.i("mVolumePaths", "path:" + path);
                File file = new File(path);
                if (file.exists()) {
                    if (mediaType.equals(VIDEO)) {
                        getLocalVideoFiles(file);
                    } else if (mediaType.equals(IMAGE)) {
                        getLocalImageFiles(file);
                    }
                }
            }
        }
    }

    public static String[] getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;
        try {
            Method methodGetPaths = storageManager.getClass().getMethod("getVolumePaths");
            paths = (String[]) methodGetPaths.invoke(storageManager);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return paths;
    }

    private void getLocalImageFiles(File file) {

        mbGetDir = false;

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {
                String name = file.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    if (mbGetDir) {
                        return false;
                    }

                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".jpg")
                            || name.equalsIgnoreCase(".jpeg")
                            //                            || name.equalsIgnoreCase(".gif")
                            || name.equalsIgnoreCase(".png")
                            || name.equalsIgnoreCase(".bmp")
                        //                            || name.equalsIgnoreCase(".tiff")
                        //                            || name.equalsIgnoreCase(".raw")
                            ) {
                        String filePath = file.getAbsolutePath();
                        int index = filePath.lastIndexOf('/');
                        String dirPath = filePath.substring(0, index);

                        if (!mDirPath.contains(dirPath)) {
                            mDirPath.add(dirPath);
                            mImagePath.add(filePath);
                        }

                        //
                        //                        Collections.sort(mDirPath);
                        //                        Collections.sort(mImagePath);

                        mbGetDir = true;
                        return true;
                    }
                } else if (file.isDirectory()) {
                    getLocalImageFiles(file);
                }
                return false;
            }
        });
    }

    private void getLocalVideoFiles(File file) {

        mbGetDir = false;

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {

                // test
                //                String test = "/mnt/extsd/Thumbs.ms/com1.{d3e34b21-9d75-101a-8c3d-00aa001a1652}/ ..";
                ////              String filePathTest = file.getAbsolutePath();
                //                String filePathTest = file.getPath();
                //                Log.i("test", "filePathTest1 = " + filePathTest);

                String name = file.getName();
                int i = name.lastIndexOf('.');
                //                Log.i("test", "name = " + name);
                //                Log.i("test", "i = " + i);
                if (i != -1) {
                    if (mbGetDir) {
                        return false;
                    }

                    String filePath = file.getAbsolutePath();

                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".mp4")
                            || name.equalsIgnoreCase(".jtb")
                            || name.equalsIgnoreCase(".jkv")
                            || name.equalsIgnoreCase(".cye")
                            || name.equalsIgnoreCase(".3gp")
                            //                            || name.equalsIgnoreCase(".wmv")
                            //                            || name.equalsIgnoreCase(".ts")
                            || name.equalsIgnoreCase(".rmvb")
                            //                            || name.equalsIgnoreCase(".mov")
                            //                            || name.equalsIgnoreCase(".m4v")
                            || name.equalsIgnoreCase(".avi")
                            //                            || name.equalsIgnoreCase(".m3u8")
                            //                            || name.equalsIgnoreCase(".3gpp")
                            //                            || name.equalsIgnoreCase(".3gpp2")
                            || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv")
                            //                            || name.equalsIgnoreCase(".divx")
                            //                            || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm")
                        //                            || name.equalsIgnoreCase(".ts")
                        //                            || name.equalsIgnoreCase(".asf")
                        //                            || name.equalsIgnoreCase(".ram")
                        //                            || name.equalsIgnoreCase(".mpg")
                        //                            || name.equalsIgnoreCase(".v8")
                        //                            || name.equalsIgnoreCase(".swf")
                        //                            || name.equalsIgnoreCase(".m2v")
                        //                            || name.equalsIgnoreCase(".asx")
                        //                            || name.equalsIgnoreCase(".ra")
                        //                            || name.equalsIgnoreCase(".ndivx")
                        //                            || name.equalsIgnoreCase(".xvid")
                            ) {
                        //                        String filePath = file.getAbsolutePath();
                        int index = filePath.lastIndexOf('/');
                        String dirPath = filePath.substring(0, index);
                        if (!mDirPath.contains(dirPath)) {
                            mDirPath.add(dirPath);
                            mImagePath.add(filePath);
                        }

                        //                        Collections.sort(mDirPath);
                        //                        Collections.sort(mImagePath);

                        mbGetDir = true;
                        return true;
                    }
                    //                    else if (filePath.contains("Thumbs.ms") && !filePath.contains("LastF/")){
                    //                        Log.i("test", "有后缀名的文件夹：" + file.getName());
                    //                        if (file.canRead() && i == 5){
                    //                            Log.i("test", "可读的文件夹：" + file.getName());
                    ////                            boolean bl = file.renameTo(new File("/mnt/extsd/Thumbs.ms/com1.{d3e34b21-9d75-101a-8c3d-00aa001a1652}/test01"));
                    ////                            if (bl){
                    ////                                Log.i("test", "改名成功：" + file.getName());
                    ////                            }
                    //                            try {
                    //                                getFileSize(file);
                    //                                Log.i("test", "getFileSize(file)：" + getFileSize(file));
                    //                            } catch (Exception e) {
                    //                                e.printStackTrace();
                    //                            }
                    //                        }
                    //                        getLocalVideoFiles(file);
                    //                    }
                }
                //                else if (filePathTest.contains("Thumbs.ms")) {
                //                    getLocalVideoFiles(file);
                //                }
                // 如果是文件夹，则继续找文件
                else if (file.isDirectory()) {
                    getLocalVideoFiles(file);
                }
                return false;
            }
        });
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     * @throws Exception
     */
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        } else {
            file.createNewFile();
            Log.e("获取文件大小", "文件不存在!");
        }
        return size;
    }

    private Bitmap createVideoThumbnail(String url, int width, int height) {

        //        // 家庭宝
        //        if (url.contains(".jtb")) {
        //            return null;
        //        }
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        int kind = MediaStore.Video.Thumbnails.MINI_KIND;
        try {
            if (Build.VERSION.SDK_INT >= 14) {
                retriever.setDataSource(url, new HashMap<String, String>());
            } else {
                retriever.setDataSource(url);
            }
            bitmap = retriever.getFrameAtTime();
            kind = MediaStore.Images.Thumbnails.MICRO_KIND;
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }
        if (kind == MediaStore.Images.Thumbnails.MICRO_KIND && bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return bitmap;
    }

    //	ViewPager每次仅最多加载三张图片（有利于防止发送内存溢出）
    private class MyPagerAdapter extends PagerAdapter {

        private final int pageItem = 6;
        private LayoutInflater layoutInflater;
        private int pageCount;
        private View[] views;
        private Bitmap[] bitmaps;

        public MyPagerAdapter() {

            this.layoutInflater = LayoutInflater.from(mContext);

            this.bitmaps = new Bitmap[mImagePath.size()];

            pageCount = (mImagePath.size() + (pageItem - 1)) / pageItem;
            this.views = new View[pageCount];
        }

        public void updateData() {
            this.bitmaps = new Bitmap[mImagePath.size()];
            pageCount = (mImagePath.size() + (pageItem - 1)) / pageItem;
            this.views = new View[pageCount];
        }

        public View getView(int position) {
            if (position < 0 || position >= views.length) {
                return null;
            }
            return views[position];
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            //判断将要显示的图片是否和现在显示的图片是同一个
            //arg0为当前显示的图片，arg1是将要显示的图片
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            for (int i = 0; i < pageItem; i++) {
                int itemTag = position * pageItem + i;
                if (itemTag >= bitmaps.length) {
                    break;
                }

                if (bitmaps[itemTag] != null && !bitmaps[itemTag].isRecycled()) {
                    bitmaps[itemTag].recycle();
                    System.gc();

                    bitmaps[itemTag] = null;
                }
            }

            //销毁该图片
            if (position < views.length) {
                container.removeView(views[position]);
                views[position] = null;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = layoutInflater.inflate(R.layout.viewpager_baby_video, null);
            views[position] = view;

            for (int i = 0; i < pageItem; i++) {
                RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.main_rl_file_01 + i * 3);
                int itemTag = position * pageItem + i;
                if (itemTag >= mImagePath.size()) {
                    layout.setVisibility(View.GONE);
                    continue;
                }

                File mediaFile = new File(mImagePath.get(itemTag));
                if (!mediaFile.exists() || !mediaFile.canRead()) {
                    bitmaps[itemTag] = null;

                    layout.setVisibility(View.GONE);
                    continue;
                } else if (mMediaType.equals(VIDEO)) {
                    bitmaps[itemTag] = ThumbnailUtils.createVideoThumbnail(mImagePath.get(itemTag), MediaStore.Images.Thumbnails.MICRO_KIND);
                    if (bitmaps[itemTag] == null) {
                        Resources r = BabyVideoActivity.this.getResources();
                        Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.format_media1);
                        bitmaps[itemTag] = bmp;
                    }
                } else if (mMediaType.equals(IMAGE)) {
                    bitmaps[itemTag] = decodeSampledBitmapFromFd(mImagePath.get(itemTag), 300, 300);
                    //                    bitmaps[itemTag] = null;
                }

                /*layout.setVisibility(View.VISIBLE);

                ImageView imageView = (ImageView) view.findViewById(R.id.main_viewpager_iv_01 + i * 3);
                if (bitmaps[itemTag] != null){
                    imageView.setImageBitmap(bitmaps[itemTag]);
                }else {
                    mImageLoader.displayImage("file://" + mImagePath.get(itemTag), imageView,
                            mOptions, mAnimateFirstListener);
                }
                imageView.setTag(itemTag);
                imageView.setOnClickListener(new ImageClickListener(position));
                imageView.setOnLongClickListener(new ImageOnLongClickListener(position));

                TextView textView = (TextView) view.findViewById(R.id.main_viewpager_tv_01 + i * 3);
                int index = mDirPath.get(itemTag).lastIndexOf('/');
                textView.setText(mDirPath.get(itemTag).substring(index + 1));
                textView.setMovementMethod(new ScrollingMovementMethod());          // 设置文本显示在规定区域内
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(24);
                textView.setTag(itemTag);*/


                if (bitmaps[itemTag] != null) {
                    layout.setVisibility(View.VISIBLE);

                    ImageView imageView = (ImageView) view.findViewById(R.id.main_viewpager_iv_01 + i * 3);
                    imageView.setImageBitmap(bitmaps[itemTag]);
                    //                    imageView.setBackground(new BitmapDrawable(bitmaps[itemTag]));
                    imageView.setTag(itemTag);
                    imageView.setOnClickListener(new ImageClickListener(position));
                    imageView.setOnLongClickListener(new ImageOnLongClickListener(position));

                    TextView textView = (TextView) view.findViewById(R.id.main_viewpager_tv_01 + i * 3);
                    int index = mDirPath.get(itemTag).lastIndexOf('/');
                    textView.setText(mDirPath.get(itemTag).substring(index + 1));
                    //                    textView.setMovementMethod(new ScrollingMovementMethod());          // 设置文本显示在规定区域内
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(24);
                    textView.setTag(itemTag);
                } else {
                    layout.setVisibility(View.GONE);
                }
            }

            //            mLoading.setVisibility(View.GONE);

            container.addView(view);
            return view;
        }
    }

    private class ImageClickListener implements View.OnClickListener {

        private int iPosition;

        public ImageClickListener(int position) {
            this.iPosition = position;
        }

        @Override
        public void onClick(View v) {

            // 注册
            if (!register()) {
                Toast.makeText(mContext, "请先注册！", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            //                viewPosition = iPosition;
            int tagPosition = (Integer) v.getTag();
            //            keyPosition = tagPosition - viewPosition * 10;
            //            clickEnter(tagPosition);

            if (tagPosition >= mDirPath.size()) {
                return;
            }

            Intent intent = new Intent(mContext, BabyVideoListActivity.class);
            intent.putExtra("DirPath", mDirPath.get(tagPosition));
            Log.i("IcoxVideo", "第一层点击的文件夹路径:" + mDirPath.get(tagPosition));
            intent.putExtra("MediaType", mMediaType);
            startActivity(intent);
        }
    }

    private class ImageOnLongClickListener implements View.OnLongClickListener {

        private int iPosition;

        public ImageOnLongClickListener(int position) {
            this.iPosition = position;
        }

        @Override
        public boolean onLongClick(View view) {
            deleteFileDialog(view);
            return false;
        }
    }

    // 删除文件对话框
    private void deleteFileDialog(final View view) {
        VideoDeleteDialog dialog = new VideoDeleteDialog(mContext) {
            @Override
            public void clickSure() {
                int tagPosition = (Integer) view.getTag();
                if (tagPosition >= mDirPath.size()) {
                    return;
                }
                File currentFile = new File(mDirPath.get(tagPosition));
                if (currentFile.exists() && currentFile.isDirectory()) {
                    deleteDirectory(mDirPath.get(tagPosition));
                }
                currentFile.delete();
                // 刷新当前
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
        };
        showDialog((Activity) mContext, dialog);


        //        AlertDialog.Builder icoxBuilder = new AlertDialog.Builder(BabyVideoActivity.this);
        //        icoxBuilder.setTitle("删除文件夹");
        //        icoxBuilder.setMessage("删除后不可恢复,是否继续?");
        //        icoxBuilder.setCancelable(true);
        //        icoxBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
        //            @Override
        //            public void onClick(DialogInterface dialogInterface, int i) {
        //
        //            }
        //        });
        //        icoxBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
        //            @Override
        //            public void onClick(DialogInterface dialogInterface, int i) {
        //                int tagPosition = (Integer) view.getTag();
        //                if (tagPosition >= mDirPath.size()) {
        //                    return;
        //                }
        //                File currentFile = new File(mDirPath.get(tagPosition));
        //                if (currentFile.exists() && currentFile.isDirectory()) {
        //                    deleteDirectory(mDirPath.get(tagPosition));
        //                }
        //                currentFile.delete();
        //                // 刷新当前
        //                handler.sendEmptyMessage(INIT_ARRAYLIST);
        //            }
        //        });
        //        icoxBuilder.create();
        //        icoxBuilder.show();
    }

    public static boolean isSuccess = false;

    public static boolean deleteDirectory(String currentPath) {

        // 根据要删除的[当前路径]转成File
        File currentFile = new File(currentPath);
        if (currentFile.isFile()) { // 若是文件则直接删除
            isSuccess = deleteOneFile(currentFile.getAbsolutePath());
        } else if (currentFile.isDirectory()) { // 若是文件夹则递归遍历删除
            File[] listFiles = currentFile.listFiles();
            for (File file : listFiles) {
                if (file.isDirectory()) { // 若是文件夹,递归遍历删除
                    deleteDirectory(file.getAbsolutePath() + "/");
                    file.delete();
                } else { // 若是文件则直接删除
                    isSuccess = deleteOneFile(file.getAbsolutePath());
                }
            }
            currentFile.delete();
        }
        return isSuccess;
    }

    /**
     * 删除文件操作
     *
     * @param currentPath
     * @return
     */
    public static boolean deleteOneFile(String currentPath) {

        boolean isSuccess = true;
        File currentFile = new File(currentPath);
        if (currentFile.exists()) {
            currentFile.delete();
            // 删除成功, 返回
            return true;
        } else {
            isSuccess = false;
        }
        return isSuccess;
    }

    // BitmapUtils
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // 从sd卡上加载图片
    public static Bitmap decodeSampledBitmapFromFd(String pathName,
                                                   int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }

    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {

        if (src == null) {
            return null;
        }

        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }


    /**
     * 增加动画
     */
    private void addIcoxAnimator(View view) {
        view.getId();
        ObjectAnimator a3 = null;
        ObjectAnimator a2 = null;
        a3 = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f);
        a2 = ObjectAnimator.ofFloat(view, "translationY", 0f, -50, 0f);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.play(a3).with(a2);
        // 设置插值器。
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }

    public void viewAnimation(View v, boolean hasFocus) {

        if (v == null) {
            return;
        }

        int focus = 0;
        if (hasFocus) {
            focus = R.anim.enlarge;
            addIcoxAnimator(v);
        } else {
            focus = R.anim.decrease;
        }
        //如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }

    /**
     * 按键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return super.onKeyDown(keyCode, event);
        }

        // 菜单键
        if (KeyEvent.KEYCODE_MENU == keyCode) {
            if (getCurrentFocus().getId() == R.id.main_viewpager_iv_01
                    || getCurrentFocus().getId() == R.id.main_viewpager_iv_02
                    || getCurrentFocus().getId() == R.id.main_viewpager_iv_03
                    || getCurrentFocus().getId() == R.id.main_viewpager_iv_04
                    || getCurrentFocus().getId() == R.id.main_viewpager_iv_05
                    || getCurrentFocus().getId() == R.id.main_viewpager_iv_06
                    ) {
                deleteFileDialog(getCurrentFocus());
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /************************/
    // 监听HOME键
    private static final String LOG_TAG = "HomeReceiver";
    private HomeWatcherReceiver mHomeKeyReceiver = null;

    public void registerHomeKeyReceiver(Context context) {
        Log.i(LOG_TAG, "registerHomeKeyReceiver");
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        context.registerReceiver(mHomeKeyReceiver, homeFilter);
    }

    public void unregisterHomeKeyReceiver(Context context) {
        Log.i(LOG_TAG, "unregisterHomeKeyReceiver");
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
            mHomeKeyReceiver = null;
        }
    }

    public class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(LOG_TAG, "onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                Log.i(LOG_TAG, "reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    Log.i(LOG_TAG, "homekey");
                    finish();
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    Log.i(LOG_TAG, "long press home key or activity switch");
                    finish();
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    Log.i(LOG_TAG, "lock");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    Log.i(LOG_TAG, "assist");
                }
            }
        }
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
