package com.ss.gallery.server.transform;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class ScalrImageTransformer implements ImageTransformer {

	@Override
	public void resize(File jpeg, String destFile, int width) throws ImageTransformException {
		try {
			BufferedImage image = ImageIO.read(jpeg);
			BufferedImage thumb = Scalr.resize(image, width);
			File f = new File(destFile);
			ImageIO.write(thumb, "jpeg", f);
		} catch (IOException e) {
			throw new ImageTransformException("Failed to resize image", e);
		}
	}

}
