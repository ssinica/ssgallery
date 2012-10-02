package com.ss.gallery.server.transform;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ImageTransformTask {

	private String sourceSrc;
	private String destSrc;
	private int width;

	public ImageTransformTask(String sourceSrc, String destSrc, int width) {
		this.sourceSrc = sourceSrc;
		this.destSrc = destSrc;
		this.width = width;
	}

	public String getSourceSrc() {
		return sourceSrc;
	}

	public void setSourceSrc(String sourceSrc) {
		this.sourceSrc = sourceSrc;
	}

	public String getDestSrc() {
		return destSrc;
	}

	public void setDestSrc(String destSrc) {
		this.destSrc = destSrc;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public String toString() {	    
	    return new ToStringBuilder(this)
	    	.append("sourceSrc", sourceSrc)
	    	.append("destSrcSrc", destSrc)
	    	.append("width", width)
	    	.toString();
	}
}
