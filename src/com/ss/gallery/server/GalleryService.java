package com.ss.gallery.server;

import java.util.Set;

import com.ss.gallery.server.transform.ImageTransformException;


public interface GalleryService {

	Set<ServerFolder> listFolders();

	Set<ServerImage> listImages(String folderId);

	void start();

	String getPathToImage(String folderId, String imageId, ImageSize size);

	ServerFolder getFolderById(String folderId);

	ImagesChunk loadNextImagesChunk(String folderId, String startImageId);

	ImagesChunk loadPrevImagesChunk(String folderId, String startImageId);

	ImagesChunk getRandomImagesFrom(ServerFolder folder, int count);

	void rotateImage(String imageId, String folderId, RotateDirection direction) throws ImageTransformException;

}
