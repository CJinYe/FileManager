package com.icox.administrator.icoxmp3player.bean;

import android.net.Uri;

/**
 * Created by XiuChou on 2015/11/24
 */
public class Music {

    //专辑的Uri
    private Uri uri;
    //标题
    private String title;
    //歌手名
    private String singer;
    //专辑名
    private String album;
    //歌曲路径
    private String url;
    //歌曲文件名
    private String name;
    //音乐id号
    private long music_id;
    //音乐大小
    private long size;
    //音乐时间
    private long time;
    //歌词路径
    private String lyricPath;
//    //是否被收藏
//    private boolean isCollected;

//    public boolean getIsCollected() {
//        return isCollected;
//    }
//
//    public void setIsCollected(boolean isCollected) {
//        this.isCollected = isCollected;
//    }

    public String getLyricPath() {
        return lyricPath;
    }

    public void setLyricPath(String lyricPaths) {
        this.lyricPath = lyricPaths;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getMusicId() {
        return music_id;
    }

    public void setMusicId(long music_id) {
        this.music_id = music_id;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
