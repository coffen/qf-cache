package com.qf.cache.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.Cache;
import com.qf.cache.CacheContext;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheOperation;
import com.qf.cache.CacheUnit;
import com.qf.cache.exception.CacheNotExistsException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.operation.CacheClearOperation;
import com.qf.cache.operation.CacheEvictOperation;
import com.qf.cache.operation.CacheGetOperation;
import com.qf.cache.operation.CacheSaveOperation;
import com.qf.cache.operation.CacheStatOperation;
import com.qf.cache.util.ClasspathPackageScanner;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 抽象缓存上下文
 * <br>
 * File Name: AbstractCacheContext.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月3日 下午2:25:57 
 * @version: v1.0
 *
 */
public abstract class AbstractCacheContext implements CacheContext {
	
	private static Logger log = LoggerFactory.getLogger(AbstractCacheContext.class);
	
	private Map<CacheUnit, Cache> cacheMap = new HashMap<CacheUnit, Cache>();	
	private CacheCascadeConfig config;
	
	private String scannedPackage;
	
	public AbstractCacheContext(String pkg) {
		this.scannedPackage = pkg;
		
		init();
	}
	
	private void init() {
		config = new CacheCascadeConfig();
		ClasspathPackageScanner scanner = new ClasspathPackageScanner(scannedPackage);
		try {
			List<String> clazzList = scanner.getFullyQualifiedClassNameList();
			config.parse(clazzList);
		} 
		catch (Exception e) {
			log.error("缓存上下文初始化错误", e);
		}
	}

	@Override
	public void save(CacheSaveOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
		cache.put(operation.getKeyValue(), operation.getExpire(), operation.getCondition());
	}

	@Override
	public <T> Map<String, T> get(CacheGetOperation operation, Class<T> clazz) throws CacheNotExistsException, CacheOperateException {	
		Cache cache = getCache(operation);
		String namespace = operation.getNamespace();
		String[] keys = operation.getKeys();
		if (clazz == null) {
			throw new CacheOperateException(namespace, "Cache batch get result error: target class is null.");
		}	
		List<T> objList = cache.get(keys, clazz);
		if (objList == null || objList.size() != keys.length) {
			throw new CacheOperateException(namespace, "Cache batch get result error: operation is null or not conform the input keys length.");
		}
		Map<String, T> result = new HashMap<String, T>();
		for (int i = 0; i < objList.size(); i++) {
			T obj = objList.get(i);
			result.put(keys[i], obj);
		}
		return result;
	}

	@Override
	public void evict(CacheEvictOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
		cache.evict(operation.getKeys());
	}

	@Override
	public void clear(CacheClearOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
		cache.clear();
	}

	@Override
	public CacheInfo stat(CacheStatOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
		return cache.stat();
	}
	
	private Cache getCache(CacheOperation operation) throws CacheNotExistsException {
		if (operation == null || StringUtils.isBlank(operation.getNamespace())) {
			throw new CacheNotExistsException("Cache namespace is empty.");
		}
		Cache cache = cacheMap.get(operation.getNamespace());
		if (cache == null) {
			throw new CacheNotExistsException("Cache not found: " + operation.getNamespace());
		}
		return cache;
	}

}
