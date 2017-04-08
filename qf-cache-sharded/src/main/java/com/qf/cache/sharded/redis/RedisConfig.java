package com.qf.cache.sharded.redis;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.net.HostAndPort;
import com.qf.cache.exception.CacheCreateException;

import redis.clients.jedis.JedisShardInfo;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Redis配置
 * <br>
 * File Name: RedisConfig.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年4月8日 下午3:31:44 
 * @version: v1.0
 *
 */
public class RedisConfig {
	
	private List<HostAndPort> hostList;
	
	private int database;	
	private int timeout;
	private String password;
	
	public void setHostList(List<HostAndPort> hostList) {
		this.hostList = hostList;
	}
	
	public List<HostAndPort> getHostList() {
		return hostList;
	}
	
	public void setDatabase(int database) {
		this.database = database < 0 ? 0 : database;
	}
	
	public int getDatabase() {
		return database;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout < 0 ? 0 : timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}
	
	public List<JedisShardInfo> buildJedisShardInfo() throws CacheCreateException {
		if (CollectionUtils.isEmpty(hostList)) {
			throw new CacheCreateException("Redis host can not be empty.");
		}
		List<JedisShardInfo> infoList = new ArrayList<JedisShardInfo>();
		for (HostAndPort hp : hostList) {
			JedisShardInfo info = new JedisShardInfo(hp.getHostText(), hp.getPort(), timeout, hp.getHostText() + hp.getPort());
			infoList.add(info);
		}
		return infoList;
	}

}
