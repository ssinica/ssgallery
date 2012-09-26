package com.ss.gallery.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class GalleryContext {

	private String CURRENT_LOGGED_USER = "__logged_user___";

	private GalleryServiceConfiguration config;

	public GalleryContext(GalleryServiceConfiguration config) {
		this.config = config;
	}

	public GalleryServiceConfiguration getConfig() {
		return config;
	}

	public String getLoggedInUser(HttpServletRequest request) {
		HttpSession session = request.getSession();
		return (String) session.getAttribute(CURRENT_LOGGED_USER);
	}

	public void setLoggedInUser(String name, HttpServletRequest request) {
		HttpSession session = request.getSession(true);
		session.setAttribute(CURRENT_LOGGED_USER, name);
	}

}
