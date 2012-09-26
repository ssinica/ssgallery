package com.ss.gallery.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

public class GalleryServiceImpl implements GalleryService, GalleryServiceConfigurationListener, CreateThumbsTask.Callback {
	
	private static final org.apache.commons.logging.Log log = LogFactory.getLog(GalleryServiceImpl.class);
	private static Random RANDOM = new Random();
	
	private ExecutorService executor;
	private GalleryContext ctx;
	private GalleryServiceConfiguration config;

	private SortedMap<ServerFolder, TreeSet<ServerImage>> data;

	public GalleryServiceImpl(GalleryContext ctx) {
		data = Collections.synchronizedSortedMap(new TreeMap<ServerFolder, TreeSet<ServerImage>>());
		this.ctx = ctx;
		this.config = ctx.getConfig();
		this.config.addListener(this);
	}

	@Override
	public void start() {
		startImpl();
	}

	@Override
	//CreateThumbsTask.Callback
	public void onCreateThumbsTaskFinished(String path) {
		DirectoryConfig foundDirConfig = null;
		List<DirectoryConfig> paths = config.getPaths();
		for (DirectoryConfig p : paths) {
			if (p.getPath().equals(path)) {
				foundDirConfig = p;
				break;
			}
		}
		if (foundDirConfig == null) {
			log.debug("Path " + path + " is found in configuration. Folder will not be reloaded.");
			return;
		}
		
		TreeSet<ServerImage> images = new TreeSet<ServerImage>();
		ServerFolder folder = reloadFolder(foundDirConfig, images);

		if (folder != null) {
			data.put(folder, images);
			log.debug("Updated folder " + path + " images list");
		}

	}

	@Override
	// GalleryServiceConfigurationListener
	public void onPathsChanged(List<DirectoryConfig> newDirs, List<DirectoryConfig> removedDirs, List<DirectoryConfig> changedDirs) {
		rebuildModel();
		createThumbsIfRequired();
	}

	@Override
	public ImagesChunk loadPrevImagesChunk(String folderId, String startImageId) {
		// load folder
		ServerFolder folder = getFolderById(folderId);
		if (folder == null) {
			return null;
		}

		// load all images
		TreeSet<ServerImage> images = data.get(folder);

		ImagesChunk chunk = loadImagesChunk(folder, startImageId, images.descendingIterator());
		chunk.setTotalImagesCount(images.size());

		// swap prev/next images
		ServerImage temp = chunk.getPrevImage();
		chunk.setPrevImage(chunk.getNextImage());
		chunk.setNextImage(temp);

		// correct start index
		chunk.setStartIndex(chunk.getTotalImagesCount() - chunk.getStartIndex() - chunk.getImages().size() + 2);

		// correct images list
		Collections.reverse(chunk.getImages());

		return chunk;
	}

	@Override
	public ImagesChunk loadNextImagesChunk(String folderId, String startImageId) {
		
		// load folder
		ServerFolder folder = getFolderById(folderId);
		if (folder == null) {
			return null;
		}
		
		// load all images
		TreeSet<ServerImage> images = data.get(folder);

		ImagesChunk chunk = loadImagesChunk(folder, startImageId, images.iterator());
		chunk.setTotalImagesCount(images.size());

		return chunk;
	}

	private ImagesChunk loadImagesChunk(ServerFolder folder, String startImageId, Iterator<ServerImage> it) {

		// prepare images chunk
		int chunkSize = config.getImagesChunkSize();
		List<ServerImage> imagesChunk = new ArrayList<ServerImage>(chunkSize);
		boolean startToLoad = StringUtils.isEmpty(startImageId); // if startImage is empty, start to load from first image
		ServerImage prevImage = null;
		ServerImage nextImage = null;
		int startIndex = startToLoad ? 1 : 0;
		while (it.hasNext()) {

			if (!startToLoad) {
				startIndex += 1;
			}

			ServerImage image = it.next();

			if (!startToLoad && image.getId().equals(startImageId)) {
				startToLoad = true;
			}

			if (startToLoad) {
				imagesChunk.add(image);
			} else {
				prevImage = image;
			}

			if (imagesChunk.size() == chunkSize) {
				try {
					nextImage = it.next();
				} catch (NoSuchElementException e) {
					// ignore
				}
				break;
			}

		}

		// prepare result
		ImagesChunk chunk = new ImagesChunk();
		chunk.setFolder(folder);
		chunk.setImages(imagesChunk);
		chunk.setPrevImage(prevImage);
		chunk.setNextImage(nextImage);
		chunk.setStartIndex(startIndex);
		return chunk;
	}

	@Override
	public ServerFolder getFolderById(String folderId) {
		return getFolderByIdImpl(folderId);
	}

	@Override
	public String getPathToImage(String folderId, String imageId, ImageSize size) {
		ServerFolder folder = getFolderById(folderId);
		if (folder == null) {
			return "";
		}
		ServerImage image = null;
		for (ServerImage im : data.get(folder)) {
			if (im.getId().equals(imageId)) {
				image = im;
				break;
			}
		}
		if (image == null) {
			return "";
		}

		String path = "";
		switch (size) {
		case SMALL:
			path = FilenameUtils.concat(folder.getPath(), config.getThumbDir());
			return FilenameUtils.concat(path, image.getName());
		case MEDIUM:
			path = FilenameUtils.concat(folder.getPath(), config.getViewDir());
			return FilenameUtils.concat(path, image.getName());
		case LARGE:
			return FilenameUtils.concat(folder.getPath(), image.getName());
		}
		return path;
	}

	@Override
	public Set<ServerFolder> listFolders() {
		return data.keySet();
	}

	@Override
	public Set<ServerImage> listImages(String folderId) {
		for (Entry<ServerFolder, TreeSet<ServerImage>> en : data.entrySet()) {
			if (en.getKey().getId().equals(folderId)) {
				return en.getValue();
			}
		}
		return Collections.emptySet();
	}

	@Override
	public List<ServerImage> getRandomImagesFrom(ServerFolder folder, int count) {
		TreeSet<ServerImage> images = data.get(folder);
		if (images == null) {
			return Collections.emptyList();
		}

		int size = images.size();
		if (size < count) {
			return new ArrayList<ServerImage>(images);
		}

		ServerImage[] array = images.toArray(new ServerImage[size]);
		List<ServerImage> res = new ArrayList<ServerImage>(count);
		for (int i = 0; i < count; i++) {
			int idx = RANDOM.nextInt(size);
			res.add(array[idx]);
		}
		return res;
	}

	// -----------------------------------------------------------------

	private ServerFolder getFolderByIdImpl(String id) {
		for (ServerFolder f : data.keySet()) {
			if (f.getId().equals(id)) {
				return f;
			}
		}
		return null;
	}

	private void startImpl() {

		// prepare executor
		GalleryServiceConfiguration config = ctx.getConfig();
		int threadsCount = config.getThreadsCount();
		executor = Executors.newFixedThreadPool(threadsCount);

		// build images model
		rebuildModel();

		// prepare thumbs on start
		createThumbsIfRequired();
	}

	private synchronized void rebuildModel() {
		long start = System.nanoTime();

		data.clear();

		log.info("Building folder model...");
		List<DirectoryConfig> paths = config.getPaths();
		for (int i = 0; i < paths.size(); i++) {
			TreeSet<ServerImage> images = new TreeSet<ServerImage>();
			ServerFolder serverFolder = reloadFolder(paths.get(i), images);
			if (serverFolder != null) {
				data.put(serverFolder, images);
			}
		}
		
		Iterator<Entry<ServerFolder, TreeSet<ServerImage>>> it = data.entrySet().iterator();
		while(it.hasNext()) {
			Entry<ServerFolder, TreeSet<ServerImage>> entry = it.next();
			log.info("Folder " + entry.getKey() + " contains " + entry.getValue().size() + " images.");
		}
		long elapsed = System.nanoTime() - start;
		log.info("Folder model build in " + TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms.");
	}

	private void createThumbsIfRequired() {
		List<DirectoryConfig> paths = config.getPaths();
		for (DirectoryConfig p : paths) {
			createThumbsIfRequired(p);
		}
	}

	private void createThumbsIfRequired(DirectoryConfig p) {
		String thumbDir = config.getThumbDir();
		int thumbSize = config.getThumbSize();
		String viewDir = config.getViewDir();
		int viewSize = config.getViewSize();
		executor.execute(new CreateThumbsTask(p.getPath(), thumbDir, thumbSize, viewDir, viewSize, this));
	}

	private ServerFolder reloadFolder(DirectoryConfig folderConfig, TreeSet<ServerImage> images) {
		File folder = GalleryUtils.getDir(folderConfig.getPath());
		if (folder == null) {
			return null;
		}

		int position = folderConfig.getPosition();
		String folderName = folder.getName();
		String folderCaption = StringUtils.isEmpty(folderConfig.getCaption()) ? folderName : folderConfig.getCaption();
		String id = GalleryUtils.genId(folderCaption);
		ServerFolder gf = new ServerFolder(id, folderCaption, folder.getPath(), position, folderConfig.getUsers());

		String thumbDir = config.getThumbDir();
		String viewDir = config.getViewDir();

		File thumbs = GalleryUtils.getDir(folder, thumbDir);
		File views = GalleryUtils.getDir(folder, viewDir);
		if (thumbs == null || views == null) {
			return gf;
		}
		
		File[] allThumbs = GalleryUtils.listJpegs(thumbs);
		File[] allViews = GalleryUtils.listJpegs(thumbs);
		File[] jpegs = GalleryUtils.listJpegs(folder);
		for (File jpeg : jpegs) {
			if (!GalleryUtils.thumbExist(jpeg, allThumbs) || !GalleryUtils.thumbExist(jpeg, allViews)) {
				continue;
			}
			String jpegName = jpeg.getName();
			String jpegId = GalleryUtils.genId(jpeg.getName());
			ServerImage gi = new ServerImage(jpegId, jpegName);
			images.add(gi);
		}

		return gf;
	}

}
