package com.icox.administrator.icoxmp3player.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.icox.administrator.icoxmp3player.R;
import com.icox.administrator.icoxmp3player.bean.Music;
import com.icox.administrator.icoxmp3player.connection.Connection;
import com.icox.administrator.icoxmp3player.db.music_dao.MusicDaoImpl;
import com.icox.administrator.icoxmp3player.myview.floatable_textview.FloatableTextView;
import com.icox.administrator.icoxmp3player.myview.lrcview.LrcView;
import com.icox.administrator.icoxmp3player.service.PlayingService;
import com.icox.administrator.icoxmp3player.uitl.MusicUtil;
import com.icox.administrator.icoxmp3player.uitl.ScreenAdaptationUtil;
import com.icox.share.BaseActivity;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by XiuChou on 2015/11/23
 */
public class PlayingActivity extends BaseActivity {

    /**
     * 屏幕适配部分
     */
    // 屏幕的宽高
    private int screenWidth = 0;
    private int screenHeight = 0;
    private ImageView mGoBack;

    /**
     * 获取屏幕宽与高
     */
    private void getScreenWidthAndHeight() {
        WindowManager wm = PlayingActivity.this.getWindowManager();
        screenWidth = wm.getDefaultDisplay().getWidth();
        screenHeight = wm.getDefaultDisplay().getHeight();
    }

    /**
     * 根据屏幕进行适当缩放
     */
    private void setViewZoom(View view, int width, int height) {
        // 获取控件所在的布局
        ViewGroup.LayoutParams para = view.getLayoutParams();
        // 修改布局中的height，width属性
        para.width = (width * screenWidth) / ScreenAdaptationUtil.backgroundWidth;
        para.height = (height * screenHeight) / ScreenAdaptationUtil.backgroundHeight;
        // 重新设置修改后的布局控件
        view.setLayoutParams(para);
    }

    // 侧边栏的适配器
    private ListViewAdapter mListViewAdapter = null;
    // 上一曲，下一曲，播放模式
    private ImageView downVolume, upVolume, mLastMusic, mNextMusic, mPlayMode = null;
    // 音乐菜单、暂停或者播放、歌词、侧边音乐菜单、音量+频谱
    private CheckBox mPlayOrPause, mLyrics, mSmallMenu, mCollect = null;
    // 侧边音乐菜单的布局
    private LinearLayout mLinearLayout = null;
    // 歌词/专辑
    private RelativeLayout albumLayout = null;
    // 专辑封面专辑:专辑名、歌手、歌曲名
    private FloatableTextView showSong, showSinger, songTitle, showAlbumText = null;
    // 服务
    private PlayingService mService = null;
    // 当前播放时间 ,总的长度
    public TextView tv_curcentTime, tv_allTime, musicMenuTitle = null;
    // 显示专辑图片
    private ImageView showAlbumPicture, album, singer, song;
    // 播放进度条
    public SeekBar seekBar = null;
    // 自定义歌词View
    public static LrcView lrc_view = null;
    // 侧边栏的音乐菜单
    private ListView mListView = null;
    // 当前播放的位置
    private int nowPlayingPosition = 0;
    // 随机播放
    private final int RANDOM_PLAY = 1;
    // 全部循环
    private final int ALL_CYCLE = 2;
    // 单曲循环
    private final int SINGLE_CYCLE = 3;

    /**
     * 根据播放歌曲position进行刷新UI;
     */
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            int position = msg.what;
            //设置专辑图片和信息
            setAlbumPictureAndInformation(position);
            checkCollected(position);
        }
    };


    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    mService = PlayingService.playingService;
                    if (mService == null) {
                        // 每隔一秒检测有没服务;
                        mHandler.sendEmptyMessageDelayed(0, 1000);
                    } else {
                        // 有服务则进行下一步
                        mHandler.sendEmptyMessage(1);
                    }
                    break;
                case 1:
                    // 获取上一个界面用户点击的位置
                    getPlayingPosition();
                    // 检查当前的歌曲是否已经收藏
                    checkCollected(nowPlayingPosition);
                    if (screenWidth != 1024 || screenHeight != 552) {
                        // 屏幕适配
                        matchScreen();
                    }
                    // 设置适配器
                    setAdapter();
                    // 控件的监听事件
                    setListener();
                    // 设置播放模式
                    mService.setPlayModeType(SINGLE_CYCLE);
                    // 连服务上则开始播放,并返回当前播放的位置,便于给上一曲、下一曲根据这个位置判断
                    refreshView(mService.playMusic(nowPlayingPosition));
                    break;
            }
        }

    };

    /**
     * 设置控件的焦点
     */
    private void setFocus() {

        // 底部七个主要操作
        downVolume.setOnFocusChangeListener(mFocusChangeListener);
        upVolume.setOnFocusChangeListener(mFocusChangeListener);
        mLastMusic.setOnFocusChangeListener(mFocusChangeListener);
        mPlayOrPause.setOnFocusChangeListener(mFocusChangeListener);
        mGoBack.setOnFocusChangeListener(mFocusChangeListener);

        mNextMusic.setOnFocusChangeListener(mFocusChangeListener);
        mLyrics.setOnFocusChangeListener(mFocusChangeListener);
        mPlayMode.setOnFocusChangeListener(mFocusChangeListener);
        mCollect.setOnFocusChangeListener(mFocusChangeListener);

        // 侧边的音乐菜单
        mSmallMenu.setOnFocusChangeListener(mFocusChangeListener);
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

    /**
     * 广播接收器
     */
    private AndroidSystemReceiver androidSystemReceiver;

    public class AndroidSystemReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 网址:   http://www.android100.org/html/201406/11/24026.html
            Log.i("MYINTENT", "intent.getAction()=" + intent.getAction());
            // 拔卡
            if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                // 当前的播放模式的路径:pathType
                String path = intent.getData().toString().substring("file://".length());
                mPlayOrPause.setChecked(true);
                // 如果是当前模式的拔出操作,则停止
                if (pathType.equals(path)) {
                    // 暂停播放
                    mService.mediaPlayerStop();
                    // 结束当前的Activity
                    PlayingActivity.this.finish();
                } else {
                    // 当我在本地音乐的时候，拔卡也继续播放
                    mService.pausePlay(nowPlayingPosition);
                    mService.pausePlay(nowPlayingPosition);
                }
                showMyToast("外存卡拔出");
                return;
            }
            // 插卡
            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                String path = intent.getData().toString().substring("file://".length());
                mPlayOrPause.setChecked(true);
                showMyToast("外存卡插入");
                return;
            }
        }
    }

    private MsgReceiver msgReceiver;

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("ICOX_RECEIVER")) {
                int position = intent.getIntExtra("Position", 0);
                //更新UI
                setAlbumPictureAndInformation(position);

                mListViewAdapter.setSelectedPosition(position);
                mListViewAdapter.notifyDataSetInvalidated();
            }
            if (intent.getAction().equals("ICOX_TIME")) {
                int duration = intent.getIntExtra("Duration", 0);
                int current = intent.getIntExtra("Current", 0);
                int lrcindex = intent.getIntExtra("LrcIndex", 0);
                //转换下时间的格式[00:00]
                String durationText = MusicUtil.formatTime(duration);
                String currentText = MusicUtil.formatTime(current);

                // 打印 时间的长度
                Log.i("ICOX_TIME", currentText + "/" + durationText);
                // 获得歌曲的长度并设置成播放进度条的最大值
                seekBar.setMax(duration);
                // 获得歌曲现在播放位置并设置成播放进度条的值
                seekBar.setProgress(current);
                // 当前播放进度
                tv_curcentTime.setText(currentText);
                // 歌曲時間長度
                tv_allTime.setText(durationText);
                // 设置歌词播放到哪里的位置
                lrc_view.SetIndex(lrcindex);
                // 刷新歌词View
                lrc_view.invalidate();
            }
        }
    }

    //音乐集合
    public static List<Music> mPlayingMusicList = new ArrayList<Music>();

    /**
     * 设置扫描界面
     *
     * @param musicList 集合数据
     */
    public void setPlayingMusicList(List<Music> musicList) {

        mPlayingMusicList.clear();
        mPlayingMusicList.addAll(musicList);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        initWindow();
        setContentView(R.layout.activity_playing);

        // 注册HOME键广播
        registerHomeKeyReceiver(this);
        // 获取扫描界面的数据
        setPlayingMusicList(Connection.musicList);
        // 实例化控件
        initView();
        // 设置控件的焦点
        //        setFocus();
        // 动态注册广播接收器
        registerIcoxReceiver();
        // 获取屏幕的宽高
        getScreenWidthAndHeight();
        // activity启动但还没加载布局之前让service启动
        startMusicService();

    }

    private void matchScreen() {
        /**设置图片的缩放**/
        //专辑信息
        setViewZoom(showAlbumPicture, ScreenAdaptationUtil.albumPictureWidth, ScreenAdaptationUtil.albumPictureHeight);
        setViewZoom(album, ScreenAdaptationUtil.albumInformationPictureWidth, ScreenAdaptationUtil.albumInformationPictureHeight);
        setViewZoom(singer, ScreenAdaptationUtil.albumInformationPictureWidth, ScreenAdaptationUtil.albumInformationPictureHeight);
        setViewZoom(song, ScreenAdaptationUtil.albumInformationPictureWidth, ScreenAdaptationUtil.albumInformationPictureHeight);
        // 菜单
        //   setViewZoom(mSmallMenu, ScreenAdaptationUtil.menuWidth, ScreenAdaptationUtil.menuHeight);
    }

    /**
     * 注册广播
     */
    private void registerIcoxReceiver() {

        //服务自动下一曲的界面更新广播
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ICOX_RECEIVER");
        intentFilter.addAction("ICOX_TIME");
        registerReceiver(msgReceiver, intentFilter);

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
    protected void onDestroy() {
        super.onDestroy();
        //注销广播
        unregisterReceiver(msgReceiver);
        unregisterReceiver(androidSystemReceiver);
        unregisterHomeKeyReceiver(this);
    }

    /**
     * 设置适配器
     */
    private void setAdapter() {
        if (null == mListViewAdapter) {
            mListViewAdapter = new ListViewAdapter(PlayingActivity.this);
        }
        mListView.setAdapter(mListViewAdapter);
    }

    /**
     * 实例化各个控件
     */

    private void initView() {

        // 底部七个主要操作
        downVolume = (ImageView) findViewById(R.id.downVolume);
        upVolume = (ImageView) findViewById(R.id.upVolume);
        mLastMusic = (ImageView) findViewById(R.id.mLastMusic);
        mPlayOrPause = (CheckBox) findViewById(R.id.mPlayOrPause);
        mNextMusic = (ImageView) findViewById(R.id.mNextMusic);
        mLyrics = (CheckBox) findViewById(R.id.mLyrics);
        mPlayMode = (ImageView) findViewById(R.id.mPlayMode);
        mGoBack = (ImageView) findViewById(R.id.music_playing_goBack);
        mCollect = (CheckBox) findViewById(R.id.mCollect);

        // 侧边的音乐菜单
        mSmallMenu = (CheckBox) findViewById(R.id.mSmallMenu);

        // 菜单的显示与隐藏
        mLinearLayout = (LinearLayout) findViewById(R.id.musicMenu);
        // 专辑
        albumLayout = (RelativeLayout) findViewById(R.id.albumLayout);
        // 专辑，歌手，歌曲
        showAlbumPicture = (ImageView) findViewById(R.id.showAlbumPicture);
        // 自定义歌词View与歌曲名
        lrc_view = (LrcView) findViewById(R.id.LyricShow);

        // 当前的进度,总的进度,seekBar进度条
        tv_curcentTime = (TextView) findViewById(R.id.tv_curcentTime);
        tv_allTime = (TextView) findViewById(R.id.tv_allTime);
        seekBar = (SeekBar) findViewById(R.id.seekBar);

        album = (ImageView) findViewById(R.id.album);
        singer = (ImageView) findViewById(R.id.singer);
        song = (ImageView) findViewById(R.id.song);

        showAlbumText = (FloatableTextView) findViewById(R.id.showAlbumText);
        showSinger = (FloatableTextView) findViewById(R.id.showSinger);
        showSong = (FloatableTextView) findViewById(R.id.showSong);
        songTitle = (FloatableTextView) findViewById(R.id.songTitle);

        // 侧边的菜单栏
        mListView = (ListView) findViewById(R.id.listMenu);
        musicMenuTitle = (TextView) findViewById(R.id.musicMenuTitle);
    }

    /**
     * 检查是否收藏
     */
    private void checkCollected(int nowPlayingPosition) {

        Music inputDbMusic = mPlayingMusicList.get(nowPlayingPosition);
        String url = inputDbMusic.getUrl();
        // 数据库部分
        MusicDaoImpl dao = new MusicDaoImpl(PlayingActivity.this);
        List<Map<String, String>> list = dao.findByMusic("url=?", new String[]{url});

        if (list.size() != 0) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> map = list.get(i);
                String myUrl = map.get("url");
                if (url.equals(myUrl)) {
                    mCollect.setChecked(true);
                } else {
                    mCollect.setChecked(false);
                }
            }
        } else {
            // 数据库为空,所有歌曲均未被收藏
            mCollect.setChecked(false);
        }
    }

    /**
     * 设置专辑图片
     */
    private void setAlbumPictureAndInformation(int position) {

        //获取ContentResolver
        ContentResolver cr = PlayingActivity.this.getContentResolver();
        //获取当前的uri
        if (mPlayingMusicList.size() != 0) {
            Uri uri = mPlayingMusicList.get(position).getUri();
            try {
                InputStream is = cr.openInputStream(uri);
                if (null != is) {
                    Bitmap coverPhoto = BitmapFactory.decodeStream(is);
                    showAlbumPicture.setImageBitmap(coverPhoto);
                    showAlbumPicture.setPadding(8, 5, 8, 5);
                } else {
                    showAlbumPicture.setImageResource(R.drawable.default_album);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //            Paint paint = new Paint();
            //            paint.setTextSize(showSong.getTextSize());
            //            Log.i("test", "paint.getTextSize() = " + paint.getTextSize());
            //            Log.i("test", "paint.measureText() = " + paint.measureText(mPlayingMusicList.get(position).getAlbum()));
            //
            //            float widthZhuanJi = paint.measureText(mPlayingMusicList.get(position).getAlbum());
            //            float widthSinger = paint.measureText(mPlayingMusicList.get(position).getSinger());
            //            float widthSong = paint.measureText(mPlayingMusicList.get(position).getTitle());
            //            float widthTitle = paint.measureText(mPlayingMusicList.get(position).getTitle());

            float textSize = 425;

            showAlbumText.init(mPlayingMusicList.get(position).getAlbum(), 0.9f, textSize);
            showSinger.init(mPlayingMusicList.get(position).getSinger(), 0.9f, textSize);
            showSong.init(mPlayingMusicList.get(position).getTitle(), 0.9f, textSize);
            songTitle.init(mPlayingMusicList.get(position).getTitle(), 0.9f, textSize);

            showAlbumText.startFloating();
            showSinger.startFloating();
            showSong.startFloating();
            songTitle.startFloating();
        }
    }

    /**
     * 获取上一个界面用户点击的位置
     */
    private String pathType = null;

    private void getPlayingPosition() {
        Intent mIntent = getIntent();
        nowPlayingPosition = mIntent.getIntExtra("BEGIN", 0);
        pathType = mIntent.getStringExtra("PATHTYPE");
        handler.sendEmptyMessage(nowPlayingPosition);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            //音量控制,初始化定义
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            //音量控制,初始化定义
            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                    AudioManager.FX_FOCUS_NAVIGATION_UP);
            return true;
        }

        /**
         * 物理按键:上一曲,播放,下一曲
         */
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {

            if (comeFrommPlayOrPause) {
                comeFrommPlayOrPause = false;
                mPlayOrPause.setChecked(false);
                mService.mMediaPlayer.start();
            } else {
                comeFrommPlayOrPause = true;
                mPlayOrPause.setChecked(true);
                mService.mMediaPlayer.pause();
            }
            if (null != mService) {
                mService.pausePlay(nowPlayingPosition);
                addIcoxAnimator(mPlayOrPause);
                addIcoxAnimator(tv_curcentTime);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {

            menuClick = true;
            mPlayOrPause.setChecked(true);
            nowPlayingPosition = mService.nextMusic();
            mService.playMusic(nowPlayingPosition);
            refreshView(nowPlayingPosition);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {

            menuClick = true;
            mPlayOrPause.setChecked(true);
            nowPlayingPosition = mService.frontMusic();
            mService.playMusic(nowPlayingPosition);
            refreshView(nowPlayingPosition);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 服务连接状态,启动音乐服务
     */
    private void startMusicService() {
        Intent serviceIntent = new Intent(this, PlayingService.class);
        startService(serviceIntent);
        mHandler.sendEmptyMessage(0);
    }

    /**
     * ListView的适配器
     */
    public class ListViewAdapter extends BaseAdapter {

        private Context mContext;

        private ListViewAdapter(Context mContext) {
            this.mContext = mContext;
        }

        // 选中的位置
        public int selectedPosition = -1;

        public void setSelectedPosition(int position) {
            this.selectedPosition = position;
        }

        //获取集合的大小
        @Override
        public int getCount() {
            return mPlayingMusicList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPlayingMusicList.get(position).getTitle();
        }

        //获取集合的位置
        @Override
        public long getItemId(int position) {
            return position;
        }

        // 绘制视图
        @Override
        public View getView(final int position, View itemView, ViewGroup viewGroup) {

            ViewHolder holder = null;
            if (null == itemView) {
                itemView = LayoutInflater.from(mContext).inflate(R.layout.item_listview_menu, null);
                holder = new ViewHolder();
                // 文字内容设置
                holder.floatableTextView = (FloatableTextView) itemView.findViewById(R.id.floatable_text_view);
                // 设置holder
                itemView.setTag(holder);
            } else {
                // 获取holder
                holder = (ViewHolder) itemView.getTag();
            }
            holder.floatableTextView.init(mPlayingMusicList.get(position).getTitle(), 0.7f, mListView.getWidth() - 25);
            // 如果文本的内容>控件的宽度
            if (holder.floatableTextView.mTextLength > (mListView.getWidth() - 25) && mListView.getWidth() != 0) {
                // 开始滚动
                holder.floatableTextView.startFloating();
            } else {
                holder.floatableTextView.stopFloating();
            }

            //            // 背景的设置和处理
            //            if (selectedPosition == position) {
            //                itemView.setBackgroundResource(R.drawable.item_selected);
            //                handler.sendEmptyMessage(selectedPosition);
            //            } else {
            //                itemView.setBackgroundResource(0);
            //            }
            itemView.setFocusable(true);
            itemView.setBackgroundResource(R.drawable.sel_focus);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 替换listview OnItemClick
                    menuClick = false;
                    nowPlayingPosition = position;
                    if (null != mService) {
                        mPlayOrPause.setChecked(true);
                        // 发中选中的歌曲名，刷新UI
                        refreshView(mService.playMusic(nowPlayingPosition));
                    }
                    mLyrics.setChecked(false);
                }
            });
            return itemView;
        }
    }

    /**
     * ListView的 ViewHolder 优化
     */
    private static class ViewHolder {
        private FloatableTextView floatableTextView = null;
    }

    /**
     * 自定义Toast样式
     */
    private void showMyToast(String information) {
        View toastRoot = getLayoutInflater().inflate(R.layout.my_toast, null);
        Toast toast = new Toast(getApplicationContext());
        toast.setView(toastRoot);
        TextView tv = (TextView) toastRoot.findViewById(R.id.TextViewInfo);
        tv.setText(information);
        tv.setTextSize(24);
        toast.setGravity(Gravity.BOTTOM, 0, 100);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * 各种监听事件
     */

    private boolean menuClick = false;
    private static boolean comeFrommPlayOrPause = false;

    private void setListener() {

        // 音量减少
        downVolume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 音量控制,初始化定义
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        });

        // 音量增加
        upVolume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // 音量控制,初始化定义
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        });

        // 暂停or开始
        mPlayOrPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isPlaying) {

                comeFrommPlayOrPause = isPlaying;
                if (isPlaying) {
                    showMyToast("正在播放");
                } else {
                    showMyToast("已暂停");
                }
                if (null != mService) {
                    mService.pausePlay(nowPlayingPosition);
                    addIcoxAnimator(compoundButton);
                    addIcoxAnimator(tv_curcentTime);
                }
            }
        });

        // 下一首
        mNextMusic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                menuClick = false;
                mPlayOrPause.setChecked(true);
                nowPlayingPosition = mService.nextMusic();
                mService.playMusic(nowPlayingPosition);
                refreshView(nowPlayingPosition);
            }
        });

        // 上一首
        mLastMusic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                menuClick = false;
                mPlayOrPause.setChecked(true);
                nowPlayingPosition = mService.frontMusic();
                mService.playMusic(nowPlayingPosition);
                refreshView(nowPlayingPosition);
            }
        });

        // 小菜单按钮
        mSmallMenu.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                addIcoxAnimator(compoundButton);
                if (isChecked) {
                    // 菜单显示
                    mLinearLayout.setVisibility(View.VISIBLE);
                } else {
                    mLinearLayout.setVisibility(View.GONE);
                }
            }
        });

        // 进度条SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            private int lastProgress = 0;

            //滑动中
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // fromUser判断是用户改变的滑块的值
                if (fromUser) {
                    if (mService != null) {
                        mService.movePlay(progress);
                    }
                }

                View view = getWindow().getDecorView().findFocus();
                if (view != null && view.getId() == R.id.seekBar &&
                        Math.abs(seekBar.getProgress() - lastProgress) > 3000)
                    if (mService != null) {
                        mService.movePlay(progress);
                    }

                lastProgress = seekBar.getProgress();
            }

            // 放下去的那一刻,即滑动开始的那一刻
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            // 抬起来的那一刻，即滑动结束的那一刻
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //        // 歌曲菜单的选择
        //        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //
        //            @Override
        //            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //
        //                menuClick = false;
        //                nowPlayingPosition = position;
        //                if (null != mService) {
        //                    mPlayOrPause.setChecked(true);
        //                    // 发中选中的歌曲名，刷新UI
        //                    refreshView(mService.playMusic(nowPlayingPosition));
        //                }
        //                mLyrics.setChecked(false);
        //            }
        //        });

        // 歌词
        mLyrics.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    albumLayout.setVisibility(View.GONE);
                    lrc_view.setVisibility(View.VISIBLE);
                    songTitle.setVisibility(View.VISIBLE);
                } else {
                    albumLayout.setVisibility(View.VISIBLE);
                    lrc_view.setVisibility(View.GONE);
                    songTitle.setVisibility(View.GONE);
                }
            }
        });

        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(6, 6, 6, 6);
        // 播放模式:单曲循环、列表循环、随机播放
        mPlayMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                addIcoxAnimator(view);
                switch (mService.getPlayModeType()) {
                    case RANDOM_PLAY:
                        mService.setPlayModeType(ALL_CYCLE);
                        mPlayMode.setBackgroundResource(R.drawable.selector_random_play);
                        mPlayMode.setLayoutParams(params);
                        showMyToast("随机播放");
                        break;
                    case ALL_CYCLE:
                        mService.setPlayModeType(SINGLE_CYCLE);
                        mPlayMode.setBackgroundResource(R.drawable.selector_all_cycle);
                        showMyToast("全部循环");
                        break;
                    case SINGLE_CYCLE:
                        mService.setPlayModeType(RANDOM_PLAY);
                        mPlayMode.setLayoutParams(params);
                        mPlayMode.setBackgroundResource(R.drawable.selector_single_cycle);
                        showMyToast("单曲循环");
                        break;
                    default:
                        break;
                }
            }
        });

        // 收藏
        mCollect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                addIcoxAnimator(compoundButton);

                MusicDaoImpl dao = new MusicDaoImpl(PlayingActivity.this);
                ContentValues values = new ContentValues();
                Music inputDbMusic = mPlayingMusicList.get(nowPlayingPosition);
                String url = inputDbMusic.getUrl();
                String name = inputDbMusic.getName();

                values.put("url", url);
                values.put("name", name);
                if (isChecked) {
                    if (!menuClick) {
                        // TODO 收藏该首音乐
                        /********去除数据库重复的内容,在加入的时候操作即可**********/
                        List<Map<String, String>> list = dao.findByMusic("url=?", new String[]{url});
                        if (list.size() == 0) {

                            dao.addMusic(values);
                        } else {
                            // 遍历list所有元素
                            for (int i = 0; i < list.size(); i++) {
                                Map<String, String> map = list.get(i);
                                String myUrl = map.get("url");
                                if (!myUrl.equals(url)) {
                                    dao.addMusic(values);
                                }
                            }
                        }
                    }
                } else {
                    if (!menuClick) {
                        // TODO 取消收藏该首音乐
                        dao.deleteMusic("name=?", new String[]{name});
                    }
                }
            }
        });

        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * 增加动画
     */
    private void addIcoxAnimator(View view) {
        view.getId();
        ObjectAnimator a3 = null;
        ObjectAnimator a2 = null;
        if (view.getId() == R.id.mSmallMenu) {
            a3 = ObjectAnimator.ofFloat(view, "rotation", 0f, 0f);
            a2 = ObjectAnimator.ofFloat(view, "translationY", 0f, -50, 0f);
        } else {
            a3 = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
            a2 = ObjectAnimator.ofFloat(view, "translationY", 0f, 0, 0f);
        }
        AnimatorSet set = new AnimatorSet();
        set.setDuration(1000);
        set.play(a3).with(a2);
        // 设置插值器。
        set.setInterpolator(new BounceInterpolator());
        set.start();
    }

    /**
     * 发送数据给handler
     */
    private void refreshView(int position) {
        mListViewAdapter.setSelectedPosition(position);
        mListViewAdapter.notifyDataSetInvalidated();
        handler.sendEmptyMessage(position);
    }

    /************
     * 监听HOME键
     ************/

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
