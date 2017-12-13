package com.icox.manager.util;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File f1, File f2) {

        //1.先比较文件夹
        if (f1.isDirectory() && f2.isDirectory()) {
            return f1.getName().compareToIgnoreCase(f2.getName());
        } else {
            if (f1.isDirectory() && f2.isFile()) {//2.比较文件夹和文件
                return -1;//为了让文件夹排在文件前边，所以返回0，返回小于0的数，f1就会排在f2前边
            } else if (f1.isFile() && f2.isDirectory()) {//3.比较文件和文件夹
                return 1;//为了让文件夹排在文件前边，所以返回1，返回大于0的数，f1就会排在f2后边
            } else {//4.比较文件
                return f1.getName().compareToIgnoreCase(f2.getName());
            }
        }
    }
}
