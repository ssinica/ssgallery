package com.ss.gallery.server;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class GalleryUtils {
	
	public static final String[] jpegFiles = new String[] { "*.jpg", "*.jpeg", "*.JPEG", "*.JPG" };

	private GalleryUtils() {
	}

	public static File[] listJpegs(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return new File[0];
		}
		return FileFilterUtils.filter(new WildcardFileFilter(jpegFiles), dir.listFiles());
	}

	public static File findThumb(File jpegFile, File[] thumbs) {
		String fileName = jpegFile.getName();
		for (File thumb : thumbs) {
			String thumbName = thumb.getName();
			if (fileName.equals(thumbName)) {
				return thumb;
			}
		}
		return null;
	}

	public static File prepareDir(File dir, String dirName) {
		String thumbsDirPath = FilenameUtils.concat(dir.getPath(), dirName);
		File f = new File(thumbsDirPath);
		if (f.exists() && f.isDirectory()) {
			return f;
		}
		if (f.mkdir()) {
			return f;
		} else {
			return null;
		}
	}

	public static File getDir(String folderPath) {
		String normalizedPath = FilenameUtils.normalize(folderPath);
		File folder = new File(normalizedPath);
		if (!folder.exists() || !folder.isDirectory()) {
			return null;
		} else {
			return folder;
		}
	}

	public static File getDir(String folderPath, String childFolderName) {
		File folder = getDir(folderPath);
		if (folder == null) {
			return null;
		} else {
			return getDir(folder, childFolderName);
		}
	}

	public static File getDir(File folder, String childFolderName) {
		if (folder == null) {
			return null;
		}
		String thumbsDirPath = FilenameUtils.concat(folder.getPath(), childFolderName);
		File f = new File(thumbsDirPath);
		if (f.exists() && f.isDirectory()) {
			return f;
		} else {
			return null;
		}
	}

	public static String genId(String fileName) {
		fileName = fileName.replaceAll(" ", "_");
		fileName = fileName.replaceAll("\\+", "_");
		return fileName;
	}

	public static boolean canUserViewFolder(String name, ServerFolder folder) {
		List<String> users = folder.getUsers();
		if (users == null || users.size() == 0) {
			return true;
		}
		if (name == null || "".equals(name)) {
			return false;
		}
		return users.contains(name);
	}

}
