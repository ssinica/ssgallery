package com.ss.gallery.server;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class ServerFolder implements Comparable<ServerFolder> {

	private String caption;
	private String path;
	private String id;
	private int position = 0;
	private List<String> users = null;

	public ServerFolder(String id, String caption, String path, int position, List<String> users) {
		this.caption = caption;
		this.path = path;
		this.id = id;
		this.position = position;
		this.users = users;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getCaption() {
		return caption;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ServerFolder) {
			ServerFolder casted = (ServerFolder) obj;
			return id.equalsIgnoreCase(casted.getId());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("id: " + id)
			.append("path: " + path)
			.toString();
	}

	@Override
	public int compareTo(ServerFolder o) {
		if (position < o.getPosition()) {
			return -1;
		} else if (position == o.getPosition()) {
			return 0;
		} else {
			return 1;
		}
	}

	public int getPosition() {
		return position;
	}

	public List<String> getUsers() {
		return users;
	}

	public void setUsers(List<String> users) {
		this.users = users;
	}

}
