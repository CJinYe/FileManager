package com.icox.mediafilemanager;

import android.app.Application;

/**
 * Created by icox-liujian on 2015/9/23
 */
public class GlobalData extends Application {
    public static final String HOME_DIR = "ICOX";

    public static final String IAIWAI_FILE_DIR = "/mnt/media";      // 爱华内置文件路径

    @Override
    public void onCreate() {
        super.onCreate();
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(this);
    }
}
