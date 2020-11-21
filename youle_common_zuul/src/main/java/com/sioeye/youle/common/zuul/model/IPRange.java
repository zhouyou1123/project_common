package com.sioeye.youle.common.zuul.model;

public class IPRange {
	
	public IPRange(long startIp, long endIP) {
		super();
		this.startIp = startIp;
		this.endIP = endIP;
	}

	private long startIp;
	
	private long endIP;

	public long getStartIp() {
		return startIp;
	}

	public void setStartIp(long startIp) {
		this.startIp = startIp;
	}

	public long getEndIP() {
		return endIP;
	}

	public void setEndIP(long endIP) {
		this.endIP = endIP;
	}
	
	

}
