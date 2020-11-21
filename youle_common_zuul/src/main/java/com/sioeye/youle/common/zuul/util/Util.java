package com.sioeye.youle.common.zuul.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netflix.zuul.context.RequestContext;
import com.sioeye.youle.common.zuul.config.EnumMessages;
import com.sioeye.youle.common.zuul.config.EnumMessagesAdmin;
import com.sioeye.youle.common.zuul.config.EnumMessagesDevice;
import com.sioeye.youle.common.zuul.config.EnumMessagesFinance;
import com.sioeye.youle.common.zuul.filter.Constants;
import com.sioeye.youle.common.zuul.model.IPRange;

/**
 * author jiangpeng ckt email:peng.jiang@ck-telecom.com 2017年5月25日 Util.java
 * description 工具类
 */

public class Util {

	@SuppressWarnings("finally")
	public static String HEXAndMd5(String plainText) throws NoSuchAlgorithmException {
		StringBuffer buf = new StringBuffer(200);
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			try {
				md.update(plainText.getBytes("UTF8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			byte b[] = md.digest();
			int i;
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset] & 0xff;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

		} catch (NoSuchAlgorithmException e) {
			throw e;
		} finally {
			return buf.toString();
		}
	}

	/**
	 * 对字符串md5加密
	 *
	 * @param str
	 * @return
	 */
	public static String getMD5(String str) throws Exception {
		try {
			// 生成一个MD5加密计算摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 计算md5函数
			md.update(str.getBytes());
			// digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
			// BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 从Request对象中获得客户端IP，处理了HTTP代理服务器和Nginx的反向代理截取了ip
	 * 
	 * @param request
	 * @return ip
	 */
	public static String getLocalIp(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		String forwarded = request.getHeader("X-Forwarded-For");
		String realIp = request.getHeader("X-Real-IP");

		String ip = null;
		if (realIp == null) {
			if (forwarded == null) {
				ip = remoteAddr;
			} else {
				ip = remoteAddr + "/" + forwarded.split(",")[0];
			}
		} else {
			if (realIp.equals(forwarded)) {
				ip = realIp;
			} else {
				if (forwarded != null) {
					forwarded = forwarded.split(",")[0];
				}
				ip = realIp + "/" + forwarded;
			}
		}
		return ip;
	}

	public static String getRealIp(HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (!Util.isStringEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			// 多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = ip.indexOf(",");
			if (index != -1) {
				return ip.substring(0, index);
			} else {
				return ip;
			}
		}
		ip = request.getHeader("X-Real-IP");
		if (!Util.isStringEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
			return ip;
		}

		ip = request.getRemoteAddr();

		if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
			// 根据网卡取本机配置的IP
			InetAddress inet = null;
			try {
				inet = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				// should not happen
				e.printStackTrace();
			}
			ip = inet.getHostAddress();
		}
		return ip;
	}

	/**
	 * 验证IP是否属于某个IP段
	 * 
	 * ipSection IP段（以'-'分隔） ip 所验证的IP号码
	 */
	public static boolean ipExistsInRange(String ip, String ipSection) {
		ipSection = ipSection.trim();
		ip = ip.trim();
		if (ip.equals("0:0:0:0:0:0:0:1")) {
			ip = "127.0.0.1";
		}

		int idx = ipSection.indexOf('-');
		String beginIP = ipSection.substring(0, idx);
		String endIP = ipSection.substring(idx + 1);

		return getIp2long(beginIP) <= getIp2long(ip) && getIp2long(ip) <= getIp2long(endIP);
	}

	/**
	 * 验证IP是否属于某个IP段
	 * 
	 * ipSection IP段（以'-'分隔） ip 所验证的IP号码
	 */
	public static boolean ipExistsInRange(String ip, List<IPRange> ipList) {
		ip = ip.trim();
		if (ip.equals("0:0:0:0:0:0:0:1")) {
			ip = "127.0.0.1";
		}
		Long longIp = getIp2long(ip);
		for (IPRange oneSection : ipList) {
			if (oneSection.getStartIp() <= longIp && longIp <= oneSection.getEndIP()) {
				return true;
			}
		}

		return false;
	}

	public static long getIp2long(String ip) {
		ip = ip.trim();
		String[] ips = ip.split("\\.");
		long ip2long = 0L;

		for (int i = 0; i < 4; ++i) {
			ip2long = ip2long << 8 | Integer.parseInt(ips[i]);
		}
		return ip2long;
	}

	public static long getIp2long2(String ip) {
		ip = ip.trim();
		String[] ips = ip.split("\\.");
		long ip1 = Integer.parseInt(ips[0]);
		long ip2 = Integer.parseInt(ips[1]);
		long ip3 = Integer.parseInt(ips[2]);
		long ip4 = Integer.parseInt(ips[3]);
		long ip2long = 1L * ip1 * 256 * 256 * 256 + ip2 * 256 * 256 + ip3 * 256 + ip4;

		return ip2long;
	}

	public static void setAccessInfoToContext(RequestContext ctx, String key, Object value) {
		// 三个地方进行设置，目的是保证下游服务器能够获取到相关信息
		ctx.set(key, value);
		ctx.getRequest().setAttribute(key, value);
		// yjz 2018/9/11
		ctx.getRequest().getParameterMap();
		Map<String, List<String>> params = ctx.getRequestQueryParams();
		if (params == null) {
			params = Maps.newHashMap();
		}
		params.put(key, Lists.newArrayList(value.toString()));
		ctx.setRequestQueryParams(params);
	}

	public static void checkPassed(RequestContext ctx) {
		ctx.set(Constants.SHOULDFILTER, true);
	}

	/**
	 * 设置请求响应
	 * 
	 * @param RequestContext ctx 请求内容
	 * @param flag           是否过滤请求
	 * @param code           响应码
	 * @param message        消息信息
	 * @param shouldFilter   是否需要执行过滤器
	 * @return null
	 */
	public static void checkFailed(RequestContext ctx, int code, String message) {

		ctx.setSendZuulResponse(false);
		ctx.setResponseStatusCode(code);
		if (isStringEmpty(ctx.getResponse().getCharacterEncoding())) {
			ctx.getResponse().setCharacterEncoding("UTF-8");
		}
		ctx.getResponse().setContentType(ctx.getRequest().getContentType());
//        ctx.getResponse().setContentType("application/json;charset=UTF-8");
		ctx.setResponseBody(message);
		checkIntercept(ctx);
	}

	public static void checkIntercept(RequestContext ctx) {
		ctx.set(Constants.SHOULDFILTER, false);
	}

	public static String getUUID() {
		return UUID.randomUUID().toString().replace("-", "").toUpperCase();
	}

	public static boolean isStringEmpty(String str) {
		return (str == null || "".equals(str.trim()));
	}

	public static boolean isNoImei(String imei) {
		// imei:839283922393133
		Pattern p = Pattern.compile("[0-9]{15}");
		Matcher m = p.matcher(imei);
		boolean b = m.matches();
		if (b) {
			return false;
		} else {
			return true;
		}
	}

	// ----------------mvp
	public static String makeError(EnumMessages message, String extMessage) {
		String jsonStr = makeStr(message, extMessage);
		return jsonStr;
	}

	public static String makeStr(EnumMessages message, String extMessage) {
		String msg = message.getMessage() + "(" + extMessage + ")";
		Map<String, Object> resultMap = new LinkedHashMap<>();
		resultMap.put("success", false);
		resultMap.put("code", message.getCode());
		resultMap.put("message", msg);
		return JSON.toJSONString(resultMap);
	}

	// ----------------admin
	public static String makeAdminError(EnumMessagesAdmin message, String extMessage) {
		String jsonStr = makeAdminStr(message, extMessage);
		return jsonStr;
	}

	public static String makeAdminStr(EnumMessagesAdmin message, String extMessage) {
		String msg = message.getMessage() + "(" + extMessage + ")";
		Map<String, Object> resultMap = new LinkedHashMap<>();
		resultMap.put("success", false);
		resultMap.put("code", message.getCode());
		resultMap.put("message", msg);
		return JSON.toJSONString(resultMap);
	}
	
	// ----------------admin
	public static String makeFinanceError(EnumMessagesFinance message, String extMessage) {
		String jsonStr = makeFinanceStr(message, extMessage);
		return jsonStr;
	}

	public static String makeFinanceStr(EnumMessagesFinance message, String extMessage) {
		String msg = message.getMessage() + "(" + extMessage + ")";
		Map<String, Object> resultMap = new LinkedHashMap<>();
		resultMap.put("success", false);
		resultMap.put("code", message.getCode());
		resultMap.put("message", msg);
		return JSON.toJSONString(resultMap);
	}

	// ----------------device
	public static String makeDeviceError(EnumMessagesDevice message, String extMessage) {
		String jsonStr = makeDeviceStr(message, extMessage);
		return jsonStr;
	}

	public static String makeDeviceStr(EnumMessagesDevice message, String extMessage) {
		String msg = message.getMessage() + "(" + extMessage + ")";
		Map<String, Object> resultMap = new LinkedHashMap<>();
		resultMap.put("success", false);
		resultMap.put("code", message.getCode());
		resultMap.put("message", msg);
		return JSON.toJSONString(resultMap);
	}
}
