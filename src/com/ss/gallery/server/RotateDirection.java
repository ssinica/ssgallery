package com.ss.gallery.server;

public enum RotateDirection {

	RIGHT(90), LEFT(-90);

	private int angle;

	private RotateDirection(int angle) {
		this.angle = angle;
	}

	public int getAngle() {
		return angle;
	}

}
