package com.qf.cache.sharded.redis;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCluster;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Redis分布式锁
 * <br>
 * File Name: RedisLock.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月13日 下午2:31:05 
 * @version: v1.0
 *
 */
public class RedisLock {
	
	private static Logger log = LoggerFactory.getLogger(RedisLock.class);
	
	private final JedisCluster jedisCluster;
	
	private final String lockNamespace;
	
	public RedisLock(JedisCluster cluster, String lockNamespace) throws Exception {
		this.jedisCluster = cluster;
		this.lockNamespace = lockNamespace;
	}
	
	public String lock(String key) {
		return lock(key, null);
	}
	
	public String lock(String key, String value) {
		if (StringUtils.isBlank(key)) {
			log.error("Redis加锁失败, 键值未设置");
			return null;
		}
		if (StringUtils.isBlank(value)) {
			value = UUID.randomUUID().toString().replace("-", "");
		}
		long result = jedisCluster.hsetnx(lockNamespace, key, value);
		return result == 1 ? value : null;		
	}

}
