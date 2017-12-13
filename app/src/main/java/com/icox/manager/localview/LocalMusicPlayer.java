package com.icox.manager.localview;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.icox.manager.R;

import java.io.IOException;

/**
 * Created by Administrator on 2015/12/31
 */
public class LocalMusicPlayer extends Activity {

    private ImageView mGoBack;

//    /**
//     * 系统物理按键的事件
//     */
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if (mMediaPlayer != null) {
//                mMediaPlayer.stop();
//                mMediaPlayer.release();
//                mMediaPlayer = null;
//            }
//            finish();
//        }
//
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            //音量控制,初始化定义
//            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
//                    AudioManager.FX_FOCUS_NAVIGATION_UP);
//        }
//
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            //音量控制,初始化定义
//            AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//            mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
//                    AudioManager.FX_FOCUS_NAVIGATION_UP);
//        }
//        return true;
//    }

    private CheckBox mPlayOrPause;
    private ImageView downVolume, upVolume;
    private MediaPlayer mMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        setContentView(R.layout.activity_musicplayer);
        registerHomeKeyReceiver(this);
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        // 实例化控件
        initView();
        //设置监听事件
        setListener();
        getInformation();
        playMusic(path);
    }

    public void playMusic(String playPath) {

        if (null != playPath) {
            /** MediaPlayer相关**/
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(playPath);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null) {
            //            mMediaPlayer.pause();
            mMediaPlayer.stop();
            //            mMediaPlayer.release();
            //            mMediaPlayer = null;

        }

    }

    @Override
    protected void onDestroy() {
        Log.i("test", "onDestroy 1111111111111111111");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        super.onDestroy();

        unregisterHomeKeyReceiver(this);
    }

    /*********************************************************/

    private String path;

    private void getInformation() {
        Intent i = getIntent();
        path = i.getStringExtra("path");
        Log.i("mylog", "获取到播放歌曲为:" + path);
    }

    public void pausePlay() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.getAudioSessionId();
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    /**
     * 设置监听事件
     */
    private void setListener() {

        mPlayOrPause.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                pausePlay();
            }
        });

        downVolume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //音量控制,初始化定义
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        });

        upVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //音量控制,初始化定义
                AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                        AudioManager.FX_FOCUS_NAVIGATION_UP);
            }
        });

        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                finish();
            }
        });
    }

    /**
     * 实例化控件
     */
    private void initView() {
        mPlayOrPause = (CheckBox) findViewById(R.id.mPlayOrPause);
        downVolume = (ImageView) findViewById(R.id.downVolume);
        upVolume = (ImageView) findViewById(R.id.upVolume);
        mGoBack = (ImageView) findViewById(R.id.exit);

        mPlayOrPause.setOnFocusChangeListener(mFocusChangeListener);
        downVolume.setOnFocusChangeListener(mFocusChangeListener);
        upVolume.setOnFocusChangeListener(mFocusChangeListener);
        mGoBack.setOnFocusChangeListener(mFocusChangeListener);
    }

    /***********
     * 放在Activity类里的的底部
     *************/

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
            focus = com.icox.administrator.icoxmp3player.R.anim.enlarge;
        } else {
            focus = com.icox.administrator.icoxmp3player.R.anim.decrease;
        }

        //如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
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
