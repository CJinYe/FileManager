package com.icox.administrator.icoxmp3player.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icox.administrator.icoxmp3player.R;
import com.icox.administrator.icoxmp3player.bean.Music;
import com.icox.administrator.icoxmp3player.connection.Connection;
import com.icox.administrator.icoxmp3player.db.music_dao.MusicDaoImpl;
import com.icox.administrator.icoxmp3player.dialog.GoBackDialog;
import com.icox.administrator.icoxmp3player.myview.floatable_textview.FloatableTextView;
import com.icox.administrator.icoxmp3player.uitl.MusicUtil;
import com.icox.share.BaseActivity;
import com.icox.share.ShareUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by XiuChou on 2015/11/23
 */
public class ChooseModeActivity extends BaseActivity {

    //运行上下文
    private Context mContext = null;
    private ListView mListView = null;
    //ListView适配器
    private ListViewAdapter mListViewAdapter = null;
    //当前的模式[本地/TF卡/U盘]
    private String pathType = null;

    //系统路径的区域划分
    private String[] mVolumePaths = null;
    private Music mMusic = null;

    //
    private View mSelectedView = null;

    /********
     * 刷新UI
     ********/
    // 三种Message
    private final int INIT_ARRAYLIST = 1;
    private final int INIT_ADPTER = 2;
    private final int UPDATE_ADPTER = 3;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {

                case INIT_ARRAYLIST:

                    mListView.setBackgroundResource(0);
                    rl_showMusicList.setBackgroundResource(0);
                    rl_showMusicListChild_B.setVisibility(View.VISIBLE);

                    //扫描歌曲
                    Thread mThread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            //根据模式，扫描音乐数据
                            GetMusicDetailedInformation(mMntType);
                            handler.sendEmptyMessage(INIT_ADPTER);
                        }
                    });
                    mThread.start();

                    break;
                case INIT_ADPTER:
                    rl_showMusicListChild_B.setVisibility(View.GONE);
                    if (mListViewAdapter == null) {
                        mListViewAdapter = new ListViewAdapter();
                        mListView.setAdapter(mListViewAdapter);
                    }
                    // 如果搜索之后，集合大小还是0，则无歌曲
                    if (0 == Connection.musicList.size()) {
                        rl_showMusicList.setBackgroundResource(R.drawable.nosongs);
                        rl_showMusicListChild_A.setVisibility(View.GONE);
                        if (myCollected == true) {
                            rl_showMusicListChild_A.setVisibility(View.VISIBLE);
                            rl_showMusicList.setBackgroundResource(0);
                            mListView.setBackgroundResource(R.drawable.collect_songs);
                        }
                        myCollected = false;
                    } else {
                        if (myCollected == true) {
                            mListView.setBackgroundResource(0);
                            //                            Toast.makeText(ChooseModeActivity.this, "长按可取消收藏", Toast.LENGTH_LONG).show();
                        }
                        rl_showMusicList.setBackgroundResource(0);
                        rl_showMusicListChild_A.setVisibility(View.VISIBLE);
                    }
                    handler.sendEmptyMessage(UPDATE_ADPTER);
                    break;
                case UPDATE_ADPTER:
                    mListViewAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };
    private ImageButton mMBtnGoBack;

    /**
     * 获取系统的路径划分的数组区间
     */
    public static String[] getVolumePaths(Context context) {

        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;
        try {
            Method methodGetPaths = storageManager.getClass().getMethod("getVolumePaths");
            paths = (String[]) methodGetPaths.invoke(storageManager);
        } catch (Exception e) {

        }
        return paths;
    }

    @Override
    protected void onCreate(Bundle Icox_Mp3Player) {
        super.onCreate(Icox_Mp3Player);
        initWindow();
        setContentView(R.layout.activity_choose_mode);

        // 注册Home键广播
        registerHomeKeyReceiver(this);
        // 注册广播接收器
        registerIcoxReceiver();
        // 初始化路径
        initPath();
        // 实例化控件
        initView();
        // 设置监听事件
        setListener();
        // 设置控件的焦点
        setFocus();
    }

    /**
     * 初始化路径
     */
    private void initPath() {
        mContext = ChooseModeActivity.this;
        mVolumePaths = ShareUtil.getVolumePaths(mContext);
    }

    /**
     * 实例化控件
     */
    private ImageButton mBtnBendi = null;
    private ImageButton mBtnTfka = null;
    private ImageButton mBtnUpan = null;

    private void initView() {

        rl_showMusicList = (RelativeLayout) findViewById(R.id.rl_showMusicList);
        rl_showMusicListChild_A = (LinearLayout) findViewById(R.id.rl_showMusicListChild_A);
        rl_showMusicListChild_B = (LinearLayout) findViewById(R.id.rl_showMusicListChild_B);
        mListView = (ListView) findViewById(R.id.listView);
        allMusics = (ImageView) findViewById(R.id.allMusics);
        myCollection = (ImageView) findViewById(R.id.myCollection);

        /*******************/
        // 左边三种模式的选择, 与监听事件
        mBtnBendi = (ImageButton) findViewById(R.id.main_ib_bendi);
        mBtnTfka = (ImageButton) findViewById(R.id.main_ib_tfka);
        mBtnUpan = (ImageButton) findViewById(R.id.main_ib_upan);
        mMBtnGoBack = (ImageButton) findViewById(R.id.iv_back);
        mMBtnGoBack.setOnClickListener(new BtnOnclickListener());
        mBtnBendi.setOnClickListener(new BtnOnclickListener());
        mBtnTfka.setOnClickListener(new BtnOnclickListener());
        mBtnUpan.setOnClickListener(new BtnOnclickListener());
    }

    private class BtnOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.iv_back) {

                showGoBackDialog();
                return;
            }

            if (v.getId() == R.id.main_ib_bendi) {

                mBtnBendi.setImageResource(R.drawable.media_3);
                mBtnTfka.setImageResource(R.drawable.media_5);
                mBtnUpan.setImageResource(R.drawable.media_1);
                pathType = mVolumePaths[0] + ",/mnt/media";
                mMntType = 0;
            }
            if (v.getId() == R.id.main_ib_tfka) {

                mBtnBendi.setImageResource(R.drawable.media_4);
                mBtnTfka.setImageResource(R.drawable.media_6);
                mBtnUpan.setImageResource(R.drawable.media_1);
                if (mVolumePaths.length > 2) {
                    pathType = mVolumePaths[1];
                }
                mMntType = 1;
            }
            if (v.getId() == R.id.main_ib_upan) {

                mBtnBendi.setImageResource(R.drawable.media_4);
                mBtnTfka.setImageResource(R.drawable.media_5);
                mBtnUpan.setImageResource(R.drawable.media_2);
                if (mVolumePaths.length > 2) {
                    pathType = mVolumePaths[2];
                }
                mMntType = 2;
            }
            myCollected = false;
            handler.sendEmptyMessage(INIT_ARRAYLIST);
        }
    }


    /**
     * 设置监听事件
     */
    private void setListener() {

        // 全部歌曲
        allMusics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                myCollected = false;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
        });

        // 我的收藏
        myCollection.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                myCollected = true;
                handler.sendEmptyMessage(INIT_ARRAYLIST);
            }
        });

        // 首页选择菜单
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(ChooseModeActivity.this, PlayingActivity.class);
                intent.putExtra("BEGIN", position);
                intent.putExtra("PATHTYPE", pathType);
                startActivity(intent);
            }
        });

        //        // 长按的操作
        //        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        //
        //            @Override
        //            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
        //
        //                // 在收藏那里才能长按
        //                cancelCollect(position);
        //
        //                /*if (myCollected == true) {
        //                    Music inputDbMusic = Connection.musicList.get(position);
        //                    AlertDialog.Builder icoxBuilder = new AlertDialog.Builder(ChooseModeActivity.this);
        //                    icoxBuilder.setTitle("取消收藏");
        //                    icoxBuilder.setMessage("不再收藏《" + inputDbMusic.getName() + "》?");
        //                    icoxBuilder.setCancelable(true);
        //                    icoxBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
        //                        @Override
        //                        public void onClick(DialogInterface dialogInterface, int i) {
        //
        //                        }
        //                    });
        //
        //                    icoxBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
        //
        //                        @Override
        //                        public void onClick(DialogInterface dialogInterface, int i) {
        //                            MusicDaoImpl dao = new MusicDaoImpl(ChooseModeActivity.this);
        //                            ContentValues values = new ContentValues();
        //                            Music inputDbMusic = Connection.musicList.get(position);
        //                            String url = inputDbMusic.getUrl();
        //                            String name = inputDbMusic.getName();
        //                            Log.i("myCollected", "url:" + url);
        //                            Log.i("myCollected", "name:" + name);
        //                            values.put("url", url);
        //                            values.put("name", name);
        //                            dao.deleteMusic("name=?", new String[]{name});
        //                            handler.sendEmptyMessage(INIT_ARRAYLIST);
        //                        }
        //                    });
        //                    icoxBuilder.create();
        //                    icoxBuilder.show();
        //                }*/
        //                return true;
        //            }
        //        });

        //        // listview选中状态
        //        mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //            @Override
        //            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        ////                if (mSelectedView != null){
        ////                    mSelectedView.setBackgroundColor(Color.TRANSPARENT);
        ////                }
        ////                view.setBackgroundColor(Color.RED);
        ////                mSelectedView = view;
        //            }
        //
        //            @Override
        //            public void onNothingSelected(AdapterView<?> adapterView) {
        //                Log.i("test", "onNothingSelected");
        //            }
        //        });
    }

    /**
     * 设置控件的焦点
     */
    private void setFocus() {

        mBtnBendi.setOnFocusChangeListener(mFocusChangeListener);
        mBtnTfka.setOnFocusChangeListener(mFocusChangeListener);
        mBtnUpan.setOnFocusChangeListener(mFocusChangeListener);
        allMusics.setOnFocusChangeListener(mFocusChangeListener);
        myCollection.setOnFocusChangeListener(mFocusChangeListener);
        mMBtnGoBack.setOnFocusChangeListener(mFocusChangeListener);

        //        mListView.setOnFocusChangeListener(mFocusChangeListener);
    }

    /**
     * 提供选中放大的效果
     */
    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

            if (hasFocus) {
                //                Log.i("test", "hasFocus");
                //                if (mListView != null && mListViewAdapter != null){
                ////                    mListView.setSelection(-1);
                //                    mListView.getSelectedView().setSelected(false);
                //                    mListViewAdapter.notifyDataSetChanged();
                //                }

                //                // listview 选中状态修改
                //                mSelectedView = mListView.getSelectedView();
                //                if (mSelectedView != null) {
                //                    mSelectedView.setBackgroundColor(Color.TRANSPARENT);
                //                    mSelectedView.setSelected(false);
                //                }
                //                else if (mListView != null){
                //                    mSelectedView = mListView.getSelectedView();
                //                    mSelectedView.setBackgroundColor(Color.TRANSPARENT);
                //                    mSelectedView.setSelected(false);
                //                }

                if (v.getId() == R.id.main_ib_bendi) {
                    btnBendiSetFocus();
                    btnTfkaSetBackground();
                    btnGoBackSetBackground();
                    btnUpanSetBackground();
                    return;
                } else if (v.getId() == R.id.main_ib_tfka) {
                    btnBendiSetBackground();
                    btnTfkaSetFocus();
                    btnGoBackSetBackground();
                    btnUpanSetBackground();
                    return;
                } else if (v.getId() == R.id.main_ib_upan) {
                    btnBendiSetBackground();
                    btnTfkaSetBackground();
                    btnGoBackSetBackground();
                    btnUpanSetFocus();
                    return;
                } else if (v.getId() == R.id.iv_back) {
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

            //            // listview 选中状态修改
            //            if (v.getId() == R.id.listView){
            //                if (!hasFocus) {
            //                    Log.i("test", "hasFocus");
            //                    if (mSelectedView != null) {
            //                        mSelectedView.setBackgroundColor(Color.TRANSPARENT);
            //                        mSelectedView.setSelected(false);
            ////                        mSelectedView.setBackgroundResource(R.drawable.sel_item_bg);
            //                    }
            //                }
            //            }
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
        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }

    private void btnBendiSetBackground() {
        if (mMntType == 0) {
            mBtnBendi.setBackgroundResource(R.drawable.media_3);
        } else {
            mBtnBendi.setBackgroundResource(R.drawable.media_4);
        }
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

    private void btnGoBackSetBackground() {
        mMBtnGoBack.setBackgroundResource(R.drawable.selector_but_go_back);
    }

    private void btnGoBackSetFocus() {
        mMBtnGoBack.setImageResource(R.drawable.selector_but_go_back);
        mMBtnGoBack.setBackgroundResource(R.drawable.sel_focus);
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


    private void registerIcoxReceiver() {

        //TF卡与U盘的插拔广播
        androidSystemReceiver = new AndroidSystemReceiver();
        IntentFilter systemFilter = new IntentFilter();

        systemFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        systemFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        // 在文件管理器处理了之后要在加这个[intent.action_media_scanner_scan_file]的接收器
        systemFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        // 必须添加，否则无法接收到系统广播
        systemFilter.addDataScheme("file");
        registerReceiver(androidSystemReceiver, systemFilter);
    }

    private AndroidSystemReceiver androidSystemReceiver;

    public class AndroidSystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                String path = intent.getData().toString().substring("file://".length());
                return;
            }
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                mVolumePaths = ShareUtil.getVolumePaths(mContext);
                handler.sendEmptyMessage(INIT_ARRAYLIST);

                return;
            }
        }
    }

    /**
     * 释放资源
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 注销HomeKey广播
        unregisterHomeKeyReceiver(this);
        unregisterReceiver(androidSystemReceiver);
        Connection.musicList.clear();
    }

    /**
     * 系统返回键的人机交互
     */

    // 停止音乐的 Intent,为广播的一部分
    private static Intent stopIntent = new Intent("STOP_RECEIVER");

    /**
     * 按键监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return super.onKeyDown(keyCode, event);
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            //            AlertDialog.Builder icoxBuilder = new AlertDialog.Builder(ChooseModeActivity.this);
            //            icoxBuilder.setTitle("退出播放器");
            //            icoxBuilder.setMessage("后台继续播放音乐?");
            //            icoxBuilder.setCancelable(true);
            //            icoxBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
            //
            //                @Override
            //                public void onClick(DialogInterface dialogInterface, int i) {
            //                    //                    scanFile(ChooseModeActivity.this, String filePath);
            //                    // TODO 发送广播,让音乐停止
            //                    stopIntent.putExtra("Stop", true);
            //                    sendBroadcast(stopIntent);
            //                    ChooseModeActivity.this.finish();
            //                }
            //            });
            //
            //            icoxBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            //
            //                @Override
            //                public void onClick(DialogInterface dialogInterface, int i) {
            //                    ChooseModeActivity.this.finish();
            //                }
            //            });
            //            icoxBuilder.create();
            //            icoxBuilder.show();

            showGoBackDialog();

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            //音量控制,初始化定义
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);

            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //音量控制,初始化定义
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);

            return true;
        }
        // 菜单键
        else if (KeyEvent.KEYCODE_MENU == keyCode) {
            if (getCurrentFocus().getId() == R.id.music_bg) {
                if (mListView != null) {
                    cancelCollect(mListView.getPositionForView(getCurrentFocus()));
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }
    //
    //    public static void scanFile(Context context, String filePath) {
    //        Uri uri = Uri.fromFile(new File(filePath));
    //        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
    //        context.sendBroadcast(scanIntent);
    //    }

    /**
     * 实例化控件
     */

    private ImageView allMusics, myCollection;

    private boolean myCollected = false;
    private RelativeLayout rl_showMusicList = null;

    // TODO 跟多U盘路径的优化
    private int mMntType = 0;

    private LinearLayout rl_showMusicListChild_A, rl_showMusicListChild_B;

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessage(INIT_ARRAYLIST);
    }

    /**
     * ListView的适配器
     */
    private class ListViewAdapter extends BaseAdapter {

        // 集合的大小
        @Override
        public int getCount() {
            return Connection.musicList.size();
        }

        // 集合的id
        @Override
        public Object getItem(int position) {
            return Connection.musicList.get(position).getTitle();
        }

        // 集合的id
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 绘制视图
        @Override
        public View getView(final int position, View itemView, ViewGroup viewGroup) {

            ViewHolder holder = null;
            if (null == itemView) {
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_listview, null);
                holder = new ViewHolder();
                holder.floatableTextView = (FloatableTextView) itemView.findViewById(R.id.floatable_text_view);
                holder.textTime = (TextView) itemView.findViewById(R.id.musicAllTime);
                holder.littleDefault = (ImageView) itemView.findViewById(R.id.littleDefault);
                itemView.setTag(holder);
            } else {
                holder = (ViewHolder) itemView.getTag();
            }
            if (0 != Connection.musicList.size()) {
                holder.floatableTextView.init(Connection.musicList.get(position).getTitle(), 0.7f, mListView.getWidth());
                holder.floatableTextView.startFloating();
                holder.textTime.setText(MusicUtil.formatTime(Connection.musicList.get(position).getTime()));

                try {
                    // 获取ContentResolver
                    ContentResolver cr = ChooseModeActivity.this.getContentResolver();
                    // 获取当前的uri
                    Uri uri = Connection.musicList.get(position).getUri();
                    InputStream is = cr.openInputStream(uri);
                    if (null != is) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        // 宽高设为原来的1/10
                        options.inSampleSize = 10;
                        Bitmap coverPhoto = BitmapFactory.decodeStream(is, null, options);
                        holder.littleDefault.setImageBitmap(coverPhoto);
                    } else {
                        holder.littleDefault.setImageResource(R.drawable.littledefault);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            //            if (myCollected == true) {
            //                holder.floatableTextView.setOnLongClickListener(new View.OnLongClickListener() {
            //                    @Override
            //                    public boolean onLongClick(View view) {
            //                        cancelCollect(position);
            //                        return false;
            //                    }
            //                });
            //            }

            //            itemView.setSelected(false);
            //            itemView.setBackgroundResource(R.drawable.sel_item_bg);

            //            itemView.setFocusable(true);
            //            itemView.setBackgroundResource(R.drawable.sel_focus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listItemClickEvent(position);
                }
            });


            return itemView;
        }
    }

    //
    public void selectedViewShow(View itemView) {
        FloatableTextView floatableTextView = (FloatableTextView) itemView.findViewById(R.id.floatable_text_view);

    }

    private void listItemClickEvent(int position) {
        Intent intent = new Intent(ChooseModeActivity.this, PlayingActivity.class);
        intent.putExtra("BEGIN", position);
        intent.putExtra("PATHTYPE", pathType);
        startActivity(intent);
    }

    /**
     * ListView的优化
     */
    private static class ViewHolder {
        private ImageView littleDefault = null;
        private TextView textTime = null;
        private FloatableTextView floatableTextView = null;
    }

    /**
     * 获取音乐的详细信息
     */
    private void GetMusicDetailedInformation(int mMntType) {

        Connection.musicList.clear();
        lrcNameList.clear();
        if (mMntType < mVolumePaths.length && mMntType == 0) {
            File file = new File(mVolumePaths[mMntType]);
            if (file.canRead()) {
                String path = mVolumePaths[mMntType] + ",/mnt/media";
                ScanMusic(path);
            }
        } else if (mMntType < mVolumePaths.length && mMntType == 1) {
            String path = mVolumePaths[mMntType];
            File file = new File(path);
            if (file.exists()) {
                if (file.canRead()) {
                    ScanMusic(path + "," + path);
                }
            }
        } else {
            // 有多个扫描的USB路径
            for (int i = 2; i < mVolumePaths.length; i++) {
                String path = mVolumePaths[i];
                File file = new File(path);
                if (file.exists()) {
                    if (file.canRead()) {
                        ScanMusic(path + "," + path);
                    }
                }
            }
        }
    }

    /**
     * 根据路径扫描音乐
     *
     * @param pathType 路径
     */
    private void ScanMusic(String pathType) {

        if (pathType.split(",").length == 2) {
            String path1 = pathType.split(",")[0];
            String path2 = pathType.split(",")[1];
            //            Log.i("cursor", "path1:" + path1);
            //            Log.i("cursor", "path2:" + path2);
            File file = new File(path1);
            if (file.canRead()) {
                ContentResolver cr = ChooseModeActivity.this.getContentResolver();
                if (cr != null) {
                    // 获取所有歌曲
                    Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            do {
                                // 这里重复创建对象
                                mMusic = new Music();
                                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                                String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                                long time = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                                String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                                long music_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                                String sbr = name.substring(name.length() - 3, name.length());
                                // 需要mp3的id号才能获取正确的uri，进而才能获取专辑图片
                                Uri albumUri = Uri.parse("content://media/external/audio/media/" + music_id + "/albumart");
                                if ("<unknown>".equals(singer)) {
                                    singer = "未知艺术家";
                                }
                                // TODO "全部歌曲"模式下直接扫描,并不需要检查数据库
                                // 包含当前模式的路径，同时不包含"教学软件"里的音乐文件;
                                if ((url.contains(path1) || url.contains(path2)) && !url.contains("教学软件")) {
                                    if (   // 支持主流的音频格式
                                            sbr.equals("mp3")
                                                    || sbr.equals("tak")
                                                    || sbr.equals("tta")
                                                    || sbr.equals("ogg")
                                                    || sbr.equals("wav")
                                                    || sbr.equals("wma")
                                                    || sbr.equals("wmv")
                                                    || sbr.equals("asf")
                                                    || sbr.equals("ape")
                                                    || sbr.equals("acc")
                                                    || sbr.equals("flac")
                                        //                                                    || sbr.equals("midi")
                                        //                                                    || sbr.equals("mid")
                                        //                                                    || sbr.equals("amr")
                                            ) {
                                        File musicFile = new File(url);
                                        if (musicFile.exists()) {
                                            mMusic.setMusicId(music_id);
                                            mMusic.setTitle(title);
                                            mMusic.setSinger(singer);
                                            mMusic.setAlbum(album);
                                            mMusic.setSize(size);
                                            mMusic.setTime(time);
                                            mMusic.setUrl(url);
                                            mMusic.setName(name);
                                            // 设置专辑图片uri
                                            mMusic.setUri(albumUri);
                                            // 默认歌词
                                            mMusic.setLyricPath("");

                                            // TODO "我的收藏"
                                            if (myCollected) {
                                                MusicDaoImpl dao = new MusicDaoImpl(ChooseModeActivity.this);
                                                List<Map<String, String>> list = dao.findByMusic("url=?", new String[]{url});
                                                // 遍历list所有元素,数据库中有的话才加入
                                                for (int i = 0; i < list.size(); i++) {
                                                    Map<String, String> map = list.get(i);
                                                    String myUrl = map.get("url");
                                                    String myName = map.get("name");
                                                    if (myUrl.equals(url) && myName.equals(name)) {
                                                        Connection.musicList.add(mMusic);
                                                    }
                                                }
                                            } else {
                                                // TODO "全部歌曲"
                                                // 直接全部加入
                                                Connection.musicList.add(mMusic);
                                            }
                                        }
                                    }
                                }
                            } while (cursor.moveToNext());
                        }
                        cursor.close();
                    }
                }
            }
            // TODO 注意歌词的部分也只是path1
            // 根据模式,扫描歌词文件
            SearchLyricFile(path1);
        }
    }

    /**
     * 扫描歌词
     */
    private void SearchLyricFile(String path) {

        File file = new File(path);
        if (file.exists()) {
            getLyricFiles(file);
            //让歌词与歌曲匹配
            letLyricMatchMusic();
        }
    }

    // 歌词集合
    private List<HashMap<String, String>> lrcNameList = new ArrayList<HashMap<String, String>>();
    private HashMap<String, String> lrcNameMap = null;

    /**
     * 扫描歌词文件
     */
    private void getLyricFiles(File file) {
        file.listFiles(new FileFilter() {

                           @Override
                           public boolean accept(final File file) {
                               String name = file.getName();
                               int i = name.lastIndexOf('.');
                               if (i != -1) {
                                   name = name.substring(i);
                                   //找到lrc格式的文件
                                   if (name.equalsIgnoreCase(".lrc")) {
                                       String filePath = file.getAbsolutePath();
                                       String lrcName = getFileNameNoEx(new File(filePath).getName());
                                       lrcNameMap = new HashMap<String, String>();
                                       lrcNameMap.put("LRCNAME", lrcName);
                                       lrcNameMap.put("FILEPATH", filePath);
                                       lrcNameList.add(lrcNameMap);
                                   }
                                   return true;
                               } else if (file.isDirectory()) {
                                   getLyricFiles(file);
                               }
                               return false;
                           }
                       }
        );
    }

    /**
     * 让歌词与歌曲匹配
     */
    private void letLyricMatchMusic() {
        // Log.i("Size", "音乐文件的大小:" + Connection.musicList.size());
        // Log.i("Size", "歌词文件的大小:" + lrcNameList.size());
        // 歌曲多,歌词少
        if (Connection.musicList.size() > lrcNameList.size()) {
            for (int i = 0; i < Connection.musicList.size(); i++) {
                for (int j = 0; j < lrcNameList.size(); j++) {
                    String Music = getFileNameNoEx(new File(Connection.musicList.get(i).getUrl()).getName());
                    String Lyric = lrcNameList.get(j).get("LRCNAME");
                    if (Lyric.equals(Music)) {
                        //歌曲匹配成功
                        String lrcUrl = lrcNameList.get(j).get("FILEPATH");
                        Connection.musicList.get(i).setLyricPath(lrcUrl);
                    }
                }
            }
        } else {
            //歌曲少,歌词多
            for (int i = 0; i < lrcNameList.size(); i++) {
                for (int j = 0; j < Connection.musicList.size(); j++) {
                    String Lyric = lrcNameList.get(i).get("LRCNAME");
                    String Music = getFileNameNoEx(new File(Connection.musicList.get(j).getUrl()).getName());
                    if (Music.equals(Lyric)) {
                        //歌曲匹配成功
                        String lrcUrl = lrcNameList.get(i).get("FILEPATH");
                        Connection.musicList.get(j).setLyricPath(lrcUrl);
                    }
                }
            }
        }
    }

    /**
     * Java文件操作: 获取不带扩展名的文件名
     */
    public static String getFileNameNoEx(String filename) {

        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 取消收藏
     */
    private void cancelCollect(final int position) {
        if (myCollected == true) {
            Music inputDbMusic = Connection.musicList.get(position);
            AlertDialog.Builder icoxBuilder = new AlertDialog.Builder(ChooseModeActivity.this);
            icoxBuilder.setTitle("取消收藏");
            icoxBuilder.setMessage("不再收藏《" + inputDbMusic.getName() + "》?");
            icoxBuilder.setCancelable(true);
            icoxBuilder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            icoxBuilder.setPositiveButton("是", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MusicDaoImpl dao = new MusicDaoImpl(ChooseModeActivity.this);
                    ContentValues values = new ContentValues();
                    Music inputDbMusic = Connection.musicList.get(position);
                    String url = inputDbMusic.getUrl();
                    String name = inputDbMusic.getName();
                    Log.i("myCollected", "url:" + url);
                    Log.i("myCollected", "name:" + name);
                    values.put("url", url);
                    values.put("name", name);
                    dao.deleteMusic("name=?", new String[]{name});
                    handler.sendEmptyMessage(INIT_ARRAYLIST);
                }
            });
            //            icoxBuilder.create();
            icoxBuilder.show();
        }
    }


    /************
     * 监听HOME键
     ************/

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

    private void showGoBackDialog() {
        GoBackDialog goBackDialog = new GoBackDialog(ChooseModeActivity.this) {
            @Override
            public void clickNo() {
                stopIntent.putExtra("Stop", true);
                sendBroadcast(stopIntent);
                ChooseModeActivity.this.finish();
            }

            @Override
            public void clickYes() {
                ChooseModeActivity.this.finish();
            }
        };

        showDialog((Activity) mContext,goBackDialog);
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


