package com.ss.gallery.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GalleryServiceConfiguration implements ConfigurationListener {

	private static final Log log = LogFactory.getLog(GalleryServiceConfiguration.class);

	private static final String PROPERTIES_FILE_NAME = "ssgallery.properties";

	private static final String APP_DIR_PREFIX = "app.dir.";
	private static final String APP_DIR_DESCRIPTION = ".caption";
	private static final String APP_DIR_USERS = ".users";

	private static final String APP_USER_PREFIX = "app.user.";
	private static final String APP_USER_NAME = ".name";
	private static final String APP_USER_PASS = ".pass";

	private static final String APP_ROOT_DIRS_PREFIX = "app.dir.root.";

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
	private List<GalleryUser> users = new ArrayList<GalleryUser>();
	private long configReloadTimeout;
	private String rootDirConfigFile;

	private Set<GalleryServiceConfigurationListener> listeners;
	private ScheduledExecutorService threadPoolExecutor;

	public GalleryServiceConfiguration() {
		init();
		loadProperties();
		startConfigurationChecker();
	}

	public void addListener(GalleryServiceConfigurationListener listener) {
		if (listeners == null) {
			listeners = new HashSet<GalleryServiceConfigurationListener>();
		}
		listeners.add(listener);
	}

	private void init() {
		try {
			pc = new PropertiesConfiguration();
			pc.setDelimiterParsingDisabled(false);
			pc.setEncoding("UTF-8");
			pc.load(PROPERTIES_FILE_NAME);
			FileChangedReloadingStrategy reloadStrategy = new FileChangedReloadingStrategy();
			reloadStrategy.setRefreshDelay(20000);
			pc.setReloadingStrategy(reloadStrategy);
			pc.addConfigurationListener(this);
		} catch (ConfigurationException e) {
			log.error("Failed to load configuration file. Default values will be used.", e);
		}
	}

	@Override
	public void configurationChanged(ConfigurationEvent event) {
		if (event.isBeforeUpdate()) {
			return;
		}

		List<DirectoryConfig> updatedDirectories = loadDirectories();

		if (CollectionUtils.isEmpty(directories) && CollectionUtils.isEmpty(updatedDirectories)) {
			// no changes
			return;
		}

		List<DirectoryConfig> newDirs = new ArrayList<DirectoryConfig>();
		List<DirectoryConfig> removedDirs = new ArrayList<DirectoryConfig>();
		List<DirectoryConfig> changedDirs = new ArrayList<DirectoryConfig>();
		if (!CollectionUtils.isEmpty(listeners)) {
			if (CollectionUtils.isEmpty(directories)) {
				// new directories added;
				newDirs.addAll(updatedDirectories);
			} else if (CollectionUtils.isEmpty(updatedDirectories)) {
				// all directories are removed
				removedDirs.addAll(directories);
			} else {
				for (DirectoryConfig dir : updatedDirectories) {
					boolean found = false;
					Iterator<DirectoryConfig> it = directories.iterator();
					while (it.hasNext()) {
						DirectoryConfig oldDir = it.next();
						if (oldDir.equals(dir)) {
							// check if old directory is modified modified

							if (!dir.hasEqualsProperties(oldDir)) {
								changedDirs.add(dir);
							}

							it.remove();
							found = true;
							break;
						}
					}
					if (!found) {
						newDirs.add(dir);
					}
				}
				removedDirs.addAll(directories);
			}
		}

		this.directories = updatedDirectories;

		if (!CollectionUtils.isEmpty(listeners)) {
			for (GalleryServiceConfigurationListener listener : listeners) {
				listener.onPathsChanged(newDirs, removedDirs, changedDirs);
			}
		}
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
			configReloadTimeout = pc.getLong("app.config.reload.timeout", 30L);
			rootDirConfigFile = pc.getString("app.root.dir.config.file", ".ssgallery.ini");

			parseUsers();

			directories = loadDirectories();

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

	private List<DirectoryConfig> loadDirectories() {
		List<DirectoryConfig> dirs = new ArrayList<DirectoryConfig>();
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
			dc.setPosition(currentIndex);

			if (dirUsers != null && dirUsers.length > 0) {
				dc.setUsers(Arrays.asList(dirUsers));
			}

			dirs.add(dc);

			currentIndex++;
		}

		List<DirectoryConfig> rootDirs = loadRootDirs(currentIndex);
		if (!CollectionUtils.isEmpty(rootDirs)) {
			dirs.addAll(rootDirs);
		}

		return dirs;
	}

	private List<DirectoryConfig> loadRootDirs(int startPriority) {
		int currentIndex = 1;
		List<DirectoryConfig> dirs = new ArrayList<DirectoryConfig>();
		while(true) {
			String rootDir = pc.getString(APP_ROOT_DIRS_PREFIX + currentIndex, "");
			if (StringUtils.isEmpty(rootDir)) {
				break;
			}
			parseRootDir(new File(rootDir), startPriority + 1, dirs);
			currentIndex++;
		}
		return dirs;
	}

	private PropertiesConfiguration loadProps(String path) {
		PropertiesConfiguration pc = new PropertiesConfiguration();
		pc.setDelimiterParsingDisabled(false);
		pc.setEncoding("UTF-8");
		try {
			pc.load(path);
			return pc;
		} catch (ConfigurationException e) {
			log.error("Failed to load properties from file: " + path, e);
			return null;
		}
	}

	private int parseRootDir(File rootDir, int priority, List<DirectoryConfig> dirs) {
		if (!rootDir.exists() || rootDir.isFile()) {
			return priority;
		}

		log.debug("Check root dir: " + rootDir.getPath());

		String configFilePath = FilenameUtils.concat(rootDir.getPath(), getRootDirConfigFile());
		File configFile = new File(configFilePath);
		if (configFile.exists() && configFile.isFile()) {
			PropertiesConfiguration props = loadProps(configFilePath);
			if (props != null) {
				log.debug("properties loaded for file: " + configFilePath);

				String caption = props.getString("caption", rootDir.getName());
				String users[] = props.getStringArray("users");
				Integer pr = props.getInt("priority", priority);
				DirectoryConfig d = new DirectoryConfig(rootDir.getPath(), caption);
				d.setPosition(pr);
				dirs.add(d);
				if (users != null) {
					d.setUsers(Arrays.asList(users));
				}
				log.debug("Added root dir: " + d);
			}
		}
		
		File[] subFiles = rootDir.listFiles();
		if (subFiles != null && subFiles.length > 0) {
			for (File f : subFiles) {
				if (f.isDirectory()) {
					priority = parseRootDir(f, priority + 1, dirs);
				}
			}
		}

		return priority;

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

	public List<GalleryUser> getUsers() {
		return users;
	}

	public long getConfigReloadTimeout() {
		return configReloadTimeout;
	}

	private void startConfigurationChecker() {
		threadPoolExecutor = Executors.newScheduledThreadPool(1);
		threadPoolExecutor.scheduleAtFixedRate(new ConfigurationChecker(pc), 5, getConfigReloadTimeout(), TimeUnit.SECONDS);
	}

	public String getRootDirConfigFile() {
		return rootDirConfigFile;
	}

	// -------------------------------------------------------------

	private static class ConfigurationChecker implements Runnable {

		private PropertiesConfiguration pc;

		public ConfigurationChecker(PropertiesConfiguration pc) {
			this.pc = pc;
		}

		@Override
		public void run() {
			pc.getString("app.war", "/");
		}

	}
}
