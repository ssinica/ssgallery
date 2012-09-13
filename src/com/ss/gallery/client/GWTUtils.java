package com.ss.gallery.client;

import java.util.Collection;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.Element;

public class GWTUtils {

	private GWTUtils() {
	}

	public static boolean isEmpty(Collection collection) {
		return collection == null || collection.size() == 0;
	}

	public static boolean isEmpty(String value) {
		return value == null || "".equals(value);
	}

	public static String safeString(String html) {
		if (html == null || "".equals(html)) {
			return "";
		}
		SafeHtml safe = SimpleHtmlSanitizer.sanitizeHtml(html);
		return safe.asString();
	}
	
	public static void setDataQueryToElement(Element el, String json) {
        String dataQuery = "{";
        dataQuery += json;
        dataQuery += "}";
        el.setAttribute("data-query", dataQuery);
    }

    public static String getDataQueryJson(String json) {
        String dataQuery = "data-query='{";
        dataQuery += json;
        dataQuery += "}'";
        return dataQuery;
    }

    public static JSONObject getDataQueryJson(Element el) {
        String value = el.getAttribute("data-query");
		if (isEmpty(value)) {
            return null;
        } else {
            return (JSONObject) JSONParser.parse(value);
        }
    }

    public static String genUid(String action) {
        return " uid=\"" + action + "\"";
    }

    public static void setUidToElement(String uid, Element elem) {
        if (elem == null) {
            return;
        }
        elem.setAttribute("uid", uid);
    }

    public static void setUidToElement(String uid, com.google.gwt.dom.client.Element elem) {
        if (elem == null) {
            return;
        }
        elem.setAttribute("uid", uid);
    }

    public static Element findDataQueryElement(Element element) {
        Element elementWithAttr = getElementWithAttr(element, "data-query");
        if (elementWithAttr != null) {
            return elementWithAttr;
        }
        return element;
    }

    public static void removeDataQueryAttribute(Element element) {
        element.removeAttribute("data-query");
    }

    public static void removeUidAttribute(Element element) {
        element.removeAttribute("uid");
    }

    public static Element getElementWithAttr(Element element, String attr) {
        if (element != null && Element.is(element) && isEmpty(element.getAttribute(attr))) {
            com.google.gwt.dom.client.Element parent = element.getParentElement();
            if (parent != null) {
                Element parentEl = parent.cast();
                element = getElementWithAttr(parentEl, attr);
            } else {
                return null;
            }
        }
        return element;
    }


}
