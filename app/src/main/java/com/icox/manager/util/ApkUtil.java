package com.icox.manager.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

public class ApkUtil {

    public static Drawable getApkIcon3(Context context, String apkPath) {
        //获得包管理器
        PackageManager pm = context.getPackageManager();
        //获得指定路径的apk文件的相关信息
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            Drawable icon = appInfo.loadIcon(pm);
            return icon;
        }
        return null;
    }

    public static Drawable getUninstallApkIcon(Context context, String apkPath) {
        Resources res = getResource(context, apkPath);
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = info.applicationInfo;
        if (appInfo.icon != 0) {
            Drawable icon = res.getDrawable(appInfo.icon);
            //AssetManager must be closed to avoid ProcessKiller after unmounting usb disk.
            res.getAssets().close();
            return icon;
        }
        return null;
    }

    public static Resources getResource(Context context, String apkPath) {
        AssetManager assetManager = createAssetManager(apkPath);
        return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

    //利用反射调用AssetManager的addAssetPath()方法
    private static AssetManager createAssetManager(String apkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                    assetManager, apkPath);
            return assetManager;
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }

    public static Drawable getApkIcon2(Context context, String apkPath) {
        // 1.先获得包管理器
        PackageManager pm = context.getPackageManager();
        // 2.通过包管理器获得指定应用的包的相关信息
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            // 3.通过PackageInfo获得应用信息类ApplicationInfo
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.publicSourceDir = apkPath;
            // 4.通过ApplicationInfo获得该apk的图标
            Drawable icon = appInfo.loadIcon(pm);
            return icon;
        }
        return null;
    }

    public static Drawable test(Context context, String apkPath) {

        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);

        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            String appName = pm.getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName; // 得到安装包名称
            String version = info.versionName; // 得到版本信息
            Toast.makeText(context, "packageName:" + packageName + ";version:" + version + "name:" + appName, Toast.LENGTH_LONG).show();
            appInfo.publicSourceDir = apkPath;
            Drawable icon = pm.getApplicationIcon(appInfo);// 得到图标信息
            return icon;
        }
        return null;
    }

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
