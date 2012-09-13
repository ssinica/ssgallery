package com.ss.gallery.server;

import java.util.List;

public class ImagesChunk {

	private List<ServerImage> images = null;
	private ServerFolder folder;
	private int totalImagesCount = 0;
	private ServerImage nextImage;
	private ServerImage prevImage;
	private int startIndex = 0;

	public ImagesChunk() {

	}

	public List<ServerImage> getImages() {
		return images;
	}

	public void setImages(List<ServerImage> images) {
		this.images = images;
	}

	public ServerFolder getFolder() {
		return folder;
	}

	public void setFolder(ServerFolder folder) {
		this.folder = folder;
	}

	public int getTotalImagesCount() {
		return totalImagesCount;
	}

	public void setTotalImagesCount(int totalImagesCount) {
		this.totalImagesCount = totalImagesCount;
	}

	public ServerImage getNextImage() {
		return nextImage;
	}

	public void setNextImage(ServerImage nextImage) {
		this.nextImage = nextImage;
	}

	public ServerImage getPrevImage() {
		return prevImage;
	}

	public void setPrevImage(ServerImage prevImage) {
		this.prevImage = prevImage;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

}
