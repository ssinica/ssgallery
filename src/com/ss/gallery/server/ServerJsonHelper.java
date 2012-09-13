package com.ss.gallery.server;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.LongSerializationPolicy;

public class ServerJsonHelper {

	private static final Log log = LogFactory.getLog(ServerJsonHelper.class);

	private static final Gson GSON = new GsonBuilder()
			.setLongSerializationPolicy(LongSerializationPolicy.STRING)
			.create();

	private ServerJsonHelper() {

	}

	public static String toJson(Object obj) {
		if (obj == null) {
			return "{}";
		}
		return GSON.toJson(obj);
	}

	public static JsonObject parseJsonObject(String jsonString) {
		JsonElement jsonElement = null;
		try {
			jsonElement = new JsonParser().parse(jsonString);
		} catch (Exception e) {
			log.error("Failed to parse json!", e);
			return null;
		}
		if (jsonElement == null) {
			log.error("request json is null!");
			return null;
		}
		JsonObject json = null;
		try {
			json = jsonElement.getAsJsonObject();
		} catch (Exception e) {
			log.error("Failed to parse json object", e);
			return null;
		}
		if (json == null) {
			log.error("json object is null!");
			return null;
		}
		return json;
	}

	public static String getString(String param, JsonObject json) {
		if (json == null || StringUtils.isEmpty(param)) {
			return "";
		}
		JsonElement member = json.get(param);
		if (member == null) {
			return "";
		}
		return member.getAsString();
	}

}
