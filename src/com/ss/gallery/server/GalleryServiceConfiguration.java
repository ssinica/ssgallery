package com.ss.gallery.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GalleryServiceConfiguration {

	private static final Log log = LogFactory.getLog(GalleryServiceConfiguration.class);

	private static final String APP_DIR_PREFIX = "app.dir.";
	private static final String APP_DIR_DESCRIPTION = ".caption";
	private static final String APP_DIR_USERS = ".users";

	private static final String APP_USER_PREFIX = "app.user.";
	private static final String APP_USER_NAME = ".name";
	private static final String APP_USER_PASS = ".pass";

	private PropertiesConfiguration pc;
	private List<DirectoryConfig> directories = new ArrayList<DirectoryConfig>();
	private int threads = 3;
	private String thumbDir;
	private int thumbSize;
	private String viewDir;
	private int viewSize;
	private String war;
	private int imagesChunkSize;
	private int httpPort;
	private int folderImagesCount;
	private List<GalleryUser> users = new ArrayList<GalleryUser>();

	public GalleryServiceConfiguration(PropertiesConfiguration pc) {
		this.pc = pc;
		loadProperties();
	}

	private void loadProperties() {
		try {
			threads = pc.getInt("app.threads", 3);
			thumbDir = pc.getString("app.thumb.dir", "thumbs");
			thumbSize = pc.getInt("app.thumb.size", 180);
			viewDir = pc.getString("app.view.dir", "views");
			viewSize = pc.getInt("app.view.size", 800);
			war = pc.getString("app.war", "/");
			imagesChunkSize = pc.getInt("app.images.chunk.size", 6);
			httpPort = pc.getInt("app.http.port", 8080);
			folderImagesCount = pc.getInt("app.folder.images.count", 7);

			parseUsers();
			parseDirectories();

		} catch (Exception e) {
			log.error("Failed to parse gallery service configuration. Default values will be used", e);
		}
	}

	private void parseUsers() {
		int currentIndex = 1;
		while (true) {
			String pathProp = APP_USER_PREFIX + currentIndex;
			String name = pc.getString(pathProp + APP_USER_NAME, "").trim();
			String pass = pc.getString(pathProp + APP_USER_PASS, "").trim();
			if (!StringUtils.isEmpty(name) || !StringUtils.isEmpty(pass)) {
				users.add(new GalleryUser(name, pass));
				log.info("Gallery user detected: " + name);
			} else {
				break;
			}
			currentIndex++;
		}
	}

	private void parseDirectories() {
		int currentIndex = 1;
		while (true) {
			String pathProp = APP_DIR_PREFIX + currentIndex;
			String path = pc.getString(APP_DIR_PREFIX + currentIndex, "");
			if (StringUtils.isEmpty(path)) {
				break;
			}
			String caption = pc.getString(pathProp + APP_DIR_DESCRIPTION, "");
			String[] dirUsers = pc.getStringArray(pathProp + APP_DIR_USERS);

			DirectoryConfig dc = new DirectoryConfig(path, caption);

			if (dirUsers != null && dirUsers.length > 0) {
				dc.setUsers(Arrays.asList(dirUsers));
			}

			directories.add(dc);

			currentIndex++;
		}
	}

	public List<DirectoryConfig> getPaths() {
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

	public int getFolderImagesCount() {
		return folderImagesCount;
	}

	public List<GalleryUser> getUsers() {
		return users;
	}
}
