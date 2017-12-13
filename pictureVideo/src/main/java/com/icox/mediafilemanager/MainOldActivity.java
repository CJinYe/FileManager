package com.icox.mediafilemanager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

//import com.nostra13.universalimageloader.core.DisplayImageOptions;
//import com.nostra13.universalimageloader.core.ImageLoader;
//import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
//import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
//import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
//import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
//import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
//import com.nostra13.universalimageloader.utils.L;
//import org.apache.http.util.EncodingUtils;

public class MainOldActivity extends BaseActivity {

    private Context mContext;

    private ImageButton mBtnBendi;
    private ImageButton mBtnTfka;
    private ImageButton mBtnUpan;

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

    // 屏幕宽高
    private int mScreenWidth;
    private int mScreenHeight;
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
        setContentView(R.layout.activity_main);
        registerHomeKeyReceiver(this);
        mContext = this;

        new Thread(new Runnable() {

            @Override
            public void run() {

                Intent mIntent = new Intent();
                mIntent.setAction(ICOX_SERVICE);
                Intent eintent = new Intent(getExplicitIntent(mContext, mIntent));
                isLink = bindService(eintent, serConn, BIND_AUTO_CREATE);
            }
        }).start();

        mMediaType = getIntent().getStringExtra("MediaType");
//        mMediaType = "video";

        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        mBtnBendi = (ImageButton) findViewById(R.id.main_ib_bendi);
        mBtnTfka = (ImageButton) findViewById(R.id.main_ib_tfka);
        mBtnUpan = (ImageButton) findViewById(R.id.main_ib_upan);

        mBtnBendi.setOnClickListener(new BtnOnclickListener());
        mBtnTfka.setOnClickListener(new BtnOnclickListener());
        mBtnUpan.setOnClickListener(new BtnOnclickListener());

        mBtnBendi.setOnFocusChangeListener(mFocusChangeListener);
        mBtnTfka.setOnFocusChangeListener(mFocusChangeListener);
        mBtnUpan.setOnFocusChangeListener(mFocusChangeListener);

        mLoadingLayout = (LinearLayout) findViewById(R.id.main_ll_loading);
        mNullShow = (ImageView) findViewById(R.id.main_iv_null);
//        mNullShow.setVisibility(View.GONE);
        if (mMediaType.equals(VIDEO)) {
            mNullShow.setBackgroundResource(R.drawable.media_9);
        } else if (mMediaType.equals(IMAGE)) {
            mNullShow.setBackgroundResource(R.drawable.media_10);
        }

        mDirPath = new ArrayList<String>();
        mImagePath = new ArrayList<String>();

        mVolumePaths = getVolumePaths(mContext);

//        initImageSource(0);

//        // 初始化图片加载
//        mImageLoader = ImageLoader.getInstance();
//        mImageLoader.init(ImageLoaderConfiguration.createDefault(mContext));
//        L.disableLogging();
//        mAnimateFirstListener = new AnimateFirstDisplayListener();
//        mOptions = new DisplayImageOptions.Builder()
//                .showStubImage(R.drawable.ic_empty)
//                .showImageForEmptyUri(R.drawable.ic_stub)
//                .showImageOnFail(R.drawable.ic_error)
//                .cacheInMemory(true)
//                .cacheOnDisc(true)
//                .displayer(new RoundedBitmapDisplayer(20))
//                .build();


        //显示图片的VIew
        mViewPager = (ViewPager) findViewById(R.id.main_vp_dir);
        //为viewPager设置适配器
        mAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mAdapter);

        handler.sendEmptyMessage(INIT_ARRAYLIST);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterHomeKeyReceiver(this);
    }

    /**
     * 图片加载监听
     */
//    private static class AnimateFirstDisplayListener extends
//            SimpleImageLoadingListener {
//
//        static final List<String> displayedImages = Collections
//                .synchronizedList(new LinkedList<String>());
//
//        @Override
//        public void onLoadingComplete(String imageUri, View view,
//                                      Bitmap loadedImage) {
//            if (loadedImage != null) {
//                ImageView imageView = (ImageView) view;
//                boolean firstDisplay = !displayedImages.contains(imageUri);
//                if (firstDisplay) {
//                    FadeInBitmapDisplayer.animate(imageView, 500);
//                    displayedImages.add(imageUri);
//                }
//            }
//        }
//    }

    // test
    // filePathTest = /mnt/extsd/Thumbs.ms
    // filePathTest = /mnt/extsd/desktop.ini
    //读SD中的文件
//    public String readFileSdcardFile(String fileName) throws IOException {
//        String res = "";
//        try {
//            FileInputStream fin = new FileInputStream(fileName);
//
//            int length = fin.available();
//
//            byte[] buffer = new byte[length];
//            fin.read(buffer);
//
//            res = EncodingUtils.getString(buffer, "GBK");
//
//            fin.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return res;
//    }

    private class BtnOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.main_ib_bendi) {
                mBtnBendi.setImageResource(R.drawable.media_3);
                mBtnTfka.setImageResource(R.drawable.media_5);
                mBtnUpan.setImageResource(R.drawable.media_1);

                mMntType = 0;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
            if (v.getId() == R.id.main_ib_tfka) {
                mBtnBendi.setImageResource(R.drawable.media_4);
                mBtnTfka.setImageResource(R.drawable.media_6);
                mBtnUpan.setImageResource(R.drawable.media_1);

                mMntType = 1;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
            if (v.getId() == R.id.main_ib_upan) {
                mBtnBendi.setImageResource(R.drawable.media_4);
                mBtnTfka.setImageResource(R.drawable.media_5);
                mBtnUpan.setImageResource(R.drawable.media_2);

                mMntType = 2;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
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
                    } else {
                        mNullShow.setVisibility(View.GONE);
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
                        if (filePath.contains("/freenote/")
                                || filePath.contains("/freenote_temp/")){
                            // 屏蔽记事本文件夹
                            return false;
                        }
                        int index = filePath.lastIndexOf('/');
                        mDirPath.add(filePath.substring(0, index));
                        mImagePath.add(filePath);

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
                            || name.equalsIgnoreCase(".cye")
                            || name.equalsIgnoreCase(".3gp")
                            || name.equalsIgnoreCase(".wmv")
//                            || name.equalsIgnoreCase(".ts")
                            || name.equalsIgnoreCase(".rmvb")
                            || name.equalsIgnoreCase(".mov")
                            || name.equalsIgnoreCase(".m4v")
                            || name.equalsIgnoreCase(".avi")
//                            || name.equalsIgnoreCase(".m3u8")
                            || name.equalsIgnoreCase(".3gpp")
                            || name.equalsIgnoreCase(".3gpp2")
                            || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv")
//                            || name.equalsIgnoreCase(".divx")
                            || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm")
                            || name.equalsIgnoreCase(".ts")
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
//                        Log.i("test", "filePath.substring(0, index) = " + filePath.substring(0, index));
//                        Log.i("test", "filePath = " + filePath + ", mDirPath.size() = " + mDirPath.size());
                        mDirPath.add(filePath.substring(0, index));
                        mImagePath.add(filePath);

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
            View view = layoutInflater.inflate(R.layout.main_viewpager, null);
            views[position] = view;

            for (int i = 0; i < pageItem; i++) {
                RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.main_rl_file_01 + i * 3);
                int itemTag = position * pageItem + i;
                if (itemTag >= mImagePath.size()) {
                    layout.setVisibility(View.GONE);
                    continue;
                }

                File mediaFile = new File(mImagePath.get(itemTag));
                if (!mediaFile.exists() || !mediaFile.canRead()){
                    bitmaps[itemTag] = null;

                    layout.setVisibility(View.GONE);
                    continue;
                }
                else if (mMediaType.equals(VIDEO)) {
                    bitmaps[itemTag] = ThumbnailUtils.createVideoThumbnail(mImagePath.get(itemTag), MediaStore.Images.Thumbnails.MICRO_KIND);
                    if (bitmaps[itemTag] == null) {
                        Resources r = getResources();
                        Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.file_frame2);
                        bitmaps[itemTag] = bmp;
                    }
                }
                else if (mMediaType.equals(IMAGE)) {
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
                    imageView.setTag(itemTag);
                    imageView.setOnClickListener(new ImageClickListener(position));
                    imageView.setOnLongClickListener(new ImageOnLongClickListener(position));

                    TextView textView = (TextView) view.findViewById(R.id.main_viewpager_tv_01 + i * 3);
                    int index = mDirPath.get(itemTag).lastIndexOf('/');
                    textView.setText(mDirPath.get(itemTag).substring(index + 1));
                    textView.setMovementMethod(new ScrollingMovementMethod());          // 设置文本显示在规定区域内
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(24);
                    textView.setTag(itemTag);
                }else {
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

            Intent intent = new Intent(mContext, FileListActivity.class);
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
    private void deleteFileDialog(final View view){
        AlertDialog.Builder icoxBuilder = new AlertDialog.Builder(mContext);
        icoxBuilder.setTitle("删除文件夹");
        icoxBuilder.setMessage("删除后不可恢复,是否继续?");
        icoxBuilder.setCancelable(true);
        icoxBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        icoxBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int tagPosition = (Integer) view.getTag();
                if (tagPosition >= mDirPath.size()){
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
        });
        icoxBuilder.create();
        icoxBuilder.show();
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

//    public static List<VideoInfo> sysVideoList = null;// 视频信息集合
//    sysVideoList = new ArrayList<VideoInfo>();
//    setVideoList();

//    public class VideoInfo{
//        String ThumbPath;
//        String Path;
//        String Title;
//        String DisplayName;
//        String MimeType;
//
//        public void setThumbPath(String thumbPath){
//            ThumbPath = thumbPath;
//        }
//    }
//
//    private void setVideoList() {
////        List<VideoInfo> sysVideoList = null;// 视频信息集合
////        sysVideoList = new ArrayList<VideoInfo>();
////        setVideoList();
//
//        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
//        String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA,
//                MediaStore.Video.Thumbnails.VIDEO_ID };
//
//        // MediaStore.Video.Media.DATA：视频文件路径；
//        // MediaStore.Video.Media.DISPLAY_NAME : 视频文件名，如 testVideo.mp4
//        // MediaStore.Video.Media.TITLE: 视频标题 : testVideo
//        String[] mediaColumns = { MediaStore.Video.Media._ID,
//                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
//                MediaStore.Video.Media.MIME_TYPE,
//                MediaStore.Video.Media.DISPLAY_NAME };
//
//        Cursor cursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
//                mediaColumns, null, null, null);
//
//        if(cursor==null){
//            Toast.makeText(SystemVideoChooseActivity.this, "没有找到可播放视频文件", 1).show();
//            return;
//        }
//        if (cursor.moveToFirst()) {
//            do {
//                VideoInfo info = new VideoInfo();
//                int id = cursor.getInt(cursor
//                        .getColumnIndex(MediaStore.Video.Media._ID));
//                Cursor thumbCursor = managedQuery(
//                        MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
//                        thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
//                                + "=" + id, null, null);
//                if (thumbCursor.moveToFirst()) {
//                    info.setThumbPath(thumbCursor.getString(thumbCursor
//                            .getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
//                }
//                info.setPath(cursor.getString(cursor
//                        .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
//                info.setTitle(cursor.getString(cursor
//                        .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
//
//                info.setDisplayName(cursor.getString(cursor
//                        .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));
////                LogUtil.log(TAG, "DisplayName:"+info.getDisplayName());
//                info.setMimeType(cursor
//                        .getString(cursor
//                                .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));
//
//                sysVideoList.add(info);
//            } while (cursor.moveToNext());
//        }
//    }
//
//    public MyOnFocusChangeListener myonfocuschangelistener = new MyOnFocusChangeListener();
//
//    public class MyOnFocusChangeListener implements View.OnFocusChangeListener {
//
//        @Override
//        public void onFocusChange(View view, boolean hasFocus) {
//
//            myViewAnimation(view, hasFocus);
//        }
//    }
//
//    public void myViewAnimation(View v, boolean hasFocus) {
//
//        if (v == null) {
//            return;
//        }
//
//        int focus = 0;
//        if (hasFocus) {
//            focus = R.anim.enlarge;
//        } else {
//            focus = R.anim.decrease;
//        }
//
//        v.bringToFront();
//        //如果有焦点就放大，没有焦点就缩小
//        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
//        mAnimation.setBackgroundColor(Color.TRANSPARENT);
//        mAnimation.setFillAfter(hasFocus);
//
//        v.startAnimation(mAnimation);
//        mAnimation.start();
//
////        // TODO 解决遮盖问题
////        View parentView = (View) v.getParent();// 解决遮盖问题
////        parentView.invalidate();
////        mViewPager.notifyAll();
//    }


    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // 如果是左侧的三个按钮获得焦点的话, 则直接触发事件
            if (hasFocus) {
//                if (v.getId() == R.id.main_ib_bendi) {
//                    mBtnBendi.setBackgroundResource(R.drawable.media_3);
//                    mBtnTfka.setBackgroundResource(R.drawable.media_5);
//                    mBtnUpan.setBackgroundResource(R.drawable.media_1);
//                    mMntType = 0;
//                    handler.sendEmptyMessage(INIT_ARRAYLIST);
//                } else if (v.getId() == R.id.main_ib_tfka) {
//                    mBtnBendi.setBackgroundResource(R.drawable.media_4);
//                    mBtnTfka.setBackgroundResource(R.drawable.media_6);
//                    mBtnUpan.setBackgroundResource(R.drawable.media_1);
//                    mMntType = 1;
//                    handler.sendEmptyMessage(INIT_ARRAYLIST);
//                } else if (v.getId() == R.id.main_ib_upan) {
//                    mBtnBendi.setBackgroundResource(R.drawable.media_4);
//                    mBtnTfka.setBackgroundResource(R.drawable.media_5);
//                    mBtnUpan.setBackgroundResource(R.drawable.media_2);
//                    mMntType = 2;
//                    handler.sendEmptyMessage(INIT_ARRAYLIST);
//                }
                if (v.getId() == R.id.main_ib_bendi) {
                    btnBendiSetFocus();
                    btnTfkaSetBackground();
                    btnUpanSetBackground();
                    return;
                }
                else if (v.getId() == R.id.main_ib_tfka) {
                    btnBendiSetBackground();
                    btnTfkaSetFocus();
                    btnUpanSetBackground();
                    return;
                }
                else if (v.getId() == R.id.main_ib_upan) {
                    btnBendiSetBackground();
                    btnTfkaSetBackground();
                    btnUpanSetFocus();
                    return;
                }
            }

            btnBendiSetBackground();
            btnTfkaSetBackground();
            btnUpanSetBackground();

//            viewAnimation(v, hasFocus);
        }
    };

    private void btnBendiSetBackground(){
        if (mMntType == 0){
            mBtnBendi.setBackgroundResource(R.drawable.media_3);
        }else {
            mBtnBendi.setBackgroundResource(R.drawable.media_4);
        }
    }
    private void btnTfkaSetBackground(){
        if (mMntType == 1){
            mBtnTfka.setBackgroundResource(R.drawable.media_6);
        }else {
            mBtnTfka.setBackgroundResource(R.drawable.media_5);
        }
    }
    private void btnUpanSetBackground(){
        if (mMntType == 2){
            mBtnUpan.setBackgroundResource(R.drawable.media_2);
        }else {
            mBtnUpan.setBackgroundResource(R.drawable.media_1);
        }
    }

    private void btnBendiSetFocus(){
        if (mMntType == 0){
            mBtnBendi.setImageResource(R.drawable.media_3);
        }else {
            mBtnBendi.setImageResource(R.drawable.media_4);
        }
        mBtnBendi.setBackgroundResource(R.drawable.sel_focus);
    }
    private void btnTfkaSetFocus(){
        if (mMntType == 1){
            mBtnTfka.setImageResource(R.drawable.media_6);
        }else {
            mBtnTfka.setImageResource(R.drawable.media_5);
        }
        mBtnTfka.setBackgroundResource(R.drawable.sel_focus);
    }
    private void btnUpanSetFocus(){
        if (mMntType == 2){
            mBtnUpan.setImageResource(R.drawable.media_2);
        }else {
            mBtnUpan.setImageResource(R.drawable.media_1);
        }
        mBtnUpan.setBackgroundResource(R.drawable.sel_focus);
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
        if (event.getAction() == KeyEvent.ACTION_UP){
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
                    ){
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
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
