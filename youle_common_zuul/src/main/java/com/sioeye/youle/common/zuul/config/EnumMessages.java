package com.sioeye.youle.common.zuul.config;

/**
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */
public enum EnumMessages {

	SIGN_VERIFICATION_FAIL(1001, "Signature verification failure.", "签名验证失败."),
	SERVERIP_VERIFICATION_FAIL(1002, "ServerIP verification failed. ", "服务ip地址验证失败."),
	USER_AUTH_FAIL(1003, "user authentication failed.", "用户授权失败."),
	CAMERA_AUTH_FAIL(1004, "camera authentication failed.", "相机授权失败."),
	INVALID_AUTH_TYPE(1005, "Invalid authentication type.", "不支持的授权认证类型."),
	OTHERCOMPANY_TYPE(1006, "Do not support other company x_youle_type.", "不支持其他公司的type类型."),
	INVALID_TYPE(1007, "Invalid x_youle_type", "无效的type类型."),
	VERIFY_HANDLER_ERROR(1008, "Verify handler errory", "内部校验处理错误."),
	REQUEST_LIMIT_COUNT(1009,"Number of requests exceeded","超过请求限制次数."),
	ZUUL_SYSTEM_ERROR(1999, "Zuul System requested is busy or could not be reached.", "网关系统忙或者服务不可达.");

	private Integer code;
	private String message;
	private String cnmessage;// 中文描述

	EnumMessages(Integer code, String message, String cnmessage) {
		this.code = code;
		this.message = message;
		this.cnmessage = cnmessage;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
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

	/**
	 * @api {GET} http://localhost 错误码列表(mvp部分功能)
	 * @apiName showErrorMessage
	 * @apiGroup youle_common_zuul
	 * @apiVersion 0.0.1
	 * @apiSuccessExample {json} zuul模块错误码列表mvp相关功能: {
	SIGN_VERIFICATION_FAIL(1001, "Signature verification failure.", "签名验证失败."),
	SERVERIP_VERIFICATION_FAIL(1002, "ServerIP verification failed. ", "服务ip地址验证失败."),
	USER_AUTH_FAIL(1003, "user authentication failed.", "用户授权失败."),
	CAMERA_AUTH_FAIL(1004, "camera authentication failed.", "相机授权失败."),
	INVALID_AUTH_TYPE(1005, "Invalid authentication type.", "不支持的授权认证类型."),
	OTHERCOMPANY_TYPE(1006, "Do not support other company x_youle_type.", "不支持其他公司的type类型."),
	INVALID_TYPE(1007, "Invalid x_youle_type", "无效的type类型."),
	VERIFY_HANDLER_ERROR(1008, "Verify handler errory", "内部校验处理错误."),
	REQUEST_LIMIT_COUNT(1009,"Number of requests exceeded","超过请求限制次数."),
	ZUUL_SYSTEM_ERROR(1999, "Zuul System requested is busy or could not be reached.", "网关系统忙或者服务不可达.");
 
	 *}
	 */
	public String showErrorMessage() {
		return "ErrorMessage";
	}

}