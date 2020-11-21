package com.sioeye.youle.common.zuul.filter;

import lombok.Data;

import java.util.Date;

@Data
public class DeviceTokenInfo {
    /**
     * 记录id
     */
    private String objectId;
    /**
     * 名称
     */
    private String name;
    /**
     * token创建日期
     */
    private Date createDate;
    /**
     * token 有效期
     */
    private Date dueDate;
    /**
     * 客户端ip
     */
    private String clientIp;

}
