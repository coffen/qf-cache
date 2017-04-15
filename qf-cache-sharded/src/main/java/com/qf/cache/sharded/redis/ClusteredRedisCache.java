package com.qf.cache.sharded.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.qf.cache.Cache;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheSaveConditionEnum;
import com.qf.cache.Serializer;
import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.serializer.KryoSerializer;

import redis.clients.jedis.JedisCluster;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Redis集群
 * <br>
 * File Name: ClusteredRedisCache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年4月15日 下午12:09:02 
 * @version: v1.0
 *
 */
public class ClusteredRedisCache implements Cache {
	
	private static Logger log = LoggerFactory.getLogger(ClusteredRedisCache.class);
	
	private static JedisCluster jedisCluster;
	private static ConcurrentHashMap<String, ClusteredRedisCache> cacheMap = new ConcurrentHashMap<String, ClusteredRedisCache>();
	
	private RedisCacheConfig config;
	
	private ClusteredRedisCache(RedisCacheConfig config) {
		this.config = config;
	}
	
	public static ClusteredRedisCache instance(String namespace) throws CacheCreateException {
		return instance(namespace, null);
	}
	
	/**
	 * 创建缓存实例
	 * 
	 * @param namespace		缓存名称
	 * @param serializer	序列化器
	 * @return
	 * @throws CacheCreateException
	 */
	public static ClusteredRedisCache instance(String namespace, Serializer serializer) throws CacheCreateException {
		if (StringUtils.isBlank(namespace)) {
			throw new CacheCreateException("ClusteredRedisCache create exception: create parameter namespace is empty.");
		}
		
		RedisCacheConfig config = new RedisCacheConfig();
		config.setSerializer(serializer == null ? new KryoSerializer() : serializer);
		config.setDb(0);
		config.setNamespace(namespace);
		ClusteredRedisCache cache = new ClusteredRedisCache(config);		
		ClusteredRedisCache existed = cacheMap.putIfAbsent(namespace, cache);
		return existed == null ? cache : existed;
	}

	@Override
	public <T> int put(Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		Serializer serializer = config.getSerializer();
		String namespace = config.getNamespace();
		if (keyValue == null || keyValue.size() == 0) {
			log.error("ClusteredRedisCache put参数错误: keyValue={}", JSON.toJSONString(keyValue));
			throw new CacheOperateException(namespace, "ClusteredRedisCache put参数错误: keyValue=" + JSON.toJSONString(keyValue));
		}
		Map<byte[], byte[]> serialedMap = new HashMap<byte[], byte[]>();
		for (Entry<String, T> entry : keyValue.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey())) {
				try {
					serialedMap.put(entry.getKey().getBytes(), serializer.serialize(entry.getValue()));
				}
				catch (IOException e) {
					log.error("序列化失败: " + namespace + "." + entry.getKey(), e);
				}
			}
		}
		byte[] nameKey = namespace.getBytes();
		if (condition == CacheSaveConditionEnum.IF_NOT_EXISTS) {
			for (Entry<byte[], byte[]> entry : serialedMap.entrySet()) {
				jedisCluster.hsetnx(nameKey, entry.getKey(), entry.getValue());
			}
		}
		else {
			jedisCluster.hmset(nameKey, serialedMap);
		}
		return serialedMap.size();
	}

	@Override
	public <T> List<T> get(String[] keys, Class<T> clazz) throws CacheOperateException {
		Serializer serializer = config.getSerializer();
		String namespace = config.getNamespace();
		List<T> list = new ArrayList<T>();
		if (keys == null || keys.length == 0 || clazz == null) {
			log.error("ClusteredRedisCache get参数错误: keys={}, clazz={}", StringUtils.join(keys), clazz);
			throw new CacheOperateException(namespace, "ClusteredRedisCache get参数错误: keys=" + keys + ",clazz=" + clazz);
		}
		List<byte[]> serialedKeyList = new ArrayList<byte[]>();
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				serialedKeyList.add(key.getBytes());
			}
		}
		List<byte[]> valueList = jedisCluster.hmget(namespace.getBytes(), (byte[][])serialedKeyList.toArray());
		if (CollectionUtils.isNotEmpty(valueList)) {
			for (int i = 0; i < valueList.size(); i++) {
				try {
					list.add(serializer.deSerialize(valueList.get(i), clazz));
				}
				catch (IOException e) {
					log.error("反序列化失败: " + keys[i], e);
				}
			}
		}
		return list;
	}

	@Override
	public int evict(String[] keys) throws CacheOperateException {
		String namespace = config.getNamespace();
		if (keys == null || keys.length == 0) {
			log.error("ClusteredRedisCache evict参数错误: keys={}", StringUtils.join(keys));
			throw new CacheOperateException(namespace, "ClusteredRedisCache evict参数错误: keys=" + keys);
		}
		List<byte[]> serialedKeyList = new ArrayList<byte[]>();
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				serialedKeyList.add(key.getBytes());
			}
		}
		long delCount = jedisCluster.hdel(namespace.getBytes(), (byte[][])serialedKeyList.toArray());
		return (int)delCount;
	}

	@Override
	public int clear() throws CacheOperateException {
		byte[] key = config.getNamespace().getBytes();
		long mapLength = jedisCluster.hlen(key);
		jedisCluster.del(key);
		return (int)mapLength;
	}

	@Override
	public CacheInfo stat() throws CacheOperateException {
		return new CacheInfo();
	}
	
	@Override
	public String getNamespace() {
		return config == null ? null : config.getNamespace();
	}
	
	public JedisCluster getJedisCluster() {
		return jedisCluster;
	}
	
	public void setJedisCluster(JedisCluster cluster) {
		jedisCluster = cluster;
	}

}
