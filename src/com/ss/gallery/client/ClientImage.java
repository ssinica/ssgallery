package com.ss.gallery.client;

public class ClientImage {

	private String id;

	public ClientImage(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ClientImage) {
			ClientImage casted = (ClientImage) obj;
			return casted.getId().equals(id);
		} else {
			return false;
		}
	};

	@Override
	public int hashCode() {
		return id.hashCode();
	}

}
