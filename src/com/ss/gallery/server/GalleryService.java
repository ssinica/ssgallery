package com.ss.gallery.server;

import java.util.List;
import java.util.Set;


public interface GalleryService {

	Set<ServerFolder> listFolders();

	Set<ServerImage> listImages(String folderId);

	void start();

	String getPathToImage(String folderId, String imageId, ImageSize size);

	ServerFolder getFolderById(String folderId);

	ImagesChunk loadNextImagesChunk(String folderId, String startImageId);

	ImagesChunk loadPrevImagesChunk(String folderId, String startImageId);

	List<ServerImage> getRandomImagesFrom(ServerFolder folder, int count);

}
