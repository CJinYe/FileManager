package com.icox.mediafilemanager;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icox.share.BaseActivity;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by icox-XiuChou on 2015/9/23
 */
public class FileListActivity extends BaseActivity {

    private Context mContext;

    private final String IMAGE = "image";
    private final String VIDEO = "video";
    private String mMediaType = IMAGE;

    private ViewPager mViewPager;// 用于显示图片
    private MyPagerAdapter mAdapter;// viewPager的适配器

    private String mDirPath;
    private ArrayList<String> mFilePathArray;
    private TextView mTvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_filelist);
        registerHomeKeyReceiver(this);
        mContext = this;

        mDirPath = getIntent().getStringExtra("DirPath");
        mMediaType = getIntent().getStringExtra("MediaType");

        Log.i("IcoxVideo", "第二层接收的文件夹路径:" + mDirPath);

        initView();

    }

    private void initView() {
        mViewPager = (ViewPager) findViewById(R.id.filelist_vp_file);
        mFilePathArray = new ArrayList<String>();

        mTvTitle = (TextView) findViewById(R.id.fileList_tv_title);

        if (mMediaType.equals(IMAGE)){
            mTvTitle.setText("相册"+mDirPath);
        }else if (mMediaType.equals(VIDEO)){
            mTvTitle.setText("视频"+mDirPath);
        }

        ImageButton btnGoBack = (ImageButton) findViewById(R.id.main_iv_back);
//        btnGoBack.setOnFocusChangeListener(mFocusChangeListener);
        handler.sendEmptyMessage(INIT_ARRAYLIST);

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterHomeKeyReceiver(this);
    }

    private static final int INIT_ARRAYLIST = 1;
    private static final int INIT_ADPTER = 2;
    private static final int UPDATE_ADPTER = 3;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_ARRAYLIST:
                    /*File file = new File(mDirPath);
                    if (mFilePathArray.size() == 0) {
                        if (mMediaType.equals(IMAGE)) {
                            getLocalImageFiles(file);
                        } else if (mMediaType.equals(VIDEO)) {
                            getLocalVideoFiles(file);
                        }
                    }

                    handler.sendEmptyMessage(INIT_ADPTER);*/
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(mDirPath);
                            if (mFilePathArray.size() == 0) {
                                if (mMediaType.equals(IMAGE)) {
                                    getLocalImageFiles(file);
                                } else if (mMediaType.equals(VIDEO)) {
                                    getLocalVideoFiles(file);
                                }

                                Collections.sort(mFilePathArray);
                            }

                            handler.sendEmptyMessage(INIT_ADPTER);
                        }
                    }).start();
                    break;
                case INIT_ADPTER:
                    //为viewPager设置适配器
                    mAdapter = new MyPagerAdapter();
                    mViewPager.setAdapter(mAdapter);
                    break;
                case UPDATE_ADPTER:
                    mAdapter.updateData();
                    mAdapter.notifyDataSetChanged();
                    mAdapter.notifyAll();
                    break;
            }
        }
    };

    private void getLocalImageFiles(File file) {

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {
                String name = file.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
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
//                        int index = filePath.lastIndexOf('/');
//                        Log.i("test", "filePath.substring(0, index) = " + filePath.substring(0, index));
//                        Log.i("test", "filePath = " + filePath + ", mDirPath.size() = " + mDirPath.size());
//                        mDirPath.add(filePath.substring(0, index));
                        mFilePathArray.add(filePath);
//                        Collections.sort(mFilePathArray);
                        return true;
                    }
                } else if (file.isDirectory()) {
//                    getLocalImageFiles(file);
                }
                return false;
            }
        });
    }

    private void getLocalVideoFiles(File file) {

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(final File file) {
                String name = file.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
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
                        String filePath = file.getAbsolutePath();
//                        int index = filePath.lastIndexOf('/');
//                        Log.i("test", "filePath.substring(0, index) = " + filePath.substring(0, index));
//                        Log.i("test", "filePath = " + filePath + ", mDirPath.size() = " + mDirPath.size());
//                        mDirPath.add(filePath.substring(0, index));
                        mFilePathArray.add(filePath);
//                        Collections.sort(mFilePathArray);
                        return true;
                    }
                } else if (file.isDirectory()) {
//                    getLocalVideoFiles(file);
                }
                return false;
            }
        });
    }

    //	ViewPager每次仅最多加载三张图片（有利于防止发送内存溢出）
    private class MyPagerAdapter extends PagerAdapter {

        private final int pageItem = 8;
        private LayoutInflater layoutInflater;
        private int pageCount;
        private View[] views;
        private Bitmap[] bitmaps;

        public MyPagerAdapter() {

            this.layoutInflater = LayoutInflater.from(mContext);

            this.bitmaps = new Bitmap[mFilePathArray.size()];

            pageCount = (mFilePathArray.size() + (pageItem - 1)) / pageItem;
            this.views = new View[pageCount];
        }

        public void updateData() {
            this.bitmaps = new Bitmap[mFilePathArray.size()];
            pageCount = (mFilePathArray.size() + (pageItem - 1)) / pageItem;
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

            View view = layoutInflater.inflate(R.layout.filelist_viewpager, null);
            views[position] = view;

//            Log.i("test", "position = " + position);
//            mLoading.setVisibility(View.VISIBLE);

            for (int i = 0; i < pageItem; i++) {
                RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.filelist_rl_file_01 + i * 3);
                int itemTag = position * pageItem + i;
                if (itemTag >= mFilePathArray.size()) {
                    layout.setVisibility(View.GONE);
                    continue;
                }

                if (mMediaType.equals(VIDEO)) {
//                    bitmaps[itemTag] = createVideoThumbnail(mFilePathArray.get(itemTag), 300, 300);
//
//                    if (bitmaps[itemTag] == null) {
//                        Resources r = FileListActivity.this.getResources();
//                        Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.file_frame2);
//                        bitmaps[itemTag] = bmp;
//                    }
//                    String filePath = mFilePathArray.get(itemTag);
//                    if (filePath.endsWith(".mp4") || filePath.endsWith(".3gp") || filePath.endsWith(".avi") || filePath.endsWith(".flv")) {
//                        bitmaps[itemTag] = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MICRO_KIND);
//                    }
                    bitmaps[itemTag] = ThumbnailUtils.createVideoThumbnail(mFilePathArray.get(itemTag), MediaStore.Images.Thumbnails.MICRO_KIND);
                    if (bitmaps[itemTag] == null) {
                        Resources r = FileListActivity.this.getResources();
                        Bitmap bmp = BitmapFactory.decodeResource(r, R.drawable.file_frame2);
                        bitmaps[itemTag] = bmp;
                    }
                } else if (mMediaType.equals(IMAGE)) {
                    bitmaps[itemTag] = decodeSampledBitmapFromFd(mFilePathArray.get(itemTag), 300, 300);
                }


                if (bitmaps[itemTag] != null) {
                    layout.setVisibility(View.VISIBLE);

                    ImageView imageView = (ImageView) view.findViewById(R.id.viewpager_iv_01 + i * 3);
//                    if (mMediaType.equals(IMAGE)) {
//                        Picasso.with(mContext)
//                                .load(new File(mFilePathArray.get(itemTag)))
//                                .resize(300, 300)
//                                .error(R.drawable.ic_empty)
//                                .into(imageView);
//                    }else {
                        imageView.setImageBitmap(bitmaps[itemTag]);
//                    }

                    imageView.setTag(itemTag);
                    imageView.setOnClickListener(new ImageClickListener(position));
                    imageView.setOnLongClickListener(new ImageOnLongClickListener(position));

                    int index = mFilePathArray.get(itemTag).lastIndexOf('/');
                    TextView textView = (TextView) view.findViewById(R.id.filelist_viewpager_tv_01 + i * 3);
                    textView.setText(mFilePathArray.get(itemTag).substring(index + 1));
//                    textView.setMovementMethod(new ScrollingMovementMethod());          // 设置文本显示在规定区域内
                    textView.setTextColor(Color.WHITE);
                    textView.setTextSize(24);
                    textView.setSingleLine();

                    textView.setMarqueeRepeatLimit(-1);
                    textView.setEllipsize(TextUtils.TruncateAt.MARQUEE);

                    textView.setSelected(true);
                    textView.setPadding(5, 0, 3, 0);

                }else {
                    layout.setVisibility(View.GONE);
                }
            }

//         mLoading.setVisibility(View.GONE);
            container.addView(view);
            return view;
        }
    }

    private static Intent stopIntent = new Intent("STOP_RECEIVER");

    private class ImageClickListener implements View.OnClickListener {

        private int iPosition;

        public ImageClickListener(int position) {
            this.iPosition = position;
        }

        @Override
        public void onClick(View v) {

            Intent intent = null;
            int tagPosition = (Integer) v.getTag();
            File file = new File(mFilePathArray.get(tagPosition));
            if (file != null && file.isFile() == true) {

                String packageName = getPackageName();
                if (mMediaType.equals(IMAGE)) {
//                    intent = new Intent();
//                    intent.setAction(android.content.Intent.ACTION_VIEW);
//                    intent.setDataAndType(Uri.fromFile(file), "image/*");
//                    mContext.startActivity(intent);

                    // 传当前的图片路径,自用的图片查看器
                    ComponentName componentName = new ComponentName(packageName, "com.icox.manager.localview.LocalImageShower");
                    Intent icoxImageIntent = new Intent();
                    icoxImageIntent.setComponent(componentName);
                    icoxImageIntent.putStringArrayListExtra("ArrayDirPath", mFilePathArray);
//                    icoxImageIntent.putExtra("path", file.getAbsolutePath());
                    icoxImageIntent.putExtra("clickPosition", tagPosition);
                    mContext.startActivity(icoxImageIntent);

                } else if (mMediaType.equals(VIDEO)) {

                    // 传当前的视频路径,自用的播放器
                    ComponentName componentName = new ComponentName(packageName, "com.icox.manager.localview.LocalVideoPlayer");
                    Intent icoxVideoIntent = new Intent();
                    icoxVideoIntent.setComponent(componentName);

                    icoxVideoIntent.putStringArrayListExtra("ArrayDirPath", mFilePathArray);
                    icoxVideoIntent.putExtra("path", file.getAbsolutePath());
                    mContext.startActivity(icoxVideoIntent);

                    // TODO 发送广播,让音乐停止
                    stopIntent.putExtra("Stop", true);
                    Log.i("Stop", "点击了");
                    mContext.sendBroadcast(stopIntent);
                }
            }
        }
    }

    private class ImageOnLongClickListener implements View.OnLongClickListener {

        private int iPosition;

        public ImageOnLongClickListener(int position) {
            this.iPosition = position;
        }

        @Override
        public boolean onLongClick(final View view) {
            deleteFileDialog(view);

            return false;
        }
    }

    // 删除文件对话框
    private void deleteFileDialog(final View view){


        FileDeleteDialog fileDeleteDialog = new FileDeleteDialog(this) {
            @Override
            public void clickCancel() {

            }

            @Override
            public void clickSure() {
                int tagPosition = (Integer) view.getTag();
                if (tagPosition >= mFilePathArray.size()){
                    return;
                }
                File currentFile = new File(mFilePathArray.get(tagPosition));
                if (currentFile.exists()) {
                    if (currentFile.delete()) {
                        // TODO 删除之后刷新UI
                        mFilePathArray.remove(tagPosition);
                        handler.sendEmptyMessage(INIT_ARRAYLIST);
                    }
                }
            }
        };

        dialogHide(fileDeleteDialog);
        fileDeleteDialog.show();
    }

    private void dialogHide(final Dialog dialog) {
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                initWindow();
            }
        });
    }

    // BitmapUtils
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
    public static Bitmap decodeSampledBitmapFromFd(String pathName, int reqWidth, int reqHeight) {

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

    // 截取视频缩略图
    private Bitmap createVideoThumbnail(String url, int width, int height) {

        // 家庭宝
        if (url.contains(".jtb")) {
            return null;
        }

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


    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            viewAnimation(v, hasFocus);


        }
    };

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
            if (getCurrentFocus().getId() == R.id.viewpager_iv_01
                    || getCurrentFocus().getId() == R.id.viewpager_iv_02
                    || getCurrentFocus().getId() == R.id.viewpager_iv_03
                    || getCurrentFocus().getId() == R.id.viewpager_iv_04
                    || getCurrentFocus().getId() == R.id.viewpager_iv_05
                    || getCurrentFocus().getId() == R.id.viewpager_iv_06
                    || getCurrentFocus().getId() == R.id.viewpager_iv_07
                    || getCurrentFocus().getId() == R.id.viewpager_iv_08
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
    }
}
