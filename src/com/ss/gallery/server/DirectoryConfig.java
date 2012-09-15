package com.ss.gallery.server;


public class DirectoryConfig {

	private String path;
	private String caption;

	public DirectoryConfig(String path, String caption) {
		this.path = path;
		this.caption = caption;
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
