package com.icox.mediafilemanager.baby;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.Iterator;
import java.util.List;

/**
 * Created by icox-developer on 16-10-11.
 */
public class Utils {

    /**
     * 检测程序是否已安装
     *
     * @param context
     * @param appPackageName: 需要检测的程序包名[ String ]
     * @return flag: 已安装为true，否则false[ boolean ]
     */
    public static boolean hasAppInstalled(Context context, String appPackageName) {
        boolean flag = false;
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
        Iterator<PackageInfo> iter = apps.iterator();
        while (iter.hasNext()) {
            PackageInfo packageinfo = iter.next();
            String packageName = packageinfo.packageName;
            if (packageName.equals(appPackageName)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

}
