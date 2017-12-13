package com.icox.administrator.icoxmp3player.uitl;

/**
 * Created by XiuChou on 2015/11/23
 */
public class MusicUtil {

    /**
     * 格式化时间，将其变成00:00的形式
     */
    public static String formatTime(long time) {
        long secondSum = time / 1000;
        long minute = secondSum / 60;
        long second = secondSum % 60;
        String result = "";
        if (minute < 10) {
            result = "0";
        }
        result = result + minute + ":";
        if (second < 10) {
            result = result + "0";
        }
        result = result + second;
        return result;
    }
}
