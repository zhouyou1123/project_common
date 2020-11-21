package com.sioeye.youle.common.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 
 * @author zhouyou 
 * @cktEmail:jinx.zhou@ck-telecom.com
 * @date 2017年8月22日
 * @fieleName ConfigApp.java 
 * @TODO
 */
@EnableDiscoveryClient
@EnableConfigServer
@SpringBootApplication
public class ConfigApp {
	public static void main(String[] args) {
		new SpringApplicationBuilder(ConfigApp.class).web(true).run(args);
		System.out.println("config server start success .");
	}
}
