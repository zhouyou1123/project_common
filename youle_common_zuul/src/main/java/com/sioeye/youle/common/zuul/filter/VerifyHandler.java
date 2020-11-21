package com.sioeye.youle.common.zuul.filter;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.sioeye.youle.common.zuul.config.EnumMessagesDevice;
import com.sioeye.youle.common.zuul.util.DeviceTokenUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;

import com.netflix.zuul.context.RequestContext;
import com.sioeye.youle.common.zuul.config.EnumMessages;
import com.sioeye.youle.common.zuul.config.EnumMessagesAdmin;
import com.sioeye.youle.common.zuul.config.EnumMessagesFinance;
import com.sioeye.youle.common.zuul.filter.Constants.SioeyeAdminCode;
import com.sioeye.youle.common.zuul.model.IPRange;
import com.sioeye.youle.common.zuul.util.TokenUtil;
import com.sioeye.youle.common.zuul.util.Util;
import org.springframework.util.StringUtils;

/**
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */
public class VerifyHandler {
	private static final Log logger = LogFactory.getLog(VerifyHandler.class);

	private String errorJsonStr = null;// 存储验证失败的错误信息
	private Map<String, Object> map = new HashMap<>();// 需要设置到请求参数域里面的参数键-值
	private HttpStatus defaultStatus = HttpStatus.OK;// http定义的通用状态码
	RequestContext ctx = null;

	private VerifyHandler(RequestContext ctx) {
		this.ctx = ctx;
	}

	public static VerifyHandler create(RequestContext ctx) {
		return new VerifyHandler(ctx);
	}

	public void build() {
		setRequestQueryParams();
		if (errorJsonStr != null) {
			Util.checkFailed(ctx, defaultStatus.value(), errorJsonStr);
		} else {
			Util.checkPassed(ctx);
		}
	}

	// 设置额外的属性值到请求的上下文
	public void setRequestQueryParams() {
		Iterator<String> iter = map.keySet().iterator();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Object obj = map.get(key) == null ? Constants.NODATA : map.get(key);
			String value = obj.toString();
			Util.setAccessInfoToContext(ctx, key, value);
		}
	}

	public VerifyHandler requestIpAddressVerify() {
		if (errorJsonStr == null) {
			HttpServletRequest request = ctx.getRequest();
			String realIP = Util.getRealIp(request);
			map.put(Constants.realIpKey, realIP);
		}
		return this;
	}

	public VerifyHandler requestUriVerify() {
		if (errorJsonStr == null) {
			HttpServletRequest request = ctx.getRequest();
			String requestUri = request.getRequestURI();
			String[] urlSections = requestUri.split("/+");

			if (urlSections.length < 2) {
				defaultStatus = HttpStatus.BAD_REQUEST;
				errorJsonStr = HttpStatus.BAD_REQUEST.getReasonPhrase();
			} else if (urlSections.length == 2) {
				map.put(Constants.serviceNameKey, Constants.servicenameValue);
				map.put(Constants.functionNameKey, urlSections[1]);
			} else {
				map.put(Constants.serviceNameKey,
						urlSections[1].equals("functions") ? Constants.servicenameValue : urlSections[1]);
				map.put(Constants.functionNameKey, urlSections[urlSections.length - 1]);
			}
		}
		return this;
	}

	public VerifyHandler requestHeaderTypeVerify() {
		HttpServletRequest request = ctx.getRequest();
		String requestUri = request.getRequestURI();
		String type = request.getHeader(Constants.X_YOULE_TYPE);
		String parkId = request.getHeader(Constants.X_YOULE_PARKID);
		map.put(Constants.parkIdKey, parkId);
		// 不需要进行x_youle_type验证的uri
		//String extenduris = "/userauth/passport/captcha,/payment/payment_back_weixin";
		String extMsg = "->requestUri:" + requestUri + "," + Constants.X_YOULE_TYPE + ":" + type;

		if (Constants.SioeyeHeaderType.sioeye.getType().equals(type)) {
			// do nothing
		} else if (Constants.SioeyeHeaderType.other.getType().equals(type)) {
			errorJsonStr = Util.makeError(EnumMessages.OTHERCOMPANY_TYPE, extMsg);
		} else {
			errorJsonStr = Util.makeError(EnumMessages.INVALID_TYPE, extMsg);
		}
		return this;
	}

	// -------------------------------------background
	/**
	 * <pre>
	  NO_AUTH("noauth") 不要验证任何东西的 , 
	  BASE_AUTH("baseauth") 验证appid ,
	  SERVER_AUTH("serverauth") 验证appid和服务器ip , 
	  THING_AUTH("thingauth")验证appid和imei , 
	  ADMIN_AUTH("adminauth") 验证后台管理的uri，appid和admintoken ,
	  USER_AUTH("userauth") 没有配置的uri就要验证appid,和sessiontoken ;
	 * </pre>
	 */

	public VerifyHandler requestNoAuthVerify() {
		HttpServletRequest request = ctx.getRequest();
		String headParkId = request.getHeader(Constants.X_YOULE_PARKID) == null ? Constants.NODATA
				: request.getHeader(Constants.X_YOULE_PARKID);
		map.put(Constants.parkIdKey, headParkId);
		return this;
	}

	public VerifyHandler requestBaseAuthVerify(String orginalAppId, String orginalSecretKey) {
		HttpServletRequest request = ctx.getRequest();
		String requestUri = request.getRequestURI();
		String appId = request.getHeader(Constants.X_YOULE_APPID);
		String appSignKey = request.getHeader(Constants.X_YOULE_APPSIGNKEY);

		EnumMessages msgEnum = checkSignKey(orginalAppId, orginalSecretKey, appId, appSignKey);
		if (msgEnum != null) {
			String extMsg = String.format("-> requestUri:%1$s,%2$s:%3$s,%4$s:%5$s", requestUri, Constants.X_YOULE_APPID,
					appId, Constants.X_YOULE_APPSIGNKEY, appSignKey);
			errorJsonStr = Util.makeError(msgEnum, extMsg);
		}
		return this;
	}

	public VerifyHandler requestServerAuthVerify(List<IPRange> serverIPList) {
		HttpServletRequest request = ctx.getRequest();
		String requestUri = request.getRequestURI();
		String realIP = Util.getRealIp(request);
		boolean isServer = false;
		if (!Util.ipExistsInRange(realIP, serverIPList)) {
			String extMsg = String.format("-> requestUri:%1$s,realIP:%2$s", requestUri, realIP);
			errorJsonStr = Util.makeError(EnumMessages.SERVERIP_VERIFICATION_FAIL, extMsg);
		} else {
			isServer = true;
		}
		map.put(Constants.realIpKey, realIP);
		map.put(Constants.isServerKey, isServer);
		return this;
	}

	public VerifyHandler requestThingAuthVerify_old(StringRedisTemplate redis) {
		HttpServletRequest request = ctx.getRequest();
		String requestUri = request.getRequestURI();
		String imei = request.getHeader(Constants.X_YOULE_CAMERAIMEI);

		if (Util.isStringEmpty(imei)) {
			String extMsg = String.format("requestUri:%1$s,%2$s:%3$s", requestUri, Constants.X_YOULE_CAMERAIMEI, imei);
			errorJsonStr = Util.makeError(EnumMessages.CAMERA_AUTH_FAIL, extMsg);
		} else {
			String cameraId = getCameraId(redis, imei);
			if (Util.isStringEmpty(cameraId)) {
				String extMsg = String.format("requestUri:%1$s,%2$s:%3$s,cameraId:%4$s", requestUri,
						Constants.X_YOULE_CAMERAIMEI, imei, cameraId);
				errorJsonStr = Util.makeError(EnumMessages.CAMERA_AUTH_FAIL, extMsg);
			}
		}

		map.put(Constants.imeiKey, imei);
		return this;
	}

	public VerifyHandler requestThingAuthVerify(StringRedisTemplate redis) {
		HttpServletRequest request = ctx.getRequest();
		String requestUri = request.getRequestURI();
		String imei = request.getHeader(Constants.X_YOULE_CAMERAIMEI);

		if (Util.isStringEmpty(imei) || Util.isNoImei(imei)) {
			String extMsg = String.format("requestUri:%1$s,%2$s:%3$s", requestUri, Constants.X_YOULE_CAMERAIMEI, imei);
			errorJsonStr = Util.makeError(EnumMessages.CAMERA_AUTH_FAIL, extMsg);
		}
		map.put(Constants.imeiKey, imei);
		return this;
	}

	public VerifyHandler requestAdminAuthVerify(StringRedisTemplate redis, String adminSecretKey) {
		HttpServletRequest request = ctx.getRequest();
		checkAdminHeaderAndUri(request, redis, adminSecretKey);
		return this;
	}
	
	public VerifyHandler requestFinanceAuthVerify(StringRedisTemplate redis, String financeSecretKey, List<IPRange> financeIPList) {
		HttpServletRequest request = ctx.getRequest();
		checkFinanceHeaderAndUri(request, redis, financeSecretKey, financeIPList);
		return this;
	}
	
	

	public VerifyHandler requestUserAuthVerify(StringRedisTemplate redis, String publicKey) {
		HttpServletRequest request = ctx.getRequest();
		String requestUri = request.getRequestURI();
		String token = request.getHeader(Constants.X_YOULE_SESSIONTOKEN);

		String extMsg = String.format("-> requestUri:%1$s,%2$s:%3$s", requestUri, Constants.X_YOULE_SESSIONTOKEN,
				token);

		if (Util.isStringEmpty(token)) {
			errorJsonStr = Util.makeError(EnumMessages.USER_AUTH_FAIL, extMsg);
		} else {
			try {
				String flag = TokenUtil.STR_FLAG;
				boolean isValid = TokenUtil.verifyToken(publicKey, token, flag);
				if (!isValid) {
					errorJsonStr = Util.makeError(EnumMessages.USER_AUTH_FAIL, extMsg);
				} else {
					String userId = getUserIdByRefreshToken(redis, token);
					if (userId == null) {
						extMsg = extMsg + ",userId==null, because this redis no " + Constants.USERTOKENKEY;
						errorJsonStr = Util.makeError(EnumMessages.USER_AUTH_FAIL, extMsg);
					} else {
						map.put(Constants.userIdKey, userId);
					}

				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				errorJsonStr = Util.makeError(EnumMessages.USER_AUTH_FAIL, extMsg);
			}
		}
		return this;
	}

	public VerifyHandler requestDeviceAuthVerify(StringRedisTemplate redis, String devicePublicKey) {
		HttpServletRequest request = ctx.getRequest();
		requestDeviceAuthVerify(request, redis, devicePublicKey);
		return this;
	}

	private void requestDeviceAuthVerify(HttpServletRequest request, StringRedisTemplate redis, String devicePublicKey) {

		if (StringUtils.hasText(errorJsonStr))return;
		String requestUri = request.getRequestURI();
		String deviceToken = request.getHeader(Constants.X_YOULE_DEVICETOKEN);
		try {

			// 必须传x_youle_devicetoken,x_youle_flag,x_youle_parkid
			if (StringUtils.hasText(deviceToken)) {

				//1、验证token 在 Redis 中是否存在
				String redisObjectIdValue = redis.opsForValue().get(deviceToken);
				if(!StringUtils.hasText(redisObjectIdValue)){
					String extMsg = String.format("->requestUri:%s,device Token:%s is not find by redis.", requestUri, deviceToken);
					errorJsonStr = Util.makeDeviceError(EnumMessagesDevice.DEVICE_TOKEN_ID_INVALID, extMsg);
				}

				// 2、解析 token
				 DeviceTokenInfo deviceTokenInfo = DeviceTokenUtil.verifyDeviceToken(deviceToken, devicePublicKey, ctx);

				 //3、判断token是否过期
				if (new Date().compareTo(deviceTokenInfo.getDueDate())>=0){
					String extMsg = String.format("->requestUri:%s,token dueDate:%s,current Date:%s", requestUri,
							deviceTokenInfo.getDueDate(), new Date());
					errorJsonStr = Util.makeDeviceError(EnumMessagesDevice.DEVICE_TOKEN_EXPIRED, extMsg);
					return;
				}
				//4、验证设备ObjectId 在 Redis中是否存在
				String redisTokenValue = redis.opsForValue().get("DevicePrinter:" + deviceTokenInfo.getObjectId());
				if (!StringUtils.hasText(redisTokenValue)){
					String extMsg = String.format("->requestUri:%s,token:%s", requestUri, deviceToken);
					errorJsonStr = Util.makeDeviceError(EnumMessagesDevice.DEVICE_TOKEN_ID_INVALID, extMsg);
					return;
				}


				//5、判断客户端ip是否一致 ，将header 中的 token 客户ip 和 redis 中的 token 客户ip进行比对
				//不相同就提示前端错误
				DeviceTokenInfo redisTokenInfo = DeviceTokenUtil.verifyDeviceToken(redisTokenValue, devicePublicKey, ctx);
				if (!deviceTokenInfo.getClientIp().equals(redisTokenInfo.getClientIp())) {
					String extMsg = String.format("->requestUri:%s token:$s,header clientIp:%s,redis clientIp:%s", requestUri,
							deviceToken,deviceTokenInfo.getClientIp(), redisTokenInfo.getClientIp());
					errorJsonStr = Util.makeDeviceError(EnumMessagesDevice.DEVICE_TOKEN_SINGLEPOINT, extMsg);
					return;
				}
				// 6、向下游服务传递参数
				map.put(Constants.userIdKey, deviceTokenInfo.getObjectId());
			} else {
				// 客户端没有传Token
				String extMsg = String.format("->requestUri:%s", requestUri);
				errorJsonStr = Util.makeDeviceError(EnumMessagesDevice.DEVICE_TOKEN_NOTNULL, extMsg);
				return;
			}

		} catch (Exception e) {
			String extMsg = String.format("->requestUri:%s,message:%s", requestUri, e.getMessage());
			errorJsonStr = Util.makeDeviceError(EnumMessagesDevice.DEVICE_TOKEN_INVALID, extMsg);
		}
		return;
	}
	

	@SuppressWarnings("unused")
	private void testPrintTime() {
		Object startTime = ctx.get("startTime");
		long time = (System.currentTimeMillis() - Long.parseLong(startTime.toString()));
		logger.info(String.format("############# access_filter time:%s ms", time));
	}

	private String getUserIdByRefreshToken(StringRedisTemplate redis, String refreshtoken) {
		String tokenKey = buildRedisKey(Constants.USERTOKENKEY, refreshtoken);
		String userid = redis.opsForValue().get(tokenKey);
		return userid;
	}

	private String buildRedisKey(String prefix, String refreshtoken) {
		String tokenkey = prefix + refreshtoken;
		return tokenkey;
	}

	private String getCameraId(StringRedisTemplate redis, String imei) {
		String cameraid = redis.opsForValue().get(imei);
		return cameraid;
	}

	private EnumMessages checkSignKey(String originAppId, String originalSecretKey, String appId, String signKey) {
		EnumMessages msg = null;
		if ((appId == null || "".equals(appId)) || (signKey == null || "".equals(signKey))
				|| (!appId.equals(originAppId))) {
			return EnumMessages.SIGN_VERIFICATION_FAIL;
		}

		String[] sign_key = signKey.split(",");
		if (sign_key.length != 2) {
			return EnumMessages.SIGN_VERIFICATION_FAIL;
		} else {
			String sign = sign_key[0];
			String timestamp = sign_key[1];

			// 时间必须为13位 return
			if (timestamp != null && timestamp.length() != Constants.timestamplength) {
				return EnumMessages.SIGN_VERIFICATION_FAIL;
			}

			try {
				String md5Key = Util.HEXAndMd5(originalSecretKey + timestamp);
				if (!md5Key.equals(sign))
					return EnumMessages.SIGN_VERIFICATION_FAIL;

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return EnumMessages.SIGN_VERIFICATION_FAIL;
			}

		}
		return msg;
	}

	/**
	 * 如果传了admintoken的话必须传 flag
	 * 
	 * @param ctx
	 * @param request
	 * @param redis
	 * @param adminSecretKey
	 */
	private void checkAdminHeaderAndUri(HttpServletRequest request, StringRedisTemplate redis, String adminSecretKey) {
		String requestUri = request.getRequestURI();
		String flag = request.getHeader(Constants.X_YOULE_FLAG);
		String admintoken = request.getHeader(Constants.X_YOULE_ADMINTOKEN);
		String headParkId = request.getHeader(Constants.X_YOULE_PARKID);
		VerifyTokenInfo verifyTokenInfo = null;
		try {

			// 必须传x_youle_admintoken,x_youle_flag,x_youle_parkid
			if (admintoken != null && headParkId != null && flag != null && flag.equalsIgnoreCase("1")) {
				// step0：验证token
				verifyTokenInfo = TokenUtil.verifyAdminToken(admintoken, adminSecretKey, ctx);
				// 设置参数信息
				String adminIdValue = verifyTokenInfo.getAdminId().toString();
				String clientTypeValue = verifyTokenInfo.getClientType().toString();
				String parkIdValue = verifyTokenInfo.getParkId().toString();
				map.put(Constants.ADMINID, adminIdValue);
				map.put(Constants.CLIENTTYPE, clientTypeValue);
				map.put(Constants.PARKID, parkIdValue);

				Object adminId = verifyTokenInfo.getAdminId();
				Object code = verifyTokenInfo.getCode();
				//添加admin role code并传递到下游服务
				map.put(Constants.CODE, code.toString());
				// step1: 验证是否是同一设备的单点登录
				if (verifyTokenInfo.getErrorJsonStr() == null) {
					Object clentTypeObj = verifyTokenInfo.getClientType();
					String clientType = clentTypeObj != null ? clentTypeObj.toString() : Constants.NODATA;
					String key = Constants.ADMINTOKEN_PREFIX + clientType + "#" + adminId.toString();
					String redistoken = redis.opsForValue().get(key);
					TokenUtil.verifyAdminTokenSinglePoint(requestUri, key, redistoken, admintoken, verifyTokenInfo);
				}

				// step2: 验证这个token对应的账号是否被禁用
				if (verifyTokenInfo.getErrorJsonStr() == null) {
					String key = Constants.ADMINTOKENSTATUS_PREFIX + adminId.toString();
					String status = redis.opsForValue().get(key);
					TokenUtil.verifyAdminTokenStatus(requestUri, status, verifyTokenInfo);
				}
				// step3:验证密码是否被修改 ( 策略是web端修改的app端请求需要重新登录，app端修改的web端请求需要重新登录)
				if (verifyTokenInfo.getErrorJsonStr() == null) {
					String key = Constants.ADMINTOKENCHANGEPASSWORD_PREFIX + adminId.toString();
					String changepassword = redis.opsForValue().get(key);
					TokenUtil.verifyAdminTokenChangePassword(requestUri, changepassword,
							verifyTokenInfo.getClientType(), verifyTokenInfo);
				}

				// step4: 验证urlh和parkId
				// 是超级管理员则过对请求uri
				if (code.toString().equals(SioeyeAdminCode.sioeyeSuper.getCode())) {
					return;
				} else if (verifyTokenInfo.getErrorJsonStr() == null) {
					// yjz ： roles集进行比对和parkId进行判断
					// step1:校验url
					String urlKey = Constants.ADMINTOKEN_PREFIX + requestUri;
					String adminIdKey = Constants.ADMINTOKEN_PREFIX + adminId;
					SetOperations<String, String> setOper = redis.opsForSet();
					Set<String> resultSet = setOper.intersect(urlKey, adminIdKey);

					Set<String> urlSet = setOper.members(urlKey);
					Set<String> adminIdSet = setOper.members(adminIdKey);
					TokenUtil.verifyAdminTokenRequestUrl(requestUri, resultSet, urlSet, adminIdSet, verifyTokenInfo);
					// step2: 校验parkId
					Object parkId = verifyTokenInfo.getParkId();
					if (verifyTokenInfo.getErrorJsonStr() == null) {
						TokenUtil.verifyAdminTokenRequestUrlParkId(requestUri, parkId, headParkId, verifyTokenInfo);
					}
				}
			} else {
				// 客户端传的参数不够
				String extMsg = String.format("->requestUri:%1$s, %2$s:%3$s,%4$s:%5$s,%6$s:%7$s", requestUri,
						Constants.X_YOULE_ADMINTOKEN, admintoken, Constants.X_YOULE_FLAG, flag,
						Constants.X_YOULE_PARKID, headParkId);
				errorJsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_INVALID, extMsg);
				return;
			}

		} catch (Exception e) {
			String extMsg = String.format("->requestUri:%1$s,message:", requestUri, e.getMessage());
			logger.error(extMsg, e);
			errorJsonStr = Util.makeError(EnumMessages.VERIFY_HANDLER_ERROR, extMsg);
		} finally {
			if (errorJsonStr == null)
				errorJsonStr = verifyTokenInfo.getErrorJsonStr();
		}
	}
	
	
	/**
	 * 如果传了financetoken的话必须传 flag
	 * 
	 * @param ctx
	 * @param request
	 * @param redis
	 * @param adminSecretKey
	 */
	private void checkFinanceHeaderAndUri(HttpServletRequest request, StringRedisTemplate redis, String financeSecretKey, List<IPRange> financeIPList) {
		String requestUri = request.getRequestURI();
		String flag = request.getHeader(Constants.X_YOULE_FLAG);
		String financetoken = request.getHeader(Constants.X_YOULE_FINANCETOKEN);
		VerifyFinanceTokenInfo verifyFinanceTokenInfo = null;
		try {

			// 必须传x_youle_admintoken,x_youle_flag,x_youle_parkid
			if (financetoken != null && flag != null && flag.equalsIgnoreCase("2")) {

				String extMsg = null;
				String jsonStr = null;
				
				// step0：验证token
				verifyFinanceTokenInfo = TokenUtil.verifyFinanceToken(financetoken, financeSecretKey, ctx);				

				String financeIdValue = verifyFinanceTokenInfo.getFinanceId().toString();
				String codeValue = verifyFinanceTokenInfo.getCode().toString();
				map.put(Constants.FINANCEID, financeIdValue);
				
				// token通过非对称加密验证后
				if (verifyFinanceTokenInfo.getErrorJsonStr() == null) {				

					// step1：验证访问ip
					String realIP = Util.getRealIp(request);
					
					if (!Util.ipExistsInRange(realIP, financeIPList)) {
						extMsg = String.format("-> requestUri:%1$s,realIP:%2$s", requestUri, realIP);
						jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_ADRESS_ILIGAL, extMsg);
						verifyFinanceTokenInfo.setErrorJsonStr(jsonStr);
						return;
					}					

					// step2: 验证签名是否有效，更改密码或被注销后签名会在用户服务中失效
					String key = Constants.FINANCETOKEN_PREFIX + financeIdValue;
					String redistoken = redis.opsForValue().get(key);
					
					extMsg = String.format("-> requestUri:%1$s,rediskey:%2$s,redistoken:%3$s,%4$s:%5$s", requestUri,
							key, redistoken.substring(0, 20), Constants.X_YOULE_FINANCETOKEN, financetoken.substring(0, 20));
					
					if( Util.isStringEmpty(redistoken)){
						jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_ACCOUNT_INVALID, extMsg);
					} else if ( !financetoken.equals(redistoken) ){
						jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_INVALID, extMsg);
					}
					
					if (jsonStr != null){
						verifyFinanceTokenInfo.setErrorJsonStr(jsonStr);
						return;
					}
					
					// step3: 验证接口权限
					// 是超级管理员则过对请求uri
					if (codeValue != null && 
							(codeValue.equals(Constants.SioeyeFinanceCode.sioeyeSuper.getCode()) ||
							codeValue.equals(Constants.SioeyeFinanceCode.sioeyeFinanceAdmin.getCode()) )) {
						return;
					} else if (verifyFinanceTokenInfo.getErrorJsonStr() == null) {
						
						String[] uriSections = requestUri.split("/");
						
						String adminSection = null;
						
						if (uriSections.length >= 2){
							adminSection = uriSections[2];
						}						
						// 不是超级管理员，但视图访问管理员接口
						if (adminSection != null && adminSection.equalsIgnoreCase(Constants.sectionAdmin)){
							extMsg = String.format("-> requestUri:%1$s,realIP:%2$s, %3$s:%4$s ", requestUri, realIP, Constants.FINANCEID, financeIdValue);
							jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_NOACCESS_TO_URL, extMsg);
							verifyFinanceTokenInfo.setErrorJsonStr(jsonStr);
							return;
						}
					}
					
				}
				
				
			} else {
				// 客户端传的参数不够
				String extMsg = String.format("->requestUri:%1$s, %2$s:%3$s,%4$s:%5$s", requestUri,
						Constants.X_YOULE_FINANCETOKEN, financetoken, Constants.X_YOULE_FLAG, flag);
				errorJsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_INVALID, extMsg);
				return;
			}

		} catch (Exception e) {
			String extMsg = String.format("->requestUri:%1$s,message:", requestUri, e.getMessage());
			logger.error(extMsg, e);
			errorJsonStr = Util.makeError(EnumMessages.VERIFY_HANDLER_ERROR, extMsg);
		} finally {
			if (errorJsonStr == null)
				errorJsonStr = verifyFinanceTokenInfo.getErrorJsonStr();
		}
	}
}
