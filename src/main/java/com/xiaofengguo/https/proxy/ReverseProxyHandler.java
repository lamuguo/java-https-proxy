// Copyright 2011 Google Inc. All Rights Reserved.

package com.xiaofengguo.https.proxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mortbay.jetty.servlet.ServletHandler;

import com.google.common.base.Joiner;

/**
 * @author xiaofengguo@google.com (Xiaofeng Guo)
 * 
 */
public class ReverseProxyHandler extends ServletHandler {
	private static final Logger LOG = Logger
			.getLogger(ReverseProxyHandler.class.getCanonicalName());

	private static final int DEFAULT_BUF_SIZE = 256 * 1024;

	public ReverseProxyHandler() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mortbay.jetty.Handler#handle(java.lang.String,
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, int)
	 */
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, int dispatch) throws IOException,
			ServletException {
		String urlString = "http://hudson.xiaofengguo.com:8080"
				+ request.getRequestURI();
		if (request.getQueryString() != null) {
			urlString += "?" + request.getQueryString();
		}
		HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				// connection.setRequestProperty(cookie.getName(),
				// cookie.getValue());
				LOG.info("Write cookie " + cookie.getName() + ", "
						+ cookie.getValue());
			}
		}
		
		for (String headerName : Collections.list((Enumeration<String>) request.getHeaderNames())) {
			connection.setRequestProperty(headerName,
					request.getHeader(headerName));
			LOG.info("Write header (" + headerName + ", "
					+ request.getHeader(headerName) + ")");
		}
		connection.setRequestMethod(request.getMethod());
		connection.setDoOutput(true);

		LOG.info("Url = " + connection.getURL());
		OutputStream output = connection.getOutputStream();
		IOUtils.copy(request.getInputStream(), output);
		output.close();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IOUtils.copy(connection.getInputStream(), out);
		byte[] bytes = out.toByteArray();
		out.close();

//		LOG.info("Output = " + new String(bytes));
		response.setContentLength(bytes.length);
		response.setStatus(connection.getResponseCode());
//		int i = 0;
//		String key = null;
//		while ((key = connection.getHeaderFieldKey(i)) != null) {
//			String value = connection.getHeaderField(i);
//			response.setHeader(key, value);
//			LOG.info("i = " + i + ") Dump header field : (" + key + ", " + value + ")");
//			++i;
//		}
		LOG.info("Header field map: " + connection.getHeaderFields());
 		for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
			String key = entry.getKey();
			String value = StringUtils.join(entry.getValue(), ";");
			if (key == null || value == null) {
				continue;
			}
			response.setHeader(key, value);
		}
		response.getOutputStream().write(bytes);
	}
}
