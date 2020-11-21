package com.sioeye.youle.common.zuul.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.sioeye.youle.common.zuul.model.AuthBean;

@Component
@ConfigurationProperties(prefix = "disney.gateway")
public class AuthProperties {

	private List<AuthBean> authList = new ArrayList<AuthBean>();

	public List<AuthBean> getAuthList() {
		return authList;
	}

	public void setAuthList(List<AuthBean> authList) {
		this.authList = authList;
	}

}