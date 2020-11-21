package com.sioeye.youle.common.zuul.filter;

import static ch.qos.logback.access.AccessConstants.LB_OUTPUT_BUFFER;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sioeye.youle.common.zuul.model.LogFilterCondition;
import com.sioeye.youle.common.zuul.model.SioeyeHttpServletRequest;
import com.sioeye.youle.common.zuul.model.SioeyeHttpServletResponse;

public class LogFilter implements Filter {

	@Autowired
	private Set<String> nologUriSet;

	@Value("${disney.zuul.log.enable}")
	boolean sioeyeZuulLogEnable;

	@Value("${disney.zuul.log.max.size}")
	long sioeyeZuulLogMaxSize;

	@Override
	public void destroy() {
		// NOP
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		if (!sioeyeZuulLogEnable || ! (request instanceof HttpServletRequest)) {
			filterChain.doFilter(request, response);
		} else {
			HttpServletRequest requestNow = (HttpServletRequest) request; 
			String uri = requestNow.getRequestURI().toLowerCase().toString();

			// 设置日志过滤条件
			LogFilterCondition logFilterCondition = new LogFilterCondition();
			logFilterCondition.setSioeyeZuulLogEnable(sioeyeZuulLogEnable);
			logFilterCondition.setSioeyeZuulLogMaxSize(sioeyeZuulLogMaxSize);
			
			SioeyeHttpServletRequest sioeyeRequest = new SioeyeHttpServletRequest(requestNow);
			SioeyeHttpServletResponse sioeyeResponse = new SioeyeHttpServletResponse((HttpServletResponse) response,
					logFilterCondition);
			// 排除不需要打印日志的接口
			if (!(nologUriSet.contains(uri)) && request instanceof HttpServletRequest) {
				try {
					// filter chain
					filterChain.doFilter(sioeyeRequest, sioeyeResponse);
					sioeyeResponse.finish();
					// 判断返回值的byteSize大小
					// ByteArrayOutputStream byteArrayOutputStream =
					// sioeyeResponse.sioeyeServletOutputStream.baosCopy;
					if (logFilterCondition.getSioeyeZuulLogMaxSize() > 0) {
						//System.out.println(sioeyeResponse.getOutputBuffer());
						sioeyeRequest.setAttribute(LB_OUTPUT_BUFFER, sioeyeResponse.getOutputBuffer());
					} else {
						sioeyeRequest.setAttribute(LB_OUTPUT_BUFFER, "\"response is too large .\"".getBytes());
					}
				} catch (IOException e) {
					e.printStackTrace();
					throw e;
				} catch (ServletException e) {
					e.printStackTrace();
					throw e;
				}
			} else {
				sioeyeRequest.setAttribute(LB_OUTPUT_BUFFER,
						"\"request method does not require printing . \"".getBytes());
				filterChain.doFilter(sioeyeRequest, sioeyeResponse);
				sioeyeResponse.finish();
			}
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// init
	}
}
