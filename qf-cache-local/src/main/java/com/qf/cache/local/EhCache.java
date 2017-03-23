package com.qf.cache.local;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.ehcache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.Cache;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheSaveConditionEnum;
import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;

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
	
	private Logger log = LoggerFactory.getLogger(EhCache.class);
	
	private CacheManager cacheManager = null;
	
	public EhCache(CacheManager mgr) throws CacheCreateException {
		if (mgr == null) {
			throw new CacheCreateException("Ehcache create exception: cacheManager parameter is null.");
		}
		this.cacheManager = mgr;
	}

	@Override
	public int put(String name, Map<String, Object> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		if (StringUtils.isBlank(name) || keyValue == null || keyValue.size() == 0) {
			log.error("EhCache put参数错误: name={}, keyValue={}", name, keyValue);
			throw new CacheOperateException(name, "EhCache put参数错误: name=" + name + ",keyValue=" + keyValue);
		}
		org.ehcache.Cache<String, Object> cache = cacheManager.getCache(name, String.class, Object.class);
		if (cache == null) {
			throw new CacheOperateException(name, "Ehcache not existed.");
		}
		int cacheCount = 0;
		for (Entry<String, Object> entry : keyValue.entrySet()) {
			if (StringUtils.isNotBlank(entry.getKey())) {
				cache.put(entry.getKey(), entry.getValue());
				cacheCount++;
			}
		}		
		return cacheCount;
	}

	@Override
	public List<Object> get(String name, String[] keys) throws CacheOperateException {
		List<Object> list = new ArrayList<Object>();
		if (StringUtils.isBlank(name) || keys == null || keys.length == 0) {
			log.error("EhCache get参数错误: name={}, keys={}", name, keys);
			throw new CacheOperateException(name, "EhCache get参数错误: name=" + name + ",keys=" + keys);
		}
		org.ehcache.Cache<String, Object> cache = cacheManager.getCache(name, String.class, Object.class);
		if (cache == null) {
			throw new CacheOperateException(name, "Ehcache not existed.");
		}
		for (String key : keys) {
			Object value = null;
			if (StringUtils.isNotBlank(key)) {
				value = cache.get(key);
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

}
