package com.icox.manager.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.icox.manager.R;
import com.icox.manager.adapter.FileViewPagerAdapter;
import com.icox.manager.dialog.FileDeleteDialog;
import com.icox.manager.dialog.FileDetailDialog;
import com.icox.manager.dialog.FileMenuDialog;
import com.icox.manager.dialog.FileRenameDialog;
import com.icox.manager.dialog.NewFileDialog;
import com.icox.manager.util.FileUtil;
import com.icox.manager.util.MyFileFilter;
import com.icox.manager.util.OpenFiles;
import com.icox.share.BaseActivity;
import com.icox.share.ShareUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by icox-XiuChou  on 2016/1/4
 */
public class FileOldActivity extends BaseActivity implements View.OnClickListener {

    /*******************监听插拔TF卡和U盘的事件***********************/
    /**
     * 注册系统广播
     */
    private AndroidSystemReceiver androidSystemReceiver = null;
    private NewFileDialog mNewFileDialog;
    private ViewPager mFileListPager;
    private ImageView mButLast;
    private ImageView mButNex;
    private TextView mTvNumber;
    private ImageButton mBtnGoBack;

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                int current = mFileListPager.getCurrentItem();
                if (current + 1 < mFileViewPagerAdapter.getCount()) {
                    mFileListPager.setCurrentItem(current + 1);
                    mTvNumber.setText(mFileListPager.getCurrentItem() + 1 + "/" + mFileViewPagerAdapter.getCount());
                }
                break;
            case R.id.last:
                mFileListPager.setCurrentItem(mFileListPager.getCurrentItem() - 1);
                mTvNumber.setText(mFileListPager.getCurrentItem() + 1 + "/" + mFileViewPagerAdapter.getCount());
                break;

            default:
                break;
        }
    }

    public class AndroidSystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // 拔卡
            if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                mBtnBendi.performClick();
                mVolumePaths = ShareUtil.getVolumePaths(context);
                mMntType = 0;
                handler.sendEmptyMessage(INIT_ARRAYLIST);

            }
            // 插卡
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mBtnBendi.performClick();
                mVolumePaths = ShareUtil.getVolumePaths(context);
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
    //    private GridView gridShow = null;
    // 底部 粘贴与移动
    private LinearLayout pasteLayout, moveLayout = null;
    // GridView的适配器
    //    private GridShowAdapter gridShowAdapter = null;
    private FileViewPagerAdapter mFileViewPagerAdapter;
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
                    //                    // 显示当前的路径
                    //                    currentPath.setText(mVolumePaths[mMntType]);
                    //                    tv_loading.setText("加载中...");
                    //                    showFilesLayout.setBackgroundResource(0);
                    //                    showFilesLayoutChild_B.setVisibility(View.VISIBLE);
                    //                    listFiles = scanFiles(mMntType);
                    //                    Log.i("test", "重新扫描模式:" + mMntType + "文件数:" + listFiles.length);
                    //                    handler.sendEmptyMessage(INIT_ADPTER);
                    // 显示当前的路径
                    showFilesLayout.setBackgroundResource(0);
                    showFilesLayoutChild_B.setVisibility(View.VISIBLE);
                    if (mMntType < mVolumePaths.length) {
                        currentPath.setText(mVolumePaths[mMntType]);
                        tv_loading.setText("加载中...");
                        listFiles = scanFiles(mMntType);
                        Log.i("test", "重新扫描模式:" + mMntType + "文件数:" + listFiles.length);
                    } else {
                        listFiles = new File[]{};
                    }

                    handler.sendEmptyMessage(INIT_ADPTER);
                    break;
                case INIT_ADPTER:
                    // 设置适配器
                    initViewPagerAdapter();

                    showFilesLayoutChild_B.setVisibility(View.GONE);
                    if (listFiles.length == 0) {
                        // 显示没有找到任何文件
                        showFilesLayout.setBackgroundResource(R.drawable.nofiles);
                        showFilesLayoutChild_A.setVisibility(View.GONE);
                    } else {
                        showFilesLayout.setBackgroundResource(0);
                        showFilesLayoutChild_A.setVisibility(View.VISIBLE);
                    }
                    //                    handler.sendEmptyMessage(UPDATE_ADPTER);
                    break;
                case UPDATE_ADPTER:
                    mFileViewPagerAdapter.notifyDataSetChanged();
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
                    Toast.makeText(FileOldActivity.this, "目标路径有误,请重新操作! " + currentPath.getText().toString(), Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_file);

        registerHomeKeyReceiver(this);
        // 获取当前运行上下文
        context = FileOldActivity.this;
        // 获取路径
        mVolumePaths = ShareUtil.getVolumePaths(context);
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

    /**
     * 设置控件的焦点
     */
    private void setFocus() {
        mBtnBendi.setOnFocusChangeListener(mFocusChangeListener);
        mBtnTfka.setOnFocusChangeListener(mFocusChangeListener);
        mBtnUpan.setOnFocusChangeListener(mFocusChangeListener);
        mBtnGoBack.setOnFocusChangeListener(mFocusChangeListener);
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
        unregisterReceiver(androidSystemReceiver);
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
            //            position = gridShow.getSelectedItemPosition();
            position = mFileViewPagerAdapter.getCount();
        } else {
            position = menuInfo.position;
        }
        if (position == -1) {
            return super.onContextItemSelected(item);
        }

        final File file = listFiles[position];

        switch (item.getItemId()) {

            case R.id.menu_copy:

                // TODO copy:获取要拷贝的当前文件的绝对路径,“粘贴”操作根据文件的绝对路径转成文件才能对文件的复制,不是简单靠路径!
                copyPath = file.getAbsolutePath();
                //                Log.i("mylog", "复制" + fileType + "的路径:" + copyPath);
                pasteLayout.setVisibility(View.VISIBLE);
                moveLayout.setVisibility(View.GONE);
                break;

            case R.id.menu_move:

                copyPath = file.getAbsolutePath();
                //                Log.i("mylog", "移动" + fileType + "的路径:" + copyPath);
                // TODO move:
                moveLayout.setVisibility(View.VISIBLE);
                pasteLayout.setVisibility(View.GONE);

                break;
            case R.id.menu_rename:

                // TODO rename:使用对话框进行对文件的重命名
                final EditText renameEditText = new EditText(context);
                renameEditText.setText(file.getName());
                renameEditText.selectAll();
                renameEditText.setHint("请输入" + fileType + "的名字");
                AlertDialog.Builder renameDialog = new AlertDialog.Builder(context);
                renameDialog.setTitle("重命名");
                renameDialog.setView(renameEditText);
                renameDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String path = file.getAbsolutePath();
                        if (path != null && !"".equals(path) && renameEditText.getText().toString().length() > 0) {
                            int lastIndexOf = path.lastIndexOf("/");
                            String path1 = path.substring(0, lastIndexOf + 1);
                            boolean b = file.renameTo(new File(path1 + renameEditText.getText().toString()));
                            if (b) {
                                // 刷新UI
                                updateGridShowUI();
                            }
                        }
                    }
                });
                renameDialog.create();
                renameDialog.show();
                break;

            case R.id.menu_delete:

                AlertDialog.Builder icoxBuilder = new AlertDialog.Builder(FileOldActivity.this);
                icoxBuilder.setTitle("删除");
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
                        copyPath = file.getAbsolutePath();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO delete:删除文件
                                handler.sendEmptyMessage(DELETE_STATE);
                                FileUtil.delete(FileOldActivity.this, copyPath);
                                handler.sendEmptyMessage(UPDATE_STATE);
                            }
                        }).start();
                    }
                });
                icoxBuilder.create();
                icoxBuilder.show();
                break;
        }
        return super.onContextItemSelected(item);
    }

    /*******************
     * 菜单相关:↑
     *******************/

    /**
     * 粘贴、重命名、删除操作之后都要刷新当前的UI
     */
    private void updateGridShowUI() {
        // TODO :updateGridShowUI
        listFiles = new File(currentPath.getText() + "/").listFiles(new MyFileFilter());
        listFiles = FileUtil.sort(listFiles);
        initViewPagerAdapter();
    }

    /**
     * 获取系统的路径划分的数组区间
     */
    public static String[] getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;
        try {
            Method methodGetPaths = storageManager.getClass().getMethod("getVolumePaths");
            paths = (String[]) methodGetPaths.invoke(storageManager);
            if (paths != null) {
                if (paths.length == 2) {
                    if (paths[1].contains("B63E-68EE")) {
                        String[] resultPaths = new String[3];
                        resultPaths[0] = paths[0];
                        resultPaths[1] = "/storage/964E-D2C8";
                        resultPaths[2] = paths[1];
                        return resultPaths;
                    }
                }
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
        mBtnGoBack = (ImageButton) findViewById(R.id.main_iv_back);
        mBtnBendi.setOnClickListener(new BtnOnclickListener());
        mBtnTfka.setOnClickListener(new BtnOnclickListener());
        mBtnUpan.setOnClickListener(new BtnOnclickListener());
        mBtnGoBack.setOnClickListener(new BtnOnclickListener());

        // 右边的视图显示,以"Layout"为单位;
        showFilesLayout = (RelativeLayout) findViewById(R.id.showFilesLayout);
        showFilesLayoutChild_A = (LinearLayout) findViewById(R.id.showFilesLayoutChild_A);
        showFilesLayoutChild_B = (LinearLayout) findViewById(R.id.showFilesLayoutChild_B);

        //        gridShow = (GridView) findViewById(gridShow);
        mFileListPager = (ViewPager) findViewById(R.id.file_viewpager);
        // 粘贴Layout
        pasteLayout = (LinearLayout) findViewById(R.id.pasteLayout);
        // 剪切Layout
        moveLayout = (LinearLayout) findViewById(R.id.moveLayout);

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

        mButLast = (ImageView) findViewById(R.id.last);
        mButNex = (ImageView) findViewById(R.id.next);
        mTvNumber = (TextView) findViewById(R.id.tv_number);
        mButLast.setOnClickListener(this);
        mButNex.setOnClickListener(this);
    }

    private class BtnOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.main_ib_bendi) {

                mBtnBendi.setImageResource(R.drawable.media_3);
                mBtnTfka.setImageResource(R.drawable.media_5);
                mBtnUpan.setImageResource(R.drawable.media_1);
                mMntType = 0;
            }
            if (v.getId() == R.id.main_ib_tfka) {

                mBtnBendi.setImageResource(R.drawable.media_4);
                mBtnTfka.setImageResource(R.drawable.media_6);
                mBtnUpan.setImageResource(R.drawable.media_1);
                mMntType = 1;
            }
            if (v.getId() == R.id.main_ib_upan) {

                mBtnBendi.setImageResource(R.drawable.media_4);
                mBtnTfka.setImageResource(R.drawable.media_5);
                mBtnUpan.setImageResource(R.drawable.media_2);
                mMntType = 2;

                //                // test
                //                if (1 == 1){
                //                    Intent intent = new Intent(FileActivity.this, TestFileActivity.class);
                //                    startActivity(intent);
                //                    return;
                //                }
            }

            if (v.getId() == R.id.main_iv_back) {
                finish();
                return;
            }

            btnBendiSetBackground();
            btnTfkaSetBackground();
            btnUpanSetBackground();
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

                mNewFileDialog = new NewFileDialog(FileOldActivity.this) {
                    @Override
                    public void clickCancel() {

                    }

                    @Override
                    public void clickSure() {
                        File file = new File(currentPath.getText() + "/" + mNewFileDialog.getEdtText());

                        //                        Toast.makeText(context, currentPath.getText().toString().toLowerCase() + "/" + et.getText().toString(), Toast.LENGTH_SHORT).show();
                        if (!file.exists()) {
                            boolean c = file.mkdir();
                        }
                        //                        // 刷新UI
                        //                        updateGridShowUI();
                        handler.sendEmptyMessage(UPDATE_STATE);
                    }
                };

                dialogHide(mNewFileDialog);
                mNewFileDialog.show();
            }
        });

        //返回上一层
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //                if (parentPath != null && !parentPath.getName().equals("") && !parentPath.getName().equals("mnt")) {
                if (parentPath != null
                        && !parentPath.getName().equals("")
                        && !parentPath.getName().equals("emulated")
                        && !parentPath.getName().equals("mnt")
                        && !parentPath.getName().equals("storage")) {

                    listFiles = parentPath.listFiles(new MyFileFilter());
                    listFiles = FileUtil.sort(listFiles);

                    initViewPagerAdapter();
                    mTvNumber.setText(mFileListPager.getCurrentItem() + 1 + "/" + mFileViewPagerAdapter.getCount());

                    parentPath = parentPath.getParentFile();
                    String nowPath = currentPath.getText().toString();
                    int indexOf = nowPath.lastIndexOf("/");
                    if (indexOf != -1) {
                        nowPath = nowPath.substring(0, indexOf);
                        currentPath.setText(nowPath);
                    }
                    setListViewIndex();
                } else {
                    FileOldActivity.this.finish();
                }
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
                                                         FileUtil.copy(FileOldActivity.this, copyPath, currentPath.getText() + "/");
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
                            FileUtil.move(FileOldActivity.this, copyPath, currentPath.getText() + "/");
                            FileUtil.delete(FileOldActivity.this, copyPath);
                            handler.sendEmptyMessage(UPDATE_STATE);
                            FileUtil.round = true;
                            return;
                        }
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

        mFileListPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mTvNumber.setText(mFileListPager.getCurrentItem() + 1 + "/" + mFileViewPagerAdapter.getCount());
            }
        });
    }

    private void initViewPagerAdapter() {
        mFileViewPagerAdapter = new FileViewPagerAdapter(context, listFiles);
        mFileListPager.setAdapter(mFileViewPagerAdapter);
        setAdapterClickListener();
        mTvNumber.setText(mFileListPager.getCurrentItem() + 1 + "/" + mFileViewPagerAdapter.getCount());
    }

    private void setAdapterClickListener() {
        mFileViewPagerAdapter.setOnLayoutClickListener(new FileViewPagerAdapter.onLayoutClickListener() {
            @Override
            public void onClickListener(File file) {
                if (file.isDirectory()) {
                    parentPath = file.getParentFile();
                    String subPathName = file.getName();
                    currentPath.append("/" + subPathName);
                    listFiles = file.listFiles(new MyFileFilter());
                    listFiles = FileUtil.sort(listFiles);
                    // 刷新数据，并更新gridView
                    //            gridShowAdapter.updateFiles(listFiles);
                    //            gridShowAdapter.notifyDataSetChanged();
                    initViewPagerAdapter();
                } else {// 是文件的处理逻辑
                    OpenFiles.open(context, file);
                }
            }

            @Override
            public void onLongClickListener(File file) {
                // 判断是文件夹还是文件
                if (file.isDirectory()) {
                    fileType = "文件夹";
                } else {
                    fileType = "文件";
                }
                // 长按则弹出个上下文菜单
                //                gridShow.setOnCreateContextMenuListener(menuList);

                showMenuDialog(file);
            }
        });
    }

    private void showMenuDialog(final File file) {
        FileMenuDialog fileMenuDialog = new FileMenuDialog(FileOldActivity.this) {
            @Override
            public void clickCopy() {
                // TODO copy:获取要拷贝的当前文件的绝对路径,“粘贴”操作根据文件的绝对路径转成文件才能对文件的复制,不是简单靠路径!
                copyPath = file.getAbsolutePath();
                pasteLayout.setVisibility(View.VISIBLE);
                moveLayout.setVisibility(View.GONE);
            }

            @Override
            public void clickCut() {
                copyPath = file.getAbsolutePath();
                moveLayout.setVisibility(View.VISIBLE);
                pasteLayout.setVisibility(View.GONE);
            }

            @Override
            public void clickRename() {
                clickMenuRename(file);
            }

            @Override
            public void clickDelete() {
                clickMenuDelete(file);
            }

            @Override
            public void clickDetail() {
                showFileInfo(file);
            }
        };
        dialogHide(fileMenuDialog);
        fileMenuDialog.show();
    }

    private void showFileInfo(File file) {
        Date date = new Date(file.lastModified());//文件最后修改时间
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:MM");

        String title = file.getName();
        String path = file.getAbsolutePath();
        String size = FileUtil.generateFileSize(file.length());
        String time = format.format(date);

        FileDetailDialog fileDetailDialog = new FileDetailDialog(
                FileOldActivity.this, title, path, size, time
        );

        dialogHide(fileDetailDialog);
        fileDetailDialog.show();
    }

    private void clickMenuDelete(final File file) {
        FileDeleteDialog fileDeleteDialog = new FileDeleteDialog(FileOldActivity.this) {
            @Override
            public void clickCancel() {

            }

            @Override
            public void clickSure() {
                copyPath = file.getAbsolutePath();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO delete:删除文件
                        handler.sendEmptyMessage(DELETE_STATE);
                        FileUtil.delete(FileOldActivity.this, copyPath);
                        handler.sendEmptyMessage(UPDATE_STATE);
                    }
                }).start();
            }
        };

        dialogHide(fileDeleteDialog);
        fileDeleteDialog.show();
    }

    private void clickMenuRename(final File file) {
        FileRenameDialog fileRenameDialog = new FileRenameDialog(FileOldActivity.this) {
            @Override
            public void clickCancel() {

            }

            @Override
            public void clickSure(String edtText) {
                String path = file.getAbsolutePath();
                if (path != null && !"".equals(path) && edtText.length() > 0) {
                    int lastIndexOf = path.lastIndexOf("/");
                    String path1 = path.substring(0, lastIndexOf + 1);
                    boolean b = file.renameTo(new File(path1 + edtText));
                    if (b) {
                        // 刷新UI
                        updateGridShowUI();
                    }
                }
            }
        };
        dialogHide(fileRenameDialog);
        fileRenameDialog.show();
    }

    /**
     * gridview item click
     */
    public void gridViewItemClickEvent(int position) {
        // 如果是目录，就进入到文件夹内部
        if (listFiles[position].isDirectory()) {
            parentPath = listFiles[position].getParentFile();
            String subPathName = listFiles[position].getName();
            currentPath.append("/" + subPathName);
            listFiles = listFiles[position].listFiles(new MyFileFilter());
            listFiles = FileUtil.sort(listFiles);
            // 刷新数据，并更新gridView
            initViewPagerAdapter();
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
                initViewPagerAdapter();
                mTvNumber.setText(mFileListPager.getCurrentItem() + 1 + "/" + mFileViewPagerAdapter.getCount());

                parentPath = parentPath.getParentFile();

                String nowPath = currentPath.getText().toString();
                int indexOf = nowPath.lastIndexOf("/");
                if (indexOf != -1) {
                    nowPath = nowPath.substring(0, indexOf);
                    currentPath.setText(nowPath);
                }
                setListViewIndex();
            } else {
                FileOldActivity.this.finish();
            }
        }

        //        Log.i("test", "keyCode = " + keyCode);
        // 物理菜单按键
        if (KeyEvent.KEYCODE_MENU == keyCode) {

            if (getCurrentFocus().getId() == R.id.item_file_manager_iv_01
                    || getCurrentFocus().getId() == R.id.item_file_manager_iv_02
                    || getCurrentFocus().getId() == R.id.item_file_manager_iv_03
                    || getCurrentFocus().getId() == R.id.item_file_manager_iv_04
                    || getCurrentFocus().getId() == R.id.item_file_manager_iv_05
                    || getCurrentFocus().getId() == R.id.item_file_manager_iv_06) {
                int position = (int) getCurrentFocus().getTag();
                File file = listFiles[position];
                if (file.isDirectory())
                    fileType = "文件夹";
                else
                    fileType = "文件";
                showMenuDialog(file);
            }


            //            if (getCurrentFocus().getId() == gridShow) {
            //                int position = gridShow.getSelectedItemPosition();
            //                // 判断是文件夹还是文件
            //                File file = listFiles[position];
            //                if (file.isDirectory()) {
            //                    fileType = "文件夹";
            //                } else {
            //                    fileType = "文件";
            //                }
            //                // 长按则弹出个上下文菜单
            //                //                gridShow.setOnCreateContextMenuListener(menuList);
            //                //                gridShow.showContextMenu();
            //
            //                showMenuDialog(file);
            //            }

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

        //        if (gridShowAdapter.state != null) {
        //            gridShow.setAdapter(gridShowAdapter);
        //            gridShow.onRestoreInstanceState(gridShowAdapter.state);
        //        }
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
                    btnGoBackSetBackground();
                    return;
                } else if (v.getId() == R.id.main_ib_tfka) {
                    btnBendiSetBackground();
                    btnTfkaSetFocus();
                    btnUpanSetBackground();
                    btnGoBackSetBackground();
                    return;
                } else if (v.getId() == R.id.main_ib_upan) {
                    btnBendiSetBackground();
                    btnTfkaSetBackground();
                    btnUpanSetFocus();
                    btnGoBackSetBackground();
                    return;
                } else if (v.getId() == R.id.main_iv_back) {
                    btnBendiSetBackground();
                    btnTfkaSetBackground();
                    btnUpanSetBackground();
                    btnGoBackSetFocus();
                    return;
                }
            }

            btnBendiSetBackground();
            btnTfkaSetBackground();
            btnUpanSetBackground();
            btnGoBackSetBackground();


            //            viewAnimation(v, hasFocus);
        }
    };

    private void btnBendiSetBackground() {
        if (mMntType == 0) {
            mBtnBendi.setBackgroundResource(R.drawable.media_3);
        } else {
            mBtnBendi.setBackgroundResource(R.drawable.media_4);
        }
    }

    private void btnGoBackSetBackground() {
        mBtnGoBack.setBackgroundResource(com.icox.mediafilemanager.R.drawable.selector_but_go_back);
    }

    private void btnGoBackSetFocus() {
        mBtnGoBack.setImageResource(com.icox.mediafilemanager.R.drawable.selector_but_go_back);
        mBtnGoBack.setBackgroundResource(com.icox.mediafilemanager.R.drawable.sel_focus);
    }

    private void btnTfkaSetBackground() {
        if (mMntType == 1) {
            mBtnTfka.setBackgroundResource(R.drawable.media_6);
        } else {
            mBtnTfka.setBackgroundResource(R.drawable.media_5);
        }
    }

    private void btnUpanSetBackground() {
        if (mMntType == 2) {
            mBtnUpan.setBackgroundResource(R.drawable.media_2);
        } else {
            mBtnUpan.setBackgroundResource(R.drawable.media_1);
        }
    }

    private void btnBendiSetFocus() {
        if (mMntType == 0) {
            mBtnBendi.setImageResource(R.drawable.media_3);
        } else {
            mBtnBendi.setImageResource(R.drawable.media_4);
        }
        mBtnBendi.setBackgroundResource(R.drawable.sel_focus);
    }

    private void btnTfkaSetFocus() {
        if (mMntType == 1) {
            mBtnTfka.setImageResource(R.drawable.media_6);
        } else {
            mBtnTfka.setImageResource(R.drawable.media_5);
        }
        mBtnTfka.setBackgroundResource(R.drawable.sel_focus);
    }

    private void btnUpanSetFocus() {
        if (mMntType == 2) {
            mBtnUpan.setImageResource(R.drawable.media_2);
        } else {
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
    }

    private void dialogHide(final Dialog dialog) {
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                initWindow();
            }
        });
    }
}
