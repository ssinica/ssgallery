package com.ss.gallery.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Expects following url params: ?folderId=[folderId]&imageId=[imageId]&size=[small|medium|large]
 * 
 * @author sergey.sinica
 * 
 */
@SuppressWarnings("serial")
public class ImageServlet extends HttpServlet {

	private GalleryService gs;

	public ImageServlet(GalleryService gs, GalleryContext ctx) {
		this.gs = gs;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String folderId = req.getParameter("folderId");
		String imageId = req.getParameter("imageId");
		ImageSize size = ImageSize.getById(req.getParameter("size"));

		if(StringUtils.isEmpty(folderId) || StringUtils.isEmpty(imageId) || size == null) {

			// return dummy image
			writeDummyImageToResponse(size, resp);

		} else {

			String path = gs.getPathToImage(folderId, imageId, size);
			File file = null;
			if (!StringUtils.isEmpty(path)) {
				file = new File(path);
			}

			if (file != null && file.exists() && !file.isDirectory()) {

				if (size == ImageSize.LARGE) {
					downloadImage(file, resp);
				} else {
					writeImageToResponse(file, resp);
				}

			} else {
				writeDummyImageToResponse(size, resp);
			}
		}

	}

	private void downloadImage(File file, HttpServletResponse resp) throws ServletException, IOException  {		
		BufferedInputStream in = null;
		try {
			resp.setContentType("image/jpeg");
			resp.setContentLength((int) file.length());
			resp.setHeader( "Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			in = new BufferedInputStream(new FileInputStream(file));
			ServletOutputStream out = resp.getOutputStream();
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private void writeDummyImageToResponse(ImageSize size, HttpServletResponse resp) throws ServletException, IOException {
		// TODO:
	}
	
	private void writeImageToResponse(File file, HttpServletResponse resp) throws ServletException, IOException {		
		BufferedInputStream in = null;
		try {
			// resp.addHeader("Cache-Control", "public");
			// resp.addHeader("Cache-Control", "max-age=1000000");
			// resp.setHeader("Expires", "Sat, 08 Jun 2020 18:12:40 GMT");
			in = new BufferedInputStream(new FileInputStream(file));
			resp.setContentType("image/jpeg");
			resp.setContentLength((int) file.length());
			ServletOutputStream out = resp.getOutputStream();
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
