package com.ss.gallery.server;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class GalleryServer implements WrapperListener {

	private static final String PROPERTIES_FILE_NAME = "ssgallery.properties";

	private static final Log log = LogFactory.getLog(GalleryServer.class);

	private GalleryService gs;
	private GalleryContext ctx;
	private static GalleryServer server;

	public static void main(String[] args) {
		server = new GalleryServer();
		WrapperManager.start(server, args);
	}

	private GalleryServer() {
	}

	public void start() {
		try {
			initContext();
			startGalleryService();
			startHttpServer();
		} catch (Exception e) {
			System.out.println("Failed to start GalleryService:" + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void startGalleryService() {
		gs = new GalleryServiceImpl(ctx);
		gs.start();
	}

	private void initContext() throws ConfigurationException {
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.setDelimiterParsingDisabled(false);
		pc.setEncoding("UTF-8");
		pc.load(PROPERTIES_FILE_NAME);

		// pc.setReloadingStrategy(new FileChangedReloadingStrategy());
		ctx = new GalleryContext(pc);
	}

	private void startHttpServer() throws Exception {
		GalleryServiceConfiguration config = ctx.getConfig();
		
		HandlerList handlerList = new HandlerList();

		ServletContextHandler servletsHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletsHandler.setContextPath("/");
		handlerList.addHandler(servletsHandler);

		servletsHandler.addServlet(new ServletHolder(new NoCacheJsServlet(ctx)), "/gallery/gallery.nocache.js");
		servletsHandler.addServlet(new ServletHolder(new GalleryServlet(gs, ctx)), "/photos");
		servletsHandler.addServlet(new ServletHolder(new ImageServlet(gs, ctx)), "/images");
		servletsHandler.addServlet(new ServletHolder(new ApiServlet(gs, ctx)), "/api");
		
		String warDir = config.getWar();
		ResourceHandler rsHandler = new ResourceHandler();
		rsHandler.setResourceBase(warDir);
		handlerList.addHandler(rsHandler);

		Server httpServer = new Server(config.getHttpPort());
		httpServer.setHandler(handlerList);
		httpServer.start();
	}

	@Override
	public void controlEvent(int event) {
		if ((event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT)
				&& (WrapperManager.isLaunchedAsService() || WrapperManager
						.isIgnoreUserLogoffs())) {
			// Ignore
		} else {
			WrapperManager.stop(0);
		}

	}

	@Override
	public Integer start(String[] arg0) {
		server.start();
		return null;
	}

	@Override
	public int stop(int exitCode) {
		return exitCode;
	}

}
