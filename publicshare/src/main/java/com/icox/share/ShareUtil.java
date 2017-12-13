package com.icox.share;

import android.content.Context;
import android.media.ExifInterface;
import android.os.storage.StorageManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Liujian on 2017/8/19.
 */

public class ShareUtil {

    /**
     * 获取系统的路径划分的数组区间
     */
    public static String[] getVolumePaths(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        String[] paths = null;
        try {
            Method methodGetPaths = storageManager.getClass().getMethod("getVolumePaths");
            paths = (String[]) methodGetPaths.invoke(storageManager);
            if (paths != null) {
                if (paths.length == 2) {
                    if (getStoragePath(context, paths[1])) {
                        String[] resultPaths = new String[3];
                        resultPaths[0] = paths[0];
                        resultPaths[1] = "";
                        resultPaths[2] = paths[1];
                        return resultPaths;
                    }
                }
            }
        } catch (Exception e) {

        }
        return paths;
    }

    /**
     * 6.0获取外置sdcard和U盘路径，并区分
     *
     * @param mContext
     * @param keyword  SD = "内部存储"; EXT = "SD卡"; USB = "U盘"
     * @return
     */
    public static boolean getStoragePath(Context mContext, String keyword) {
        String targetpath = "";
        boolean isUpan = false;
        try {
            StorageManager mStorageManager = (StorageManager) mContext
                    .getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = null;
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");

            Method getPath = storageVolumeClazz.getMethod("getPath");

            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);

            Method getUserLabel = storageVolumeClazz.getMethod("getUserLabel");


            for (int i = 0; i < length; i++) {

                Object storageVolumeElement = Array.get(result, i);

                String userLabel = (String) getUserLabel.invoke(storageVolumeElement);

                String path = (String) getPath.invoke(storageVolumeElement);

                //                Toast.makeText(this, "path循环 = " + path + " \n userLabel = " + userLabel, Toast.LENGTH_LONG).show();

                if (path.equals(keyword) && (userLabel.contains("U") || userLabel.contains("盘"))) {
                    isUpan = true;
                    targetpath = path;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return isUpan;
    }


    public static boolean canPlayVide(String videoFilePath){
        String model= android.os.Build.MODEL;
        if (videoFilePath.endsWith(".jkv"))
            return false;
        if (model.equals("C10")){
            if (videoFilePath.endsWith(".rmvb") || videoFilePath.endsWith(".RMVB") || videoFilePath.endsWith(".mkv")){
                return false;
            }
        }
        else if (model.equals("C7")){
            if (videoFilePath.contains("/卡拉OK/")){
                return false;
            }
        }

        return true;
    }

    /**
     * 读取图片中相机方向
     * @param imgpath
     * @return
     */
    public static int getBitmapDigree(String imgpath){
        int digree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imgpath);
        } catch (IOException e) {
            e.printStackTrace();
            exif = null;
        }
        if (exif != null) {
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }
        }
        return digree;
    }

}
