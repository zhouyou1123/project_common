package com.sioeye.youle.common.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author jinze.yuan
 * @email  jinze.yuan@sioeye.com
 * 
 */
@EnableHystrixDashboard
@EnableZuulProxy
@SpringBootApplication
@EnableCircuitBreaker
public class ZuulApp {
	public static void main(String[] args) {
		SpringApplication.run(ZuulApp.class, args);
		System.out.println("start zuul server success .");
	}
}
