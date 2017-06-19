package com.qf.cache.sharded.memcache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.qf.cache.Cache;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheSaveConditionEnum;
import com.qf.cache.CacheUnit;
import com.qf.cache.Serializer;
import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.serializer.kryo.KryoSerializer;

import net.rubyeye.xmemcached.MemcachedClient;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Memcache集群
 * <br>
 * File Name: ClusteredMemcache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年5月7日 下午3:32:37 
 * @version: v1.0
 *
 */
public class ClusteredMemcache implements Cache {
	
	private static Logger log = LoggerFactory.getLogger(ClusteredMemcache.class);
	
	private static MemcachedClient memcachedClient;
	private static ConcurrentHashMap<String, ClusteredMemcache> cacheMap = new ConcurrentHashMap<String, ClusteredMemcache>();
	
	private MemcacheConfig config;
	
	private ClusteredMemcache(MemcacheConfig config) {
		this.config = config;
	}
	
	public static ClusteredMemcache instance(String namespace) throws CacheCreateException {
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
	public static ClusteredMemcache instance(String namespace, Serializer serializer) throws CacheCreateException {
		if (StringUtils.isBlank(namespace)) {
			throw new CacheCreateException("ClusteredMemcache create exception: create parameter namespace is empty.");
		}
		
		MemcacheConfig config = new MemcacheConfig();
		config.setSerializer(serializer == null ? new KryoSerializer() : serializer);
		config.setNamespace(namespace);
		ClusteredMemcache cache = new ClusteredMemcache(config);		
		ClusteredMemcache existed = cacheMap.putIfAbsent(namespace, cache);
		return existed == null ? cache : existed;
	}

	@Override
	public <T> int put(Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		Serializer serializer = config.getSerializer();
		String namespace = config.getNamespace();
		int exp = expire == null ? 0 : expire.intValue();
		if (keyValue == null || keyValue.size() == 0) {
			log.error("ClusteredMemcache put参数错误: keyValue={}", JSON.toJSONString(keyValue));
			throw new CacheOperateException(namespace, "ClusteredMemcache put参数错误: keyValue=" + JSON.toJSONString(keyValue));
		}
		Map<String, byte[]> serialedMap = new HashMap<String, byte[]>();
		for (Entry<String, T> entry : keyValue.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey())) {
				String nameKey = buildKey(namespace, entry.getKey());
				try {
					serialedMap.put(nameKey, serializer.serialize(entry.getValue()));
				}
				catch (IOException e) {
					log.error("序列化失败: " + namespace + "." + entry.getKey(), e);
				}
			}
		}
		if (condition == CacheSaveConditionEnum.IF_NOT_EXISTS) {
			for (Entry<String, byte[]> entry : serialedMap.entrySet()) {
				try {
					memcachedClient.add(entry.getKey(), exp, entry.getValue());
				}
				catch (Exception e) {
					log.error("MemcachedClient.add异常: key=" + entry.getKey(), e);
				}
			}
		}
		else if (condition == CacheSaveConditionEnum.IF_EXISTS) {
			for (Entry<String, byte[]> entry : serialedMap.entrySet()) {
				try {
					memcachedClient.replace(entry.getKey(), exp, entry.getValue());
				}
				catch (Exception e) {
					log.error("MemcachedClient.replace异常: key=" + entry.getKey(), e);
				}
			}
		}
		else {
			for (Entry<String, byte[]> entry : serialedMap.entrySet()) {
				try {
					memcachedClient.set(entry.getKey(), exp, entry.getValue());
				}
				catch (Exception e) {
					log.error("MemcachedClient.set异常: key=" + entry.getKey(), e);
				}
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
			log.error("ClusteredMemcache get参数错误: keys={}, clazz={}", StringUtils.join(keys), clazz);
			throw new CacheOperateException(namespace, "ClusteredMemcache get参数错误: keys=" + keys + ",clazz=" + clazz);
		}
		List<String> serialedKeyList = new ArrayList<String>();
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				serialedKeyList.add(buildKey(namespace, key));
			}
		}
		if (serialedKeyList.size() > 0) {
			Map<String, byte[]> map = null;
			try {
				map = memcachedClient.get(serialedKeyList);
			}
			catch (Exception e) {
				log.error("MemcachedClient.get异常: keys=" + StringUtils.join(keys), e);
				throw new CacheOperateException(namespace, "MemcachedClient.get异常: keys=" + StringUtils.join(keys));
			}
			if (map != null && map.size() > 0) {
				for (Entry<String, byte[]> entry : map.entrySet()) {
					if (entry.getValue() != null) {
						try {
							T t = serializer.deSerialize(entry.getValue(), clazz);
							list.add(t);
						}
						catch (IOException e) {
							log.error("反序列化失败: " + entry.getKey(), e);
						}
					}
					else {
						list.add(null);
					}
				}
			}
		}
		return list;
	}

	@Override
	public int evict(String[] keys) throws CacheOperateException {
		String namespace = config.getNamespace();
		long delCount = 0;
		if (keys == null || keys.length == 0) {
			log.error("ClusteredMemcache evict参数错误: keys={}", StringUtils.join(keys));
			throw new CacheOperateException(namespace, "ClusteredMemcache evict参数错误: keys=" + keys);
		}
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				String nameKey = buildKey(namespace, key);
				try {
					memcachedClient.delete(nameKey);
					delCount++;
				}
				catch (Exception e) {
					log.error("memcachedClient.delete异常: " + nameKey, e);
				}
			}
		}
		return (int)delCount;
	}

	@Override
	public int clear() throws CacheOperateException {
		return 0;
	}

	@Override
	public CacheInfo stat() throws CacheOperateException {
		return new CacheInfo();
	}

	@Override
	public CacheUnit getCacheUnit() {
		return new CacheUnit(config.getNamespace(), config.getSerializer());
	}
	
	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}
	
	public void setMemcachedClient(MemcachedClient memcachedClient) {
		ClusteredMemcache.memcachedClient = memcachedClient;
	}
	
	private String buildKey(String namespace, String key) {
		return namespace + ":" + key;
	}

}
