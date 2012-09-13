package com.ss.gallery.client;

public class GalleryClientUtils {

	private GalleryClientUtils() {

	}
	
	public static String genSmallImageSrc(String folderId, String imageId) {
		return "/images?folderId=" + folderId + "&imageId=" + imageId + "&size=small";
	}
	
	public static String genMediumImageSrc(String folderId, String imageId) {
		return "/images?folderId=" + folderId + "&imageId=" + imageId + "&size=medium";
	}
	
	public static String genLargeImageSrc(String folderId, String imageId) {
		return "/images?folderId=" + folderId + "&imageId=" + imageId + "&size=large";
	}
}
