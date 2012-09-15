package com.ss.gallery.client;

import java.util.List;

public class ClientFolder {

	private String caption;
	private String id;
	private List<String> randomImagesList;

	public ClientFolder(String id, String caption, List<String> randomImagesIds) {
		this.id = id;
		this.caption = caption;
		this.randomImagesList = randomImagesIds;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getRandomImagesList() {
		return randomImagesList;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ClientFolder) {
			ClientFolder casted = (ClientFolder) obj;
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
