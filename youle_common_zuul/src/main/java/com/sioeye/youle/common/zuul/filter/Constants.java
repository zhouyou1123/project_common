package com.sioeye.youle.common.zuul.filter;

/**
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 */
public class Constants {
	//see http://wiki.cktcd.net/wiki/Http-headerParams
	// common-----头信息(必须传 sioeye内部调用1,即是外部第三方调用为2)
	public static String X_YOULE_TYPE       = "x_youle_type";

	// baseauth----头信息（签名验证）
	public static String X_YOULE_APPID      = "x_youle_appid"; 
	public static String X_YOULE_APPSIGNKEY = "x_youle_appsignkey"; 

	// userauth----头信息 （小程序用户授权登录后获得的sessiontoken）
	public static String X_YOULE_SESSIONTOKEN = "x_youle_sessiontoken";  
	// thingauth----头信息 （相机唯一imei号）
	public static String X_YOULE_CAMERAIMEI   = "x_youle_cameraimei"; 

	// adminauth----头信息（后台管理-web和app请求 需要添加的请求头)
	public static String X_YOULE_FLAG        = "x_youle_flag"; // 1为后台管理, 2为财务管理
	public static String X_YOULE_ADMINTOKEN  = "x_youle_admintoken";//管理员登录token
	public static String X_YOULE_PARKID      = "x_youle_parkid";//游乐园id
	

	//-----admintoken及financetoken里面保存的字段信息及
	public static String ADMINID     = "adminId";
	public static String FINANCEID     = "financeId";
	public static String CLIENTID    = "clientId";
	public static String CLIENTTYPE  = "clientType";
	public static String PARKID      = "parkId";
	public static String CODE        = "code";

	
	// financeauth----头信息（后台管理-web和app请求 需要添加的请求头)
	public static String X_YOULE_FINANCETOKEN  = "x_youle_financetoken";//财务系统登录token

	//deviceauth ---头信息
	public static String X_YOULE_DEVICETOKEN = "x_youle_devicetoken"; //设备登录token
	public static String X_YOULE_DEVICEIMEI = "x_youle_deviceimei"; //设备imei
	
	//see http://wiki.cktcd.net/wiki/%E7%AE%A1%E7%90%86%E5%91%98%E7%94%A8%E6%88%B7%E8%AE%A4%E8%AF%81token%E7%AE%97%E6%B3%95%E5%8F%8A%E7%94%9F%E6%88%90%E8%AE%BE%E8%AE%A1
	//------redis里面的key前缀信息
	public static String ADMINTOKEN_PREFIX               = "admin_userauth#";
	public static String ADMINTOKENSTATUS_PREFIX         = "admin_userauth_status#";
	public static String ADMINTOKENCHANGEPASSWORD_PREFIX = "admin_userauth_changepassword#";
	public static String USERTOKENKEY                    = "run_user_sessiontoken#";
	public static String REQUEST_LIMIT                   = "req_limit#";
	public static String CLIENTSIGNKEY                   = "clientSignKey";

	//see http://wiki.cktcd.net/wiki/%E7%AE%A1%E7%90%86%E5%91%98%E7%94%A8%E6%88%B7%E8%AE%A4%E8%AF%81token%E7%AE%97%E6%B3%95%E5%8F%8A%E7%94%9F%E6%88%90%E8%AE%BE%E8%AE%A1
	//------redis里面的finance id 相关key前缀信息
	public static String FINANCETOKEN_PREFIX               = "finance_userauth#";
	/*public static String FINANCETOKENSTATUS_PREFIX         = "finance_userauth_status#";
	public static String FINANCETOKENCHANGEPASSWORD_PREFIX = "finance_userauth_changepassword#";*/

	
	//------zuul框架名字是否走下一个过滤器
	public static final String SHOULDFILTER = "shouldFilter";
	public static final String NODATA       = "nodata";
	
	public static final String servicenameValue = "appserver";
	public static final String serviceNameKey   = "serviceName";	
	public static final String functionNameKey  = "functionName";
	
	public static final String realIpKey       = "realIP";
	public static final String isServerKey     = "isServer";
	public static final String auth_typeKey    = "auth_type";
	public static final String userIdKey       = "userId";
	public static final String imeiKey         = "imei";
	public static final String parkIdKey       = "parkId";
	
	public static final String sectionAdmin       = "admin";
	
	//验证签名的时间戳长度必须为13位毫秒级别
	public static final int timestamplength=13;

	public enum SioeyeHeaderType {
		sioeye("1"), other("2");
		String type;
	
		SioeyeHeaderType(String type) {
			this.type = type;
		}
	
		public String getType() {
			return type;
		}
	}

	public enum SioeyeAdminStatus {
		DISABLE("0")/*禁用*/, ENABLE("1")/*启用*/,STOP("2")/*游乐园停运*/;
		String status;

		SioeyeAdminStatus(String status) {
			this.status = status;
		}

		public String getStatus() {
			return status;
		}
	}

	public enum SioeyeAdminCode {
		sioeyeSuper("0000"), // 超级管理员
		sioeyebusiness("0101"), // sioeye账号，运营角色
		sioeyemaintenance("0102"), // sioeye账号, 运维角色
		sioeyeadmin("0103"), // sioeye账号,admin角色
		youlebusiness("0201"), // 游乐园账号,运营角色
		youlemaintances("0202"), // 游乐园账号,运维角色
		youleadmin("0203");// 游乐园账号,admin角色
		String code;

		SioeyeAdminCode(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
	}
	
	public enum SioeyeFinanceCode {
		sioeyeSuper("1000"), // 超级管理员
		sioeyeFinanceAdmin("1001"),
		sioeyeFinanceOperator("1002");
		String code;

		SioeyeFinanceCode(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

	}

	public enum SioeyeAuthType {
	
		NO_AUTH("noauth")/* 不要验证任何东西的 */, 
		BASE_AUTH("baseauth")/* 验证appid */,
		SERVER_AUTH("serverauth")/* 验证appid和服务器ip */, 
		THING_AUTH("thingauth")/* 验证appid和imei */,
		ADMIN_AUTH("adminauth")/* 验证后台管理的uri，appid和admintoken */,
		USER_AUTH("userauth")/* 没有配置的uri就要验证appid,和sessiontoken */,
		FINANCE_AUTH("financeauth")/* 没有配置的uri就要验证appid,和sessiontoken */,
		DEVICE_AUTH("deviceauth")  /*照片打印设备、广告机都通过此验证方式，验证appid和sessiontoken */;
	
		private String authtype;
	
		SioeyeAuthType(String authtype) {
			this.authtype = authtype;
		}
	
		public String getAuthtype() {
			return authtype;
		}
	
		public static SioeyeAuthType getAuthType(String authtype) {
			switch (authtype) {
			case "noauth":
				return SioeyeAuthType.NO_AUTH;
			case "baseauth":
				return SioeyeAuthType.BASE_AUTH;
			case "serverauth":
				return SioeyeAuthType.SERVER_AUTH;
			case "thingauth":
				return SioeyeAuthType.THING_AUTH;
			case "adminauth":
				return SioeyeAuthType.ADMIN_AUTH;
			case "userauth":
				return SioeyeAuthType.USER_AUTH;
			case "financeauth":
				return SioeyeAuthType.FINANCE_AUTH;
			case "deviceauth":
				return SioeyeAuthType.DEVICE_AUTH;
			default:
				throw new IllegalArgumentException();
			}
		}
	}

	public enum FilterType {
		pre("pre"), route("route"), post("post"), error("error");
		String name;

		FilterType(String name) {
			this.name = name;
		}

		public String getValue() {
			return name;
		}
	}

	public enum FilterOrder {
		one(1), two(2);

		int order;

		FilterOrder(int order) {
			this.order = order;
		}

		public int getValue() {
			return order;
		}
	}
}
