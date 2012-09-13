package com.ss.gallery.client;

public class DataQueryJsonBuilder {

    private String json = "";

    public DataQueryJsonBuilder() {

    }

    public DataQueryJsonBuilder add(String param, Object value) {
        if (value == null) {
            return this;
        }

        String str = "\"" + param + "\":";

        if (value instanceof String) {
            str += "\"" + ((String) value) + "\"";
        } else if (value instanceof Boolean) {
            str += ((Boolean) value) ? "true" : "false";
        } else if (value instanceof Long) {
            str += "\"" + value.toString() + "\"";
        } else if (value instanceof Integer || value instanceof Double) {
            str += value.toString();
        } else {
            str += "\"" + value.toString() + "\"";
        }

		if (!GWTUtils.isEmpty(json)) {
            json += ",";
        }
        json += str;

        return this;
    }

    public String toJson() {
        return json;
    }

    public String toDataQuery() {
		return GWTUtils.getDataQueryJson(json);
    }
}
