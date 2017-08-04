package com.qf.cache.sharded.redis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
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
	private String lockScriptPath = "/lua/redis-lock.lua";
	private String unLockScriptPath = "/lua/redis-unlock.lua";
	
	private final String namespace;	
	private String lockShal;
	private String unLockShal;
	
	public RedisLock(JedisCluster cluster, String namespace) throws Exception {
		this.jedisCluster = cluster;
		this.namespace = namespace;
		
		loadScript();
	}
	
	private void loadScript() throws Exception {
		lockShal = loadScript(lockScriptPath);
		unLockShal = loadScript(unLockScriptPath);
	}
	
	private String loadScript(String path) throws Exception {
		String basePath = getClass().getClassLoader().getResource("").toString();
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		URL url = new URL(basePath + path);
		BufferedReader reader = new BufferedReader(new FileReader(url.getFile()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		reader = null;
		return this.jedisCluster.scriptLoad(builder.toString(), null);
	}
	
	public String lock(String key, int expiration) {
		return lock(key, null, expiration);
	}
	
	public String lock(String key, String value, int expiration) {
		if (StringUtils.isBlank(key)) {
			log.error("Redis加锁失败, 键未设置");
			return null;
		}
		if (StringUtils.isBlank(value)) {
			value = generateValue();
		}
		key = generateLockKey(key);
		int result = (Integer)this.jedisCluster.evalsha(lockShal, 3, key, value, String.valueOf(expiration));
		return result == 1 ? value : null;		
	}
	
	public boolean unlock(String key, String value) {
		if (StringUtils.isBlank(key) || StringUtils.isBlank(value) ) {
			log.error("Redis解锁失败, 键值未设置");
			return false;
		}
		key = generateLockKey(key);
		int result = (Integer)this.jedisCluster.evalsha(unLockShal, 2, key, value);
		return result == 1;		
	}
	
	private String generateLockKey(String key) {
		String compoundKey = null;
		if (StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(key)) {
			compoundKey = namespace + ":" + key;
		}
		return compoundKey;		
	}
	
	private String generateValue() {
		return UUID.randomUUID().toString().replace("-", "");		
	}

}
