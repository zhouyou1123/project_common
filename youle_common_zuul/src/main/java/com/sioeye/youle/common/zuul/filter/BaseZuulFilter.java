package com.sioeye.youle.common.zuul.filter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.sioeye.youle.common.zuul.model.IPRange;

/**
 * @author jinze.yuan
 * @email jinze.yuan@sioeye.com
 * 
 */
public class BaseZuulFilter extends ZuulFilter {
	@Autowired
	List<IPRange> serverIPList;

	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		ctx.set("startTime", System.currentTimeMillis());
		//log.info(lock.toString() + "->Basezuul receive message");

		VerifyHandler handler = VerifyHandler.create(ctx);
		handler.requestIpAddressVerify().requestUriVerify().build();
		return null;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public String filterType() {
		return Constants.FilterType.pre.getValue();
	}

	@Override
	public int filterOrder() {
		return Constants.FilterOrder.one.getValue();
	}

}
