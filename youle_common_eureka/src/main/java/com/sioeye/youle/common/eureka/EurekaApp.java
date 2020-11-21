package com.sioeye.youle.common.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 
 * @author zhouyou email jinx.zhou@ck-telecom.com
 *
 * @date 2017年3月16日
 * 
 * @ClassName EurekaApp.java
 * 
 * @Version v2.0.1
 *
 * @Todo Eureka server
 *
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaApp 
{
    public static void main( String[] args )
    {
        SpringApplication.run(EurekaApp.class, args);
        System.out.println("start eureka server success .");
    }
}
