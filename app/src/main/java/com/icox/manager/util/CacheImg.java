package com.icox.manager.util;

import android.graphics.Bitmap;

public class CacheImg {

	private String pathOrUrl;
	private Bitmap bitmap;

	public String getPathOrUrl() {
		return pathOrUrl;
	}

	public void setPathOrUrl(String pathOrUrl) {
		this.pathOrUrl = pathOrUrl;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathOrUrl == null) ? 0 : pathOrUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CacheImg other = (CacheImg) obj;
		if (pathOrUrl == null) {
			if (other.pathOrUrl != null)
				return false;
		} else if (!pathOrUrl.equals(other.pathOrUrl))
			return false;
		return true;
	}
}
