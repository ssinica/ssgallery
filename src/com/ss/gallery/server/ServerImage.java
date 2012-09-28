package com.ss.gallery.server;

public class ServerImage implements Comparable<ServerImage> {

	private String name;
	private String id;
	private ImageSizeInBytes size;

	public ServerImage(String id, String name, ImageSizeInBytes size) {
		this.name = name;
		this.id = id;
		this.size = size;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ImageSizeInBytes getSize() {
		return size;
	}

	public void setSize(ImageSizeInBytes size) {
		this.size = size;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof ServerImage) {
			ServerImage casted = (ServerImage) obj;
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
		return "[" + name + "]";
	}

	@Override
	public int compareTo(ServerImage o) {
		return name.compareToIgnoreCase(o.getName());
	}
}
