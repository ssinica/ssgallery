package com.ss.gallery.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;

public class JSONHelper {

	public static JSONObject getJsonFromString(String str) {
		if (str == null || "".equals(str)) {
			return null;
		}
		JSONValue value = JSONParser.parseStrict(str);
		if (value == null) {
			return null;
		}
		return value.isObject();
	}

	public static JSONObject getJsonFromElement(String id) {
		JSONValue value = getJsonValueFromElement(id);
		if (value == null) {
			return null;
		}
		JSONObject obj = value.isObject();
		return obj;
	}

	public static JSONValue getJsonValueFromElement(String id) {
		Element el = DOM.getElementById(id);
		if (el == null) {
			return null;
		}
		String json = el.getInnerHTML();
		if (json == null || "".equals(json)) {
			return null;
		}
		return JSONParser.parseStrict(json);
	}

	public static String getString(JSONObject json, String param) {
		if (json == null || param == null) {
			return null;
		}
		JSONValue value = json.get(param);
		if (value == null) {
			return null;
		}
		JSONString s = value.isString();
		if (s == null) {
			return null;
		}
		return s.stringValue();
	}

	public static JSONObject getObject(JSONObject json, String param) {
		if (json == null || param == null) {
			return null;
		}
		JSONValue value = json.get(param);
		if (value == null) {
			return null;
		}
		return value.isObject();
	}

	public static boolean getPrimitiveBoolean(JSONObject json, String param) {
		return getPrimitiveBoolean(json, param, false);
	}

	public static boolean getPrimitiveBoolean(JSONObject json, String param,
			boolean defaultValue) {
		if (json == null || param == null) {
			return defaultValue;
		}
		JSONBoolean value = (JSONBoolean) json.get(param);
		if (value == null) {
			return defaultValue;
		}
		return value.booleanValue();
	}

	public static long getPrimitiveLong(JSONObject json, String param) {
		Long value = getLong(json, param);
		return value == null ? 0L : value.longValue();
	}

	public static int getPrimitiveInt(JSONObject json, String param) {
		if (json == null || param == null) {
			return -1;
		}
		JSONNumber value = (JSONNumber) json.get(param);
		if (value == null) {
			return -1;
		}
		double d = value.doubleValue();
		return Double.valueOf(d).intValue();
	}

	public static Integer getInteger(JSONObject json, String param) {
		if (json == null || param == null) {
			return null;
		}
		JSONNumber value = (JSONNumber) json.get(param);
		if (value == null) {
			return null;
		}
		double d = value.doubleValue();
		int v = Double.valueOf(d).intValue();
		return Integer.valueOf(v);
	}

	public static List<Integer> getIntegers(JSONObject json, String param) {
		List<Integer> res = getArray(json, param, new ValueParser<Integer>() {
			@Override
			public Integer parse(JSONValue json) {
				JSONNumber value = json.isNumber();
				if (value == null) {
					return null;
				}
				double d = value.doubleValue();
				int v = Double.valueOf(d).intValue();
				return Integer.valueOf(v);
			}
		});
		return res;
	}

	public static List<String> getStrings(JSONObject json, String param) {
		List<String> res = getArray(json, param, new ValueParser<String>() {
			@Override
			public String parse(JSONValue json) {
				JSONString s = json.isString();
				if (s == null) {
					return null;
				}
				return s.stringValue();
			}
		});
		return res;
	}

	public static Long getLong(JSONObject json, String param) {
		String value = getString(json, param);
		if (value == null || "".equals(value)) {
			return null;
		}
		try {
			return Long.valueOf(value);
		} catch (Exception e) {
			return null;
		}
	}

	public static List<Long> getLongs(JSONObject json, String param) {
		List<Long> res = getArray(json, param, new ValueParser<Long>() {
			@Override
			public Long parse(JSONValue json) {
				JSONString stringValue = json.isString();
				if (stringValue == null) {
					return null;
				}
				String value = stringValue.stringValue();
				if (value == null || "".equals(value)) {
					return null;
				}
				try {
					return Long.valueOf(value);
				} catch (Exception e) {
					return null;
				}
			}
		});
		return res;
	}

	public static <RESULT> List<RESULT> getArray(JSONObject json, String param,
			ValueParser<RESULT> valueParser) {
		List<RESULT> res = new ArrayList<RESULT>();
		JSONValue value = json.get(param);
		if (value == null) {
			return res;
		}
		JSONArray array = value.isArray();
		if (array == null || array.size() == 0) {
			return res;
		}
		for (int i = 0; i < array.size(); i++) {
			JSONValue item = array.get(i);
			if (item != null) {
				RESULT r = valueParser.parse(item);
				if (r != null) {
					res.add(r);
				}
			}
		}
		return res;
	}

	public static <RESULT> List<RESULT> getArray(JSONValue json,
			ValueParser<RESULT> valueParser) {
		List<RESULT> res = new ArrayList<RESULT>();
		JSONArray array = json.isArray();
		if (array == null || array.size() == 0) {
			return res;
		}
		for (int i = 0; i < array.size(); i++) {
			JSONValue item = array.get(i);
			if (item != null) {
				RESULT r = valueParser.parse(item);
				if (r != null) {
					res.add(r);
				}
			}
		}
		return res;
	}

	public static interface ValueParser<RESULT> {
		RESULT parse(JSONValue json);
	}
}
