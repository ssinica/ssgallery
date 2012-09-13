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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

@SuppressWarnings("serial")
public class NoCacheJsServlet extends HttpServlet {

	private GalleryContext ctx;

	public NoCacheJsServlet(GalleryContext ctx) {
		this.ctx = ctx;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setCharacterEncoding("UTF8");
		resp.addHeader("Pragma", "no-cache");
		resp.addHeader("Cache-Control", "no-cache");
		resp.addHeader("Cache-Control", "no-store");
		resp.addHeader("Cache-Control", "must-revalidate");
		resp.addHeader("Expires", "Mon, 8 Aug 2006 10:00:00 GMT");
		resp.setContentType("application/x-javascript");

		String warPath = ctx.getConfig().getWar();
		String noCacheFilePath = FilenameUtils.concat(warPath, "gallery/gallery.nocache.js");
		BufferedInputStream in = null;
		try {
			File file = new File(noCacheFilePath);
			resp.setContentLength((int) file.length());
			in = new BufferedInputStream(new FileInputStream(file));
			ServletOutputStream out = resp.getOutputStream();
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
