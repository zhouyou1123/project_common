package com.sioeye.youle.common.zuul.model;

import static ch.qos.logback.access.AccessConstants.LB_INPUT_BUFFER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import ch.qos.logback.access.servlet.Util;

public class SioeyeHttpServletRequest extends HttpServletRequestWrapper {

	private SioeyeServletInputStream inStream;
	private BufferedReader reader;
	boolean postedParametersMode = false;

	public SioeyeHttpServletRequest(HttpServletRequest request) {
		super(request);
		// we can't access the input stream and access the request parameters
		// at the same time
		if (Util.isFormUrlEncoded(request)) {
			postedParametersMode = true;
		} else {
			inStream = new SioeyeServletInputStream(request);
			// add the contents of the input buffer as an attribute of the
			// request
			if (inStream.getInputBuffer() == null || inStream.getInputBuffer().length == 0) {
				request.setAttribute(LB_INPUT_BUFFER, "\"\"".getBytes());
			} else {
				request.setAttribute(LB_INPUT_BUFFER, inStream.getInputBuffer());
			}
			reader = new BufferedReader(new InputStreamReader(inStream));
		}
	}

	public byte[] getInputBuffer() {
		if (postedParametersMode) {
			throw new IllegalStateException("Call disallowed in postedParametersMode");
		}
		return inStream.getInputBuffer();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (!postedParametersMode) {
			return inStream;
		} else {
			return super.getInputStream();
		}
	}

	@Override
	public BufferedReader getReader() throws IOException {
		if (!postedParametersMode) {
			return reader;
		} else {
			return super.getReader();
		}
	}

	public boolean isPostedParametersMode() {
		return postedParametersMode;
	}
}
