package com.icox.administrator.icoxmp3player.db.music_dao;

import android.content.ContentValues;

import java.util.List;
import java.util.Map;

/**
 * Created by icox-xiuchou on 2016/1/13
 */
public interface MusicDao {

    // 添加收藏
    void addMusic(ContentValues values);

    // 删除收藏
    void deleteMusic(String whereClause, String[] whereArgs);

    // 更新收藏
    void updateMusic(ContentValues values, String whereClause, String[] whereArgs);

    // 根据某一条件查找
    List<Map<String, String>> findByMusic(String selection, String[] selectionArgs);
    // 查询所有
    List<Map<String, String>> listMusicMap();

}
