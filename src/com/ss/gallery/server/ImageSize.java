package com.ss.gallery.server;

import org.apache.commons.lang.StringUtils;

public enum ImageSize {

	SMALL("small"),
	MEDIUM("medium"),
	LARGE("large");
	
	private String id;

	private ImageSize(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public static ImageSize getById(String id) {
		if (StringUtils.isEmpty(id)) {
			return null;
		}
		ImageSize[] sizes = values();
		for (ImageSize size : sizes) {
			if (size.getId().equals(id)) {
				return size;
			}
		}
		return null;
	}
}
