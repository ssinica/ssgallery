package com.ss.gallery.server;

import java.util.List;

public interface GalleryServiceConfigurationListener {

	void onPathsChanged(List<DirectoryConfig> newDirs, List<DirectoryConfig> removedDirs, List<DirectoryConfig> changedDirs);

}
