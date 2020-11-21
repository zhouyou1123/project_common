package com.sioeye.youle.common.zuul.config;

/**
 * 针对后台管理的错误码
 * @author jinze.yuan
 * @email  jinze.yuan@sioeye.com
 * 
 */
public enum EnumMessagesAdmin {
    
	ADMIN_TOKEN_ACCOUNT_INVALID("110301","admintoken corresponding account is disabled.","管理员账号被禁用."), 
	ADMIN_TOKEN_INVALID("110302","admintoken invalid.","管理员账号token无效."), 
	ADMIN_TOKEN_EXPIRED("110303","admintoken expired,please refresh the interface to get the new token.","管理员账号token过期,请调刷新接口获取新的token."), 
	ADMIN_TOKEN_NOACCESS_TO_URL("110304","admintoken  no authority access this request uri.","管理员账号没有权限访问该请求url."),
	ADMIN_TOKEN_CHANGEPASSWORD("110305","admintoken password is modified, please login again.","管理员账号密码被修改请重新登录."),
	ADMIN_TOKEN_SINGLEPOINT("110306","admintoken single sign on another device.","管理员账号已在另一个设备上单点登录.");

	private String code;
	private String message;
	private String cnmessage;// 中文描述

	EnumMessagesAdmin(String code, String message, String cnmessage) {
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

	/**
	 * @api {GET} http://localhost 错误码列表(后台管理相关)
	 * @apiName showErrorMessageAdmin
	 * @apiGroup youle_common_zuul
	 * @apiVersion 0.0.1
	 * @apiSuccessExample {json} zuul模块错误码列表主要是后台管理功能web和app: {
	ADMIN_TOKEN_ACCOUNT_INVALID("110301","admintoken corresponding account is disabled.","管理员账号被禁用."), 
	ADMIN_TOKEN_INVALID("110302","admintoken invalid.","管理员账号token无效."), 
	ADMIN_TOKEN_EXPIRED("110303","admintoken expired,please refresh the interface to get the new token.","管理员账号token过期,请调刷新接口获取新的token."), 
	ADMIN_TOKEN_NOACCESS_TO_URL("110304","admintoken  no authority access this request uri.","管理员账号没有权限访问该请求url."),
	ADMIN_TOKEN_CHANGEPASSWORD("110305","admintoken password is modified, please login again.","管理员账号密码被修改请重新登录."),
	ADMIN_TOKEN_SINGLEPOINT("110306","admintoken single sign on another device.","管理员账号已在另一个设备上单点登录.");
     * }
	 * 
	 */
	public String showErrorMessage() {
		return "ErrorMessage";
	}

}