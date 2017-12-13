package com.icox.administrator.icoxmp3player.db.music_dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.icox.administrator.icoxmp3player.db.sqlite_open_helper.DBOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by icox-xiuchou on 2016/1/13
 */
public class MusicDaoImpl implements MusicDao {

    private DBOpenHelper helper = null;
    // 表名
    private String table = "music";

    public MusicDaoImpl(Context context) {
        helper = new DBOpenHelper(context);
    }

    /**
     * 收藏
     *
     * @param values ContentValues实例
     */
    @Override
    public void addMusic(ContentValues values) {
        SQLiteDatabase db = null;
        db = helper.open();
        db.insert(table, null, values);
        db.close();
    }

    /**
     * 取消收藏
     *
     * @param whereClause 字段名
     * @param whereArgs   字段值
     */
    @Override
    public void deleteMusic(String whereClause, String[] whereArgs) {
        SQLiteDatabase db = null;
        db = helper.open();
        db.delete(table, whereClause, whereArgs);
        db.close();
    }

    /**
     * 刷新收藏
     *
     * @param values      ContentValues实例
     * @param whereClause 字段名
     * @param whereArgs   字段值
     */
    @Override
    public void updateMusic(ContentValues values, String whereClause, String[] whereArgs) {
        SQLiteDatabase db = null;
        db = helper.open();
        db.update(table, values, whereClause, whereArgs);
        db.close();
    }

    /**
     * 根据特定的字段查找相应的内容
     *
     * @param selection     字段名
     * @param selectionArgs 字段值
     * @return 所有符合检索条件的信息集合
     */
    @Override
    public List<Map<String, String>> findByMusic(String selection, String[] selectionArgs) {

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = null;
        db = helper.open();
        if (selection != null) {
            Cursor c = db.query(false, table, null, selection, selectionArgs, null, null, null, null);
            int columns = c.getColumnCount();
            while (c.moveToNext()) {
                Map<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < columns; i++) {
                    String columnName = c.getColumnName(i);
                    String columnValue = c.getString(c.getColumnIndex(columnName));
                    map.put(columnName, columnValue);
                }
                list.add(map);
            }
            c.close();
            db.close();
        }
        return list;
    }

    /**
     * 显示音乐所有信息
     * sql语句根据字段查询所有音乐信息
     */
    @Override
    public List<Map<String, String>> listMusicMap() {

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        SQLiteDatabase db = null;

        db = helper.open();
        // TODO 要显示的内容
        String sql = "select name from music";
        Cursor c = db.rawQuery(sql, null);
        int columns = c.getColumnCount();
        while (c.moveToNext()) {
            for (int i = 0; i < columns; i++) {
                Map<String, String> map = new HashMap<String, String>();
                String columnName = c.getColumnName(i);
                String columnValue = c.getString(c.getColumnIndex(columnName));
                map.put(columnName, columnValue);
                list.add(map);
            }
        }
        c.close();
        db.close();

        return list;
    }
}
