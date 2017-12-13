package com.icox.manager.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * Created by icox-xiuchou on 2016/1/14
 */
public class ScanUtil {

    /**
     * 删除音乐文件后发送广播
     *
     * @param context 上下文
     * @param filePath 文件路径
     */
    public static void scanFile(Context context, String filePath) {
        Uri uri = Uri.fromFile(new File(filePath));
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(scanIntent);
    }
}
