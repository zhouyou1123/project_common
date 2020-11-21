package com.sioeye.youle.common.zuul.filter;

/**
 * 管理员账号生成token字符串里面包含的数据字段信息
 * 
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */
public class VerifyTokenInfo {
	/**
	 * <pre>
	 * adminId 管理员账号在数据库的唯一标识 
	 * clientId 客户端请求登录后服务端生成的唯一标识客户端的Id值 
	 * clientType客户端请求登录接口的是传的客户端类型(web,app)
	 * parkId 管理员账号对应的游乐园id
	 * code 类型区分(0000,0101,0102,0103,0201,0202,0203)
	 *  
	code	生成token的时候增加一个字段type表示类型区分	
	0000	超级管理员 web,app都可以访问，不做任何拦截
	0101	sioeye账号，运营角色	
	0102	sioeyye账号，运维角色	
	0103	sioeye账号，admin角色	
	0201	游乐园账号，运营角色	
	0202	游乐园账号，运维角色	
	0203	游乐园账号，admin角色	
	
	app： sioeye admin，sioeye运维，游乐园admin，游乐园运维，超级管理员
	web： sioyeye admin，sioeye运营，游乐园admin，游乐园运营，超级管理员
	 * </pre>
	 */
	Object adminId = Constants.NODATA;
	Object clientId = Constants.NODATA;
	Object clientType = Constants.NODATA;
	Object parkId = Constants.NODATA;
	Object code = Constants.NODATA;

	private String errorJsonStr = null;// 记录错误信息

	public Object getAdminId() {
		return adminId;
	}

	public void setAdminId(Object adminId) {
		if (adminId != null)
			this.adminId = adminId;
	}

	public Object getClientId() {
		return clientId;
	}

	public void setClientId(Object clientId) {
		if (clientId != null)
			this.clientId = clientId;
	}

	public Object getClientType() {
		return clientType;
	}

	public void setClientType(Object clientType) {
		if (clientType != null)
			this.clientType = clientType;
	}

	public Object getParkId() {
		return parkId;
	}

	public void setParkId(Object parkId) {
		if (parkId != null)
			this.parkId = parkId;
	}

	public Object getCode() {
		return code;
	}

	public void setCode(Object code) {
		if (code != null)
			this.code = code;
	}

	public String getErrorJsonStr() {
		return errorJsonStr;
	}

	public void setErrorJsonStr(String errorJsonStr) {
		this.errorJsonStr = errorJsonStr;
	}

}
