package com.ss.gallery.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GalleryServiceConfiguration {

	private static final Log log = LogFactory.getLog(GalleryServiceConfiguration.class);

	private PropertiesConfiguration pc;
	private Set<DirectoryConfig> directories = new HashSet<DirectoryConfig>();
	private int threads = 3;
	private String thumbDir;
	private int thumbSize;
	private String viewDir;
	private int viewSize;
	private String war;
	private int imagesChunkSize;
	private int httpPort;

	public GalleryServiceConfiguration(PropertiesConfiguration pc) {
		this.pc = pc;
		loadProperties();
	}

	private void loadProperties() {
		try {

			String[] paths = pc.getStringArray("app.paths");
			for (String path : paths) {
				directories.add(DirectoryConfig.parse(path));
			}

			threads = pc.getInt("app.threads", 3);
			thumbDir = pc.getString("app.thumb.dir", "thumbs");
			thumbSize = pc.getInt("app.thumb.size", 180);
			viewDir = pc.getString("app.view.dir", "views");
			viewSize = pc.getInt("app.view.size", 800);
			war = pc.getString("app.war", "/");
			imagesChunkSize = pc.getInt("app.images.chunk.size", 6);
			httpPort = pc.getInt("app.http.port", 8080);
		} catch (Exception e) {
			log.warn("Failed to parse gallery service configuration. Default values will be used", e);
		}
	}

	public Set<DirectoryConfig> getPaths() {
		return directories;
	}

	public int getThreadsCount() {
		return threads;
	}

	public String getThumbDir() {
		return thumbDir;
	}

	public int getThumbSize() {
		return thumbSize;
	}

	public String getViewDir() {
		return viewDir;
	}

	public int getViewSize() {
		return viewSize;
	}

	public String getWar() {
		return war;
	}

	public int getImagesChunkSize() {
		return imagesChunkSize;
	}

	public int getHttpPort() {
		return httpPort;
	}
}
