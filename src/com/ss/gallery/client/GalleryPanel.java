package com.ss.gallery.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import com.ss.gallery.client.AjaxRequest.AjaxCallback;
import com.ss.gallery.client.JSONHelper.ValueParser;
import com.ss.gallery.shared.Actions;

public class GalleryPanel extends Composite implements
		ValueChangeHandler<String>, ClickHandler, KeyDownHandler {

	private static final String CSS_ARROW_OFF = "arrow-off";
	private static final String CSS_ACTIVE_SMALL = "sf-img-w__active";
	private static final String CSS_BIG_PHOTO_ON = "bf-w__on";
	private static final String CSS_SMALL_PHOTOS_ANIME = "sf-w__anime";

	private static final String UID_SMALL_LEFT = "uid-sl";
	private static final String UID_SMALL_RIGHT = "uid-sr";
	private static final String UID_FOLDER_CLICK = "uid-f";
	private static final String UID_CAPTION_CLICK = "uid-c";
	private static final String UID_SMALL_IMAGE_CLICK = "uid-sic";
	private static final String UID_LOGIN = "uid-login";
	private static final String UID_LOGOUT = "uid-logout";
	private static final String UID_ROTATE_LEFT = "uid-rotate-left";
	private static final String UID_ROTATE_RIGHT = "uid-rotate-right";
	
	private static final String ID_NEXT_SMALL = "id-nsi";
	private static final String ID_PREV_SMALL = "id-psi";

	private static GalleryPanelUiBinder uiBinder = GWT.create(GalleryPanelUiBinder.class);

	interface GalleryPanelUiBinder extends UiBinder<Widget, GalleryPanel> {
	}
	
	@UiField HTMLPanel elMain;
	@UiField DivElement elBigPhotoW;
	@UiField Element elSmallPhotosW;
	@UiField SpanElement elCaption;
	@UiField SpanElement elFolder;
	@UiField SpanElement elCount;
	@UiField SpanElement elCountDetails;
	@UiField SpanElement elLogin;
	@UiField InputElement elName;
	@UiField InputElement elPass;
	@UiField DivElement elLoginWrapper;
	@UiField DivElement elLoggedWrapper;
	@UiField SpanElement elLogged;
	@UiField SpanElement elLogout;

	private ClientFolder selectedFolder;
	private List<ClientImage> loadedImages = new ArrayList<ClientImage>();
	private int totalImagesCount = 0;
	private String prevImageId;
	private String nextImageId;
	private String selectedImageId;

	private int startIndex;

	public GalleryPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		initUI();
		String currentToken = History.getToken();
		processHistoryChange(currentToken);
	}

	private void initUI() {
		History.addValueChangeHandler(this);
		addDomHandler(this, ClickEvent.getType());
		GWTUtils.setUidToElement(UID_CAPTION_CLICK, elCaption);
		GWTUtils.setUidToElement(UID_LOGIN, elLogin);
		GWTUtils.setUidToElement(UID_LOGOUT, elLogout);

		JSONObject json = JSONHelper.getJsonFromElement(Actions.JSON_DATA_EL_ID);
		String loggedInUser = null;
		if (json != null) {
			loggedInUser = JSONHelper.getString(json, "user");
		}
		setCurrentUser(loggedInUser);
	}

	/**
	 * {@code ValueChangeHandler}
	 */
	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		String value = event.getValue();
		processHistoryChange(value);
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		if (event.isLeftArrow()) {
			navigateLeft();
		} else if (event.isRightArrow()) {
			navigateRight();
		}
	}

	/**
	 * {@code ClickHandler}
	 */
	@Override
	public void onClick(ClickEvent event) {
		NativeEvent nativeEvent = event.getNativeEvent();
		Element element = DOM.eventGetTarget(Event.as(nativeEvent));
        element = GWTUtils.getElementWithAttr((com.google.gwt.user.client.Element) element.cast(), "uid");        
        Element dataQueryEl = null;
        if (element != null) {
            dataQueryEl = GWTUtils.findDataQueryElement((com.google.gwt.user.client.Element) element.cast());
            String uid = element.getAttribute("uid");
            String json = dataQueryEl.getAttribute("data-query");

			if (!GWTUtils.isEmpty(uid)) {
				JSONObject targetJson = null;
				if (!GWTUtils.isEmpty(json)) {
					targetJson = (JSONObject) JSONParser.parse(json);
				}
				processClickEvent(uid, targetJson);
			}
        } else {
            return;
        }
	}

	private void processHistoryChange(String token) {
		if (GWTUtils.isEmpty(token)) {

			loadWelcomeScreen();

		} else {

			String folderId = token;
			String imageId = null;
			int idx = token.indexOf("/");
			if (idx > 0) {
				folderId = token.substring(0, idx);
				imageId = token.substring(idx + 1);
			}

			boolean folderDataLoaded = selectedFolder != null && selectedFolder.getId().equals(folderId);
			boolean imageToSelectLoaded = false;
			if (imageId != null && !GWTUtils.isEmpty(loadedImages)) {
				for (ClientImage im : loadedImages) {
					if (im.getId().equals(imageId)) {
						imageToSelectLoaded = true;
						break;
					}
				}
			}
			
			if (!folderDataLoaded || !imageToSelectLoaded) {
				loadSmallImages(folderId, imageId, true);
			} else {
				showImages(selectedFolder, imageId, loadedImages, false);
			}

		}
	}

	private void loadSmallImages(final String folderId, final String imageId, boolean next) {
		new AjaxRequest(Actions.IMAGES)
			.addParam("folderId", folderId)
			.addParam("imageId", imageId)
			.addParam("next", Boolean.toString(next))
			.send(new AjaxCallback() {
				@Override
				public void onResponse(JSONObject json) {
					applyImages(folderId, imageId, json);
				}
			}
		);
	}

	protected void applyImages(String folderId, String selectedImageId,  JSONObject json) {		
		
		JSONObject jsonObj = JSONHelper.getObject(json, "data");

		totalImagesCount = JSONHelper.getInteger(jsonObj, "totalImagesCount");
		prevImageId = JSONHelper.getString(jsonObj, "prevImageId");
		nextImageId = JSONHelper.getString(jsonObj, "nextImageId");
		startIndex = JSONHelper.getPrimitiveInt(jsonObj, "startIndex");
		
		// parse images
		List<ClientImage> images = JSONHelper.getArray(jsonObj, "images", new ValueParser<ClientImage>() {
			@Override
			public ClientImage parse(JSONValue jsonValue) {
				JSONObject json = jsonValue.isObject();
				String id = JSONHelper.getString(json, "id");
				return new ClientImage(id);
			}
		});
		if (GWTUtils.isEmpty(images)) {
			Window.alert("Failed to load images list");
			return;
		}
		loadedImages.clear();
		loadedImages.addAll(images);


		// parse folder
		JSONObject folderJson = JSONHelper.getObject(jsonObj, "folder");
		if (folderJson == null) {
			Window.alert("Failed to get images list!");
			return;
		}
		String folderCaption = JSONHelper.getString(folderJson, "caption");
		long folderSize = JSONHelper.getPrimitiveLong(json, "folderSize");
		int imagesCount = JSONHelper.getPrimitiveInt(json, "imagesCount");
		selectedFolder = new ClientFolder(folderId, folderCaption, null, folderSize, imagesCount);

		if (GWTUtils.isEmpty(selectedImageId)) {
			selectedImageId = loadedImages.get(0).getId();
			History.newItem(folderId + "/" + selectedImageId, false);
		}

		// paint folder, big photo, small images
		showImages(selectedFolder, selectedImageId, images, true);
	}
	
	protected void showImages(ClientFolder folder, final String selectedImageId,  List<ClientImage> images, boolean repaintImagesList) {	
		
		if (GWTUtils.isEmpty(selectedImageId)) {
			Window.alert("Navigation error");
			return;
		}

		String folderId = folder.getId();
		
		// set folder name
		if (repaintImagesList) {
			elFolder.setInnerHTML(GWTUtils.safeString(folder.getCaption()));
			elCount.setInnerHTML(Integer.toString(totalImagesCount));
			String s = startIndex + " - " + (startIndex + loadedImages.size() - 1);
			elCountDetails.setInnerHTML(s);
		}

		// set big photo
		clearBigPhotoWrapper();
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='bf-iw'>");
			sb.append("<div uid='" + UID_ROTATE_LEFT + "' class='bf-act bf-act-rl'></div>");
			sb.append("<div uid='" + UID_ROTATE_RIGHT + "' class='bf-act bf-act-rr'></div>");
			sb.append("<img id='big-" + selectedImageId + "' src='" + GalleryClientUtils.genMediumImageSrc(folderId, selectedImageId) + "'></img>");
		sb.append("</div>");
		sb.append("<div class='bf-down-w'>");
		sb.append("<a class='bf-down' href='" + GalleryClientUtils.genLargeImageSrc(folderId, selectedImageId) + "'>скачать в полном размере</a>");
		sb.append("</div>");
		final String bigPhotoHtml = sb.toString();
		new Timer() {
			@Override
			public void run() {
				showBigPhotoWrapper(bigPhotoHtml);
				GalleryPanel.this.selectedImageId = selectedImageId;
			}
		}.schedule(100);

		// show small images list
		if (repaintImagesList) {
			String smallImagesHtml = "";
			for (int i = 0; i < images.size(); i++) {
				ClientImage image = images.get(i);
				boolean navigateLeft = i == 0;
				boolean navigateRight = i == images.size() - 1;
				boolean last = navigateRight;
				smallImagesHtml += genSmallImageHtml(folderId, image, last, navigateLeft, navigateRight);
			}
			setSmallPhotosHtml("", true);
			elBigPhotoW.removeClassName(CSS_SMALL_PHOTOS_ANIME);
			final String resHtml = smallImagesHtml;
			new Timer() {
				@Override
				public void run() {
					setSmallPhotosHtml(resHtml, true);
					elSmallPhotosW.addClassName(CSS_SMALL_PHOTOS_ANIME);
					updateSelectedImageAndNavigation(selectedImageId);
				}
			}.schedule(100);
		} else {
			updateSelectedImageAndNavigation(selectedImageId);
		}
	}
	
	private void updateSelectedImageAndNavigation(String selectedImageId) {
		// mark selected images in small images list
		updateImageAsSelected(selectedImageId);

		// update navigation
		com.google.gwt.user.client.Element elNext = DOM.getElementById(ID_NEXT_SMALL);
		if(elNext != null) {
			if(GWTUtils.isEmpty(nextImageId)) {
				elNext.addClassName(CSS_ARROW_OFF);
			} else {
				elNext.removeClassName(CSS_ARROW_OFF);
			}
		}
		com.google.gwt.user.client.Element elPrev = DOM.getElementById(ID_PREV_SMALL);
		if(elPrev != null) {
			if(GWTUtils.isEmpty(prevImageId)) {
				elPrev.addClassName(CSS_ARROW_OFF);
			} else {
				elPrev.removeClassName(CSS_ARROW_OFF);
			}
		}				
	}
	
	private void clearImagesState() {
		selectedFolder = null;
		loadedImages.clear();
		totalImagesCount = 0;
		nextImageId = null;
		prevImageId = null;
		startIndex = 0;
		selectedImageId = null;

		// clear and hide big photo wrapper
		clearBigPhotoWrapper();
		elFolder.setInnerHTML("");
		elCount.setInnerHTML("");
		elCountDetails.setInnerHTML("");
	}

	private void updateImageAsSelected(String imageToSelect) {
		for (ClientImage im : loadedImages) {
			if (im.getId().equals(imageToSelect)) {
				markImageAsSelected(im.getId(), true);
			} else {
				markImageAsSelected(im.getId(), false);
			}
		}
	}

	private void markImageAsSelected(String imageId, boolean selected) {
		com.google.gwt.user.client.Element el = DOM
				.getElementById(genSmallImageWrapperId(imageId));
		if(el == null) {
			return;
		}
		if(selected) {
			el.addClassName(CSS_ACTIVE_SMALL);
		} else {
			el.removeClassName(CSS_ACTIVE_SMALL);
		}

	}

	private String genSmallImageHtml(String folderId, ClientImage image, boolean last, boolean navigateLeft, boolean navigateRight) {
		String dq = new DataQueryJsonBuilder()
			.add("imageId", image.getId())
			.add("folderId", folderId)
			.toDataQuery();
		String uidAndDq = GWTUtils.genUid(UID_SMALL_IMAGE_CLICK) + " " + dq;

		String navigateCustomCss = navigateLeft ? "arrow-left" : "arrow-right";
		String navigateUid = navigateLeft ? UID_SMALL_LEFT : UID_SMALL_RIGHT;
		String navigateId = navigateLeft ? ID_PREV_SMALL : ID_NEXT_SMALL;

		String id = genSmallImageWrapperId(image.getId());
		String itemCustomCss = last ? "sf-item-last" : "";

		String html = "";
		html += "<li class='sf-item " + itemCustomCss + "' " + uidAndDq + ">";
			html += "<div id='" + id + "' class='sf-img-w'>";
				if(navigateLeft || navigateRight) {
				html += "<div id='" + navigateId + "' class='arrow " + navigateCustomCss + "' " + GWTUtils.genUid(navigateUid) + ">";
					html += "<div class='arrow-point arrow-point-1'></div>";
					html += "<div class='arrow-point arrow-point-2'></div>";
					html += "<div class='arrow-point arrow-point-3'></div>";
					html += "<div class='arrow-point arrow-point-4'></div>";
					html += "<div class='arrow-point arrow-point-5'></div>";
					html += "<div class='arrow-point arrow-point-6'></div>";
					html += "<div class='arrow-point arrow-point-7'></div>";
					html += "<div class='arrow-point arrow-point-8'></div>";
					html += "<div class='arrow-point arrow-point-9'></div>";
				html += "</div>";	
				}	
				html += "<img id='" + "small-" + image.getId() + "' src='" + GalleryClientUtils.genSmallImageSrc(folderId, image.getId()) + "'></img>";				
			html += "</div>";
		html += "</li>";
		return html;
	}

	private String genSmallImageWrapperId(String imageId) {
		return "sfwid-" + imageId;
	}

	private String genFolderImage(String imageId, String folderId, int position, int zindex) {
		String className = "fld-i fld-i__" + position;
		String src = GalleryClientUtils.genSmallImageSrc(folderId, imageId);
		return "<div class='" + className + "' style='z-index:" + zindex + ";'><img src='" + src + "'></img></div>";
	}
	
	public void shuffle(List<?> list) {
		int size = list.size();
		int SHUFFLE_THRESHOLD = 5;
		if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
			for (int i = size; i > 1; i--)
				Collections.swap(list, i - 1, Random.nextInt(i));
		} else {
			Object arr[] = list.toArray();

			// Shuffle array
			for (int i = size; i > 1; i--)
				swap(arr, i - 1, Random.nextInt(i));

            // Dump array back into list
			ListIterator it = list.listIterator();
			for (int i = 0; i < arr.length; i++) {
				it.next();
				it.set(arr[i]);
			}
		}
	}

	private static void swap(Object[] arr, int i, int j) {
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}

	private String genFolderHtml(ClientFolder folder) {

		List<Integer> zindexes = Arrays.asList(21, 22, 23, 24);
		shuffle(zindexes);

		String folderId = folder.getId();
		
		String imagesHtml = "";
		List<String> images = folder.getRandomImagesList();
		if (!GWTUtils.isEmpty(images)) {
			int size = Math.min(4, images.size());
			for (int i = 0; i < size; i++) {
				imagesHtml += genFolderImage(images.get(i), folderId, i + 1, zindexes.get(i));
			}
		}

		String dq = new DataQueryJsonBuilder().add("folderId", folderId).toDataQuery();
		String uidAndDq = GWTUtils.genUid(UID_FOLDER_CLICK) + " " + dq;
		String html = "";
		html += "<li class='sf-item' " + uidAndDq + ">";
			html += "<div class='fld'>";
				html += "<div class='fld-ic'>" + folder.getImagesCount() + "</div>";
				html += "<div class='fld-cw'></div>";
				html += "<div class='fld-c'>" + GWTUtils.safeString(folder.getCaption()) + "</div>";				
				html += imagesHtml;
			html += "</div>";
		html += "</li>";
		return html;
	}

	private void processClickEvent(String uid, JSONObject json) {
		if (UID_FOLDER_CLICK.equals(uid)) {
			String folderId = JSONHelper.getString(json, "folderId");
			History.newItem(folderId);
		} else if (UID_CAPTION_CLICK.equals(uid)) {
			History.newItem("");
		} else if (UID_SMALL_IMAGE_CLICK.equals(uid)) {
			String folderId = JSONHelper.getString(json, "folderId");
			String imageId = JSONHelper.getString(json, "imageId");
			History.newItem(folderId + "/" + imageId);

			FullScreenHelper.startFullscreen(elBigPhotoW);

		} else if (UID_SMALL_LEFT.equals(uid)) {
			if (selectedFolder == null || GWTUtils.isEmpty(prevImageId)) {
				Window.alert("No more images");
			} else {
				loadSmallImages(selectedFolder.getId(), prevImageId, false);
				History.newItem(selectedFolder.getId() + "/" + prevImageId, false);
			}
		} else if (UID_SMALL_RIGHT.equals(uid)) {
			if (selectedFolder == null || GWTUtils.isEmpty(nextImageId)) {
				Window.alert("No more images");
			} else {
				loadSmallImages(selectedFolder.getId(), nextImageId, true);
				History.newItem(selectedFolder.getId() + "/" + nextImageId, false);
			}
		} else if (UID_LOGIN.equals(uid)) {
			login();
		} else if (UID_LOGOUT.equals(uid)) {
			logout();
		} else if (UID_ROTATE_LEFT.equals(uid)) {
			rotate(true);
		} else if (UID_ROTATE_RIGHT.equals(uid)) {
			rotate(false);
		}
	}

	private void showBigPhotoWrapper(String html) {
		elBigPhotoW.setInnerHTML(html);
		elBigPhotoW.addClassName(CSS_BIG_PHOTO_ON);
	}

	private void clearBigPhotoWrapper() {
		elBigPhotoW.setInnerHTML("");
		elBigPhotoW.removeClassName(CSS_BIG_PHOTO_ON);
	}
	
	private void loadWelcomeScreen() {
		new AjaxRequest(Actions.WELCOME).send(new AjaxCallback() {
			@Override
			public void onResponse(JSONObject json) {
				applyWelcomeScreen(json);
			}
		});
	}

	private void applyWelcomeScreen(JSONObject json) {

		// clears all variables and UI for images list state
		clearImagesState();

		JSONObject jsonObj = JSONHelper.getObject(json, "data");

		// paint folders list
		List<ClientFolder> folders = JSONHelper.getArray(jsonObj, "folders", new ValueParser<ClientFolder>() {
			@Override
			public ClientFolder parse(JSONValue jsonValue) {
				JSONObject json = jsonValue.isObject();
				String caption = JSONHelper.getString(json, "caption");
				String id = JSONHelper.getString(json, "id");
				List<String> randomImagesIds = JSONHelper.getStrings(json, "images");
				long folderSize = JSONHelper.getPrimitiveLong(json, "folderSize");
				int imagesCount = JSONHelper.getPrimitiveInt(json, "imagesCount");

				ClientFolder f = new ClientFolder(id, caption, randomImagesIds, folderSize, imagesCount);
				return f;
			}
		});
		String html = "";
		for (ClientFolder folder : folders) {
			html += genFolderHtml(folder);
		}
		setSmallPhotosHtml(html, false);
	}

	private void setSmallPhotosHtml(String html, boolean center) {
		if (center) {
			elSmallPhotosW.addClassName("sf-w__center");
		} else {
			elSmallPhotosW.removeClassName("sf-w__center");
		}
		elSmallPhotosW.setInnerHTML(html);
	}

	private void navigateRight() {
		if (selectedFolder == null || selectedImageId == null) {
			return;
		}
		int idx = -1;
		for (int i = 0; i < loadedImages.size(); i++) {
			if (loadedImages.get(i).getId().equals(selectedImageId)) {
				idx = i;
				break;
			}
		}
		if (idx == -1) {
			return;
		}

		if (idx == loadedImages.size() - 1) {
			// this is last image
			if (!GWTUtils.isEmpty(nextImageId)) {
				// show next chunk
				loadSmallImages(selectedFolder.getId(), nextImageId, true);
				History.newItem(selectedFolder.getId() + "/" + nextImageId, false);
			}
		} else {
			String imageIdToSelect = loadedImages.get(idx + 1).getId();
			History.newItem(selectedFolder.getId() + "/" + imageIdToSelect);
		}
	}

	private void navigateLeft() {
		if (selectedFolder == null || selectedImageId == null) {
			return;
		}
		int idx = -1;
		for (int i = 0; i < loadedImages.size(); i++) {
			if (loadedImages.get(i).getId().equals(selectedImageId)) {
				idx = i;
				break;
			}
		}
		if (idx == -1) {
			return;
		}

		if (idx == 0) {
			// this is first image
			if (!GWTUtils.isEmpty(prevImageId)) {
				// show prev chunk
				loadSmallImages(selectedFolder.getId(), prevImageId, false);
				History.newItem(selectedFolder.getId() + "/" + prevImageId, false);
			}
		} else {
			String imageIdToSelect = loadedImages.get(idx - 1).getId();
			History.newItem(selectedFolder.getId() + "/" + imageIdToSelect);
		}
	}

	private void login() {
		final String name = elName.getValue().trim();
		String pass = elPass.getValue().trim();
		elPass.setValue("");
		if (GWTUtils.isEmpty(name) || GWTUtils.isEmpty(pass)) {
			return;
		}
		new AjaxRequest(Actions.LOGIN)
			.addParam("name", name)
			.addParam("pass", pass)
			.send(new AjaxCallback() {
				@Override
				public void onResponse(JSONObject json) {
					applyLogin(name, json);
				}
		});

	}

	private void applyLogin(String name, JSONObject json) {
		JSONObject jsonObj = JSONHelper.getObject(json, "data");
		boolean found = JSONHelper.getPrimitiveBoolean(jsonObj, "found", false);
		if (!found) {
			Window.alert("Пользователь " + GWTUtils.safeString(name) + " не найден!");
		} else {
			elName.setValue("");
			setCurrentUser(name);

			Window.Location.reload();
		}
	}

	private void logout() {
		new AjaxRequest(Actions.LOGOUT).send(new AjaxCallback() {
			@Override
			public void onResponse(JSONObject json) {
				setCurrentUser(null);
				Window.Location.reload();
			}
		});
	}

	private void setCurrentUser(String loggedInUser) {
		if (GWTUtils.isEmpty(loggedInUser)) {
			elLogged.setInnerHTML("");
			elLoggedWrapper.getStyle().setDisplay(Display.NONE);
			elLoginWrapper.getStyle().setDisplay(Display.BLOCK);
		} else {
			elLogged.setInnerHTML(GWTUtils.safeString(loggedInUser));
			elLoggedWrapper.getStyle().setDisplay(Display.BLOCK);
			elLoginWrapper.getStyle().setDisplay(Display.NONE);
		}
	}

	private void rotate(boolean left) {
		new AjaxRequest(Actions.ROTATE)
			.addParam("imageId", selectedImageId)
			.addParam("folderId", selectedFolder.getId())
			.addParam("direction", left ? "left" : "right")
			.send(new AjaxCallback() {
				@Override
			    public void onResponse(JSONObject json) {
				    ImageElement el = DOM.getElementById("big-" + selectedImageId).cast();
				    String src = el.getSrc();
				    el.setSrc(src + "&time=" + System.currentTimeMillis());
					
					ImageElement el2 = DOM.getElementById("small-" + selectedImageId).cast();
					String src2 = el2.getSrc();
					el2.setSrc(src2  + "&time=" + System.currentTimeMillis());
				}
		});

	}

}