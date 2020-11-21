package com.sioeye.youle.common.zuul.config;

/**
 * 针对后台管理的错误码
 * @author jinze.yuan
 * @email  jinze.yuan@sioeye.com
 * 
 */
public enum EnumMessagesDevice {

	DEVICE_TOKEN_ID_INVALID("110501","devicetoken corresponding objectid is disabled.","设备被禁用."),
	DEVICE_TOKEN_INVALID("110502","devicetoken invalid.","设备token无效."),
	DEVICE_TOKEN_EXPIRED("110503","devicetoken expired,please refresh the interface to get the new token.","设备token过期,请调刷新接口获取新的token."),
	DEVICE_TOKEN_NOACCESS_TO_URL("110504","devicetoken  no authority access this request uri.","设备账号没有权限访问该请求url."),
	DEVICE_TOKEN_CHANGEPARK("110505","devicetoken park is modified, please login again.","设备账号已更换游乐园，请重新登录."),
	DEVICE_TOKEN_SINGLEPOINT("110506","devicetoken single sign on another device.","设备账号已在另一个设备上单点登录."),
	DEVICE_TOKEN_NOTNULL("110507","devicetoken is not null.","设备Token不能为空.");

	private String code;
	private String message;
	private String cnmessage;// 中文描述

	EnumMessagesDevice(String code, String message, String cnmessage) {
		this.code = code;
		this.message = message;
		this.cnmessage = cnmessage;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCnmessage() {
		return cnmessage;
	}

	public void setCnmessage(String cnmessage) {
		this.cnmessage = cnmessage;
	}


	public String showErrorMessage() {
		return "ErrorMessage";
	}

}