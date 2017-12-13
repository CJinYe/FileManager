package com.icox.manager.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();

    private static final double KB = 1024.0;
    private static final double MB = 1048576.0;
    private static final double GB = 1073741824.0;

    /**
     * 转换文件大小
     *
     * @param size
     * @return
     */
    public static String generateFileSize(long size) {
        String fileSize;
        if (size < KB)
            fileSize = size + "B";
        else if (size < MB)
            fileSize = String.format("%.1f", size / KB) + "KB";
        else if (size < GB)
            fileSize = String.format("%.1f", size / MB) + "MB";
        else
            fileSize = String.format("%.1f", size / GB) + "GB";

        return fileSize;
    }

    /**
     * 对文件夹和文件排序
     *
     * @param files
     * @return File[] listFiles
     */
    public static File[] sort(File[] files) {

        if (files != null) {
            List<File> list = Arrays.asList(files);
            Collections.sort(list, new FileComparator());
            File[] array = list.toArray(new File[list.size()]);
            return array;
        }

        return null;
    }

    public static boolean isSuccess = false;

    /**
     * 根据路径做对应的复制操作
     *
     * @param fromPath 原始路径
     * @param toPath 目标路径
     * @return 成功与否
     * @throws IOException
     */

    public static boolean round = true;

    public static boolean copy(Context context, String fromPath, String toPath) throws IOException {

        // 1.将原始路径和目标路径转为File
        File fromFile = new File(fromPath);
        if (!fromFile.exists()) {
            isSuccess = false;
        }
        Log.i("copy", "复制路径:" + fromFile.getAbsolutePath());
        File toFile = new File(toPath);
        // 2.如果目标路径不存在，则创建该路径
        if (!toFile.exists()) {
            toFile.mkdir();
        }
        Log.i("copy", "粘贴路径:" + toFile.getAbsolutePath());
        // 有路径
        if (toFile.canRead()) {
            // 3.判断[原始路径]是一个目录还是一个文件
            // 递归遍历
            if (fromFile.isFile()) {// 若是文件,直接复制
                isSuccess = copyFile(fromFile.getAbsolutePath(), toPath + fromFile.getName());
                ScanUtil.scanFile(context, fromFile.getAbsolutePath());
                ScanUtil.scanFile(context, toPath + fromFile.getName());
                //                return isSuccess;
            } else if (fromFile.isDirectory()) {// 若是文件夹,对其遍历复制其里面的文件

                // 创建一个文件夹
                if (round) {
                    File newCreateFile = new File(toPath + "/" + fromFile.getName());
                    if (!newCreateFile.exists()) {
                        newCreateFile.mkdir();
                    }
                    toPath = newCreateFile.getAbsolutePath();
                    round = false;
                }

                File[] listFiles = fromFile.listFiles();
                for (File file : listFiles) {
                    // toPath = newCreateFilePath;
                    if (file.isDirectory()) {
                        // 4.如果是一个文件夹，得到该文件夹下的所有文件，需要将该文件夹的所有文件都复制，文件夹需要
                        // 递归遍历,因为是文件夹，所以最后都要加上"/"
                        copy(context, file.getAbsolutePath() + "/", toPath + "/" + file.getName() + "/");
                    } else {
                        // 5.是一个目录还是一个文件，如果是一个文件，直接复制
                        isSuccess = copyFile(file.getAbsolutePath(), toPath + "/" + file.getName());
                        ScanUtil.scanFile(context, file.getAbsolutePath());
                        ScanUtil.scanFile(context, toPath + "/" + file.getName());
                    }
                }
            }
            if (!isSuccess) {
                Log.d(TAG, "fail");
            }
        }
        return isSuccess;
    }

    /**
     * 复制文件
     *
     * @param fromPath 原始路径
     * @param toPath   目标路径
     * @throws IOException 异常
     */
    private static boolean copyFile(String fromPath, String toPath) {
        boolean isSuccess = true;

        InputStream is = null;
        OutputStream os = null;
        try {
            // 1.将源文件路径和目标文件路径转为File对象
            File fromFile = new File(fromPath);
            File toFile = new File(toPath);

            //            toFile.mkdirs();
            //            toFile.createNewFile();
            //            if (!toFile.canWrite()){
            //                return false;
            //            }
            // 2.得到一个输入流和输出流
            is = new FileInputStream(fromFile);
            os = new FileOutputStream(toFile);
            // 3.输入流读文件，输出流写文件，建立一个缓冲区，提高读写效率
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (FileNotFoundException e) {
            isSuccess = false;
            Log.e(TAG, "copyFile", e);
        } catch (IOException e) {
            isSuccess = false;
            Log.e(TAG, "copyFile", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                isSuccess = false;
                Log.e(TAG, "copyFile", e);
            }
        }
        return isSuccess;
    }

    /**
     * 根据路径做对应的删除操作
     *
     * @param currentPath
     * @return
     */
    public static boolean delete(Context context, String currentPath) {

        // 根据要删除的[当前路径]转成File
        File currentFile = new File(currentPath);
        if (currentFile.isFile()) { // 若是文件则直接删除
            isSuccess = deleteFile(currentFile.getAbsolutePath());
            ScanUtil.scanFile(context, currentFile.getAbsolutePath());
        } else if (currentFile.isDirectory()) { // 若是文件夹则递归遍历删除
            File[] listFiles = currentFile.listFiles();
            for (File file : listFiles) {
                if (file.isDirectory()) { // 若是文件夹,递归遍历删除
                    delete(context, file.getAbsolutePath() + "/");
                    file.delete();
                } else { // 若是文件则直接删除
                    isSuccess = deleteFile(file.getAbsolutePath());
                    ScanUtil.scanFile(context, file.getAbsolutePath());
                }
            }
            currentFile.delete();
        }

        notifySystemToScan(context, currentPath);
        return isSuccess;
    }

    /**
     * 通知电脑刷新SD卡
     *
     * @param context
     * @param filePath
     */
    public static void notifySystemToScan(Context context, String filePath) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(filePath);

        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        context.sendBroadcast(intent);
    }

    /**
     * 删除文件操作
     *
     * @param currentPath
     * @return
     */
    public static boolean deleteFile(String currentPath) {

        boolean isSuccess = true;
        File currentFile = new File(currentPath);
        if (currentFile.exists()) {
            currentFile.delete();
            // 删除成功, 返回
            return true;
        } else {
            isSuccess = false;
        }
        return isSuccess;
    }


    /**
     * 移动操作
     *
     * @param fromPath 原始路径
     * @param toPath   目标路径
     * @return 是否移动成功
     */
    public static boolean move(Context context, String fromPath, String toPath) {

        // 1.将原始路径和目标路径转为File
        File fromFile = new File(fromPath);
        if (!fromFile.exists()) {
            isSuccess = false;
        }
        Log.i("copy", "复制路径:" + fromFile.getAbsolutePath());

        File toFile = new File(toPath);
        // 2.如果目标路径不存在，则创建该路径
        if (!toFile.exists()) {
            toFile.mkdir();
        }
        Log.i("copy", "粘贴路径:" + toFile.getAbsolutePath());

        if (toFile.canRead()) {

            // 3.判断[原始路径]是一个目录还是一个文件
            // 递归遍历
            if (fromFile.isFile()) {// 若是文件,直接复制
                isSuccess = moveFile(fromFile.getAbsolutePath(), toPath + fromFile.getName());
                ScanUtil.scanFile(context, fromFile.getAbsolutePath());
                ScanUtil.scanFile(context, toPath + fromFile.getName());
                //                return isSuccess;
            } else if (fromFile.isDirectory()) {// 若是文件夹,对其遍历复制其里面的文件

                // 创建一个文件夹
                if (round) {
                    File newCreateFile = new File(toPath + "/" + fromFile.getName());
                    if (!newCreateFile.exists()) {
                        newCreateFile.mkdir();
                    }
                    toPath = newCreateFile.getAbsolutePath();
                    round = false;
                }

                File[] listFiles = fromFile.listFiles();

                for (File file : listFiles) {
                    // toPath = newCreateFilePath;
                    if (file.isDirectory()) {
                        // 4.如果是一个文件夹，得到该文件夹下的所有文件，需要将该文件夹的所有文件都复制，文件夹需要
                        // 递归遍历,因为是文件夹，所以最后都要加上"/"
                        move(context, file.getAbsolutePath() + "/", toPath + "/" + file.getName() + "/");
                    } else {
                        // 5.是一个目录还是一个文件，如果是一个文件，直接复制
                        isSuccess = moveFile(file.getAbsolutePath(), toPath + "/" + file.getName());
                        ScanUtil.scanFile(context, fromFile.getAbsolutePath());
                        ScanUtil.scanFile(context, toPath + "/" + file.getName() + "/");
                    }
                }
            }
            if (!isSuccess) {
                Log.d(TAG, "fail");
            }
        }
        return isSuccess;
    }

    /**
     * 对单个文件的移动操作
     *
     * @param fromPath 原始路径
     * @param toPath   目标路径
     * @return 是否移动成功
     */
    public static boolean moveFile(String fromPath, String toPath) {

        //        File file = new File(fromPath);
        //        file.renameTo(new File(toPath));
        //        return false;

        return copyFile(fromPath, toPath);
    }
}
