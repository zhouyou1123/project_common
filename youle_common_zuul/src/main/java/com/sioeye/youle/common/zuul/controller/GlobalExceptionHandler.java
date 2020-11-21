package com.sioeye.youle.common.zuul.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.AbstractErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/error")
public class GlobalExceptionHandler extends AbstractErrorController {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@Autowired
	public GlobalExceptionHandler(ErrorAttributes errorAttributes) {
		super(errorAttributes);
	}

	@Override
	public String getErrorPath() {
		return "/error";
	}

	@RequestMapping(produces = MediaType.ALL_VALUE)
    @ResponseBody
	public String  error(HttpServletRequest request) {
		
		StringBuffer result=new StringBuffer();
    	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	String originalUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
    	
        Map<String, Object> map= getErrorAttributes(request, true);
        result.append("{\"code\":\"");
        result.append(map.get("status"));
        result.append("\",\"reqUrl\":\"");
        result.append(originalUri);
        result.append("\",\"error\":\"");
        result.append(map.get("error"));
        result.append("\",\"timestamp\":\"");
        result.append(sdf.format((Date)map.get("timestamp")));
        result.append("\",\"exception\":\"");
        result.append(map.get("exception"));
        result.append("\"}");
        // 日志处理
 		logger.info("{}", result.toString());
        return result.toString();
    }
	
}
