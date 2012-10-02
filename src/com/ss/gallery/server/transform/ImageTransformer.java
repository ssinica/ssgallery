package com.ss.gallery.server.transform;

import java.io.File;

public interface ImageTransformer {

	void resize(File jpeg, String destFile, int width) throws ImageTransformException;

}
