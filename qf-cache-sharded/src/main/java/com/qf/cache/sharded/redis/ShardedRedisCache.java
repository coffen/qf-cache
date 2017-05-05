package com.qf.cache.sharded.redis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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

import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Redis分片缓存（轻量级集群）
 * <br>
 * File Name: ShardedRedisCache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年4月15日 下午2:37:47 
 * @version: v1.0
 *
 */
public class ShardedRedisCache implements Cache {
	
	private static Logger log = LoggerFactory.getLogger(ShardedRedisCache.class);
	
	private static ShardedJedisPool shardedJedisPool;
	private static ConcurrentHashMap<String, ShardedRedisCache> cacheMap = new ConcurrentHashMap<String, ShardedRedisCache>();
	
	private RedisCacheConfig config;
	
	private ShardedRedisCache(RedisCacheConfig config) {
		this.config = config;
	}
	
	public static ShardedRedisCache instance(String namespace, Integer db) throws CacheCreateException {
		return instance(namespace, db, null);
	}
	
	/**
	 * 创建缓存实例
	 * 
	 * @param namespace		缓存名称
	 * @param db			库名
	 * @param serializer	序列化器
	 * @return
	 * @throws CacheCreateException
	 */
	public static ShardedRedisCache instance(String namespace, Integer db, Serializer serializer) throws CacheCreateException {
		if (StringUtils.isBlank(namespace) || db == null || db < 0) {
			throw new CacheCreateException("ShardedRedisCache create exception: create parameter is valid: namespace=" + namespace + ", db=" + db);
		}
		
		RedisCacheConfig config = new RedisCacheConfig();
		config.setSerializer(serializer == null ? new KryoSerializer() : serializer);
		config.setDb(db);
		config.setNamespace(namespace);
		ShardedRedisCache cache = new ShardedRedisCache(config);		
		ShardedRedisCache existed = cacheMap.putIfAbsent(namespace, cache);
		return existed == null ? cache : existed;
	}

	@Override
	public <T> int put(Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		Serializer serializer = config.getSerializer();
		String namespace = config.getNamespace();
		if (keyValue == null || keyValue.size() == 0) {
			log.error("ShardedRedisCache put参数错误: keyValue={}", JSON.toJSONString(keyValue));
			throw new CacheOperateException(namespace, "ShardedRedisCache put参数错误: keyValue=" + JSON.toJSONString(keyValue));
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
		try (ShardedJedis shardedJedis = getShardedJedis(config.getDb())) {
			byte[] nameKey = namespace.getBytes();		
			if (condition == CacheSaveConditionEnum.IF_NOT_EXISTS) {
				for (Entry<byte[], byte[]> entry : serialedMap.entrySet()) {
					shardedJedis.hsetnx(nameKey, entry.getKey(), entry.getValue());
				}
			}
			else {
				shardedJedis.hmset(nameKey, serialedMap);
			}
		}
		return serialedMap.size();
	}

	@Override
	public <T> List<T> get(String[] keys, Class<T> clazz) throws CacheOperateException {
		Serializer serializer = config.getSerializer();
		String namespace = config.getNamespace();
		List<T> list = new ArrayList<T>();
		if (keys == null || keys.length == 0 || clazz == null) {
			log.error("ShardedRedisCache get参数错误: keys={}, clazz={}", StringUtils.join(keys), clazz);
			throw new CacheOperateException(namespace, "ShardedRedisCache get参数错误: keys=" + keys + ",clazz=" + clazz);
		}
		List<byte[]> serialedKeyList = new ArrayList<byte[]>();
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				serialedKeyList.add(key.getBytes());
			}
		}
		if (serialedKeyList.size() > 0) {
			byte[][] serialedKeyArr = new byte[serialedKeyList.size()][];
			for (int i = 0; i < serialedKeyList.size(); i++) {
				serialedKeyArr[i] = serialedKeyList.get(i);
			}
			try (ShardedJedis shardedJedis = getShardedJedis(config.getDb())) {
				List<byte[]> valueList = shardedJedis.hmget(namespace.getBytes(), serialedKeyArr);
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
			}
		}
		return list;
	}

	@Override
	public int evict(String[] keys) throws CacheOperateException {
		String namespace = config.getNamespace();
		long delCount = 0L;
		if (keys == null || keys.length == 0) {
			log.error("ShardedRedisCache evict参数错误: keys={}", StringUtils.join(keys));
			throw new CacheOperateException(namespace, "ShardedRedisCache evict参数错误: keys=" + keys);
		}
		List<byte[]> serialedKeyList = new ArrayList<byte[]>();
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				serialedKeyList.add(key.getBytes());
			}
		}
		if (serialedKeyList.size() > 0) {
			byte[][] serialedKeyArr = new byte[serialedKeyList.size()][];
			for (int i = 0; i < serialedKeyList.size(); i++) {
				serialedKeyArr[i] = serialedKeyList.get(i);
			}
			try (ShardedJedis shardedJedis = getShardedJedis(config.getDb())) {
				delCount = shardedJedis.hdel(namespace.getBytes(), serialedKeyArr);
			}
		}
		return (int)delCount;
	}

	@Override
	public int clear() throws CacheOperateException {
		byte[] key = config.getNamespace().getBytes();
		long mapLength = 0;
		try (ShardedJedis shardedJedis = getShardedJedis(config.getDb())) {
			mapLength = shardedJedis.hlen(key);
			shardedJedis.decr(key);
		}
		return (int)mapLength;
	}

	@Override
	public CacheInfo stat() throws CacheOperateException {
		return new CacheInfo();
	}

	@Override
	public String getNamespace() {
		return null;
	}
	
	private ShardedJedis getShardedJedis(int db) {
		ShardedJedis shardedJedis = shardedJedisPool.getResource();
		Collection<Jedis> jedisCol = shardedJedis.getAllShards();
		if (CollectionUtils.isNotEmpty(jedisCol)) {
			for (Jedis jedis : jedisCol) {
				jedis.select(db);
			}
		}
		return shardedJedis;
	}
	
	public ShardedJedisPool getShardedJedisPool() {
		return shardedJedisPool;
	}
	
	public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
		ShardedRedisCache.shardedJedisPool = shardedJedisPool;
	}

}
