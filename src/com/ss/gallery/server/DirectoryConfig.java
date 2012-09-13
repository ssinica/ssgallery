package com.ss.gallery.server;

import org.apache.commons.lang.StringUtils;

public class DirectoryConfig {

	private String path;
	private String caption;

	private DirectoryConfig(String path, String caption) {
		this.path = path;
		this.caption = caption;
	}

	public static DirectoryConfig parse(String s) {
		if (StringUtils.isEmpty(s)) {
			return null;
		}
		int idx = s.indexOf("#");
		if (idx <= 0) {
			return new DirectoryConfig(s, "");
		} else {
			String[] parts = s.split("#");
			return new DirectoryConfig(parts[0], parts[1]);
		}
	}

	public String getPath() {
		return path;
	}

	public String getCaption() {
		return caption;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof DirectoryConfig) {
			DirectoryConfig casted = (DirectoryConfig) obj;
			return path.equals(casted.getPath());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}
}
