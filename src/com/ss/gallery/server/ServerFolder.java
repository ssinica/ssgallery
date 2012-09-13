package com.ss.gallery.server;

public class ServerFolder implements Comparable<ServerFolder> {

	private String caption;
	private String path;
	private String id;

	public ServerFolder(String id, String caption, String path) {
		this.caption = caption;
		this.path = path;
		this.id = id;
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
		return "{" + caption + "}";
	}

	@Override
	public int compareTo(ServerFolder o) {
		return caption.compareToIgnoreCase(o.getCaption());
	}

}
