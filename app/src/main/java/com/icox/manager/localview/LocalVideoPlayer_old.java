/*
package com.icox.manager.localview;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.icox.manager.R;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class LocalVideoPlayer extends Activity {

    */
/****************************************************//*

    private final String TAG = "test";
    private EditText et_path;
    private SurfaceView sv;
    private Button btn_play, btn_replay, btn_stop, btn_back;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private int currentPosition = 0;
    private boolean isPlaying;

    //  private GlobalData mGlobalData;

    private TextView time_current, time_total;
    private ImageButton btn_pause, btn_volume1, btn_volume2, btn_pre, btn_nex;

    private RelativeLayout bottomlayout;
    private boolean isShow;

    private String mVideoPath;
    private String mVideoType;
    private String mVideoName;
    private int mPosition = 0;
    private long mVideoTime;

    private AudioManager mAudioManager;
    */
/**
     * 最大声音
     *//*

    private int mMaxVolume;

//    private int mArrCount = 0;
//    private String[] mArrPath;
//    private String[] mArrTitle;

    private boolean mbCreateFirst = true;

    private View mDecorView;
    private TextView text_name;
    */
/**
     * 接收的值
     *//*

    private ArrayList<String> mFilePathArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        registerHomeKeyReceiver(this);
//        mGlobalData = (GlobalData) getAppli

        */
/* 设置屏幕常亮 *//*
*/
/* flag：标记 ； *//*

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mDecorView = getWindow().getDecorView();
        hideVirtualkey(mDecorView);

        setContentView(R.layout.activity_videoplayer);

        Intent intent = getIntent();
//        mTypeId = intent.getIntExtra("tid", 0);
        intent.getExtras();

        if (mbCreateFirst) {
            mVideoPath = intent.getStringExtra("path");
            mVideoType = intent.getStringExtra("type");
            mPosition = intent.getIntExtra("position", 0);
//            getLocalData();
            mbCreateFirst = false;

            mVideoType = mVideoPath.substring(mVideoPath.lastIndexOf(".") + 1, mVideoPath.length());
        }
        */
/***************************************************//*

        mFilePathArray = getIntent().getStringArrayListExtra("ArrayDirPath");
        if (mFilePathArray == null) {
            mFilePathArray = new ArrayList<String>();
            mFilePathArray.add(getIntent().getStringExtra("path"));
        }
        for (int i = 0; i < mFilePathArray.size(); i++) {
            if (mFilePathArray.get(i).equals(mVideoPath)) {
                mPosition = i;
                Log.i("IcoxVideo", "当前播放的位置对于集合中的位置:" + mPosition + "/" + mFilePathArray.size());


                */
/**
                 * 给视频加个标题
                 *//*

                addVideoTitle(mFilePathArray.get(i));
            }
        }


        */
/***************************************************//*


        */
/**
         * 不需要指定都可以
         *//*

        // mVideoPath = "/mnt/sdcard/DCIM/Camera/VID_20151229_210037.mp4";
//        Log.i(TAG, "mVideoType=" + mVideoType + ", mVideoPath=" + mVideoPath);

//        if (mVideoPath == null || mVideoPath.length() == 0) {
//            Uri uri = intent.getData();
//            mVideoPath = uri.getPath();
//            mVideoType = "jtb";
//            Log.i(TAG, "getData()" + ", mVideoPath=" + mVideoPath);
//        }

//        if (mArrCount == 0){
//            return;
//        }

//        mVideoPath = mArrPath[mPosition];
//        mVideoType = mVideoPath.substring(mVideoPath.lastIndexOf('.') + 1);
//        mVideoName = mArrTitle[mPosition];

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

//        //TODO 加个焦点
//        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
//                AudioManager.AUDIOFOCUS_GAIN);

        */
/**************************************************//*


        btn_volume1 = (ImageButton) findViewById(R.id.btn_volume1);
        btn_volume2 = (ImageButton) findViewById(R.id.btn_volume2);
        btn_volume1.setOnClickListener(click);
        btn_volume2.setOnClickListener(click);

//        mFocusChangeListener
//        btn_volume1.setBackgroundColor(Color.BLUE);
//        btn_volume2.setBackgroundColor(Color.RED);

        btn_pre = (ImageButton) findViewById(R.id.btn_pre);
        btn_nex = (ImageButton) findViewById(R.id.btn_nex);
        btn_pre.setOnClickListener(click);
        btn_nex.setOnClickListener(click);

        text_name = (TextView) findViewById(R.id.text_name);
        text_name.setText(addVideoTitle(mFilePathArray.get(mPosition)));

        bottomlayout = (RelativeLayout) findViewById(R.id.bottomlayout);
//        bottomlayout.setOnKeyListener(new ListViewKeyEvent());
        isShow = false;

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        sv = (SurfaceView) findViewById(R.id.sv);

        time_current = (TextView) findViewById(R.id.time_current);
        time_total = (TextView) findViewById(R.id.time_total);

        btn_play = (Button) findViewById(R.id.btn_play);
        btn_pause = (ImageButton) findViewById(R.id.btn_pause);

        btn_replay = (Button) findViewById(R.id.btn_replay);
        btn_stop = (Button) findViewById(R.id.btn_stop);
        btn_back = (Button) findViewById(R.id.btn_back);

        btn_play.setOnClickListener(click);
        btn_pause.setOnClickListener(click);
        btn_replay.setOnClickListener(click);
        btn_stop.setOnClickListener(click);
        btn_back.setOnClickListener(click);

        // 为SurfaceHolder添加回调
        sv.getHolder().addCallback(callback);

        // 4.0版本之下需要设置的属性
        // 设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到界面
        // sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 为进度条添加进度更改事件
        seekBar.setOnSeekBarChangeListener(change);

        // init
        mKeyPosition = 2;

        // 设置焦点
//        setFocus();
//        btn_volume1.setOnFocusChangeListener(mFocusChangeListener);
//        btn_volume2.setOnFocusChangeListener(mFocusChangeListener);
    }

    */
/**
     * 根据路径，加个标题
     *
     * @param path
     * @return
     *//*

    private String addVideoTitle(String path) {

        String realTitle = null;
        File tempFile = new File(path.trim());
        String fileName = tempFile.getName();
        if ((fileName != null) && (fileName.length() > 0)) {
            int dot = fileName.lastIndexOf('.');
            if ((dot > -1) && (dot < (fileName.length()))) {
                realTitle = fileName.substring(0, dot);
            }
        }
        return realTitle;
    }

//    private void setFocus() {
//        btn_volume1.setOnFocusChangeListener(mFocusChangeListener);
//        btn_volume2.setOnFocusChangeListener(mFocusChangeListener);
//        btn_pause.setOnFocusChangeListener(mFocusChangeListener);
//        btn_pre.setOnFocusChangeListener(mFocusChangeListener);
//        btn_nex.setOnFocusChangeListener(mFocusChangeListener);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        // 释放
//        if (mAudioManager != null) {
//            mAudioManager.abandonAudioFocus(mAudioFocusListener);
//            mAudioFocusListener = null;
//            mediaPlayer = null;
//        }
        unregisterHomeKeyReceiver(this);
    }

    private void preVideo() {

        mPosition--;
        if (mPosition < 0) {
            mPosition = 0;
            Toast.makeText(this, "已经是第一个视频了~", Toast.LENGTH_SHORT).show();
        }

        text_name.setText(addVideoTitle(mFilePathArray.get(mPosition)));
        stop();
        play(0, mPosition);
        //onCreate(null);
    }

    private void nexVideo(int flag) {
//        if (flag == 0 && mediaPlayer.getCurrentPosition() >= mVideoTime){
//            Toast.makeText(LocalVideoPlayer.this, "本视频播放结束，即将播放下一个~", Toast.LENGTH_SHORT).show();
//        }
        mPosition++;
        if (mPosition >= mFilePathArray.size()) {
            mPosition = mFilePathArray.size() - 1;
            Toast.makeText(this, "已经是最后一个视频了~", Toast.LENGTH_SHORT).show();
        }
        text_name.setText(addVideoTitle(mFilePathArray.get(mPosition)));
//        if (mPosition >= mArrCount) {
//            if (flag == 0){
//                finish();
//                return;
//            }
//            mPosition = mArrCount - 1;
//            Toast.makeText(this, "已经是最后一个视频了~", Toast.LENGTH_SHORT).show();
//        }else {
////            Log.i("test", "mediaPlayer.getCurrentPosition() = " + mediaPlayer.getCurrentPosition());
////            Log.i("test", "mVideoTime = " + mVideoTime);
////            Log.i("test", "currentPosition = " + currentPosition);
//            if (flag == 0 && mediaPlayer.getCurrentPosition() >= (mVideoTime - 1000)){
//                Toast.makeText(LocalVideoPlayer.this, "本视频播放结束，即将播放下一个~", Toast.LENGTH_SHORT).show();
//            }
//        }
        stop();
        play(0, mPosition);
        //onCreate(null);
    }

//    private void getLocalData(){
//        // 获取数据库的本地视频数据
//        ReadableDatabase readableDatabase;
//        MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
//        readableDatabase = new ReadableDatabase(helper.getReadableDatabase());
//
//        String dir = mGlobalData.getmDownloadPath();
//        int index = dir.indexOf(PlayerConfig.VIDEO_DOWNLOAD_DIR);
//        dir = dir.substring(index);
//        Cursor cursor = readableDatabase.myvideo_getDataByTtitleType(dir, readableDatabase.TYPE_DOWNLOAD);
//        mArrCount = cursor.getCount();
//        if (mArrCount == 0){
//            return;
//        }
//
//        mArrPath = new String[mArrCount];
//        mArrTitle = new String[mArrCount];
//        int k = 0;
//        while (cursor.moveToNext()) {
//            mArrTitle[k] = cursor.getString(cursor.getColumnIndex("title_id"));
//            mArrPath[k] = cursor.getString(cursor.getColumnIndex("complete"));
//            k++;
//        }
//    }

    private android.os.Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    bottomlayout.setVisibility(View.GONE);
                    isShow = false;
                    hideVirtualkey(mDecorView);
                    hideVirtualkey(bottomlayout);
                    break;
            }
        }
    };

    // 隐藏虚拟按键
    public void hideVirtualkey(View decorView) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

//        decorView.setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private Callback callback = new Callback() {
        // SurfaceHolder被修改的时候回调
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
//            Log.i(TAG, "SurfaceHolder 被销毁");
            // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                mediaPlayer.stop();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
//            Log.i(TAG, "SurfaceHolder 被创建");
            if (currentPosition > 0) {
                // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
                play(currentPosition, mPosition);
                currentPosition = 0;
            } else {
                // 从头开始播放
                currentPosition = 0;
                play(currentPosition, mPosition);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            Log.i(TAG, "SurfaceHolder 大小被改变");
        }
    };

    private OnSeekBarChangeListener change = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 当进度条停止修改的时候触发
            // 取得当前进度条的刻度
            int progress = seekBar.getProgress();
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // 设置当前播放的位置
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            //302360
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            time_current.setText(generateTime(progress));
        }
    };

    */
/**
     * 转换时间显示
     *
     * @param time 毫秒
     * @return
     *//*

    public static String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes,
                seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private View.OnClickListener click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.btn_start:
                    play(0, mPosition);
                    break;
                case R.id.btn_play:
                    play(0, mPosition);
                    break;
                case R.id.btn_pause:
                    pause();
                    break;
                case R.id.btn_replay:
                    replay();
                    break;
                case R.id.btn_stop:
                    stop();
                    break;
                case R.id.btn_back:
                    finish();
                    break;
                case R.id.btn_volume1:
                    changeVolume(false);
                    break;
                case R.id.btn_volume2:
                    changeVolume(true);
                    break;
                case R.id.btn_pre:
                    preVideo();
                    break;
                case R.id.btn_nex:
                    nexVideo(1);
                    break;
                default:
                    break;
            }
        }
    };

    */
/*
     * 停止播放
     *//*

    protected void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            btn_play.setEnabled(true);
            isPlaying = false;
        }
    }

    public final static String ICOXVIDEO = "MVIDEO506ICOXEDU";

    */
/**
     * 开始播放
     *
     * @param msec             播放初始位置
     * @param inFolderPosition 文件夹的的第几首歌
     *//*

    protected void play(final int msec, int inFolderPosition) {
        // 获取视频文件地址
//		String path = et_path.getText().toString().trim();
//      String path = "/mnt/sdcard/cache0.jtb";
//      String path = "/mnt/sdcard/cctv1-001.mp4";
//        String path = mVideoPath;
        String path = mFilePathArray.get(inFolderPosition);
        Log.i("PLAYPATH", "PLAYPATH:" + path);
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
//			mediaPlayer.setDataSource(file.getAbsolutePath());
            if (mVideoType != null && mVideoType.equalsIgnoreCase("jtb")) {
                byte[] tempBuffer = ICOXVIDEO.getBytes();
                FileInputStream inputStream = new FileInputStream(file);
                mediaPlayer.setDataSource(inputStream.getFD(), tempBuffer.length, file.length() - tempBuffer.length);
                inputStream.close();
            } else {
                mediaPlayer.setDataSource(file.getAbsolutePath());
            }

//            FileInputStream inputStream = new FileInputStream(file);
//            byte[] buffer = new byte[1024];
//            byte [] tempBuffer = "abcdefg".getBytes();
//            int bytesRead = tempBuffer.length;
//            bytesRead = inputStream.read(buffer, 0, bytesRead);
//            mediaPlayer.setDataSource(inputStream.getFD());
//            Log.i("test", "file.length() = " + file.length());
//            Log.i("test", "inputStream.available() = " + inputStream.available());
//            mediaPlayer.setDataSource(inputStream.getFD(), 7, file.length() - 7);
//            mediaPlayer.setDataSource(inputStream.getFD());
//            inputStream.close();

            // 设置显示视频的SurfaceHolder
            mediaPlayer.setDisplay(sv.getHolder());
//            Log.i(TAG, "0开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
//                    Log.i(TAG, "1装载完成");
                    mediaPlayer.start();
                    // 按照初始位置播放
                    mediaPlayer.seekTo(msec);
                    // 设置进度条的最大进度为视频流的最大播放时长
                    mVideoTime = mediaPlayer.getDuration();
                    time_total.setText(generateTime(mVideoTime));
                    seekBar.setMax(mediaPlayer.getDuration());
                    // 开始线程，更新进度条的刻度
                    new Thread() {

                        @Override
                        public void run() {
                            try {
                                isPlaying = true;
                                while (isPlaying) {
                                    int current = mediaPlayer.getCurrentPosition();
                                    seekBar.setProgress(current);

                                    sleep(500);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    btn_play.setEnabled(false);
                }
            });
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调
                    btn_play.setEnabled(true);
                    nexVideo(0);
//                    finish();
                }
            });

            mediaPlayer.setOnErrorListener(new OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
                    play(0, mPosition);
                    isPlaying = false;
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    */
/**
     * 重新开始播放
     *//*

    protected void replay() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
            Toast.makeText(this, "重新播放", Toast.LENGTH_SHORT).show();
//            btn_pause.setText("暂停");
            return;
        }
        isPlaying = false;
        play(0, mPosition);
    }

    */
/**
     * 暂停或继续
     *//*

    protected void pause() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
//            btn_pause.setBackgroundResource(R.drawable.player_mediacontroller_pause);
            btn_pause.setImageResource(R.drawable.player_mediacontroller_pause);
//            btn_pause.setBackgroundColor(0);
//            btn_pause.getBackground().setAlpha(0);
            return;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
//            btn_pause.setBackgroundResource(R.drawable.player_mediacontroller_play);
            btn_pause.setImageResource(R.drawable.player_mediacontroller_play);
//            btn_pause.setBackgroundColor(0);
//            btn_pause.getBackground().setAlpha(0);
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (isShow && bottomlayout != null) {
//                    bottomlayout.setVisibility(View.GONE);
//                    isShow = false;
                    handler.sendEmptyMessage(1);
                } else if (bottomlayout != null) {
                    bottomlayout.setVisibility(View.VISIBLE);
                    isShow = true;

                    handler.removeMessages(1);
                    handler.sendEmptyMessageDelayed(1, 6000);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
        }

        return super.onTouchEvent(event);
    }

    public void changeVolume(boolean add) {
        int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (add) {
            volume++;
        } else {
            volume--;
        }

        if (volume > mMaxVolume)
            volume = mMaxVolume;
        else if (volume < 0)
            volume = 0;

        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
    }

//    @Override
//    public boolean dispatchKeyEvent(KeyEvent event) {
//        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
//            onKeyDownEvent(event.getKeyCode(), event);
//        }
//        return super.dispatchKeyEvent(event);
//    }

    */
/*键按下事件*//*

    private int mKeyPosition = 2;
    private boolean mbKeyEvent = false;

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mbKeyEvent) {
            return super.onKeyDown(keyCode, event);
//            return false;
        }
        mbKeyEvent = true;

//        DisplayToast("keyCode = " + keyCode);
        if (isShow) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_ENTER:
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    keyPositionEnterEvent(mKeyPosition);
                    mbKeyEvent = false;
                    return true;
//                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if ((mKeyPosition - 1) >= 0) {
                        keyPositionEvent(mKeyPosition, 0, 0);
                        mKeyPosition = mKeyPosition - 1;
                        keyPositionEvent(mKeyPosition, Color.GREEN, 150);
                    }
                    mbKeyEvent = false;
                    return true;
//                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if ((mKeyPosition + 1) < 5) {
                        keyPositionEvent(mKeyPosition, 0, 0);
                        mKeyPosition = mKeyPosition + 1;
                        keyPositionEvent(mKeyPosition, Color.GREEN, 150);
                    }
                    mbKeyEvent = false;
                    return true;
//                    break;
                default:
                    break;
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                pause();
                break;
            case KeyEvent.KEYCODE_MENU:
                if (isShow) {
                    isShow = false;
                    bottomlayout.setVisibility(View.GONE);
                } else {
                    isShow = true;
                    bottomlayout.setVisibility(View.VISIBLE);
                }
//                DisplayToast("bottomlayout.getVisibility() = " + bottomlayout.getVisibility());
                break;

            case KeyEvent.KEYCODE_ENTER:

            case KeyEvent.KEYCODE_DPAD_CENTER:
                pause();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                changeVolume(true);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                changeVolume(false);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                nexVideo(1);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                preVideo();
                break;
            default:
                mbKeyEvent = false;
                return super.onKeyDown(keyCode, event);
//                break;
        }

        mbKeyEvent = false;
        return true;
    }

    private void keyPositionEnterEvent(int keyPosition) {
        switch (keyPosition) {
            case 0:
                changeVolume(false);
                break;
            case 1:
                preVideo();
                break;
            case 2:
                pause();
                break;
            case 3:
                nexVideo(1);
                break;
            case 4:
                changeVolume(true);
                break;
        }
    }

    private void DisplayToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void keyPositionEvent(int keyPosition, int color, int alpha) {

        int tempFocusId = R.id.btn_volume1;

        switch (keyPosition) {
            case 0:
                tempFocusId = R.id.btn_volume1;
                break;
            case 1:
                tempFocusId = R.id.btn_pre;
                break;
            case 2:
                tempFocusId = R.id.btn_pause;
                break;
            case 3:
                tempFocusId = R.id.btn_nex;
                break;
            case 4:
                tempFocusId = R.id.btn_volume2;
                break;
        }

        ImageButton imageButton = (ImageButton) findViewById(tempFocusId);
        imageButton.setBackgroundColor(color);
        imageButton.getBackground().setAlpha(alpha); // 设置填充透明度 范围：0-255
    }


    */
/**
     * 提供选中放大的效果
     *//*

    public View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            viewAnimation(v, hasFocus);
        }
    };

    */
/**
     * 增加动画
     *//*

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
            focus = com.icox.mediafilemanager.R.anim.enlarge;
            addIcoxAnimator(v);
        } else {
            focus = com.icox.mediafilemanager.R.anim.decrease;
        }

        //如果有焦点就放大，没有焦点就缩小
        Animation mAnimation = AnimationUtils.loadAnimation(getApplication(), focus);
        mAnimation.setBackgroundColor(Color.TRANSPARENT);
        mAnimation.setFillAfter(hasFocus);
        v.startAnimation(mAnimation);
        mAnimation.start();
        v.bringToFront();
    }


    */
/***********
     * 监听HOME键
     *************//*


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
}
*/
