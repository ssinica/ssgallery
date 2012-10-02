package com.ss.gallery.server.transform;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ss.gallery.server.GalleryServiceConfiguration;

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

	public void addToResize(File sourceJpeg, boolean thumb, boolean view) {

		if (!thumb && !view) {
			return;
		}

		String sourceFileName = sourceJpeg.getName();
		String pathToJpeg = sourceJpeg.getPath();
		String sourceJpegDir = sourceJpeg.getParent();
		
		// create thumb
		if (thumb) {
			String thumbDir = FilenameUtils.concat(sourceJpegDir, config.getThumbDir());
			String thumbFilePath = FilenameUtils.concat(thumbDir, sourceFileName);
			ImageTransformTask thumbTask = new ImageTransformTask(pathToJpeg, thumbFilePath, config.getThumbSize());
			producerExecutor.submit(new TransformTaskProducer(thumbTask));
		}

		// create view
		if (view) {
			String viewDir = FilenameUtils.concat(sourceJpegDir, config.getViewDir());
			String viewFilePath = FilenameUtils.concat(viewDir, sourceFileName);
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

		public TransformTaskExecutor(ImageTransformTask task, GalleryServiceConfiguration config) {
			this.task = task;
			this.transformer = new ImageMagicImageTransformer(config);
			//this.transformer = new ScalrImageTransformer();
		}

		@Override
		public void run() {
			log.debug("Starting to resize: " + task);
			long startTime = System.currentTimeMillis();
			File jpeg = new File(task.getSourceSrc());
			try {
				transformer.resize(jpeg, task.getDestSrc(), task.getWidth());
			} catch (ImageTransformException e) {
				log.error("Failed to resize image: " + task, e);
			}			
			long executionTime = System.currentTimeMillis() - startTime;
			log.debug("Finished to resize (" + executionTime + "ms): " + task);
		}

	}

}
