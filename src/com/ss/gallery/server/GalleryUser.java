package com.ss.gallery.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

public class GalleryUser {

	private String name;
	private String pass;
	private Set<UserRole> roles = new HashSet<UserRole>();

	public GalleryUser(String name, String pass, Set<UserRole> roles) {
		this.name = name;
		this.pass = pass;
		if (roles != null) {
			this.roles = roles;
		}
	}

	public boolean hasRole(UserRole role) {
		return roles.contains(role);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPass() {
		return pass;
	}

	public void setPass(String pass) {
		this.pass = pass;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof GalleryUser) {
			GalleryUser casted = (GalleryUser) obj;
			return name.equals(casted.getName());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name: " + name).toString();
	}
}
