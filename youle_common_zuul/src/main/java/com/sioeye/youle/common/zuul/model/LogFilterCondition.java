package com.sioeye.youle.common.zuul.model;

public class LogFilterCondition {

    public long sioeyeZuulLogMaxSize;
    public boolean sioeyeZuulLogEnable;

    public long getSioeyeZuulLogMaxSize() {
        return sioeyeZuulLogMaxSize;
    }

    public void setSioeyeZuulLogMaxSize(long sioeyeZuulLogMaxSize) {
        this.sioeyeZuulLogMaxSize = sioeyeZuulLogMaxSize;
    }

    public boolean getSioeyeZuulLogEnable() {
        return sioeyeZuulLogEnable;
    }

    public void setSioeyeZuulLogEnable(boolean sioeyeZuulLogEnable) {
        this.sioeyeZuulLogEnable = sioeyeZuulLogEnable;
    }

}
