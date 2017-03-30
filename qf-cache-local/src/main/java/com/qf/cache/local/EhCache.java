package com.qf.cache.local;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.Cache;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheSaveConditionEnum;
import com.qf.cache.Serializer;
import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.serializer.KryoSerializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: EhCache
 * <br>
 * File Name: EhCache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月23日 下午4:40:38 
 * @version: v1.0
 *
 */
public class EhCache implements Cache {
	
	private static Logger log = LoggerFactory.getLogger(EhCache.class);
	
	private static long DEFAULT_POOL_SIZE = 1000;
	
	private static CacheManager cacheManager = null;
	private Serializer serializer = null;
	
	private String namespace;
	
	static {
		cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build();
		cacheManager.init();
	}
	
	private EhCache() {}
	
	public static EhCache instance(String namespace) throws CacheCreateException {
		return instance(namespace, DEFAULT_POOL_SIZE, 0, null);
	}
	
	public static EhCache instance(String namespace, long heapSize, long expireTime) throws CacheCreateException {
		return instance(namespace, heapSize, expireTime, null);
	}
	
	public static EhCache instance(String namespace, long heapSize, long expireTime, Serializer serializer) throws CacheCreateException {
		if (StringUtils.isBlank(namespace) || heapSize < 10 || expireTime < 0) {
			throw new CacheCreateException("Ehcache create exception: create parameter is invalid.");
		}
		
		CacheConfigurationBuilder<String, byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class, ResourcePoolsBuilder.heap(heapSize));
		if (expireTime > 0) {
			builder.withExpiry(Expirations.timeToIdleExpiration(new Duration(expireTime, TimeUnit.MINUTES)));
		}
		CacheConfiguration<String, byte[]> config = builder.build();
		try {
			cacheManager.createCache(namespace, config);
		}
		catch (IllegalArgumentException e) {
			log.error("Ehcache create exception: cache with namespace[" + namespace + "] already exists." , e);
			throw new CacheCreateException("Ehcache create exception: cache with namespace[" + namespace + "] already exists.");
		}
		catch (IllegalStateException e) {
			log.error("Ehcache create exception: create failed." , e);
			throw new CacheCreateException("Ehcache create exception: create failed.");
		}

		EhCache cache = new EhCache();
		cache.namespace = namespace;
		cache.serializer = serializer == null ? new KryoSerializer() : serializer;
		return cache;
	}

	@Override
	public <T> int put(String name, Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		if (StringUtils.isBlank(name) || keyValue == null || keyValue.size() == 0) {
			log.error("EhCache put参数错误: name={}, keyValue={}", name, keyValue);
			throw new CacheOperateException(name, "EhCache put参数错误: name=" + name + ",keyValue=" + keyValue);
		}
		org.ehcache.Cache<String, byte[]> cache = cacheManager.getCache(name, String.class, byte[].class);
		if (cache == null) {
			log.error("EhCache put错误, 指定cache不存在: name={}", name);
			throw new CacheOperateException(name, "Ehcache not existed.");
		}
		int cacheCount = 0;
		for (Entry<String, T> entry : keyValue.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey())) {
				try {
					cache.put(entry.getKey(), serializer.serialize(entry.getValue()));
				}
				catch (IOException e) {
					log.error("序列化失败: " + entry.getKey(), e);
				}
				cacheCount++;
			}
		}		
		return cacheCount;
	}

	@Override
	public <T> List<T> get(String name, String[] keys, Class<T> clazz) throws CacheOperateException {
		List<T> list = new ArrayList<T>();
		if (StringUtils.isBlank(name) || keys == null || keys.length == 0) {
			log.error("EhCache get参数错误: name={}, keys={}", name, keys);
			throw new CacheOperateException(name, "EhCache get参数错误: name=" + name + ",keys=" + keys);
		}
		org.ehcache.Cache<String, byte[]> cache = cacheManager.getCache(name, String.class, byte[].class);
		if (cache == null) {
			log.error("EhCache get错误, 指定cache不存在: name={}", name);
			throw new CacheOperateException(name, "Ehcache not existed.");
		}
		for (String key : keys) {
			T value = null;
			if (StringUtils.isNotBlank(key)) {
				byte[] bytes = cache.get(key);
				if (bytes != null) {
					try {
						value = serializer.deSerialize(bytes, clazz);
					}
					catch (IOException e) {
						log.error("序列化失败: " + key, e);
					}
				}
			}
			list.add(value);
		}
		return list;
	}

	@Override
	public int evict(String name, String[] keys) throws CacheOperateException {
		if (StringUtils.isBlank(name) || keys == null || keys.length == 0) {
			log.error("EhCache evict参数错误: name={}, keys={}", name, keys);
			throw new CacheOperateException(name, "EhCache evict参数错误: name=" + name + ",keys=" + keys);
		}
		org.ehcache.Cache<String, Object> cache = cacheManager.getCache(name, String.class, Object.class);
		int cacheCount = 0;
		for (String key : keys) {
			if (StringUtils.isNotBlank(key)) {
				cache.remove(key);
				cacheCount++;
			}
		}
		return cacheCount;
	}

	@Override
	public int clear(String name) throws CacheOperateException {
		if (StringUtils.isBlank(name)) {
			log.error("EhCache clear参数错误: name={}", name);
			throw new CacheOperateException(name, "EhCache clear参数错误: name=" + name);
		}
		org.ehcache.Cache<String, Object> cache = cacheManager.getCache(name, String.class, Object.class);
		Iterator<org.ehcache.Cache.Entry<String, Object>> it = cache.iterator();
		int clearCount = 0;
		while (it.hasNext()) {
			clearCount++;
		}
		cache.clear();
		return clearCount;
	}

	@Override
	public CacheInfo stat(String name) throws CacheOperateException {
		if (StringUtils.isBlank(name)) {
			log.error("EhCache stat参数错误: name={}", name);
			throw new CacheOperateException(name, "EhCache stat参数错误: name=" + name);
		}
		return new CacheInfo();
	}
	
	public String getNamespace() {
		return namespace;
	}

}
