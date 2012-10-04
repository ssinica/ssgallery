package com.ss.gallery.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.JsonObject;
import com.ss.gallery.server.transform.ImageTransformException;
import com.ss.gallery.shared.Actions;

@SuppressWarnings("serial")
public class ApiServlet extends HttpServlet {

	private static final Log log = LogFactory.getLog(ApiServlet.class);

	private GalleryService gs;
	private GalleryContext ctx;

	public ApiServlet(GalleryService gs, GalleryContext ctx) {
		this.gs = gs;
		this.ctx = ctx;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String data = req.getParameter("data");
		log.debug("received data: " + data);

		JsonObject json = ServerJsonHelper.parseJsonObject(data);
		if (json == null) {
			printError("Failed to parse request json", resp);
			return;
		}

		String action = ServerJsonHelper.getString("action", json);
		if (StringUtils.isEmpty(action)) {
			printError("Action is undefined", resp);
			return;
		}

		processAction(action, json, req, resp);
	}

	private void processAction(String action, JsonObject json, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.debug("Processing action: " + action);
		if(Actions.WELCOME.equals(action)) {
			processWelcome(json, req, resp);
		} else if (Actions.IMAGES.equals(action)) {
			processImages(json, req, resp);
		} else if (Actions.LOGIN.equals(action)) {
			login(json, req, resp);
		} else if (Actions.LOGOUT.equals(action)) {
			logout(json, req, resp);
		} else if (Actions.ROTATE.equals(action)) {
			rotate(json, req, resp);
		}
	}

	private void rotate(JsonObject json, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String imageId = ServerJsonHelper.getString("imageId", json);
		String folderId = ServerJsonHelper.getString("folderId", json);
		String direction = ServerJsonHelper.getString("direction", json);

		if (!"left".equals(direction) && !"right".equals(direction)) {
			writeResponseData("{}", resp);
			return;
		}

		ServerFolder folder = gs.getFolderById(folderId);

		if (!GalleryUtils.canUserViewFolder(ctx.getLoggedInUser(req), folder)) {
			writeResponseData("{}", resp);
			return;
		}

		RotateDirection rotateTo = "left".equals(direction) ? RotateDirection.LEFT : RotateDirection.RIGHT;
		try {
			gs.rotateImage(imageId, folderId, rotateTo);
			writeResponseData("{}", resp);
		} catch (ImageTransformException e) {
			log.error("Failed to rotate image", e);
			printError("Failed to rotate image", resp);
		}
	}

	private void logout(JsonObject json, HttpServletRequest req, HttpServletResponse resp) {
		ctx.setLoggedInUser(null, req);
	}

	private void login(JsonObject json, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String name = ServerJsonHelper.getString("name", json);
		String pass = ServerJsonHelper.getString("pass", json);

		boolean found = false;
		List<GalleryUser> users = ctx.getConfig().getUsers();
		if (users != null) {
			for (GalleryUser u : users) {
				String uname = u.getName();
				String upass = u.getPass();
				if (uname.equalsIgnoreCase(name) && upass.equals(pass)) {
					// bingo!
					found = true;
					break;
				}
			}
		}

		if (found) {
			ctx.setLoggedInUser(name, req);
		}

		HashMap<String, Object> folderData = new HashMap<String, Object>();
		folderData.put("found", found);
		String responseJson = ServerJsonHelper.toJson(folderData);
		writeResponseData(responseJson, resp);
	}

	private void processImages(JsonObject json, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String folderId = ServerJsonHelper.getString("folderId",json);		
		String imageId = ServerJsonHelper.getString("imageId", json);
		boolean next = Boolean.valueOf(ServerJsonHelper.getString("next", json));

		ImagesChunk chunk = null;
		if (next) {
			chunk = gs.loadNextImagesChunk(folderId, imageId);
		} else {
			chunk = gs.loadPrevImagesChunk(folderId, imageId);
		}
		if (chunk == null) {
			writeResponseData("{}", resp);
			return;
		}
		ServerFolder folder = chunk.getFolder();
		if (!GalleryUtils.canUserViewFolder(ctx.getLoggedInUser(req), folder)) {
			writeResponseData("{}", resp);
			return;
		}
		
		// images
		List<HashMap<String, Object>> imagesData = new ArrayList<HashMap<String, Object>>();
		for (ServerImage image : chunk.getImages()) {
			HashMap<String, Object> imageData = new HashMap<String, Object>();
			imageData.put("id", image.getId());
			imagesData.add(imageData);
		}

		// folder
		HashMap<String, Object> folderData = new HashMap<String, Object>();
		folderData.put("caption", folder.getCaption());
		folderData.put("id", folder.getId());
		folderData.put("imagesCount", chunk.getTotalImagesCount());
		folderData.put("folderSize", folder.getSizeInBytes());

		ServerImage nextImage = chunk.getNextImage();
		ServerImage prevImage = chunk.getPrevImage();

		// response
		HashMap<String, Object> responseData = new HashMap<String, Object>();
		responseData.put("images", imagesData);
		responseData.put("folder", folderData);
		responseData.put("totalImagesCount", chunk.getTotalImagesCount());
		if (nextImage != null) {
			responseData.put("nextImageId", nextImage.getId());
		}
		if (prevImage != null) {
			responseData.put("prevImageId", prevImage.getId());
		}
		responseData.put("startIndex", chunk.getStartIndex());
		
		String responseJson = ServerJsonHelper.toJson(responseData);
		writeResponseData(responseJson, resp);
	}

	private void processWelcome(JsonObject json, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Set<ServerFolder> folders = gs.listFolders();
		int collageImagesCount = 4;
		List<HashMap<String, Object>> foldersData = new ArrayList<HashMap<String, Object>>();

		String loggedInUser = ctx.getLoggedInUser(req);

		for (ServerFolder folder : folders) {

			if (!GalleryUtils.canUserViewFolder(loggedInUser, folder)) {
				continue;
			}

			ImagesChunk chunk = gs.getRandomImagesFrom(folder, collageImagesCount);
			List<String> imagesJsonData = new ArrayList<String>(chunk.getImages().size());
			for (ServerImage img : chunk.getImages()) {
				imagesJsonData.add(img.getId());
			}

			HashMap<String, Object> folderData = new HashMap<String, Object>();
			folderData.put("caption", folder.getCaption());
			folderData.put("id", folder.getId());
			folderData.put("images", imagesJsonData);
			folderData.put("imagesCount", chunk.getTotalImagesCount());
			folderData.put("folderSize", folder.getSizeInBytes());

			foldersData.add(folderData);
		}

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("folders", foldersData);

		String responseJson = ServerJsonHelper.toJson(data);
		writeResponseData(responseJson, resp);
	}
	
	private void printError(String code, HttpServletResponse resp) throws IOException {
		HashMap<String, String> error = new HashMap<String, String>();
		error.put("error", code);
		String responseJson = ServerJsonHelper.toJson(error);
		log.error("Error: " + responseJson);
		writeResponseData(responseJson, resp);
	}
	
	private void writeResponseData(String json, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json;charset=utf-8");
		String responseJson = "{\"data\":" + json + "}";
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.getWriter().write(responseJson);
	}

}
