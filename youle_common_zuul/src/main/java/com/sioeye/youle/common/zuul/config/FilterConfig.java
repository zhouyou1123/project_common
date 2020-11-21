package com.sioeye.youle.common.zuul.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sioeye.youle.common.zuul.filter.BaseZuulFilter;
import com.sioeye.youle.common.zuul.filter.LogFilter;
import com.sioeye.youle.common.zuul.filter.AccessAuthZuulFilter;
import com.sioeye.youle.common.zuul.model.IPRange;
import com.sioeye.youle.common.zuul.util.Util;

/**
 * 
 * @author zhouyou email jinx.zhou@ck-telecom.com
 *
 * @date 2017年3月16日
 * 
 * @ClassName FilterConfig.java
 * 
 * @Version v2.0.1
 *
 * @Todo 服务过滤配置类
 *
 */
@Configuration
@RefreshScope
public class FilterConfig {

	@Value("${disney.gateway.nolog.uri}")
	String nologUri;

	@Value("${disney.gateway.serverip}")
	String ipSection;

	@Value("${disney.gateway.financeip}")
	String ipFinanceSection;

	@Autowired
	AuthProperties authProperties;

	@Bean
	public Set<String> nologUriSet() {
		Set<String> nologUriSet = new HashSet<String>();
		StringTokenizer st = new StringTokenizer(nologUri, ",");

		while (st.hasMoreTokens()) {
			nologUriSet.add(st.nextToken());
		}

		return nologUriSet;
	}

	@Bean
	public List<IPRange> serverIPList() {
		List<IPRange> serverIPList = new ArrayList<IPRange>();
		StringTokenizer st = new StringTokenizer(ipSection, ",");

		while (st.hasMoreTokens()) {
			String oneSection = st.nextToken();
			String[] ips = oneSection.split("-");

			String beginIP = ips[0];
			String endIP = ips[ips.length - 1];
			IPRange ipRange = new IPRange(Util.getIp2long(beginIP), Util.getIp2long(endIP));
			serverIPList.add(ipRange);
		}
		return serverIPList;
	}

	@Bean
	public List<IPRange> financeIPList() {
		List<IPRange> financeIPList = new ArrayList<IPRange>();
		StringTokenizer st = new StringTokenizer(ipFinanceSection, ",");

		while (st.hasMoreTokens()) {
			String oneSection = st.nextToken();
			String[] ips = oneSection.split("-");

			String beginIP = ips[0];
			String endIP = ips[ips.length - 1];
			IPRange ipRange = new IPRange(Util.getIp2long(beginIP), Util.getIp2long(endIP));
			financeIPList.add(ipRange);
		}
		return financeIPList;
	}

	@Bean
	public Map<String, String> authMap() {
		Map<String, String> authMap = new HashMap<String, String>();
		authProperties.getAuthList().forEach(authbean -> {
			authbean.getUri().forEach(uri -> {
				authMap.put(uri.toLowerCase(), authbean.getType().toLowerCase());
			});
		});
		// refresh();
		return authMap;
	}

	@Bean
	public GlobalFallbackProvider createSioeyeFallback() {
		return new GlobalFallbackProvider();
	}

	@Bean
	public LogFilter createLogFilter() {
		return new LogFilter();
	}

	@Bean
	public BaseZuulFilter createBaseFilter() {
		return new BaseZuulFilter();
	}

	@Bean
	public AccessAuthZuulFilter createAccessAuthZuulFilter() {
		return new AccessAuthZuulFilter();
	}

	public void refresh() {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		Runnable command = new Runnable() {
			@Override
			public void run() {
				System.out.println(
						"auth size check :" + (new Date()).toString() + ",size:" + authProperties.getAuthList().size());
			}
		};

		long initialDelay = 0;
		long delay = 60;
		TimeUnit unit = TimeUnit.SECONDS;
		System.out.println("current" + (new Date()).toString());
		scheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

}
