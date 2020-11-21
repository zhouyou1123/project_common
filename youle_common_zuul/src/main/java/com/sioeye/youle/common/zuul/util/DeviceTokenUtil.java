package com.sioeye.youle.common.zuul.util;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.context.RequestContext;
import com.sioeye.youle.common.zuul.filter.DeviceTokenInfo;
import lombok.extern.log4j.Log4j;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

@Log4j
public class DeviceTokenUtil {
    // -------------------------------------  device 设备
    public static DeviceTokenInfo verifyDeviceToken(String deviceToken, String jwtPublickey, RequestContext ctx){

        try
        {
            RsaVerifier verifier=new RsaVerifier(jwtPublickey);

            Jwt jwt = JwtHelper.decodeAndVerify(deviceToken, verifier);

            DeviceTokenInfo deviceTokenInfo = JSONObject.parseObject(jwt.getClaims(),DeviceTokenInfo.class);

            return deviceTokenInfo;
        }catch (Exception ex){
            log.error("{" +
                    "\"deviceToken\":\"" + deviceToken + "\"," +
                    "\"jwtPublickey\":\"" + jwtPublickey + "\"," +
                    "\"error\":\"" + ex.getMessage() + "\"" +
                    "}");
            throw ex;
        }
    }
}
