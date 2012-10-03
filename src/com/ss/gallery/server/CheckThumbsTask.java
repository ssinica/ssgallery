package com.ss.gallery.server;

import java.io.File;
import java.util.concurrent.TimeUnit;

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

	private ImagesTransformService imagesTransformService;
	private GalleryServiceConfiguration config;
	private String folderPath;
	private String folderId;

	public CheckThumbsTask(String folderId, String folderPath, ImagesTransformService imagesTransformService, GalleryServiceConfiguration config) {
		this.imagesTransformService = imagesTransformService;
		this.folderPath = folderPath;
		this.folderId = folderId;
		this.config = config;
	}

	@Override
	public void run() {
		long start = System.nanoTime();

		String storePath = config.getStorePath();
		File storeRootDir = new File(storePath);
		File storeDir = GalleryUtils.prepareDir(storeRootDir, folderId);
		if (storeDir == null) {
			log.error("Failed to create store dir for folder = " + folderId);
			return;
		}

		File[] jpegs = GalleryUtils.listJpegs(new File(folderPath));

		for (File jpeg : jpegs) {

			/*File fileToProcess = jpeg;
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
			}*/

			String originalFileName = jpeg.getName();
			File thumb = GalleryUtils.getThumb(originalFileName, folderId, storePath);
			File view = GalleryUtils.getView(originalFileName, folderId, storePath);
			if (thumb == null || view == null) {
				imagesTransformService.addToResize(jpeg, folderId, thumb == null, view == null);
			}
		}
		
		long elapsed = System.nanoTime() - start;
		log.info("Thumbs check in dir " + folderId + " finished within " + TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms.");
	}

}