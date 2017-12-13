package com.icox.manager.localview;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.icox.manager.R;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

//import static com.icox.manager.R.id.btn_back;
//import static com.icox.manager.R.id.btn_play;
//import static com.icox.manager.R.id.btn_replay;
//import static com.icox.manager.R.id.btn_stop;

public class LocalVideoPlayer extends Activity {
    private final String TAG = "test";
    private EditText et_path;
    private SurfaceView sv;
    //    private Button btn_play, btn_replay, btn_stop, btn_back;
    private MediaPlayer mediaPlayer = null;
    private SeekBar seekBar;
    private int currentPosition = 0;
    private boolean isPlaying;

    //    private GlobalData mGlobalData;
    private final static int DEFAULT_SHOW_TIME = 15000;

    private TextView time_current, time_total;
    private ImageButton btn_pause, btn_volume1, btn_volume2;
    private RelativeLayout bottomlayout;
    private boolean isShow;

    private String mVideoPath;
    private String mVideoType;
    private String mVideoName;
    private int mPosition = 0;
    private long mVideoTime;
    private String mLocalDir;

    private AudioManager mAudioManager;
    /**
     * 最大声音
     */
    private int mMaxVolume;

    private static int mArrCount = 0;
    private List<String> mArrPath;
    private List<String> mArrTitle;

    private boolean mbCreateFirst = true;

    private View mDecorView;

    private boolean mbNextVideo;

    private Context mContext;

    public final static String ICOX_VIDEO = "MVIDEO506ICOXEDU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWindow();
        mContext = this;
        //        mGlobalData = (GlobalData) getApplicationContext();
        /* 设置屏幕常亮 *//* flag：标记 ； */

        setContentView(R.layout.activity_videoplayer);

        Intent intent = getIntent();

        mbNextVideo = false;
        currentPosition = 0;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        mediaPlayer = new MediaPlayer();

        if (mbCreateFirst) {
            mVideoPath = intent.getStringExtra("path");
            mVideoType = intent.getStringExtra("type");
            mPosition = intent.getIntExtra("position", 0);
            //            mLocalDir = intent.getStringExtra("video_dir");

            mArrPath = new ArrayList<String>();
            mArrTitle = new ArrayList<String>();

            String videoDir = mVideoPath.substring(0, mVideoPath.lastIndexOf("/"));
            File file = new File(videoDir);
            getLocalVideoFiles(file);

            Comparator comp = new SortComparator();
            Collections.sort(mArrPath, comp);
            //            mArrPath = getIntent().getStringArrayListExtra("ArrayDirPath");
            for (int i = 0; i < mArrPath.size(); i++) {
                if (mArrPath.get(i).equals(mVideoPath)) {
                    mPosition = i;
                    break;
                    //                    Log.i("IcoxVideo", "当前播放的位置对于集合中的位置:" + mPosition + "/" + mFilePathArray.size());
                }
            }

            //            getLocalData();
            mArrCount = mArrPath.size();

            mbCreateFirst = false;
        }

        if (mPosition >= mArrPath.size()) {
            return;
        }
        mVideoPath = mArrPath.get(mPosition);
        mVideoType = mVideoPath.substring(mVideoPath.lastIndexOf('.') + 1);

        //        mVideoName = mArrTitle.get(mPosition);
        mVideoName = mVideoPath.substring(mVideoPath.lastIndexOf('/') + 1, mVideoPath.lastIndexOf('.'));

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        focusAudioManager(mAudioManager);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        btn_volume1 = (ImageButton) findViewById(R.id.btn_volume1);
        btn_volume2 = (ImageButton) findViewById(R.id.btn_volume2);
        btn_volume1.setOnClickListener(click);
        btn_volume2.setOnClickListener(click);

        ImageButton butBack = (ImageButton) findViewById(R.id.btn_back_r);
        butBack.setOnClickListener(click);

        ImageButton butPre = (ImageButton) findViewById(R.id.btn_pre);
        ImageButton butNext = (ImageButton) findViewById(R.id.btn_nex);
        butPre.setOnClickListener(click);
        butNext.setOnClickListener(click);

        TextView textView = (TextView) findViewById(R.id.text_name);
        textView.setText(mVideoName);

        bottomlayout = (RelativeLayout) findViewById(R.id.bottomlayout);
        isShow = true;
        handler.sendEmptyMessageDelayed(1, DEFAULT_SHOW_TIME);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        sv = (SurfaceView) findViewById(R.id.sv);

        time_current = (TextView) findViewById(R.id.time_current);
        time_total = (TextView) findViewById(R.id.time_total);


        btn_pause = (ImageButton) findViewById(R.id.btn_pause);
        btn_pause.setOnClickListener(click);

        //        btn_play = (Button) findViewById(btn_play);
        //        btn_replay = (Button) findViewById(btn_replay);
        //        btn_stop = (Button) findViewById(btn_stop);
        //        btn_back = (Button) findViewById(btn_back);
        //
        //        btn_play.setOnClickListener(click);
        //        btn_replay.setOnClickListener(click);
        //        btn_stop.setOnClickListener(click);
        //        btn_back.setOnClickListener(click);

        btn_volume1.setOnFocusChangeListener(mFocusChangeListener);
        btn_volume2.setOnFocusChangeListener(mFocusChangeListener);
        btn_pause.setOnFocusChangeListener(mFocusChangeListener);
        butPre.setOnFocusChangeListener(mFocusChangeListener);
        butNext.setOnFocusChangeListener(mFocusChangeListener);
        butBack.setOnFocusChangeListener(mFocusChangeListener);


        // 为SurfaceHolder添加回调
        sv.getHolder().addCallback(callback);

        // 4.0版本之下需要设置的属性
        // 设置Surface不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到界面
        // sv.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // 为进度条添加进度更改事件
        seekBar.setOnSeekBarChangeListener(change);

        // init
        mKeyPosition = 2;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(afChangeListener);
        }
    }

    /**
     * 本地视频排序
     *
     * @param
     */
    public class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            String a = (String) lhs;
            String b = (String) rhs;

            //            return (b.getDownloadSaveName().compareTo(a.getDownloadSaveName()));
            return (a.compareTo(b));
        }
    }

    private void preVideo() {
        mPosition--;
        if (mPosition < 0) {
            mPosition = 0;
            Toast.makeText(this, "已经是第一个视频了~", Toast.LENGTH_SHORT).show();
        }
        stop();
        onCreate(null);
    }

    private void nexVideo(int flag) {
        if (mbNextVideo) {
            return;
        }
        mbNextVideo = true;

        mPosition++;
        if (mPosition >= mArrCount) {
            if (flag == 0) {
                finish();
                return;
            }
            mPosition = mArrCount - 1;
            Toast.makeText(this, "已经是最后一个视频了~", Toast.LENGTH_SHORT).show();
        } else {
            if (flag == 0 && mediaPlayer.getCurrentPosition() >= (mVideoTime - 1000) && mediaPlayer.getCurrentPosition() < mVideoTime) {
                Toast.makeText(mContext, "本视频播放结束，即将播放下一个~", Toast.LENGTH_SHORT).show();
            }
        }
        stop();

        onCreate(null);
    }

    /**
     * 获取本地视频文件信息
     *
     * @param
     */
    private void getLocalVideoFiles(File file) {

        file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String name = file.getName();
                int i = name.lastIndexOf('.');
                if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".mp4")
                            || name.equalsIgnoreCase(".jtb")
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
                        //                            || name.equalsIgnoreCase(".rm")
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
                        mArrTitle.add(file.getName());
                        mArrPath.add(file.getAbsolutePath());
                        return true;
                    }
                } else if (file.isDirectory()) {
                    //                    getLocalVideoFiles(file);
                }
                return false;
            }
        });
    }

    private void getLocalData() {
        //        List<VideoInfo> mLocalListData = ListDataPagerView.mLocalListData;
        //        mArrPath = new String[mArrCount];
        //        mArrTitle = new String[mArrCount];
        //        for (int i = 0; i < mLocalListData.size(); i++){
        //            mArrTitle[i] = mLocalListData.get(i).getDownloadSaveName();
        //            mArrPath[i] = mLocalListData.get(i).getDownloadSavePath();
        //        }

        /*String[] localPaths = ServiceUtil.getVolumePaths(mContext);
        for (int i = 0; i < localPaths.length; i++){
            File file = new File(localPaths[i]);
            if (file.exists() && file.canRead()){
                getLocalVideoFiles(file);
            }
        }*/

        /*// 获取数据库的本地视频数据
        ReadableDatabase readableDatabase;
        MySQLiteOpenHelper helper = MySQLiteOpenHelper.getInstance(this);
        readableDatabase = new ReadableDatabase(helper.getReadableDatabase());

        String dir = mGlobalData.getmDownloadPath();
        int index = dir.indexOf(PlayerConfig.VIDEO_DOWNLOAD_DIR);
        dir = dir.substring(index);
        Cursor cursor = readableDatabase.myvideo_getDataByTtitleType(dir, readableDatabase.TYPE_DOWNLOAD);
        mArrCount = cursor.getCount();
        if (mArrCount == 0){
            return;
        }

        mArrPath = new String[mArrCount];
        mArrTitle = new String[mArrCount];
        int k = 0;
        while (cursor.moveToNext()) {
            mArrTitle[k] = cursor.getString(cursor.getColumnIndex("title_id"));
            mArrPath[k] = cursor.getString(cursor.getColumnIndex("complete"));
            k++;
        }*/
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    bottomlayout.setVisibility(View.GONE);
                    isShow = false;
                    initWindow();
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
                play(currentPosition);
                currentPosition = 0;
            } else {
                currentPosition = 0;
                play(currentPosition);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //            Log.i(TAG, "SurfaceHolder 大小被改变");
        }

    };

    private OnSeekBarChangeListener change = new OnSeekBarChangeListener() {

        private int lastProgress = 0;

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

            View view = getWindow().getDecorView().findFocus();
            if (view != null && view.getId() == R.id.seekBar &&
                    Math.abs(seekBar.getProgress() - lastProgress) > 1000)
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }

            lastProgress = seekBar.getProgress();
        }
    };

    /**
     * 转换时间显示
     *
     * @param time 毫秒
     * @return
     */
    private String generateTime(long time) {
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
                //                case btn_play:
                //                    play(0);
                //                    break;
                //                case btn_replay:
                //                    replay();
                //                    break;
                //                case btn_stop:
                //                    stop();
                //                    break;
                case R.id.btn_start:
                    play(0);
                    break;
                case R.id.btn_pause:
                    pause();
                    break;

                case R.id.btn_back_r:
                    //                case btn_back:
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


    /*
     * 停止播放
     */
    protected void stop() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            //            btn_play.setEnabled(true);
            isPlaying = false;
        }
    }

    /**
     * 开始播放
     *
     * @param msec 播放初始位置
     */
    protected void play(final int msec) {
        Log.i("test", "play");
        // 获取视频文件地址
        String path = mVideoPath;
        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(this, "视频文件路径错误", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
            if (mVideoType != null && mVideoType.equalsIgnoreCase("jtb")) {
                byte[] tempBuffer = ICOX_VIDEO.getBytes();
                FileInputStream inputStream = new FileInputStream(file);
                mediaPlayer.setDataSource(inputStream.getFD(), tempBuffer.length, file.length() - tempBuffer.length);
                inputStream.close();
            } else if (file.getAbsolutePath().endsWith(".cye")) {
                FileInputStream inputStream = new FileInputStream(file);
                mediaPlayer.setDataSource(inputStream.getFD(), 39057, file.length());
            } else {
                mediaPlayer.setDataSource(file.getAbsolutePath());
            }

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
                                    if (mediaPlayer != null) {
                                        int current = mediaPlayer
                                                .getCurrentPosition();
                                        if (seekBar.isFocusable())
                                            seekBar.setProgress(current);
                                    }
                                    sleep(500);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    //                    btn_play.setEnabled(false);
                }
            });
            mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    // 在播放完毕被回调
                    //                    btn_play.setEnabled(true);


                    nexVideo(0);
                    //                    finish();
                }
            });

            mediaPlayer.setOnErrorListener(new OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
                    //                    play(0);
                    isPlaying = false;

                    if (what == 1) {
                        if (extra == -1010) {
                            try {
                                Intent videoIntent = new Intent();
                                videoIntent.setComponent(new ComponentName("com.icox.onlinevideoplayer", "cn.icoxedu.bvideoplayer.BVideoPlayerActivity"));
                                videoIntent.setData(Uri.parse(mVideoPath));
                                mContext.startActivity(videoIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            finish();
                        }
                    }

                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 重新开始播放
     */
    protected void replay() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
            Toast.makeText(this, "重新播放", Toast.LENGTH_SHORT).show();
            //            btn_pause.setText("暂停");
            return;
        }
        isPlaying = false;
        play(0);
    }

    /**
     * 暂停或继续
     */
    protected void pause() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btn_pause.setImageResource(R.drawable.selector_video_pause);
            return;
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btn_pause.setImageResource(R.drawable.selector_video_play);
        }
    }

    private void processBootomLayout() {
        if (isShow && bottomlayout != null) {
            handler.sendEmptyMessage(1);
        } else if (bottomlayout != null) {
            bottomlayout.setVisibility(View.VISIBLE);
            isShow = true;

            handler.removeMessages(1);
            handler.sendEmptyMessageDelayed(1, DEFAULT_SHOW_TIME);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                processBootomLayout();
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

    /*键按下事件*/
    private int mKeyPosition = 2;
    private boolean mbKeyEvent = false;
    //    public boolean onKeyDown(int keyCode,KeyEvent event){
    //        if (mbKeyEvent){
    //            return super.onKeyDown(keyCode, event);
    //        }
    //        mbKeyEvent = true;
    //
    //        if (isShow) {
    //            switch(keyCode){
    //                case KeyEvent.KEYCODE_ENTER:
    //                case KeyEvent.KEYCODE_DPAD_CENTER:
    //                    keyPositionEnterEvent(mKeyPosition);
    //                    mbKeyEvent = false;
    //                    return true;
    //                case KeyEvent.KEYCODE_DPAD_LEFT:
    //                    if ((mKeyPosition - 1) >= 0){
    //                        keyPositionEvent(mKeyPosition, 0, 0);
    //                        mKeyPosition = mKeyPosition - 1;
    //                        keyPositionEvent(mKeyPosition, Color.GREEN, 150);
    //                    }
    //                    mbKeyEvent = false;
    //                    return true;
    //                case KeyEvent.KEYCODE_DPAD_RIGHT:
    //                    if ((mKeyPosition + 1) < 5){
    //                        keyPositionEvent(mKeyPosition, 0, 0);
    //                        mKeyPosition = mKeyPosition + 1;
    //                        keyPositionEvent(mKeyPosition, Color.GREEN, 150);
    //                    }
    //                    mbKeyEvent = false;
    //                    return true;
    //                default:
    //                    break;
    //            }
    //        }
    //
    //        switch(keyCode){
    //            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
    //                pause();
    //                break;
    //            case KeyEvent.KEYCODE_MENU:
    //                if (isShow) {
    //                    isShow = false;
    //                    bottomlayout.setVisibility(View.GONE);
    //                } else {
    //                    isShow = true;
    //                    bottomlayout.setVisibility(View.VISIBLE);
    //                }
    //                break;
    //
    //            case KeyEvent.KEYCODE_ENTER:
    //            case KeyEvent.KEYCODE_DPAD_CENTER:
    //                pause();
    //                break;
    //            case KeyEvent.KEYCODE_DPAD_UP:
    //                changeVolume(true);
    //                break;
    //            case KeyEvent.KEYCODE_DPAD_DOWN:
    //                changeVolume(false);
    //                break;
    //            case KeyEvent.KEYCODE_DPAD_RIGHT:
    //            case KeyEvent.KEYCODE_MEDIA_NEXT:
    //                nexVideo(1);
    //                break;
    //            case KeyEvent.KEYCODE_DPAD_LEFT:
    //            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
    //                preVideo();
    //                break;
    //            default:
    //                mbKeyEvent = false;
    //                return super.onKeyDown(keyCode, event);
    //        }
    //
    //        mbKeyEvent = false;
    //        return true;
    //    }
    //
    //    private void keyPositionEnterEvent(int keyPosition){
    //        switch (keyPosition){
    //            case 0:
    //                changeVolume(false);
    //                break;
    //            case 1:
    //                preVideo();
    //                break;
    //            case 2:
    //                pause();
    //                break;
    //            case 3:
    //                nexVideo(1);
    //                break;
    //            case 4:
    //                changeVolume(true);
    //                break;
    //        }
    //    }
    //
    //    private void DisplayToast(String str) {
    //        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    //    }
    //
    //    private void keyPositionEvent(int keyPosition, int color, int alpha){
    //        int tempFocusId = R.id.btn_volume1;
    //
    //        switch (keyPosition){
    //            case 0:
    //                tempFocusId = R.id.btn_volume1;
    //                break;
    //            case 1:
    //                tempFocusId = R.id.btn_pre;
    //                break;
    //            case 2:
    //                tempFocusId = R.id.btn_pause;
    //                break;
    //            case 3:
    //                tempFocusId = R.id.btn_nex;
    //                break;
    //            case 4:
    //                tempFocusId = R.id.btn_volume2;
    //                break;
    //        }
    //
    //        ImageButton imageButton = (ImageButton) findViewById(tempFocusId);
    //        imageButton.setBackgroundColor(color);
    //        imageButton.getBackground().setAlpha(alpha); // 设置填充透明度 范围：0-255
    //    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            View view = getWindow().getDecorView().findFocus();
            if (view != null && (view.getId() != R.id.seekBar
                    || view.getId() != R.id.btn_volume1
                    || view.getId() != R.id.btn_pre
                    || view.getId() != R.id.btn_pause
                    || view.getId() != R.id.btn_nex
                    || view.getId() != R.id.btn_volume2
                    || view.getId() != R.id.btn_back_r)) {
                processBootomLayout();
            } else if (view == null) {
                processBootomLayout();
            }
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            processBootomLayout();
        }

        if ((keyCode == KeyEvent.KEYCODE_HOME ||
                keyCode == KeyEvent.KEYCODE_DPAD_UP ||
                keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
                keyCode == KeyEvent.KEYCODE_DPAD_LEFT ||
                keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                && bottomlayout.getVisibility() == View.GONE) {
            processBootomLayout();
        }

        return super.onKeyDown(keyCode, event);
    }

    // AudioManager获取媒体焦点
    private void focusAudioManager(AudioManager audioManager) {
        int result = audioManager.requestAudioFocus(afChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        //        AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        //        Toast.makeText(PlayerActivity.this, "获取音频焦点：" + result + ", 成功是：" + AudioManager.AUDIOFOCUS_REQUEST_GRANTED, Toast.LENGTH_SHORT).show();
    }

    private AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int i) {
            //            Toast.makeText(PlayerActivity.this, "音频焦点改变：" + i, Toast.LENGTH_SHORT).show();
        }
    };

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

}
