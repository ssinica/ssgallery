package com.ss.gallery.server.transform;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ss.gallery.server.GalleryServiceConfiguration;

public class ImageMagicImageTransformer implements ImageTransformer {

	private static final Log log = LogFactory.getLog(ImageMagicImageTransformer.class);

	private GalleryServiceConfiguration config;

	public ImageMagicImageTransformer(GalleryServiceConfiguration config) {
		this.config = config;
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

		try {
			RuntimeExecutor.execute(cmd, config.getThreadResizeTimeout());
		} catch (RuntimeExecutorTimeoutException e) {
			throw new ImageTransformException("Failed to resize image because timeout", e);
		} catch (IOException e) {
			throw new ImageTransformException("Failed to resize image.", e);
		}

	}


}
