package com.ss.gallery.server;

public enum UserRole {
	ADMIN("admin");

	private String value;

	private UserRole(String role) {
		this.value = role;
	}

	public String getValue() {
		return value;
	}

	public static UserRole getByRole(String role) {
		for (UserRole r : values()) {
			if (r.getValue().equals(role)) {
				return r;
			}
		}
		return null;
	}
}
