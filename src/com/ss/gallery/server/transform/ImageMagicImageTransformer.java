package com.ss.gallery.server.transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ss.gallery.server.GalleryServiceConfiguration;

public class ImageMagicImageTransformer implements ImageTransformer {

	private static final Log log = LogFactory.getLog(ImageMagicImageTransformer.class);

	private Process process;
	private GalleryServiceConfiguration config;
	private int timeout;

	public ImageMagicImageTransformer(GalleryServiceConfiguration config) {
		this.config = config;
		timeout = this.config.getThreadResizeTimeout();
	}

	@Override
	public void resize(File jpeg, String destFile, int width) throws ImageTransformException {
		String convertCommand = config.getImageMagickConvertCommand();

		String sourcePath = jpeg.getPath();
		if (sourcePath.contains(" ")) {
			log.error("Source path " + sourcePath + " contains spaces. Image resize aborted");
			return;
		}

		if (destFile.contains(" ")) {
			log.error("Disctination path " + destFile + " contains spaces. Image resize aborted");
			return;
		}

		String cmd = convertCommand + " " + sourcePath + " -resize " + width + "x" + width + " " + destFile;
		log.debug("execute cmd: " + cmd);
		InputStream in = null;
		OutputStream out = null;
		InputStream err = null;
		Timer timer = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
			in = process.getInputStream();
			out = process.getOutputStream();
			err = process.getErrorStream();
			timer = new Timer();
			timer.schedule(new TimeoutTimer(Thread.currentThread()), TimeUnit.SECONDS.toMillis(timeout));
			
			int result = process.waitFor(); // wait while process finishes
			if (result != 0) {
				String error = "";
				if (err != null) {
					byte[] errOut = IOUtils.toByteArray(err);
					if (null != errOut) {
						error = new String(errOut);
					}
				}
				log.error("Failed to resize image with command: '" + cmd + "'. Error message: " + error);
			} else {
				if (in != null) {
					String msg = new String(IOUtils.toByteArray(in));
					log.debug("Image resized. " + msg);
				}
			}

		} catch (IOException e) {
			log.error("Failed to resize image.", e);
			throw new ImageTransformException("Failed to resize image.", e);
		} catch (InterruptedException e) {
			try {
				process.destroy();
			} catch (Exception e2) {
				// ignore
			}
			log.error("Failed to resize image because timeout limit " + timeout + "s exceeded");
			throw new ImageTransformException("Failed to resize image. Process interrupted", e);
		} finally {
			if (timer != null) {
				try {
					timer.cancel();
				} catch (Exception e) {
					// ignore
				}
			}
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(err);
		}
	}

	private static class TimeoutTimer extends TimerTask {
        private Thread target = null;

        public TimeoutTimer(Thread target) {
            this.target = target;
        }

        @Override
        public void run() {
            target.interrupt();
        }

	}

}
