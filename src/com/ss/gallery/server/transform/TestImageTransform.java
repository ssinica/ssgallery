package com.ss.gallery.server.transform;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class TestImageTransform {

	public static void main(String[] args) {
		new TestImageTransform().run();
	}

	public TestImageTransform() {

	}


	public void run() {
		try {
			long start = System.nanoTime();
			testScalr();
			System.out.println("Scalr finished with: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));

			/*start = System.nanoTime();
			testImageMagic();
			System.out.println("Magic finished with: " + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
			*/
		} catch (ImageTransformException e) {
			e.printStackTrace();
		}

	}

	private void testScalr() throws ImageTransformException {
		ScalrImageTransformer transformer = new ScalrImageTransformer();
		File jpeg = new File("C:/my/test.photos/Gostjam/1.jpg");
		for (int i = 0; i < 20; i++) {
			transformer.resize(jpeg, "C:/my/test.photos/" + i + ".jpg", 200);
		}
	}

	/*private void testImageMagic() throws ImageTransformException {
		ImageMagicImageTransformer transformer = new ImageMagicImageTransformer();
		File jpeg = new File("C:/my/test.photos/Gostjam/1.jpg");
		for (int i = 0; i < 20; i++) {
			transformer.resize(jpeg, "C:/my/test.photos/" + i + "-magic.jpg", 200);
		}
	}*/

}
