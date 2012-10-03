package com.ss.gallery.server.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ss.gallery.server.GalleryServiceConfiguration;
import com.ss.gallery.server.GalleryUtils;

/**
 * The service which transforms images.
 * @author sergey.sinica
 *
 */
public class ImagesTransformService {

	private static final Log log = LogFactory.getLog(ImagesTransformService.class);

	private GalleryServiceConfiguration config;
	private BlockingQueue<ImageTransformTask> transformTasks;

	private ExecutorService consumerExecutor;
	private ExecutorService transformExecutor;
	private ExecutorService producerExecutor;

	public ImagesTransformService(GalleryServiceConfiguration config) {
		this.config = config;
		this.transformTasks = new LinkedBlockingQueue<ImageTransformTask>(10);

		consumerExecutor = Executors.newFixedThreadPool(config.getThreadCountConsumeResize());
		transformExecutor = Executors.newFixedThreadPool(config.getThreadCountResize());
		producerExecutor = Executors.newFixedThreadPool(1);

		consumerExecutor.submit(new TransformTaskConsumer());
	}

	public void addToResize(File sourceJpeg, String folderId, boolean thumb, boolean view) {

		if (!thumb && !view) {
			return;
		}

		String sourceFileName = sourceJpeg.getName();
		String pathToJpeg = sourceJpeg.getPath();
		
		// create thumb
		if (thumb) {
			String thumbFilePath = GalleryUtils.getThumbPath(sourceFileName, folderId, config.getStorePath());
			ImageTransformTask thumbTask = new ImageTransformTask(pathToJpeg, thumbFilePath, config.getThumbSize());
			producerExecutor.submit(new TransformTaskProducer(thumbTask));
		}

		// create view
		if (view) {
			String viewFilePath = GalleryUtils.getViewPath(sourceFileName, folderId, config.getStorePath());
			ImageTransformTask viewTask = new ImageTransformTask(pathToJpeg, viewFilePath, config.getViewSize());
			producerExecutor.submit(new TransformTaskProducer(viewTask));
		}
	}

	private void executeTask(ImageTransformTask task) {
		transformExecutor.submit(new TransformTaskExecutor(task, config));
	}

	// -------------------------------------------------------------------------------------

	private class TransformTaskProducer implements Runnable {

		private ImageTransformTask task;

		public TransformTaskProducer(ImageTransformTask task) {
			this.task = task;
		}

		@Override
		public void run() {
			try {
				transformTasks.put(task);
				log.debug("Transform task submitted: " + task);
			} catch (InterruptedException e) {
				log.error("TransformTaskProducer interrupted.", e);
			}
		}
	}

	private class TransformTaskConsumer implements Runnable {
		@Override
		public void run() {
			try {
				while (true) {
					ImageTransformTask task = transformTasks.take();
					executeTask(task);
				}
			} catch (InterruptedException e) {
				log.error("TransformTaskConsumer interrupted.", e);
			}
		}
	}

	private class TransformTaskExecutor implements Runnable {

		private ImageTransformTask task;
		private ImageTransformer transformer;
		private GalleryServiceConfiguration config;

		public TransformTaskExecutor(ImageTransformTask task, GalleryServiceConfiguration config) {
			this.task = task;
			this.config = config;
			this.transformer = new ImageMagicImageTransformer(config);
			//this.transformer = new ScalrImageTransformer();
		}

		@Override
		public void run() {
			log.debug("Starting to resize: " + task);
			long startTime = System.currentTimeMillis();

			InputStream is = null;
			OutputStream os = null;
			File tmpCopy = null;
			try {
				File originalJpeg = new File(task.getSourceSrc());
				String tmpFilePath = FilenameUtils.concat(config.getTmpPath(), System.currentTimeMillis() + ".jpg");
				tmpCopy = new File(tmpFilePath);
				is = new FileInputStream(originalJpeg);
				os = new FileOutputStream(tmpCopy);
				IOUtils.copy(is, os);

				transformer.resize(tmpCopy, task.getDestSrc(), task.getWidth());

				long executionTime = System.currentTimeMillis() - startTime;
				log.debug("Finished to resize (" + executionTime + "ms): " + task);

			} catch (FileNotFoundException e) {
				log.error("Failed to resize image: " + task, e);
			} catch (IOException e) {
				log.error("Failed to resize image: " + task, e);
			} catch (ImageTransformException e) {
				log.error("Failed to resize image: " + task, e);
			} finally {
				IOUtils.closeQuietly(is);
				IOUtils.closeQuietly(os);
				if (tmpCopy != null) {
					String tmpPath = tmpCopy.getPath();
					try {
						tmpCopy.delete();
					} catch (Exception e) {
						log.debug("Failed to delete tmp file " + tmpPath);
					}
				}
			}
		}

	}

}
