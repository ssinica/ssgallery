package com.ss.gallery.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ss.gallery.shared.Actions;

@SuppressWarnings("serial")
public class GalleryServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(GalleryServlet.class);

	private GalleryService gs;
	private GalleryContext ctx;

	public GalleryServlet(GalleryService gs, GalleryContext ctx) {
		this.gs = gs;
		this.ctx = ctx;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		String loggedInUser = ctx.getLoggedInUser(req);
		StringBuilder jsonData = new StringBuilder();
		if (!StringUtils.isEmpty(loggedInUser)) {
			HashMap<String, Object> data = new HashMap<String, Object>();
			data.put("user", loggedInUser);
			String json = ServerJsonHelper.toJson(data);
			jsonData.append("<div id='" + Actions.JSON_DATA_EL_ID + "' class='json'>");
			jsonData.append(json);
			jsonData.append("</div>");
		}

		Duration duration = new Duration();
		resp.setCharacterEncoding("UTF8");
		resp.addHeader("Pragma", "no-cache");
		resp.addHeader("Cache-Control", "no-cache");
		resp.addHeader("Cache-Control", "no-store");
		resp.addHeader("Cache-Control", "must-revalidate");
		resp.addHeader("Expires", "Mon, 8 Aug 2006 10:00:00 GMT");
		PrintWriter wr = resp.getWriter();
		StringBuilder sb = new StringBuilder();
		sb.append("<!doctype html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"css/bootstrap.css\">");
		sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"main.css?v8\">");
		sb.append("<title>SSGallery</title>");
		sb.append("<script type=\"text/javascript\" language=\"javascript\" src=\"gallery/gallery.nocache.js\"></script>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append(jsonData.toString());
		sb.append("<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>");
		sb.append("<noscript>");
		sb.append("<div style=\"width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif\">");
		sb.append("Your web browser must have JavaScript enabled in order for this application to display correctly. </div>");
		sb.append("</noscript>");
		sb.append("</body>");
		sb.append("</html>");

		log.info("doGet time: " + duration.elapsedNanos() + " ms.");
		wr.print(sb.toString());
	}



}
