package com.ss.gallery.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.imgscalr.Scalr;

public class CreateThumbsTask implements Runnable {

	private static final Log log = LogFactory.getLog(CreateThumbsTask.class);

	private String path;
	private String normalizedPath;
	private String thumbDir;
	private int thumbSize;
	private String viewDir;
	private int viewSize;
	private Callback callback;

	public CreateThumbsTask(String path, String thumbsDir, int thumbSize, String viewDir, int viewSize, Callback callback) {
		this.path = path;
		this.thumbDir = thumbsDir;
		this.thumbSize = thumbSize;
		this.viewDir = viewDir;
		this.viewSize = viewSize;
		this.callback = callback;
	}

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Creating thumbs in dir: " + path);
		normalizedPath = FilenameUtils.normalize(path);
		File dir = new File(normalizedPath);
		if (!dir.isDirectory()) {
			log.error("Failed to prepare thumbs. " + path + " is not a directory.");
			return;
		}

		File tdir = GalleryUtils.prepareDir(dir, thumbDir);
		if (tdir == null) {
			log.error("Failed to create thumbs dir.");
			return;
		}

		File vdir = GalleryUtils.prepareDir(dir, viewDir);
		if (vdir == null) {
			log.error("Failed to create view dir.");
			return;
		}

		File[] jpegs = GalleryUtils.listJpegs(dir);
		File[] thumbs = GalleryUtils.listJpegs(tdir);
		File[] views = GalleryUtils.listJpegs(vdir);

		int scalledThumbs = 0;
		int scalledViews = 0;
		for (File jpeg : jpegs) {
			if (!GalleryUtils.thumbExist(jpeg, thumbs)) {
				boolean created = createThumb(jpeg, tdir, thumbSize);
				if (created) {
					scalledThumbs += 1;
				}
			}
			if (!GalleryUtils.thumbExist(jpeg, views)) {
				boolean created = createThumb(jpeg, vdir, viewSize);
				if (created) {
					scalledViews += 1;
				}
			}
		}
		
		long elapsed = System.nanoTime() - start;
		log.info("In dir " + path + " created " + scalledThumbs
				+ " thumbs and " + scalledViews + " views in "
				+ TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms.");

		if (callback != null && (scalledThumbs > 0 || scalledViews > 0)) {
			callback.onCreateThumbsTaskFinished(path);
		}
	}

	private boolean createThumb(File jpeg, File thumbsDir, int width) {
		String jpegPath = "";
		try {
			jpegPath = jpeg.getPath();
			BufferedImage image = ImageIO.read(jpeg);
			BufferedImage thumb = Scalr.resize(image, width);
			String thumbPath = FilenameUtils.concat(thumbsDir.getPath(), jpeg.getName());
			File f = new File(thumbPath);
			ImageIO.write(thumb, "jpeg", f);
			log.debug("Thumb " + thumbPath + " created!");
			return true;
		} catch (Exception e) {
			log.error("Filed to create thumb for file: " + jpegPath, e);
			return false;
		}

	}

	public static interface Callback {
		void onCreateThumbsTaskFinished(String path);
	}

}