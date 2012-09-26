package com.ss.gallery.server;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ToStringBuilder;


public class DirectoryConfig {

	private String path;
	private String caption;
	private List<String> users = null;
	private int position;

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

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public boolean hasEqualsProperties(DirectoryConfig config) {
		boolean usersEquals = false;
		if(users == null && config.getUsers() == null) {
			usersEquals = true;
		} else if (users != null && config.getUsers() != null) {
			usersEquals = CollectionUtils.isEqualCollection(users, config.getUsers());
		}

		if (!usersEquals) {
			return false;
		}

		if (caption == null && config.getCaption() == null) {
			return true;
		} else if (caption != null && config.getCaption() != null) {
			return caption.equals(config.getCaption());
		}

		return false;
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
	
	@Override
	public String toString() {	    
	    return new ToStringBuilder(this)
	    	.append("path", path)
	    	.append("caption", caption)
	    	.append("users", users == null ? "null" : Arrays.toString(users.toArray()))
	    	.toString();
	}
}
