package com.sioeye.youle.common.zuul.filter;

/**
 * 管理员账号生成token字符串里面包含的数据字段信息
 * 
 * @author will.song
 * @email will.song@sioeye.com
 * 
 */
public class VerifyFinanceTokenInfo {
	/**
	 * <pre>
	 * financeId 财务账号在数据库的唯一标识 
	 * code 类型区分(1000,1001,1002)
	 *  
	code	生成token的时候增加一个字段type表示类型区分	
	1000	超级管理员 web,app都可以访问，不做任何拦截
	1001	财务管理员
	1002	财务操作员
	 * </pre>
	 */

	Object financeId = Constants.NODATA;
	Object code = Constants.NODATA;

	private String errorJsonStr = null;// 记录错误信息
	
	public Object getFinanceId() {
		return financeId;
	}

	public void setFinanceId(Object financeId) {
		this.financeId = financeId;
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
