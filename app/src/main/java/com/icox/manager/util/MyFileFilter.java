package com.icox.manager.util;

import java.io.File;
import java.io.FileFilter;

public class MyFileFilter implements FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (!pathname.getName().startsWith(".")) {
			return true;
		} else {
			return false;
		}
	}
}
