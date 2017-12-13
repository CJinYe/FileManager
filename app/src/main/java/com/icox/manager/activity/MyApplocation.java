package com.icox.manager.activity;

import android.app.Application;

import com.icox.manager.util.CrashHandler;

/**
 * @author 陈锦业
 * @version $Rev$
 * @time 2017-10-16 16:48
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @updateDes ${TODO}
 */
public class MyApplocation extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(this);
    }
}
