package com.ss.gallery.server;

public class ImageSizeInBytes {

	private long originalSize;
	private long thumbSize;
	private long viewSize;

	public ImageSizeInBytes(long originalSize, long thumbSize, long viewSize) {
		this.originalSize = originalSize;
		this.thumbSize = thumbSize;
		this.viewSize = viewSize;
	}

	public long getOriginalSize() {
		return originalSize;
	}

	public void setOriginalSize(int originalSize) {
		this.originalSize = originalSize;
	}

	public long getThumbSize() {
		return thumbSize;
	}

	public void setThumbSize(int thumbSize) {
		this.thumbSize = thumbSize;
	}

	public long getViewSize() {
		return viewSize;
	}

	public void setViewSize(int viewSize) {
		this.viewSize = viewSize;
	}

}
