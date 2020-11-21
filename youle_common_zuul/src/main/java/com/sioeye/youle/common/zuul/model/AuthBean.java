package com.sioeye.youle.common.zuul.model;

import java.util.List;

/**
 * Rate limit policy definition
 * @author will.song
 *
 */
public class AuthBean {
	
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}


    
	public List<String> getUri() {
		return uri;
	}

	public void setUri(List<String> uri) {
		this.uri = uri;
	}


	// auth typ
	private String type;
	// 
	private List<String> uri;
	


}
