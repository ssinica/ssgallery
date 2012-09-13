package com.ss.gallery.server;

import org.apache.commons.configuration.PropertiesConfiguration;

public class GalleryContext {

	private GalleryServiceConfiguration config;

	public GalleryContext(PropertiesConfiguration pc) {
		this.config = new GalleryServiceConfiguration(pc);
	}

	public GalleryServiceConfiguration getConfig() {
		return config;
	}

}
