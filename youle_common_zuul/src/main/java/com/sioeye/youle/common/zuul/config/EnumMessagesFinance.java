package com.sioeye.youle.common.zuul.config;

/**
 * 针对后台管理的错误码
 * @author jinze.yuan
 * @email  jinze.yuan@sioeye.com
 * 
 */
public enum EnumMessagesFinance {
    
	FINANCE_TOKEN_ACCOUNT_INVALID("110401","finance account is invalid, please retry or contact administrator.","财务账号或登录状态已失效,请重试或联系管理员"), 
	FINANCE_TOKEN_INVALID("110402","financetoken invalid.","财务账号token无效."), 
	FINANCE_TOKEN_EXPIRED("110403","financetoken expired,please refresh the interface to get the new token.","财务账号token过期,请调刷新接口获取新的token."), 
	FINANCE_TOKEN_NOACCESS_TO_URL("110404","financetoken has no authority to access this request uri.","财务账号没有权限访问该请求url."),
	FINANCE_TOKEN_ADRESS_ILIGAL("110405","finance account is used in illegal location.","财务账号在非法区域使用.");

	private String code;
	private String message;
	private String cnmessage;// 中文描述

	EnumMessagesFinance(String code, String message, String cnmessage) {
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