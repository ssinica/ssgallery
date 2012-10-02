package com.ss.gallery.server;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ss.gallery.server.transform.ImagesTransformService;

/**
 * Check if thumbs exist for images in specified directory. If not, then schedules thumbs creation.
 * @author sergey.sinica
 *
 */
public class CheckThumbsTask implements Runnable {

	private static final Log log = LogFactory.getLog(CheckThumbsTask.class);

	private String path;
	private String normalizedPath;
	private String thumbDir;
	private String viewDir;
	private ImagesTransformService imagesTransformService;

	public CheckThumbsTask(String path, String thumbsDir, String viewDir, ImagesTransformService imagesTransformService) {
		this.path = path;
		this.thumbDir = thumbsDir;
		this.viewDir = viewDir;
		this.imagesTransformService = imagesTransformService;
	}

	@Override
	public void run() {
		long start = System.nanoTime();
		log.info("Checking thumbs in " + path);
		normalizedPath = FilenameUtils.normalize(path);
		File dir = new File(normalizedPath);
		if (!dir.isDirectory()) {
			log.error("Failed to check thumbs. " + path + " is not a directory.");
			return;
		}

		if (normalizedPath.contains(" ")) {
			log.error("Invalid path " + normalizedPath + ". No spaces in path are allowed");
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

		for (File jpeg : jpegs) {

			File fileToProcess = jpeg;

			String currentJpegName = jpeg.getName();
			if (currentJpegName.contains(" ")) {
				String newJpegName = currentJpegName.replaceAll(" ", "_");
				File renameToFile = new File(FilenameUtils.concat(jpeg.getParent(), newJpegName));
				if (jpeg.renameTo(renameToFile)) {
					fileToProcess = renameToFile;
				} else {
					log.debug("Filed to rename file " + currentJpegName + " to " + newJpegName);
					fileToProcess = null;
				}
			}

			if (fileToProcess != null) {
				boolean hasThumb = GalleryUtils.findThumb(jpeg, thumbs) != null;
				boolean hasView = GalleryUtils.findThumb(jpeg, views) != null;
				if (!hasThumb || !hasView) {
					imagesTransformService.addToResize(jpeg, !hasThumb, !hasView);
				}
			}
		}
		
		long elapsed = System.nanoTime() - start;
		log.info("Thumbs check in dir " + path + " finished within " + TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms.");
	}

}