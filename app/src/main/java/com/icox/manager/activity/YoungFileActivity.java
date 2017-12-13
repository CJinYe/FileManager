package com.icox.manager.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icox.manager.R;
import com.icox.manager.adapter.GridShowBabyAdapter;
import com.icox.manager.dialog.YoungDeleteDialog;
import com.icox.manager.dialog.YoungInfoDialog;
import com.icox.manager.dialog.YoungMenuDialog;
import com.icox.manager.dialog.YoungRenameDialog;
import com.icox.manager.util.ApkUtil;
import com.icox.manager.util.FileUtil;
import com.icox.manager.util.MyFileFilter;
import com.icox.manager.util.OpenFiles;
import com.icox.share.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by icox-XiuChou  on 2016/1/4
 */
public class YoungFileActivity extends BaseActivity {

    /*******************监听插拔TF卡和U盘的事件***********************/
    /**
     * 注册系统广播
     */
    private AndroidSystemReceiver androidSystemReceiver = null;

    private void register() {

        //TF卡与U盘的插拔广播
        androidSystemReceiver = new AndroidSystemReceiver();
        IntentFilter systemFilter = new IntentFilter();
        systemFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        systemFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        // 必须添加，否则无法接收到系统广播
        systemFilter.addDataScheme("file");
        registerReceiver(androidSystemReceiver, systemFilter);
    }

    public class AndroidSystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // 拔卡
            if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                mBtnTfka.setSelected(false);
                mBtnUpan.setSelected(false);
                mBtnBendi.setSelected(true);
                mVolumePaths = getVolumePaths(context);
                mMntType = 0;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
            // 插卡
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mBtnTfka.setSelected(false);
                mBtnUpan.setSelected(false);
                mBtnBendi.setSelected(true);
                mVolumePaths = getVolumePaths(context);
                mMntType = 0;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
        }
    }

    /******************************************/
    // 运行上下文
    private Context context = null;
    // 当前路径、返回上一层
    private TextView currentPath = null;
    // 网格显示
    private GridView gridShow = null;
    // 底部 粘贴与移动
    //    private LinearLayout pasteLayout, moveLayout = null;
    private View pasteLayout, moveLayout = null;
    // GridView的适配器
    private GridShowBabyAdapter gridShowAdapter = null;
    // 文件集合
    private File[] listFiles = null;
    // 父目录路径
    private File parentPath = null;
    // 系统路径的区域划分
    private String[] mVolumePaths = null;

    private RelativeLayout showFilesLayout = null;
    private TextView tv_loading = null;

    private ImageButton mBtnBendi = null;
    private ImageButton mBtnTfka = null;
    private ImageButton mBtnUpan = null;

    // TODO 跟多U盘路径的优化
    private int mMntType = 0;
    /********
     * 刷新UI
     ********/
    // 初始化集合
    private final int INIT_ARRAYLIST = 1;
    // 设置下适配器
    private final int INIT_ADPTER = 2;
    // 刷新适配器
    private final int UPDATE_ADPTER = 3;
    // 复制状态
    private final int COPY_STATE = 4;
    // 删除状态
    private final int DELETE_STATE = 5;
    // 移动状态
    private final int MOVE_STATE = 6;
    // 错误状态
    private final int ERROR_STATE = 7;

    // 刷新界面
    private final int UPDATE_STATE = 10;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_ARRAYLIST:

                    if (mMntType < mVolumePaths.length) {
                        // 显示当前的路径
                        currentPath.setText(mVolumePaths[mMntType]);
                    }
                    //                    Toast.makeText(context,"把卡1",Toast.LENGTH_SHORT).show();

                    tv_loading.setText("加载中...");
                    //                    showFilesLayout.setBackgroundResource(0);
                    showFilesLayout.setVisibility(View.GONE);
                    showFilesLayoutChild_B.setVisibility(View.VISIBLE);
                    //                    Toast.makeText(context,"把卡2",Toast.LENGTH_SHORT).show();

                    listFiles = scanFiles(mMntType);
                    //                    Toast.makeText(context,"把卡3",Toast.LENGTH_SHORT).show();

                    handler.sendEmptyMessage(INIT_ADPTER);
                    //                    handler.sendEmptyMessageDelayed(INIT_ADPTER, 1000);
                    //                    Toast.makeText(context,"把卡4",Toast.LENGTH_SHORT).show();
                    break;
                case INIT_ADPTER:
                    // 设置适配器
                    if (gridShowAdapter == null) {
                        // 首次运行的话,实例化适配器
                        gridShowAdapter = new GridShowBabyAdapter(context, listFiles, gridShow, false);

                        gridShow.setAdapter(gridShowAdapter);
                    } else {
                        // 第二次的话，适配器只需要刷新数据
                        if (listFiles != null)
                            gridShowAdapter.updateFiles(listFiles);
                    }
                    //                    gridShow.setAdapter(gridShowAdapter);
                    showFilesLayoutChild_B.setVisibility(View.GONE);
                    if (listFiles == null || listFiles.length == 0) {
                        // 显示没有找到任何文件
                        //                        showFilesLayout.setBackgroundResource(R.drawable.nofiles);
                        showFilesLayout.setVisibility(View.VISIBLE);
                        showFilesLayoutChild_A.setVisibility(View.GONE);
                    } else {
                        //                        showFilesLayout.setBackgroundResource(0);
                        showFilesLayout.setVisibility(View.GONE);
                        showFilesLayoutChild_A.setVisibility(View.VISIBLE);
                    }
                    //                    handler.sendEmptyMessage(UPDATE_ADPTER);
                    break;
                case UPDATE_ADPTER:
                    gridShowAdapter.notifyDataSetChanged();
                    break;
                /**
                 *  复制中,删除中,移动中,错误,刷新UI
                 */
                case COPY_STATE:
                    showFilesLayoutChild_B.setVisibility(View.VISIBLE);
                    tv_loading.setText("复制中...");
                    break;
                case DELETE_STATE:
                    showFilesLayoutChild_B.setVisibility(View.VISIBLE);
                    tv_loading.setText("删除中...");
                    break;
                case MOVE_STATE:
                    showFilesLayoutChild_B.setVisibility(View.VISIBLE);
                    tv_loading.setText("移动中...");
                    break;
                case ERROR_STATE:
                    // TODO:这里不能updateGridShowUI(),不然会崩!
                    Toast.makeText(YoungFileActivity.this, "目标路径有误,请重新操作! " + currentPath.getText().toString(), Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_STATE:
                    showFilesLayoutChild_B.setVisibility(View.GONE);
                    updateGridShowUI();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_file_new_ui);

        //        String path = getStoragePath(this, "U盘");
        //        Toast.makeText(this, "path = " + path, Toast.LENGTH_LONG).show();

        registerHomeKeyReceiver(this);
        // 获取当前运行上下文
        context = YoungFileActivity.this;
        // 获取路径
        mVolumePaths = getVolumePaths(context);
        // 实例化控件
        initView();
        // 设置控件的监听事件
        setListener();
        // 设置控件的焦点
        setFocus();
        // 注册系统广播
        register();
        handler.sendEmptyMessage(INIT_ARRAYLIST);
    }

    //    @Override
    //    protected void onResume() {
    //        super.onResume();
    //
    //        Log.i("test", "sdk: " + Build.VERSION.SDK_INT);
    //        if (Build.VERSION.SDK_INT >= 23){
    //            requestPermissions(this);
    //        }
    //    }
    //
    //    /**
    //     * android 6.0 请求用户授权
    //     */
    //    public static void requestPermissions(Activity activity){
    //        int permissionCheck1 = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
    //        int permissionCheck2 = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    //        Log.i("test", "requestPermissions: permissionCheck1 = " + permissionCheck1 + ", permissionCheck2 = " + permissionCheck2);
    //        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED) {
    //            ActivityCompat.requestPermissions(activity,
    //                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
    //                            Manifest.permission.READ_EXTERNAL_STORAGE},
    //                    124);       // 随便定义，onRequestPermissionsResult中的requestCode
    //        }
    //    }


    /**
     * 6.0获取外置sdcard和U盘路径，并区分
     *
     * @param mContext
     * @param keyword  SD = "内部存储"; EXT = "SD卡"; USB = "U盘"
     * @return
     */
    public boolean getStoragePath(Context mContext, String keyword) {
        String targetpath = "";
        boolean isUpan = false;
        try {
            StorageManager mStorageManager = (StorageManager) mContext
                    .getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = null;
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");

            Method getPath = storageVolumeClazz.getMethod("getPath");

            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);

            Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");


            for (int i = 0; i < length; i++) {

                Object storageVolumeElement = Array.get(result, i);

                String userLabel = (String) getUserLabel.invoke(storageVolumeElement);

                String path = (String) getPath.invoke(storageVolumeElement);

                //                Toast.makeText(this, "path循环 = " + path + " \n userLabel = " + userLabel, Toast.LENGTH_LONG).show();

                if (path.equals(keyword) && (userLabel.contains("U") || userLabel.contains("盘"))) {
                    isUpan = true;
                    targetpath = path;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return isUpan;
    }

    /**
     * 设置控件的焦点
     */
    private void setFocus() {
        mBtnBendi.setOnFocusChangeListener(mFocusChangeListener);
        mBtnTfka.setOnFocusChangeListener(mFocusChangeListener);
        mBtnUpan.setOnFocusChangeListener(mFocusChangeListener);
        new_folder.setOnFocusChangeListener(mFocusChangeListener);
        back.setOnFocusChangeListener(mFocusChangeListener);
        paste.setOnFocusChangeListener(mFocusChangeListener);
        pasteCancel.setOnFocusChangeListener(mFocusChangeListener);
        move.setOnFocusChangeListener(mFocusChangeListener);
        moveCancel.setOnFocusChangeListener(mFocusChangeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterHomeKeyReceiver(this);
        if (androidSystemReceiver != null) {
            unregisterReceiver(androidSystemReceiver);
        }
    }

    /***************
     * 菜单相关:↓
     ***************/
    GridView.OnCreateContextMenuListener menuList = new GridView.OnCreateContextMenuListener() {

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            // 动态设置图标或者标题
            menu.setHeaderTitle("请选择操作");
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.file_context_menu, menu);
        }
    };

    // 复制的路径
    private String copyPath = null;
    // 有文件夹或者文件类型
    private String fileType = null;

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position;
        if (menuInfo == null) {
            position = gridShow.getSelectedItemPosition();
        } else {
            position = menuInfo.position;
        }
        if (position == -1) {
            return super.onContextItemSelected(item);
        }

        final File file = listFiles[position];

        switch (item.getItemId()) {

            case R.id.menu_copy:

                clickMenuCopy(file);

                break;

            case R.id.menu_move:

                clickMenuMove(file);

                break;
            case R.id.menu_rename:

                clickMenuRename(file,"重命名");

                break;

            case R.id.menu_delete:

                clickMenuDelete(file);
                break;

            case R.id.menu_info:
                showFileInfo(file);
                break;
        }
        return super.onContextItemSelected(item);
    }

    private void clickMenuDelete(final File file) {
        YoungDeleteDialog dialog = new YoungDeleteDialog(mContext) {
            @Override
            public void clickSure() {
                copyPath = file.getAbsolutePath();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO delete:删除文件
                        handler.sendEmptyMessage(DELETE_STATE);
                        FileUtil.delete(YoungFileActivity.this, copyPath);
                        handler.sendEmptyMessage(UPDATE_STATE);
                    }
                }).start();
            }
        };
        showDialog((Activity) mContext, dialog);
    }

    private void clickMenuRename(final File file,String title) {
        YoungRenameDialog dialog = new YoungRenameDialog(mContext, title) {
            @Override
            public void clickSure(String trim) {
                String path = file.getAbsolutePath();
                if (path != null && !"".equals(path) && trim.length() > 0) {
                    int lastIndexOf = path.lastIndexOf("/");
                    String path1 = path.substring(0, lastIndexOf + 1);
                    boolean b = file.renameTo(new File(path1 + trim));
                    if (b) {
                        // 刷新UI
                        updateGridShowUI();
                    }
                }
            }
        };
        showDialog((Activity) mContext, dialog);
    }

    private void clickMenuMove(File file) {
        copyPath = file.getAbsolutePath();
        //                Log.i("mylog", "移动" + fileType + "的路径:" + copyPath);
        // TODO move:
        moveLayout.setVisibility(View.VISIBLE);
        pasteLayout.setVisibility(View.GONE);

    }

    private void clickMenuCopy(File file) {
        // TODO copy:获取要拷贝的当前文件的绝对路径,“粘贴”操作根据文件的绝对路径转成文件才能对文件的复制,不是简单靠路径!
        copyPath = file.getAbsolutePath();
        //                Log.i("mylog", "复制" + fileType + "的路径:" + copyPath);
        pasteLayout.setVisibility(View.VISIBLE);
        moveLayout.setVisibility(View.GONE);
    }

    private void showFileInfo(File file) {
        Date date = new Date(file.lastModified());//文件最后修改时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:MM");

        String msg = "";
        msg += "名称：" + file.getName() + "\n";
        msg += "路径：" + file.getAbsolutePath() + "\n";
        msg += "大小：" + FileUtil.generateFileSize(file.length()) + "\n";
        msg += "时间：" + format.format(date);

        YoungInfoDialog dialog = new YoungInfoDialog(mContext,msg);
        showDialog((Activity) mContext,dialog);
    }


    /**
     * 粘贴、重命名、删除操作之后都要刷新当前的UI
     */
    private void updateGridShowUI() {
        // TODO :updateGridShowUI
        listFiles = new File(currentPath.getText() + "/").listFiles(new MyFileFilter());
        listFiles = FileUtil.sort(listFiles);
        gridShowAdapter.updateFiles(listFiles);
        gridShowAdapter.notifyDataSetChanged();
    }

    //版本大于6.0的情况

    /**
     * 获取系统的路径划分的数组区间
     */
    public String[] getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;
        try {
            Method methodGetPaths = storageManager.getClass().getMethod("getVolumePaths");
            paths = (String[]) methodGetPaths.invoke(storageManager);
            if (paths != null) {
                if (paths.length == 2) {
                    if (getStoragePath(context, paths[1])) {
                        String[] resultPaths = new String[3];
                        resultPaths[0] = paths[0];
                        //                        resultPaths[1] = "/storage/964E-D2C8";
                        resultPaths[1] = "";
                        resultPaths[2] = paths[1];
                        return resultPaths;
                    }
                }

                //                paths = new String[getPaths.length + 1];
                //                int i = 0;
                //                for (; i < getPaths.length; i++) {
                //                    paths[i] = getPaths[i];
                //                }
                //                paths[i] = "/mnt/m_internal_storage/";
            }
        } catch (Exception e) {

        }
        return paths;
    }

    /**
     * 扫描文件,并获取文件数组
     */
    private File[] scanFiles(int mMntType) {
        listFiles = null;
        listFiles = new File[]{};

        if (mMntType == 0) {
            String path = mVolumePaths[mMntType];
            File file = new File(path);

            if (file.exists()) {
                if (file.canRead()) {
                    listFiles = FileUtil.sort(file.listFiles(new MyFileFilter()));
                }
            }
        }
        if (mMntType < mVolumePaths.length && mMntType < 2) {
            String path = mVolumePaths[mMntType];
            File file = new File(path);
            if (file.exists()) {
                if (file.canRead()) {
                    listFiles = FileUtil.sort(file.listFiles(new MyFileFilter()));
                }
            }
        } else {
            for (int i = 2; i < mVolumePaths.length; i++) {
                String path = mVolumePaths[i];
                File file = new File(path);
                if (file.exists()) {
                    if (file.canRead()) {
                        listFiles = FileUtil.sort(file.listFiles(new MyFileFilter()));
                    }
                }
            }
        }
        return listFiles;
    }

    private ImageView new_folder, back, paste, pasteCancel, move, moveCancel;
    LinearLayout showFilesLayoutChild_A, showFilesLayoutChild_B;

    private void initView() {

        // 左边三种模式的选择, 与监听事件
        mBtnBendi = (ImageButton) findViewById(R.id.main_ib_bendi);
        mBtnTfka = (ImageButton) findViewById(R.id.main_ib_tfka);
        mBtnUpan = (ImageButton) findViewById(R.id.main_ib_upan);
        mBtnBendi.setOnClickListener(new BtnOnclickListener());
        mBtnTfka.setOnClickListener(new BtnOnclickListener());
        mBtnUpan.setOnClickListener(new BtnOnclickListener());

        // 右边的视图显示,以"Layout"为单位;
        showFilesLayout = (RelativeLayout) findViewById(R.id.showFilesLayout);
        showFilesLayoutChild_A = (LinearLayout) findViewById(R.id.showFilesLayoutChild_A);
        showFilesLayoutChild_B = (LinearLayout) findViewById(R.id.showFilesLayoutChild_B);

        gridShow = (GridView) findViewById(R.id.gridShow);
        //        // 粘贴Layout
        //        pasteLayout = (LinearLayout) findViewById(R.id.pasteLayout);
        //        // 剪切Layout
        //        moveLayout = (LinearLayout) findViewById(R.id.moveLayout);
        // 粘贴Layout
        pasteLayout = findViewById(R.id.pasteLayout);
        // 剪切Layout
        moveLayout = findViewById(R.id.moveLayout);

        // 当前路径
        currentPath = (TextView) findViewById(R.id.currentPath);
        // 中间的加载效果
        tv_loading = (TextView) findViewById(R.id.tv_loading);

        // 更换UI
        new_folder = (ImageView) findViewById(R.id.new_folder);
        back = (ImageView) findViewById(R.id.back);
        paste = (ImageView) findViewById(R.id.paste);
        pasteCancel = (ImageView) findViewById(R.id.pasteCancel);
        move = (ImageView) findViewById(R.id.move);
        moveCancel = (ImageView) findViewById(R.id.moveCancel);

        //
        initBaby();
    }

    // 背景图宽高
    public final static int BG_WIDTH = 1280;
    public final static int BG_HEIGHT = 750;
    // 屏幕宽高
    public static int mScreenWidth;
    public static int mScreenHeight;

    private final int[][] BTN_DATA = new int[][]{
            {46, 115, 294, 313, R.drawable.baby_btn_bendi},
            {46, 314, 294, 512, R.drawable.baby_btn_tf},
            {46, 513, 294, 711, R.drawable.baby_btn_upan},
            {320, 150, 1183, 668, -1},
            //            {459, 632, 1047, 710, -1},
            {459, 632, 1047, 710, -1},      //
            {516, 28, 758, 112, -1},
            {846, 28, 1082, 112, -1},
            {297, 120, 1206, 181, -1},
            {459, 632, 758, 716, -1},
            {758, 632, 1047, 716, -1},
    };

    private void initBaby() {
        // 获取屏幕宽高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        //        if (!hasPermanentKey()){
        //            mScreenHeight = getDpi(FileActivity.this);
        //        }
        if (ApkUtil.hasAppInstalled(YoungFileActivity.this, "com.icox.home_child_xinan")
                || ApkUtil.hasAppInstalled(YoungFileActivity.this, "com.icox.home_child_xinan_key")) {
            mScreenHeight = getDpi(YoungFileActivity.this);
        }

        //
        initItemLocation(mBtnUpan, BTN_DATA[2]);
        initItemLocation(mBtnTfka, BTN_DATA[1]);
        initItemLocation(mBtnBendi, BTN_DATA[0]);
        mBtnBendi.setSelected(true);
        //
        initItemLocation(gridShow, BTN_DATA[3]);
        initItemLocation(showFilesLayout, BTN_DATA[3]);
        //
        //        initItemLocation(pasteLayout, BTN_DATA[4]);
        //        initItemLocation(moveLayout, BTN_DATA[4]);
        //
        initItemLocation(new_folder, BTN_DATA[5]);
        initItemLocation(back, BTN_DATA[6]);
        // 路径
        initItemLocation(currentPath, BTN_DATA[7]);
        //
        initItemLocation(pasteCancel, BTN_DATA[8]);
        initItemLocation(paste, BTN_DATA[9]);
        initItemLocation(moveCancel, BTN_DATA[8]);
        initItemLocation(move, BTN_DATA[9]);
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

    private class BtnOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mBtnBendi.setSelected(false);
            mBtnTfka.setSelected(false);
            mBtnUpan.setSelected(false);
            if (v.getId() == R.id.main_ib_bendi) {

                mBtnBendi.setSelected(true);
                mMntType = 0;
            }
            if (v.getId() == R.id.main_ib_tfka) {

                mBtnTfka.setSelected(true);
                mMntType = 1;
            }
            if (v.getId() == R.id.main_ib_upan) {

                mBtnUpan.setSelected(true);
                mMntType = 2;
            }
            handler.sendEmptyMessage(INIT_ARRAYLIST);
        }
    }

    /**
     * 设置控件的监听事件
     */
    private void setListener() {

        // 新建文件夹
        new_folder.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                YoungRenameDialog dialog = new YoungRenameDialog(mContext,"新建文件夹") {
                    @Override
                    public void clickSure(String trim) {
                        File file = new File(currentPath.getText() + "/" + trim);
                        if (!file.exists()) {
                            if (file.mkdir()) {
                                Toast.makeText(context, currentPath.getText().toString().toLowerCase() + "/" + trim, Toast.LENGTH_SHORT).show();

                                handler.sendEmptyMessage(UPDATE_STATE);
                            } else {
                                handler.sendEmptyMessage(ERROR_STATE);
                            }
                        }
                    }
                };

                showDialog((Activity) mContext,dialog);

            }
        });

        //返回上一层
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (parentPath != null
                        && !parentPath.getName().equals("")
                        && !parentPath.getName().equals("emulated")
                        && !parentPath.getName().equals("mnt")
                        && !parentPath.getName().equals("storage")) {

                    listFiles = parentPath.listFiles(new MyFileFilter());
                    listFiles = FileUtil.sort(listFiles);

                    gridShowAdapter.updateFiles(listFiles);
                    gridShowAdapter.notifyDataSetChanged();
                    parentPath = parentPath.getParentFile();
                    String nowPath = currentPath.getText().toString();
                    int indexOf = nowPath.lastIndexOf("/");
                    if (indexOf != -1) {
                        nowPath = nowPath.substring(0, indexOf);
                        currentPath.setText(nowPath);
                    }
                    setListViewIndex();
                } else {
                    //                    Toast.makeText(FileActivity.this, "已经是根目录了!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        // 文件夹或者文件的选择
        gridShow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                /*// 如果是目录，就进入到文件夹内部
                if (listFiles[position].isDirectory()) {
                    parentPath = listFiles[position].getParentFile();
                    String subPathName = listFiles[position].getName();
                    currentPath.append("/" + subPathName);
                    listFiles = listFiles[position].listFiles(new MyFileFilter());
                    listFiles = FileUtil.sort(listFiles);
                    // 刷新数据，并更新gridView
                    gridShowAdapter.updateFiles(listFiles);
                    gridShowAdapter.notifyDataSetChanged();
                } else {// 是文件的处理逻辑
                    OpenFiles.open(context, listFiles[position]);
                }*/
                gridViewItemClickEvent(position);
            }
        });

        // 长按弹出菜单进行操作
        gridShow.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                // 判断是文件夹还是文件
                final File file = listFiles[position];
                if (file.isDirectory()) {
                    fileType = "文件夹";
                } else {
                    fileType = "文件";
                }

                YoungMenuDialog dialog = new YoungMenuDialog(mContext) {
                    @Override
                    protected void clickMenuInfo() {
                        showFileInfo(file);
                    }

                    @Override
                    protected void clickMenuDelete() {
                       YoungFileActivity.this.clickMenuDelete(file);
                    }

                    @Override
                    protected void clickMenuRename() {
                        YoungFileActivity.this.clickMenuRename(file,"重命名");
                    }

                    @Override
                    protected void clickMenuMove() {
                        YoungFileActivity.this.clickMenuMove(file);
                    }

                    @Override
                    public void clickMenuCopy() {
                        YoungFileActivity.this.clickMenuCopy(file);
                    }
                };

                showDialog((Activity) mContext, dialog);
                // 长按则弹出个上下文菜单
                //                gridShow.setOnCreateContextMenuListener(menuList);


                return true;
            }
        });

        // 粘贴
        paste.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 try {
                                                     File hasFile = new File(currentPath.getText().toString());
                                                     if (hasFile.canRead()) {
                                                         handler.sendEmptyMessage(COPY_STATE);
                                                         FileUtil.copy(YoungFileActivity.this, copyPath, currentPath.getText() + "/");
                                                         handler.sendEmptyMessage(UPDATE_STATE);
                                                         FileUtil.round = true;
                                                     } else {
                                                         handler.sendEmptyMessage(ERROR_STATE);
                                                     }
                                                 } catch (IOException e) {

                                                 }
                                             }
                                         }).start();
                                         pasteLayout.setVisibility(View.GONE);
                                     }
                                 }
        );

        // 取消粘贴
        pasteCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                pasteLayout.setVisibility(View.GONE);
            }
        });

        // 移动至此
        move.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // TODO 剪切:new File(copyPath).renameTo(new File(currentPath.getText() + "/"));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File hasFile = new File(currentPath.getText().toString());
                        if (hasFile.canRead()) {
                            handler.sendEmptyMessage(MOVE_STATE);
                            FileUtil.move(YoungFileActivity.this, copyPath, currentPath.getText() + "/");
                            FileUtil.delete(YoungFileActivity.this, copyPath);
                            handler.sendEmptyMessage(UPDATE_STATE);
                            FileUtil.round = true;
                            return;
                            //                            boolean bMove = FileUtil.move(FileActivity.this, copyPath, currentPath.getText() + "/");
                            //                            if (bMove) {
                            //                                FileUtil.delete(FileActivity.this, copyPath);
                            //                                handler.sendEmptyMessage(UPDATE_STATE);
                            //                                FileUtil.round = true;
                            //                                return;
                            //                            }
                        }
                        //                        else {
                        //                            handler.sendEmptyMessage(ERROR_STATE);
                        //                        }
                        //                        FileUtil.round = true;
                        handler.sendEmptyMessage(ERROR_STATE);
                    }
                }).start();
                moveLayout.setVisibility(View.GONE);
            }
        });

        // 取消移动
        moveCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                moveLayout.setVisibility(View.GONE);
            }
        });
    }

    /**
     * gridview item click
     */
    public void gridViewItemClickEvent(int position) {
        if (listFiles == null || listFiles.length <= position) {
            return;
        }

        // 如果是目录，就进入到文件夹内部
        if (listFiles[position].isDirectory()) {
            parentPath = listFiles[position].getParentFile();
            String subPathName = listFiles[position].getName();
            currentPath.append("/" + subPathName);
            listFiles = listFiles[position].listFiles(new MyFileFilter());
            if (listFiles == null) {
                return;
            }
            listFiles = FileUtil.sort(listFiles);
            // 刷新数据，并更新gridView
            gridShowAdapter.updateFiles(listFiles);
            gridShowAdapter.notifyDataSetChanged();
        } else {// 是文件的处理逻辑
            OpenFiles.open(context, listFiles[position]);
        }
    }

    /**
     * 系统返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return false;
        }

        // 当用户按返回键的时候，返回上一级的目录
        if (KeyEvent.KEYCODE_BACK == keyCode) {

            if (parentPath != null
                    && !parentPath.getName().equals("")
                    && !parentPath.getName().equals("emulated")
                    && !parentPath.getName().equals("mnt")
                    && !parentPath.getName().equals("storage")) {
                listFiles = parentPath.listFiles(new MyFileFilter());
                listFiles = FileUtil.sort(listFiles);
                gridShowAdapter.updateFiles(listFiles);
                gridShowAdapter.notifyDataSetChanged();

                parentPath = parentPath.getParentFile();

                String nowPath = currentPath.getText().toString();
                int indexOf = nowPath.lastIndexOf("/");
                if (indexOf != -1) {
                    nowPath = nowPath.substring(0, indexOf);
                    currentPath.setText(nowPath);
                }
                setListViewIndex();
            } else {
                YoungFileActivity.this.finish();
            }
        }

        //        Log.i("test", "keyCode = " + keyCode);
        // 物理菜单按键
        if (KeyEvent.KEYCODE_MENU == keyCode) {
            if (getCurrentFocus().getId() == R.id.gridShow) {
                int position = gridShow.getSelectedItemPosition();
                // 判断是文件夹还是文件
                File file = listFiles[position];
                if (file.isDirectory()) {
                    fileType = "文件夹";
                } else {
                    fileType = "文件";
                }
                // 长按则弹出个上下文菜单
                gridShow.setOnCreateContextMenuListener(menuList);

                gridShow.showContextMenu();
            }

            //            int position = gridShow.getSelectedItemPosition();
            //
            //            Log.i("test", "gridShow.getSelectedItemPosition = " + position);
            //
            //            Log.i("test", "getCurrentFocus 1 = " + getCurrentFocus().getId());
            //
            //            gridShow.requestFocus();
            //            Log.i("test", "getCurrentFocus 2 = " + getCurrentFocus().getId());

            //            Toast.makeText(this, "KEYCODE_MENU", Toast.LENGTH_SHORT).show();
            //            View view = getCurrentFocus();
            //            Toast.makeText(this, "" + view.getId(), Toast.LENGTH_SHORT).show();
            //            Toast.makeText(this, "" + view.getTag(), Toast.LENGTH_SHORT).show();
        }
        // 返回true，菜单键可能无法显示
        return false;
    }

    private void setListViewIndex() {

        if (gridShowAdapter.state != null) {
            gridShow.setAdapter(gridShowAdapter);
            gridShow.onRestoreInstanceState(gridShowAdapter.state);
        }
    }

    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            //  Toast.makeText(FileActivity.this, v.getId() + "", Toast.LENGTH_SHORT).show();
            if (hasFocus) {
                // 如果是左侧的三个按钮获得焦点的话, 则直接触发事件
                if (v.getId() == R.id.main_ib_bendi) {
                    btnBendiSetFocus();
                    btnTfkaSetBackground();
                    btnUpanSetBackground();
                    return;
                } else if (v.getId() == R.id.main_ib_tfka) {
                    btnBendiSetBackground();
                    btnTfkaSetFocus();
                    btnUpanSetBackground();
                    return;
                } else if (v.getId() == R.id.main_ib_upan) {
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

    private void btnBendiSetBackground() {
        if (mMntType == 0) {
            mBtnBendi.setBackgroundResource(R.mipmap.file_btn_bendi_sel);
        } else {
            mBtnBendi.setBackgroundResource(R.mipmap.file_btn_bendi);
        }
    }

    private void btnTfkaSetBackground() {
        if (mMntType == 1) {
            mBtnTfka.setBackgroundResource(R.mipmap.file_btn_tfka_sel);
        } else {
            mBtnTfka.setBackgroundResource(R.mipmap.file_btn_tfka);
        }
    }

    private void btnUpanSetBackground() {
        if (mMntType == 2) {
            mBtnUpan.setBackgroundResource(R.mipmap.file_btn_upan_sel);
        } else {
            mBtnUpan.setBackgroundResource(R.mipmap.file_btn_upan);
        }
    }

    private void btnBendiSetFocus() {
        if (mMntType == 0) {
            mBtnBendi.setImageResource(R.mipmap.file_btn_bendi_sel);
        } else {
            mBtnBendi.setImageResource(R.mipmap.file_btn_bendi);
        }
        mBtnBendi.setBackgroundResource(R.drawable.sel_focus);
    }

    private void btnTfkaSetFocus() {
        if (mMntType == 1) {
            mBtnTfka.setImageResource(R.mipmap.file_btn_tfka_sel);
        } else {
            mBtnTfka.setImageResource(R.mipmap.file_btn_tfka);
        }
        mBtnTfka.setBackgroundResource(R.drawable.sel_focus);
    }

    private void btnUpanSetFocus() {
        if (mMntType == 2) {
            mBtnUpan.setImageResource(R.mipmap.file_btn_upan_sel);
        } else {
            mBtnUpan.setImageResource(R.mipmap.file_btn_upan);
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

        // 如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }

    /**************************
     * 放在Activity类里的的底部
     ************************/

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
        //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
