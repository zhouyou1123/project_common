package com.sioeye.youle.common.zuul.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;

import com.netflix.zuul.context.RequestContext;
import com.sioeye.youle.common.zuul.config.EnumMessagesAdmin;
import com.sioeye.youle.common.zuul.config.EnumMessagesFinance;
import com.sioeye.youle.common.zuul.filter.Constants;
import com.sioeye.youle.common.zuul.filter.Constants.SioeyeAdminStatus;
import com.sioeye.youle.common.zuul.filter.VerifyFinanceTokenInfo;
import com.sioeye.youle.common.zuul.filter.VerifyTokenInfo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;

/**
 * <pre>
 *  采用非对称加密算法对生成token实现加密和解密 
 * 本方法默认采用SHA－1+RSA算法进行签名和加密 
 * 生成的Token数据不进行加密，仅作签名，
 
 * 生成token字符串的算法如下：
 * 例如业务token数据字符串格式为 ：
 *    signature___createdTime___aid___clientType
 *  step1：
 *      将需要的业务字段按如下拼接
 *       ___createdTime___aid___clientType
 *  step2：
 *       对拼接后的字符串进行SHA1WITHRSA签名和加密
 *       ___createdTime___aid___clientType 进行SHA1WITHRSA
 *  step3:     
 *       对签名和加密后的数据进行Base64的encode操作，目的是为了网络传输正确不出现特殊字符
 *       Base64.getEncoder().encodeToString(xxx)
 *  step4：
 *       将encode的签名数据与原有的业务字段一起拼接，最后活动token字符串信息
 *       signature___createdTime___aid___clientType    
 *  
 *  解密token字符串的算法如下：
 *       signature___createdTime___aid___clientType
 *   step1：
 *       按照分割标志信息進行分割字符串
 *       String[] tokenSegments = token.split(STR_FLAG);
 *   step2：
 *       对数组下标为0的字符进行Base64的decode 
 *       tokenSignatureString=Base64.getDecoder().decode(tokenSegments[0])   
 *   step3:
 *    对tokenSignatureString的字符进行SHA1WITHRSA的签名验证。
 * 
 * 
 * </pre>
 * 
 * </pre>
 * 
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */

public class TokenUtil {
	public static final String CHARSET = "UTF-8";// 字符编码格式
	public static final String STR_FLAG = "___";// 三个下划线
	private static SignatureUtils.AsymmetricAlgorithm defaultAsymmetricAlgorithm = SignatureUtils.AsymmetricAlgorithm.RSA;
	private static SignatureUtils.SignatureAlgorithm defaultSignatureAlgorithm = SignatureUtils.SignatureAlgorithm.SHA1WithRSA;

	/**
	 * <pre>
	 *   生成token字符串
	 * 
	 *  例如 原始数据格式为: ___createdTime___aid___clientType 生成token数据格式为：
	 * signature___createdTime___aid___clientType
	 * </pre>
	 * 
	 * @param privateKey
	 * @param tokenString 例如：String tokenString = STR_FLAG + createdTime.getTime() +
	 *                    STR_FLAG + aid + STR_FLAG + clientType;
	 */
	public static String buildToken(String privateKey, String[] tokenParams, String flag) {
		String tokenString = "";
		for (String str : tokenParams)
			tokenString += flag + str;

		// tokenString = flag + createdTime + flag + aid + flag + clientType;
		String tokenCipherString = null;// 生成的token字符串
		try {
			tokenCipherString = SignatureUtils.signByPrivateKey(defaultAsymmetricAlgorithm, defaultSignatureAlgorithm,
					tokenString.getBytes(), privateKey);
			// 用base64编码，产生没有http特殊字符特殊字符串
			tokenCipherString = Base64.getEncoder().encodeToString(tokenCipherString.getBytes(CHARSET));
			tokenCipherString += tokenString;
		} catch (Exception e) {
			throw new RuntimeException("build token failed", e);
		}
		return tokenCipherString.replaceAll("\n", "");
	}

	/**
	 * 对给定的Token进行解析，并验证Token的有效性
	 * 
	 * @param publicKey
	 * @param tokenCipherString
	 * @return boolean true 为成功，false为失败
	 */
	public static boolean verifyToken(String publicKey, String tokenCipherString, String flag) {
		// 验证签名的有效性
		String[] tokenSegments = tokenCipherString.split(flag);
		String tokenSignature = tokenSegments[0];

		// 将timaBASE64的字符串换回来
		tokenSignature = new String(Base64.getDecoder().decode(tokenSignature));

		System.out.println(tokenSignature);

		String tokenString = tokenCipherString.substring(tokenCipherString.indexOf(flag), tokenCipherString.length());
		boolean isValid = SignatureUtils.verifyByPublicKey(defaultAsymmetricAlgorithm, defaultSignatureAlgorithm,
				publicKey, tokenSignature, tokenString);
		return isValid;
	}

	// ------------------------------------admin 相关的检验

	public static VerifyTokenInfo verifyAdminToken(String admintoken, String jwtSecret, RequestContext ctx) {
		VerifyTokenInfo verifyTokenInfo = new VerifyTokenInfo();
		try {
			String base64Key = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
			Claims decodeToken = JwtHelper.parseJWT(admintoken, base64Key);
			makeAdminTokenInfo(verifyTokenInfo, decodeToken);

		} catch (Exception e) {
			String jsonStr = "";
			if (e instanceof ExpiredJwtException) {
				jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_EXPIRED, e.getMessage());
			} else {
				jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_INVALID, e.getMessage());
			}
			verifyTokenInfo.setErrorJsonStr(jsonStr);
		}
		return verifyTokenInfo;
	}
		

	private static void makeAdminTokenInfo(VerifyTokenInfo adminToken, Claims decodeToken) {
		Object adminId = null;
		Object clientId = null;
		Object clientType = null;
		Object parkId = null;
		Object code = null;
		adminId = decodeToken.get(Constants.ADMINID);
		clientId = decodeToken.get(Constants.CLIENTID);
		clientType = decodeToken.get(Constants.CLIENTTYPE);
		parkId = decodeToken.get(Constants.PARKID);
		code = decodeToken.get(Constants.CODE);

		adminToken.setAdminId(adminId);
		adminToken.setClientId(clientId);
		adminToken.setClientType(clientType);
		adminToken.setParkId(parkId);
		adminToken.setCode(code);
	}

	public static void verifyAdminTokenStatus(String requestUri, String status, VerifyTokenInfo verifyTokenInfo) {
		// 禁用0或者游乐园停运2
		if (status != null && (status.equalsIgnoreCase(SioeyeAdminStatus.DISABLE.getStatus())
				|| status.equalsIgnoreCase(SioeyeAdminStatus.STOP.getStatus()))) {
			String extMsg = String.format("->requestUri:%1$s,status:%2$s", requestUri, status);
			String jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_ACCOUNT_INVALID, extMsg);
			verifyTokenInfo.setErrorJsonStr(jsonStr);
		}
	}
	
	/**
	 * @see #verifyAdminTokenRequestUrl
	 * @param requestUri
	 * @param resultMap
	 * @param ctx
	 * @return
	 * 
	 *         <pre>
	 *         String key = FilterContants.ADMINTOKEN_PREFIX + adminId.toString();
	 *         Map<?, ?> resultMap = redis.opsForHash().entries(key);
	 *         TokenUtil.verifyAdminTokenRequestUrl(requestUri, resultMap, ctx);
	 *         </pre>
	 */
	@Deprecated
	public static boolean verifyAdminTokenRequestUrl(String requestUri, Map<?, ?> resultMap, RequestContext ctx) {
		if (!resultMap.containsKey(requestUri)) {
			String jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_NOACCESS_TO_URL, requestUri);
			Util.checkFailed(ctx, HttpStatus.OK.value(), jsonStr);
			return false;
		}
		return true;
	}

	public static void verifyAdminTokenChangePassword(String requestUri, String changepassword, Object clientType,
			VerifyTokenInfo verifyTokenInfo) {
		// redis里面有才出来这个逻辑
		if (changepassword != null && clientType != null) {
			// 策略是web端修改的app端请求需要重新登录,app端修改的web端请求需要重新登录 (即redis里面保存的值与token里面的值不一致就需要抛异常)
			if (!changepassword.equalsIgnoreCase(clientType.toString())) {
				String extMsg = String.format("-> requestUri:%1$s,changepassword:%2$s,clientType:%3$s", requestUri,
						changepassword, clientType);
				String jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_CHANGEPASSWORD, extMsg);
				verifyTokenInfo.setErrorJsonStr(jsonStr);
			}
		}

	}

	public static void verifyAdminTokenSinglePoint(String requestUri, String rediskey, String redistoken,
			String admintoken, VerifyTokenInfo verifyTokenInfo) {
		// redis里面的token 跟header里面传的admintoken不一致，则视为用户在同一设备已经登录了，
		if (redistoken != null && !redistoken.equalsIgnoreCase(admintoken)) {
			String extMsg = String.format("-> requestUri:%1$s,rediskey:%2$s,redistoken:%3$s,%4$s:%5$s", requestUri,
					rediskey, redistoken.substring(0, 20), Constants.X_YOULE_ADMINTOKEN, admintoken.substring(0, 20));
			// String extMsg = String.format("-> requestUri:%1$s,rediskey:%2$s", requestUri,
			// rediskey);
			String jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_SINGLEPOINT, extMsg);
			verifyTokenInfo.setErrorJsonStr(jsonStr);
		}

	}

	public static void verifyAdminTokenRequestUrl(String requestUri, Set<String> resultSet, Set<String> urlSet,
			Set<String> adminIdSet, VerifyTokenInfo verifyTokenInfo) {
		if (resultSet.size() == 0) {
			String extMsg = "->requestUri:" + requestUri + ",urlSet:" + urlSet + ",adminIdSet:" + adminIdSet;
			String jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_NOACCESS_TO_URL, extMsg);
			verifyTokenInfo.setErrorJsonStr(jsonStr);
		}
	}

	public static void verifyAdminTokenRequestUrlParkId(String requestUri, Object parkId, String x_youle_parkid,
			VerifyTokenInfo verifyTokenInfo) {
		if (parkId != null && x_youle_parkid != null) {
			String extMsg = String.format("-> requestUri:%1$s, token in[parkId]:%2$s,%3$s:%4$s", requestUri,
					parkId.toString(), Constants.X_YOULE_PARKID, x_youle_parkid);
			if (compareParkId(parkId.toString(), x_youle_parkid) != 0) {
				String jsonStr = Util.makeAdminError(EnumMessagesAdmin.ADMIN_TOKEN_NOACCESS_TO_URL, extMsg);
				verifyTokenInfo.setErrorJsonStr(jsonStr);
			}
		}
	}

	private static int compareParkId(String parkId, String requestParkId) {
		byte[] b = parkId.toUpperCase().getBytes();// 值全部转为大写
		byte[] rb = requestParkId.toUpperCase().getBytes();// 全部转为大写
		Arrays.sort(b, 0, b.length);
		Arrays.sort(rb, 0, rb.length);

		String comp = new String(b);
		String anotherRequest = new String(rb);
		int result = comp.compareTo(anotherRequest);// 为0则相同
		return result;
	}
	

	
	// ------------------------------------Finance 相关的检验

	public static VerifyFinanceTokenInfo verifyFinanceToken(String financetoken, String jwtSecret, RequestContext ctx) {
		VerifyFinanceTokenInfo verifyFinanceTokenInfo = new VerifyFinanceTokenInfo();
		try {
			String base64Key = Base64.getEncoder().encodeToString(jwtSecret.getBytes());
			Claims decodeToken = JwtHelper.parseJWT(financetoken, base64Key);
			makeFinanceTokenInfo(verifyFinanceTokenInfo, decodeToken);

		} catch (Exception e) {
			String jsonStr = "";
			if (e instanceof ExpiredJwtException) {
				jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_EXPIRED, e.getMessage());
			} else {
				jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_INVALID, e.getMessage());
			}
			verifyFinanceTokenInfo.setErrorJsonStr(jsonStr);
		}
		return verifyFinanceTokenInfo;
	}
	
	


	private static void makeFinanceTokenInfo(VerifyFinanceTokenInfo financeToken, Claims decodeToken) {
		Object financeId = null;
		Object code = null;
		financeId = decodeToken.get(Constants.FINANCEID);
		code = decodeToken.get(Constants.CODE);

		financeToken.setFinanceId(financeId);
		financeToken.setCode(code);
	}
	
	public static void verifyFinanceTokenStatus(String requestUri, String status, VerifyFinanceTokenInfo financeToken) {
		// 禁用0或者游乐园停运2
		if (status != null && (status.equalsIgnoreCase(SioeyeAdminStatus.DISABLE.getStatus())
				|| status.equalsIgnoreCase(SioeyeAdminStatus.STOP.getStatus()))) {
			String extMsg = String.format("->requestUri:%1$s,status:%2$s", requestUri, status);
			String jsonStr = Util.makeFinanceError(EnumMessagesFinance.FINANCE_TOKEN_ACCOUNT_INVALID, extMsg);
			financeToken.setErrorJsonStr(jsonStr);
		}
	}




}
