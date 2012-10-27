package com.ss.gallery.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;

public class AjaxRequest implements RequestCallback {

	private JSONObject json;
	private AjaxCallback callback;


	public AjaxRequest(String action) {
		json = new JSONObject();
		json.put("action", new JSONString(action));
	}

	public AjaxRequest addParam(String key, String value) {
		if (GWTUtils.isEmpty(value) || GWTUtils.isEmpty(key)) {
			return this;
		}
		json.put(key, new JSONString(value));
		return this;
	}

	public void send(AjaxCallback callback) {
		this.callback = callback;
		String postData = json.toString();

		RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, "/api");
		rb.setHeader("Content-Type", "application/x-www-form-urlencoded");
		rb.setRequestData("data=" + postData);
		rb.setCallback(this);
		try {
			rb.send();
		} catch (RequestException e) {
			Window.alert("Не достучаться до сервера :(");
		}
	}

	/**
	 * {@code RequestCallback}
	 */
	@Override
	public void onResponseReceived(Request request, Response response) {
		String responseText = response.getText();
		JSONObject data = null;
		try {
			data = JSONHelper.getJsonFromString(responseText);

			JSONObject innerData = JSONHelper.getObject(data, "data");
			if (innerData != null) {
				String error = JSONHelper.getString(innerData, "error");
				if (!GWTUtils.isEmpty(error)) {
					Window.alert(error);
					return;
				}
			}

			callback.onResponse(data);

		} catch (Exception e) {
			Window.alert("Сервер прислал билебирду - ничего не понять :(");
			return;
		}
	}

	/**
	 * {@code RequestCallback}
	 */
	@Override
	public void onError(Request request, Throwable exception) {
		Window.alert("Error: " + exception.getMessage());
	}

	// ------------------------------------------------

	public interface AjaxCallback {
		void onResponse(JSONObject json);
	}

}
