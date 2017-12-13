package com.icox.manager.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.icox.manager.R;
import com.icox.manager.localview.LocalImageShower;
import com.icox.manager.localview.LocalMusicPlayer;
import com.icox.manager.localview.LocalVideoPlayer;
import com.icox.share.ShareUtil;

import java.io.File;

public class OpenFiles {

    /**
     * 打开sdcard上的文件
     *
     * @param context 上下文
     * @param file    要打开的文件
     */
    private static Intent stopIntent = new Intent("STOP_RECEIVER");

    public static void open(Context context, File file) {
        if (file != null && file.isFile()) {
            String fileName = file.toString();
            Intent intent;
            if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingImage))) {
                // 传当前的图片路径
                Intent icoxImageIntent = new Intent(context, LocalImageShower.class);
                icoxImageIntent.putExtra("path", file.getAbsolutePath());
                context.startActivity(icoxImageIntent);

            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingWebText))) {
                OpenFiles.getHtmlFileIntent(file, context);
            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingPackage))) {
                OpenFiles.getApkFileIntent(file, context);

            } else if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingAudio))) {

                // 传当前的音乐路径
                Intent icoxMusicIntent = new Intent(context, LocalMusicPlayer.class);
                icoxMusicIntent.putExtra("path", file.getAbsolutePath());
                context.startActivity(icoxMusicIntent);

                // TODO 发送广播,让音乐停止
                stopIntent.putExtra("Stop", true);
                Log.i("Stop", "点击了");
                context.sendBroadcast(stopIntent);

            } else if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingVideo))) {

                try {
                    String filePath = file.getAbsolutePath();
                    if (!ShareUtil.canPlayVide(filePath)) {
                        try {
                            Intent videoIntent = new Intent();
                            videoIntent.setComponent(new ComponentName("com.icox.onlinevideoplayer", "com.icox.player.common.CommonPlayerActivity"));
                            videoIntent.setData(Uri.parse(filePath));
                            context.startActivity(videoIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (filePath.endsWith("mp4") || filePath.endsWith("3gp") || filePath.endsWith("FLV")) {
                        // 传当前的视频路径
                        Intent icoxVideoIntent = new Intent(context, LocalVideoPlayer.class);
                        icoxVideoIntent.putExtra("path", file.getAbsolutePath());
                        context.startActivity(icoxVideoIntent);
                    } else {
                        Intent videoIntent = new Intent();
                        videoIntent.setComponent(new ComponentName("com.icox.onlinevideoplayer", "cn.icoxedu.bvideoplayer.activity.LocalVideoPlayer"));
                        videoIntent.setData(Uri.parse(filePath));
                        context.startActivity(videoIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // 传当前的视频路径
                    Intent icoxVideoIntent = new Intent(context, LocalVideoPlayer.class);
                    icoxVideoIntent.putExtra("path", file.getAbsolutePath());
                    context.startActivity(icoxVideoIntent);

                }

                // TODO 发送广播,让音乐停止
                stopIntent.putExtra("Stop", true);
                Log.i("Stop", "点击了");
                context.sendBroadcast(stopIntent);

            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingText))) {
                OpenFiles.getTextFileIntent(file, context);
            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingPdf))) {
                OpenFiles.getPdfFileIntent(file, context);
            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingWord))) {
                OpenFiles.getWordFileIntent(file, context);
            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingExcel))) {
                OpenFiles.getExcelFileIntent(file, context);
            } else if (checkEndsWithInStringArray(fileName, context
                    .getResources().getStringArray(R.array.fileEndingPPT))) {
                OpenFiles.getPPTFileIntent(file, context);
            } else {
                Toast.makeText(context, context.getString(R.string.can_not_open_file), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.not_file), Toast.LENGTH_SHORT).show();
        }

        /**
         * 不让用户选择
         */
/*
        if (file != null && file.isFile()) {
            Intent intent = getFileOpenIntent(context, file);
            if (intent != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, context.getString(R.string.can_not_open_file), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, context.getString(R.string.not_file), Toast.LENGTH_SHORT).show();
        }*/
    }
    //
    //    public static Intent getFileOpenIntent(Context context, File file) {
    //        Intent intent = null;
    //        String fileName = file.toString();
    //        if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingImage))) {
    //            intent = OpenFiles.getImageFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingWebText))) {
    //            intent = OpenFiles.getHtmlFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingPackage))) {
    //            intent = OpenFiles.getApkFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingAudio))) {
    //            intent = OpenFiles.getAudioFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingVideo))) {
    //            intent = OpenFiles.getVideoFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingText))) {
    //            intent = OpenFiles.getTextFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingPdf))) {
    //            intent = OpenFiles.getPdfFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingWord))) {
    //            intent = OpenFiles.getWordFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingExcel))) {
    //            intent = OpenFiles.getExcelFileIntent(file);
    //        } else if (checkEndsWithInStringArray(fileName,
    //                context.getResources().getStringArray(R.array.fileEndingPPT))) {
    //            intent = OpenFiles.getPPTFileIntent(file);
    //        } else {
    //            // do nothing
    //            Toast.makeText(context, context.getString(R.string.can_not_open_file), Toast.LENGTH_SHORT).show();
    //        }
    //        return intent;
    //    }

    private static boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {

        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd))
                return true;
        }
        return false;
    }

    // android获取一个用于打开HTML文件的intent
    public static void getHtmlFileIntent(File file, Context context) {
        try {
            Uri uri = Uri.parse(file.toString()).buildUpon().encodedAuthority("com.android.htmlfileprovider")
                    .scheme("content").encodedPath(file.toString()).build();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "text/html");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }
    }

    //    // android获取一个用于打开图片文件的intent
    //    public static Intent getImageFileIntent(File file) {
    //        Intent intent = new Intent(Intent.ACTION_VIEW);
    //        intent.addCategory(Intent.CATEGORY_DEFAULT);
    //        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //        Uri uri = Uri.fromFile(file);
    //        intent.setDataAndType(uri, "image/*");
    //        return intent;
    //    }

    // android获取一个用于打开PDF文件的intent
    public static void getPdfFileIntent(File file, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/pdf");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }
    }

    // android获取一个用于打开文本文件的intent
    public static void getTextFileIntent(File file, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "text/plain");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }

    }

    //    // android获取一个用于打开音频文件的intent
    //    public static Intent getAudioFileIntent(File file) {
    //        Intent intent = new Intent(Intent.ACTION_VIEW);
    //        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //        intent.putExtra("oneshot", 0);
    //        intent.putExtra("configchange", 0);
    //        Uri uri = Uri.fromFile(file);
    //        intent.setDataAndType(uri, "audio/*");
    //        return intent;
    //    }

    //    // android获取一个用于打开视频文件的intent
    //    public static Intent getVideoFileIntent(File file) {
    //        Intent intent = new Intent(Intent.ACTION_VIEW);
    //        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    //        intent.putExtra("oneshot", 0);
    //        intent.putExtra("configchange", 0);
    //        Uri uri = Uri.fromFile(file);
    //        intent.setDataAndType(uri, "video/*");
    //        return intent;
    //    }

    // android获取一个用于打开CHM文件的intent
    public static Intent getChmFileIntent(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "application/x-chm");
        return intent;
    }

    // android获取一个用于打开Word文件的intent
    public static void getWordFileIntent(File file, Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/msword");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }
    }

    // android获取一个用于打开Excel文件的intent
    public static void getExcelFileIntent(File file, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.ms-excel");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }
    }

    // android获取一个用于打开PPT文件的intent
    public static void getPPTFileIntent(File file, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromFile(file);
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }
    }

    // android获取一个用于打开apk文件的intent
    public static void getApkFileIntent(File file, Context context) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            startMoreActivity(file, context);
        }
    }

    public static void startMoreActivity(File file, Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(file);
        intent.setDataAndType(uri, "*/*");
        context.startActivity(intent);
    }
}