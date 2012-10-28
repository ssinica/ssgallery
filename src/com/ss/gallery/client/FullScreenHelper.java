package com.ss.gallery.client;

import com.google.gwt.dom.client.Element;

public class FullScreenHelper {

	public static void startFullscreen(Element div) {
		try {
			startFullscreenWebkit(div);
		} catch (Exception e) {
			try {
				startFullscreenMozilla(div);
			} catch (Exception e2) {
				try {
					startFullscreenOpera(div);
				} catch (Exception e3) {
					throw new RuntimeException("Failed to start fullscreen", e3);
				}
			}
		}
	}

	private static native void startFullscreenWebkit(Element div) /*-{
		div.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
	}-*/;

	private static native void startFullscreenMozilla(Element div) /*-{
		div.mozRequestFullScreen();
	}-*/;

	private static native void startFullscreenOpera(Element div) /*-{
		div.requestFullscreen();
	}-*/;

}
