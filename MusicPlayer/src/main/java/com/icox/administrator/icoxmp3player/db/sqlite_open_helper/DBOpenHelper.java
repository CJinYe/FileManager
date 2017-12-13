package com.icox.administrator.icoxmp3player.db.sqlite_open_helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by icox-xiuchou on 2016/1/13
 */
public class DBOpenHelper extends SQLiteOpenHelper {

    // 数据库名称
    private final static String name = "icoxMusic.db";
    // 数据库版本号
    private final static int version = 1;
    // 运行上下文
    private Context context;
    // DBOpenHelper实例
    private DBOpenHelper dbOpenHelper;
    // SQLiteDatabase实例
    private SQLiteDatabase db;

    public DBOpenHelper(Context context) {
        super(context, name, null, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        // 创建数据表,music(id,name,address);
//        String sql = "CREATE TABLE music(id integer primary key autoincrement ,name varchar(64),address varchar(64))";

//        String sql = "create table music(isCollected boolean,music_id long ,uri varchar(64),title varchar(64),singer varchar(64),album varchar(64),url varchar(64),name varchar(64),size long,time long,lyricPath varchar(64))";

        String sql = "create table music(url varchar(64),name varchar(64))";
        sqLiteDatabase.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        // 插入新的数据表字段
        String sql = "alter table music add String varchar(10)";
        sqLiteDatabase.execSQL(sql);
    }

    /**
     * 打开数据库
     *
     * @return SQLiteDatabase实例
     */
    public SQLiteDatabase open() {
        try {
            dbOpenHelper = new DBOpenHelper(context);
            db = dbOpenHelper.getWritableDatabase();
        } catch (Exception e) {
            db = dbOpenHelper.getReadableDatabase();
        }
        return db;
    }

    /**
     * 关闭数据库
     */
    public void close() {
        if (db != null) {
            db.close();
            db = null;
        }
    }
}
