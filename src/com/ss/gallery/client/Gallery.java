package com.ss.gallery.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class Gallery implements EntryPoint {

	private GalleryPanel panel;
	private FocusPanel focusPanel;

	@Override
	public void onModuleLoad() {
		panel = new GalleryPanel();
		focusPanel = new FocusPanel(panel);
		focusPanel.addStyleName("focus-panel");
		focusPanel.addKeyDownHandler(panel);
		RootPanel.get().add(focusPanel);
		Scheduler.get().scheduleFinally(new ScheduledCommand() {
			@Override
			public void execute() {
				focusPanel.setFocus(true);
			}
		});
	}

}
