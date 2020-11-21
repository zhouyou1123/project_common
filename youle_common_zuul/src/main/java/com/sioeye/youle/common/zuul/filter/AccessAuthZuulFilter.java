package com.sioeye.youle.common.zuul.filter;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.sioeye.youle.common.zuul.filter.Constants.SioeyeAuthType;
import com.sioeye.youle.common.zuul.model.IPRange;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

/**
 * @author jinze.yuan, will.song
 * @email jinze.yuan@sioeye.com,will.song@sioeye.com
 */
@Slf4j
public class AccessAuthZuulFilter extends ZuulFilter {

    @Autowired
    Map<String, String> authMap;
    @Autowired
    List<IPRange> serverIPList;
    @Autowired
    List<IPRange> financeIPList;
    @Autowired
    private StringRedisTemplate redis;

    // 用户token
    @Value("${user.token.publickey}")
    private String publicKey;

    @Value("${admin.token.secretkey}")
    private String adminSecretKey;

    @Value("${device.token.secretkey}")
    private String deviceSecretKey;

    @Value("${finance.token.secretkey}")
    private String financeTokenSecretKey;

    // 所有访问需要的appid和secret
    @Value("${disney.access.key.id}")
    String appId;
    @Value("${disney.access.key.secret}")
    String secretKey;

    // 所有访问需要的appid和secret
    @Value("${disney.access.key.financeid}")
    String financeAppId;
    @Value("${disney.access.key.financesecret}")
    String financeSecretKey;

    boolean adminCheckPass = true;

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        SioeyeAuthType type = checkAuthType(request);
        VerifyHandler handler = VerifyHandler.create(ctx);

        switch (type) {
            case NO_AUTH:
                handler.requestNoAuthVerify().build();
                break;
            case BASE_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(appId, secretKey).build();
                break;
            case SERVER_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(appId, secretKey).requestServerAuthVerify(serverIPList).build();
                break;
            case THING_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(appId, secretKey).requestThingAuthVerify(redis).build();
                break;
            case ADMIN_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(appId, secretKey).requestAdminAuthVerify(redis, adminSecretKey).build();
                break;
            case USER_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(appId, secretKey).requestUserAuthVerify(redis, publicKey).build();
                break;
            case FINANCE_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(financeAppId, financeSecretKey).requestFinanceAuthVerify(redis, financeTokenSecretKey, financeIPList).build();
                break;
            case DEVICE_AUTH:
                handler.requestHeaderTypeVerify().requestBaseAuthVerify(appId, secretKey).requestDeviceAuthVerify(redis, deviceSecretKey).build();
                break;
        }
        return null;
    }

    @Override
    public boolean shouldFilter() {
        // 判断上一个处理过滤器设置的状态
        RequestContext ctx = RequestContext.getCurrentContext();
        boolean flage = ctx.getBoolean(Constants.SHOULDFILTER);
        if (flage) {// 如果为true 才能调run方法
            return true;
        }
        return false;
    }

    @Override
    public String filterType() {
        return Constants.FilterType.pre.getValue();
    }

    @Override
    public int filterOrder() {
        return Constants.FilterOrder.two.getValue();
    }

    // -----------------------private method
    private Constants.SioeyeAuthType checkAuthType(HttpServletRequest request) {
        String uri = (String) request.getRequestURI().toLowerCase();
        Constants.SioeyeAuthType finaltype = null;
        //uri 精确匹配
        String authType = authMap.get(uri.toLowerCase());
        if (StringUtils.hasText(authType)) {
            return Constants.SioeyeAuthType.getAuthType(authType);

        }
        //通配符匹配
        authType = checkPatternAuthType(uri);
        if (StringUtils.hasText(authType)) {
            return Constants.SioeyeAuthType.getAuthType(authType);

        }
        //都没有匹配成功，就返回回USER_AUTH
        return  Constants.SioeyeAuthType.USER_AUTH;
    }

    protected String checkPatternAuthType(String uri) {
        for (Map.Entry<String, String> entry : authMap.entrySet()) {
            String pattern = entry.getKey();
            log.debug("Matching pattern:" + pattern);
            if (this.pathMatcher.match(pattern, uri)) {
                return entry.getValue();
            }
        }
        return null;
    }

}
