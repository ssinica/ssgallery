package com.ss.gallery.server;

import java.util.List;


public class DirectoryConfig {

	private String path;
	private String caption;
	private List<String> users = null;

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

	public void setUsers(List<String> users) {
		this.users = users;
	}

	public List<String> getUsers() {
		return users;
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
