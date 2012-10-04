package com.ss.gallery.server;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class GalleryUtils {
	
	private static final String THUMB_POSTFIX = "-thumb";
	private static final String VIEW_POSTFIX = "-view";

	public static final String[] jpegFiles = new String[] { "*.jpg", "*.jpeg", "*.JPEG", "*.JPG" };

	private GalleryUtils() {
	}

	public static File[] listJpegs(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			return new File[0];
		}
		return FileFilterUtils.filter(new WildcardFileFilter(jpegFiles), dir.listFiles());
	}

	public static String getThumbPath(String originalFileName, String folderId, String storePath) {
		String normalizedFileName = normalizeThumbFileName(originalFileName);
		return getThumbSrcImpl(normalizedFileName, folderId, storePath);
	}

	public static String getViewPath(String originalFileName, String folderId, String storePath) {
		String normalizedFileName = normalizeViewFileName(originalFileName);
		return getThumbSrcImpl(normalizedFileName, folderId, storePath);
	}

	public static String getThumbSrcImpl(String normalizedFileName, String folderId, String storePath) {
		String pathToDirWithThumb = FilenameUtils.concat(storePath, folderId);
		return FilenameUtils.concat(pathToDirWithThumb, normalizedFileName);
	}

	public static File getThumb(String originalFileName, String folderId, String storePath) {
		String src = getThumbPath(originalFileName, folderId, storePath);
		File f = new File(src);
		return f.exists() ? f : null;
	}

	public static File getView(String originalFileName, String folderId, String storePath) {
		String src = getViewPath(originalFileName, folderId, storePath);
		File f = new File(src);
		return f.exists() ? f : null;
	}

	public static String normalizeThumbFileName(String originalFileName) {
		return normalizeFileNameImpl(originalFileName, THUMB_POSTFIX);
	}

	public static String normalizeViewFileName(String originalFileName) {
		return normalizeFileNameImpl(originalFileName, VIEW_POSTFIX);
	}

	private static String normalizeFileNameImpl(String originalFileName, String postfix) {
		originalFileName = originalFileName.toLowerCase();
		originalFileName = genFolderId(originalFileName);
		int concatSize = originalFileName.endsWith("jpeg") ? 5 : 4;
		String name = originalFileName.substring(0, originalFileName.length() - concatSize);
		return name + postfix + ".jpg";
	}

	public static File prepareDir(File dir, String dirName) {
		String thumbsDirPath = FilenameUtils.concat(dir.getPath(), dirName);
		File f = new File(thumbsDirPath);
		if (f.exists() && f.isDirectory()) {
			return f;
		}
		if (f.mkdir()) {
			f.setWritable(true, false);
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

	public static String genFolderId(String fileName) {
		fileName = fileName.replaceAll(" ", "_");
		fileName = fileName.replaceAll("\\+", "_");
		return fileName;
	}

	public static boolean canUserViewFolder(String name, ServerFolder folder) {
		if (folder == null) {
			return false;
		}
		List<String> users = folder.getUsers();
		if (users == null || users.size() == 0) {
			return true;
		}
		if (name == null || "".equals(name)) {
			return false;
		}
		return users.contains(name);
	}

	public static synchronized String getTmpFileNameBase() {
		return String.valueOf(System.currentTimeMillis());
	}

}
