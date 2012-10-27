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
import com.ss.gallery.server.RotateDirection;

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
	private ImagesTransformServiceListener listener;

	public ImagesTransformService(GalleryServiceConfiguration config, ImagesTransformServiceListener listener) {
		this.config = config;
		this.listener = listener;
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
			ImageTransformTask thumbTask = new ImageTransformTask(pathToJpeg, thumbFilePath, config.getThumbSize(), folderId, sourceFileName);
			producerExecutor.submit(new TransformTaskProducer(thumbTask));
		}

		// create view
		if (view) {
			String viewFilePath = GalleryUtils.getViewPath(sourceFileName, folderId, config.getStorePath());
			ImageTransformTask viewTask = new ImageTransformTask(pathToJpeg, viewFilePath, config.getViewSize(), folderId, sourceFileName);
			producerExecutor.submit(new TransformTaskProducer(viewTask));
		}
	}

	private void executeTask(ImageTransformTask task) {
		transformExecutor.submit(new TransformTaskExecutor(task, config));
	}

	private String genRotateCommand(String filePath, RotateDirection direction) {
		return config.getImageMagickConvertCommand() + " " + filePath + " -rotate " + direction.getAngle() + " " + filePath;
	}

	public void rotateImage(String originalImagePath, String folderId, RotateDirection direction) throws ImageTransformException {
		long startTime = System.currentTimeMillis();

		InputStream is = null;
		OutputStream os = null;
		File tmpCopy = null;
		try {
			File originalJpeg = new File(originalImagePath);
			if (!originalJpeg.exists()) {
				throw new ImageTransformException("File does not exist " + originalImagePath);
			}
			
			String originalFileName = originalJpeg.getName();
			File thumb = GalleryUtils.getThumb(originalFileName, folderId, config.getStorePath());
			File view = GalleryUtils.getView(originalFileName, folderId, config.getStorePath());

			if (thumb == null || view == null) {
				throw new ImageTransformException("Thumb or view does not exist yet for image " + originalImagePath);
			}

			try {
				// rotate thumb			
				RuntimeExecutor.execute(genRotateCommand(thumb.getPath(), direction), 300);

				// rotate view			
				RuntimeExecutor.execute(genRotateCommand(view.getPath(), direction), 300);
			} catch (RuntimeExecutorTimeoutException e) {
				try {
					thumb.delete();
					view.delete();
				} catch (Exception ee) {
					log.error("Faile to remove thumb and view after rotate failure");
				}
				throw e;
			} catch (IOException e) {
				try {
					thumb.delete();
					view.delete();
				} catch (Exception ee) {
					log.error("Faile to remove thumb and view after rotate failure");
				}
				throw e;
			}

			// copy original to tmp file
			/*String tmpFilePath = FilenameUtils.concat(config.getTmpPath(), GalleryUtils.getTmpFileNameBase() + ".jpg");
			tmpCopy = new File(tmpFilePath);
			is = new FileInputStream(originalJpeg);
			os = new FileOutputStream(tmpCopy);
			IOUtils.copy(is, os);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);*/

			// rotate original copy
			//RuntimeExecutor.execute(genRotateCommand(tmpFilePath, direction), 300);

			// copy original back
			/*is = new FileInputStream(tmpCopy);
			os = new FileOutputStream(originalJpeg);
			IOUtils.copy(is, os);
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(os);*/

			long executionTime = System.currentTimeMillis() - startTime;
			log.debug("Finished to rotate " + originalImagePath + " (" + executionTime + "ms)");

		} catch (RuntimeExecutorTimeoutException e) {
			throw new ImageTransformException("Failed to rotate image", e);
		} catch (FileNotFoundException e) {
			throw new ImageTransformException("Failed to rotate image", e);
		} catch (IOException e) {
			throw new ImageTransformException("Failed to rotate image", e);
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
				String tmpFilePath = FilenameUtils.concat(config.getTmpPath(), GalleryUtils.getTmpFileNameBase() + ".jpg");
				tmpCopy = new File(tmpFilePath);
				is = new FileInputStream(originalJpeg);
				os = new FileOutputStream(tmpCopy);
				IOUtils.copy(is, os);

				transformer.resize(tmpCopy, task.getDestSrc(), task.getWidth());

				long executionTime = System.currentTimeMillis() - startTime;
				log.debug("Finished to resize (" + executionTime + "ms): " + task);

				if(listener != null) {
					listener.onImageResized(task.getFolderId(), task.getSourceFileName(), task.getSourceSrc());
				}
				
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

	public static interface ImagesTransformServiceListener {
		void onImageResized(String folderId, String sourceFileName, String sourceFilePath);
	}

}
