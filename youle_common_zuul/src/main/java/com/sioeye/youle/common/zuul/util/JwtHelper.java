package com.sioeye.youle.common.zuul.util;

import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import io.jsonwebtoken.*;

/**
 * 创建和解析 token 工具类
 * 
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */
public class JwtHelper {

	/**
	 * 解析token信息
	 * 
	 * @param jsonWebToken
	 * @param base64Security
	 * @return
	 */
	public static Claims parseJWT(String jsonWebToken, String base64Security) {
		Claims claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(base64Security))
				.parseClaimsJws(jsonWebToken).getBody();
		return claims;
	}

	/**
	 * @param adminId 管理员账号在数据库的唯一标识
	 * @param clientId  客户端请求登录后服务端生成的唯一标识客户端的Id值
	 * @param clientType 客户端请求登录接口的是传的客户端类型(web,app)
	 * @param audience 接收者
	 * @param issuer 发行者
	 * @param expirationtimeMillis 过期时间(毫秒)
	 * @param secretKey 加密key
	 * @return
	 */
	public static String createJWT(String adminId, String clientId, String clientType, String audience, String issuer,
			long expirationtimeMillis, String secretKey) {
		// 采用那种算法
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		// 当前时间
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// 生成签名密钥
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(secretKey);
		Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());// HmacSHA256

		// 构成JWT的参数
		JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT").claim("adminId", adminId).claim("clientId", clientId)
				.claim("clientType", clientType).setAudience(audience).setIssuer(issuer)
				.signWith(signatureAlgorithm, signingKey);

		// 添加Token过期时间
		if (expirationtimeMillis >= 0) {
			long expMillis = nowMillis + expirationtimeMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp).setNotBefore(now);
		}

		// 生成token
		return builder.compact();
	}
}
