package com.sioeye.youle.common.zuul.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

import com.sioeye.youle.common.zuul.util.Util;

/**
 * @author wenlong.song@ck-telecom.com
 * @author jinze.yuan
 */
class GlobalFallbackProvider implements FallbackProvider {
	private static final Log logger = LogFactory.getLog(GlobalFallbackProvider.class);

	@Override
	public String getRoute() {
		return "*";
	}

	@Override
	public ClientHttpResponse fallbackResponse() {
		return new ClientHttpResponse() {

			@Override
			public InputStream getBody() throws IOException {
				String jsonStr = Util.makeError(EnumMessages.ZUUL_SYSTEM_ERROR, "Fallback");
				return new ByteArrayInputStream(jsonStr.getBytes());
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
				return headers;
			}

			/**
			 * <pre>
			 * zuul 微服务端的请求是失败了，但是客户端向网关发起的请求是OK的，而不能把微服务端的错误抛给客户端
			 * </pre>
			 */
			@Override
			public HttpStatus getStatusCode() throws IOException {
				return HttpStatus.INTERNAL_SERVER_ERROR;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return HttpStatus.INTERNAL_SERVER_ERROR.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
			}

			@Override
			public void close() {

			}
		};
	}

	@Override
	public ClientHttpResponse fallbackResponse(Throwable cause) {
		if (cause != null && cause.getCause() != null) {			
			logger.error("########### GlobalFallbackProvider error(Fall-back)", cause);
		}

		return fallbackResponse();
	}

}