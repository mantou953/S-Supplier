package com.xbz.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebUtils {

	public static void sendData(HttpServletRequest request, HttpServletResponse response, String data, String contentType, String charset) {
		try {
			response.setContentType(contentType);
			response.setCharacterEncoding(charset);
			response.getWriter().write(data);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static void sendText(HttpServletRequest request, HttpServletResponse response, String data, String charset) {
		sendData(request, response, data, "text/plain;charset=" + charset, charset);
	}

	public static void sendHtml(HttpServletRequest request, HttpServletResponse response, String data, String charset) {
		sendData(request, response, data, "text/html;charset=" + charset, charset);
	}

	public static void sendXML(HttpServletRequest request, HttpServletResponse response, String data, String charset){
		sendData(request, response, data, "text/xml;charset=" + charset, charset);
	}

	public static void sendJSON(HttpServletRequest request, HttpServletResponse response, String data, String charset) {
		sendData(request, response,  data, "application/json;charset=" + charset, charset);
	}

}
