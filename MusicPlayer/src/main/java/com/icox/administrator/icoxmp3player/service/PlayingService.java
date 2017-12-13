package com.icox.administrator.icoxmp3player.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.icox.administrator.icoxmp3player.R;
import com.icox.administrator.icoxmp3player.bean.Music;
import com.icox.administrator.icoxmp3player.connection.Connection;
import com.icox.administrator.icoxmp3player.myview.lrcview.LrcProcess;
import com.icox.administrator.icoxmp3player.ui.activity.PlayingActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by XiuChou on 2015/11/24
 */
public class PlayingService extends Service implements Runnable {

    private AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                Log.i("focusChange", "focusChange:" + focusChange);
            } else {
                // TODO 其他音量焦点,统一①暂停②停止③释放④mMediaPlayer=null;
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                }
            }
        }
    };

    //构造方法
    public PlayingService() {
        if (lrcList == null) {
            lrcList = new ArrayList<LrcProcess.LrcContent>();
        }
        mHandler = new Handler();
    }

    private LrcProcess mLrcProcess = null;
    private List<LrcProcess.LrcContent> lrcList = null;
    private Handler mHandler = null;
    public MediaPlayer mMediaPlayer = null;

    /**
     * 次线程实时刷新,每秒变化的需要刷新,例如歌曲进度，歌词进度
     */

    @Override
    public void run() {
        //可以在屏幕关了停止这些线程的刷新
        if (null != mMediaPlayer) {
            // 获得歌曲的长度并设置成播放进度条的最大值
            int duration = getDuration();
            // 获得歌曲现在播放位置并设置成播放进度条的值
            int current = getCurrent();
            //设置歌词播放到哪里的位置
            int lrcindex = LrcIndex();

            //发送Action为ICOX.RECEIVER的广播
            intentTime.putExtra("Duration", duration);
            intentTime.putExtra("Current", current);
            intentTime.putExtra("LrcIndex", lrcindex);
            sendBroadcast(intentTime);
            //延迟100毫秒再次发送
            mHandler.postDelayed(PlayingService.this, 1000);
        }


    }

    // 初始化歌词检索值
    private int index = 0;
    // 初始化歌曲播放时间的变量
    private int CurrentTime = 0;
    // 初始化歌曲总时间的变量
    private int CountTime = 0;

    /**
     * 歌词同步处理类
     */
    public int LrcIndex() {

        if (mMediaPlayer.isPlaying()) {
            // 获得歌曲播放在哪的时间
            CurrentTime = mMediaPlayer.getCurrentPosition();
            // 获得歌曲总时间长度
            CountTime = mMediaPlayer.getDuration();
        }
        if (CurrentTime < CountTime) {
            for (int i = 0; i < lrcList.size(); i++) {
                if (i < lrcList.size() - 1) {
                    if (CurrentTime < lrcList.get(i).getLrc_time() && i == 0) {
                        index = i;
                    }
                    if (CurrentTime > lrcList.get(i).getLrc_time() && CurrentTime < lrcList.get(i + 1).getLrc_time()) {
                        index = i;
                    }
                }
                if (i == lrcList.size() - 1 && CurrentTime > lrcList.get(i).getLrc_time()) {
                    index = i;
                }
            }
        }
        return index;
    }

    // 声音焦点
    private AudioManager mAudioManager;

    public static PlayingService playingService = null;

    //音乐集合
    public List<Music> mMusicList = new ArrayList<Music>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        setMusicList(Connection.musicList);
        playingService = this;
        registerMusicServiceReceiver();
        return super.onStartCommand(intent, flags, startId);
    }

    public void setMusicList(List<Music> musicList) {
        mMusicList.clear();
        mMusicList.addAll(musicList);
    }

    /**
     * Service里的 Receiver
     */
    private void registerMusicServiceReceiver() {
        // 服务自动下一曲的界面更新广播
        msr = new MusicServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("STOP_RECEIVER");
        registerReceiver(msr, intentFilter);
    }

    private MusicServiceReceiver msr;

    public class MusicServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            // 别的有音量的应用启动则停止播放
            if (intent.getAction().equals("STOP_RECEIVER")) {
                Log.i("Stop", "接受了");
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer = null;
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            //销毁操作前先暂停播放
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            lrcList = null;
        }

        // 释放
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
            mAudioFocusListener = null;
            mMediaPlayer = null;
        }
        // 注销广播
        unregisterReceiver(msr);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 得到当前播放进度
     */
    public int getCurrent() {
        if (mMediaPlayer.isPlaying()) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return nowPlayingProgress;
        }
    }

    /**
     * 歌曲是否真在播放
     */
    public boolean isPlay() {
        return mMediaPlayer.isPlaying();
    }

    //让播放器停止
    public void mediaPlayerStop() {
        mMediaPlayer.pause();
    }

    /**
     * 跳到输入的进度
     */
    public void movePlay(int progress) {
        mMediaPlayer.seekTo(progress);
        nowPlayingProgress = progress;
    }

    /**
     * 歌词同步
     */
    public void synchronizLyric(int position) {
        mLrcProcess = new LrcProcess();

        //根据position找到其路径读取歌词文件
        if (mMusicList.size() != 0) {
            mLrcProcess.readLRC(mMusicList.get(position).getLyricPath());
            // 传回处理后的歌词文件
            lrcList = mLrcProcess.getLrcContent();

            PlayingActivity.lrc_view.setSentenceEntities(lrcList);
            // 切换带动画显示歌词
            PlayingActivity.lrc_view.setAnimation(AnimationUtils.loadAnimation(PlayingService.this, R.anim.alpha_z));
            // 启动线程
            mHandler.post(PlayingService.this);
        }
    }

    // 完成的位置
    private int completedPosition = 0;

    /**
     * 播放歌曲，根据歌曲存储路径
     */
    private Intent intent = new Intent("ICOX_RECEIVER");
    private Intent intentTime = new Intent("ICOX_TIME");

    public int playMusic(int position) {

        nowPlayingPosition = position;
        if (mMusicList.size() != 0) {
            String playPath = mMusicList.get(position).getUrl();
            if (null != playPath) {
                try {

                    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                            AudioManager.AUDIOFOCUS_GAIN);
                    /** MediaPlayer相关**/
                    if (mMediaPlayer != null) {
                        // 重置MediaPlayer
                        mMediaPlayer.reset();
                        // 设置要播放的文件的路径
                        mMediaPlayer.setDataSource(playPath);
                        // 准备播放
                        mMediaPlayer.prepare();
                        // 开始播放
                        mMediaPlayer.start();
                        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            // 设置完成的
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                // 自动下一首
                                completedPosition = nextMusic();
                                //播放的歌曲
                                playMusic(completedPosition);
                                synchronizLyric(completedPosition);

                                // 发送Action为ICOX.RECEIVER的广播
                                intent.putExtra("Position", completedPosition);
                                sendBroadcast(intent);
                            }
                        });
                    }
                } catch (IOException e) {

                }
                synchronizLyric(position);
            }
        }
        return position;
    }

    // 歌曲播放进度
    private int nowPlayingProgress = 0;
    // 随机播放的位置
    private int randomPlayPosition = 0;
    // 当前播放的位置
    private int nowPlayingPosition = 0;
    // 下一曲播放的位置
    private int nextWillPlayPosition = 0;
    // 上一曲播放的位置
    private int frontPlayedPosition = 0;

    /**
     * 暂停或开始播放歌曲
     */

    public int pausePlay(int position) {
        nowPlayingPosition = position;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mMediaPlayer.getAudioSessionId();
            nowPlayingProgress = mMediaPlayer.getCurrentPosition();
        } else {
            mMediaPlayer.start();
        }
        return nowPlayingPosition;
    }

    /**
     * 下一首,根据不同模式下进行对应的操作
     */
    public int nextMusic() {
        //根据播放模式进行选择
        switch (getPlayModeType()) {
            case 1:
                //当单曲循环在有效范围内
                if (0 <= nowPlayingPosition && nowPlayingPosition <= mMusicList.size()) {
                    nextWillPlayPosition = nowPlayingPosition;
                }
                break;
            case 2:
                //当随机的position与当前的不同即为有效
                do {
                    //随机生成一个position
                    randomPlayPosition = new Random().nextInt(mMusicList.size());
                    //直到这个随机数与当前不同，下一曲的位置才有效
                }
                while (nowPlayingPosition == randomPlayPosition);
                nextWillPlayPosition = randomPlayPosition;
                break;
            case 3:
                //列表循环的最后一曲的处理
                if (++nowPlayingPosition < mMusicList.size()) {
                    nextWillPlayPosition = nowPlayingPosition;
                } else {
                    nowPlayingPosition = 0;
                    nextWillPlayPosition = nowPlayingPosition;
                }
                break;
        }
        return nextWillPlayPosition;
    }

    /**
     * 上一首,根据不同模式下进行对应的操作
     */
    public int frontMusic() {
        //根据播放模式进行选择
        switch (getPlayModeType()) {
            case 1:
                //当单曲循环在有效范围内
                if (0 <= nowPlayingPosition && nowPlayingPosition <= mMusicList.size()) {
                    frontPlayedPosition = nowPlayingPosition;
                }
                break;
            case 2:
                //随机生成一个position
                randomPlayPosition = new Random().nextInt(mMusicList.size());
                //当随机的position与当前的不同即为有效
                if (randomPlayPosition != nowPlayingPosition) {
                    //下一首的position才有效
                    frontPlayedPosition = randomPlayPosition;
                }
                break;
            case 3:
                //列表循环的第一首的处理
                if (--nowPlayingPosition < 0) {
                    nowPlayingPosition = mMusicList.size() - 1;
                    frontPlayedPosition = nowPlayingPosition;
                } else {
                    frontPlayedPosition = nowPlayingPosition;
                }
                break;
        }
        return frontPlayedPosition;
    }

    /**
     * 播放模式的成员变量
     */
    private int playModeType;

    public int getPlayModeType() {
        return playModeType;
    }

    public void setPlayModeType(int playModeType) {
        this.playModeType = playModeType;
    }
}
