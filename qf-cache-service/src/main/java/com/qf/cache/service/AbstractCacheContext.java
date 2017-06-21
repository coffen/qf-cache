package com.qf.cache.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.CollectionUtils;
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
import com.qf.cache.operation.CacheFieldOperation;
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
	
	private ConcurrentHashMap<String, Cache> cacheMap = new ConcurrentHashMap<String, Cache>();	
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
	
	public void addCache(Cache cache) {
		if (cache == null) {
			log.error("添加缓存错误, 参数为空");
		}
		CacheUnit unit = cache.getCacheUnit();
		if (unit == null) {
			log.error("缓存单元获取失败");
		}
		if (cacheMap.containsKey(unit.getNamespace())) {
			log.error("缓存已添加: {}", unit.getNamespace());
		}
		cacheMap.putIfAbsent(unit.getNamespace(), cache);
	}

	@Override
	public void save(CacheSaveOperation operation) throws CacheNotExistsException, CacheOperateException {
		if (operation == null || operation.getKeyValue() == null) {
			throw new CacheOperateException(operation == null ? "" : operation.getNamespace(), "Cache save result error: operation is null or valid.");
		}
		CacheKeyHolder holder = buildHolder();
		save(operation, holder);
	}
	
	private void save(CacheSaveOperation operation, CacheKeyHolder holder) throws CacheNotExistsException, CacheOperateException {
		String namespace = operation.getNamespace();
		Map<String, Object> copyedKeyValue = convertKeyValue(operation);
		if (StringUtils.isNotBlank(namespace)) {
			Set<String> existedKey = holder.addCacheKeySet(namespace, copyedKeyValue.keySet());
			copyedKeyValue.keySet().removeAll(existedKey);
			if (copyedKeyValue.size() > 0) {
				Cache cache = getCache(operation);
				cache.put(copyedKeyValue, operation.getExpire(), operation.getCondition());
			}
		}
		for (Entry<String, Object> entry : operation.getKeyValue().entrySet()) {
			String key = entry.getKey();
			Object cacheObject = entry.getValue();
			if (cacheObject == null) {
				continue;
			}
			String[] namespaces = config.getTargetNamespace(cacheObject.getClass());
			if (namespaces != null && namespaces.length > 0) {
				Map<String, Object> singleMap = new HashMap<String, Object>();
				Cache cache = null;
				for (String ns : namespaces) {
					if (holder.addCacheKey(ns, key)) {
						cache = getCache(ns);
						singleMap.clear();
						singleMap.put(key, copyedKeyValue.get(key));
						cache.put(singleMap, operation.getExpire(), operation.getCondition());
					}
				}
			}
		}
		cascadeSaveFields(operation, holder);
	}
	
	private Map<String, Object> convertKeyValue(CacheSaveOperation operation) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (operation != null && operation.getKeyValue() != null) {
			for (Entry<String, Object> entry : operation.getKeyValue().entrySet()) {
				map.put(entry.getKey(), config.copyBean(entry.getValue()));
			}
		}
		return map;
	}
	
	private void cascadeSaveFields(CacheSaveOperation operation, CacheKeyHolder holder) throws CacheNotExistsException, CacheOperateException {
		for (Entry<String, Object> entry : operation.getKeyValue().entrySet()) {
			Object cacheObject = entry.getValue();
			if (cacheObject == null) {
				continue;
			}
			List<CacheFieldOperation> operationList = config.getFieldsOperation(cacheObject, cacheObject.getClass());
			if (CollectionUtils.isNotEmpty(operationList)) {
				for (CacheFieldOperation cfo : operationList) {
					if (holder.containsCacheKey(cfo.getNamespace(), cfo.getKey())) {
						continue;
					}
					CacheSaveOperation saveOperation = null;
					try {
						saveOperation = cfo.generateCacheSaveOperation(cfo.getKey(), cacheObject, operation.getExpire(), operation.getCondition());
					}
					catch (Exception e) {
						log.error("生成级联保存操作错误: " + cfo.getFieldName(), e);
						throw new CacheOperateException(cfo.getNamespace(), "生成级联保存操作错误: " + cfo.getFieldName());
					}
					if (saveOperation != null) {
						save(saveOperation, holder);
					}
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Map<String, T> get(CacheGetOperation operation, Class<T> clazz) throws CacheNotExistsException, CacheOperateException {
		if (operation == null || StringUtils.isBlank(operation.getNamespace()) || operation.getKeys() == null || operation.getKeys().length == 0) {
			throw new CacheOperateException(operation == null ? "" : operation.getNamespace(), "Cache batch get result error: operation is null or valid.");
		}
		Cache cache = getCache(operation);
		String namespace = operation.getNamespace();
		String[] keys = operation.getKeys();
		if (clazz == null) {
			throw new CacheOperateException(namespace, "Cache batch get result error: target class is null.");
		}
		Class<?> serializeClazz = config.getTargetClass(clazz);
		if (serializeClazz == null) {
			serializeClazz = Object.class;
		}
		List<?> objList = cache.get(keys, serializeClazz);
		if (objList == null || objList.size() != keys.length) {
			throw new CacheOperateException(namespace, "Cache batch get result error: operation is null or not conform the input keys length.");
		}
		Map<String, T> result = new HashMap<String, T>();
		for (int i = 0; i < objList.size(); i++) {
			Object obj = objList.get(i);
			Object copyed = config.reverseCopyBean(obj);
			fulfillFields(cache, copyed, clazz);
			T t = null;
			if (copyed != null) {
				t = (T)copyed;
			}
			result.put(keys[i], t);
		}
		return result;
	}
	
	private void fulfillFields(Cache cache, Object obj, Class<?> clazz) throws CacheNotExistsException, CacheOperateException {
		List<CacheFieldOperation> operationList = config.getFieldsOperation(obj, clazz);
		if (CollectionUtils.isNotEmpty(operationList)) {
			for (CacheFieldOperation fieldOperation : operationList) {
				if (fieldOperation == null) {
					continue;
				}
				CacheGetOperation getOperation = fieldOperation.generateCacheGetOperation();
				Map<String, ?> fieldValue = get(getOperation, fieldOperation.getFieldClass());
				if (obj != null && fieldValue != null) {
					try {
						fieldOperation.setValue(obj, fieldValue.get(fieldOperation.getKey()));
					}
					catch (Exception e) {
						log.error("设置缓存对象级联属性错误: " + fieldOperation.getFieldName(), e);
						throw new CacheOperateException(fieldOperation.getNamespace(), "设置缓存对象级联属性错误: " + fieldOperation.getFieldName());
					}
				}
			}
		}
	}

	@Override
	public void evict(CacheEvictOperation operation) throws CacheNotExistsException, CacheOperateException {
		if (operation == null || StringUtils.isBlank(operation.getNamespace()) || operation.getKeys() == null) {
			throw new CacheOperateException(operation == null ? "" : operation.getNamespace(), "Cache evict result error: operation is null or valid.");
		}
		Cache cache = getCache(operation);
		cache.evict(operation.getKeys());
	}

	@Override
	public void clear(CacheClearOperation operation) throws CacheNotExistsException, CacheOperateException {
		if (operation == null || StringUtils.isBlank(operation.getNamespace())) {
			throw new CacheOperateException(operation == null ? "" : operation.getNamespace(), "Cache clear result error: operation is null or valid.");
		}
		Cache cache = getCache(operation);
		cache.clear();
	}

	@Override
	public CacheInfo stat(CacheStatOperation operation) throws CacheNotExistsException, CacheOperateException {
		if (operation == null || StringUtils.isBlank(operation.getNamespace())) {
			throw new CacheOperateException(operation == null ? "" : operation.getNamespace(), "Cache stat result error: operation is null or valid.");
		}
		Cache cache = getCache(operation);
		return cache.stat();
	}
	
	private Cache getCache(CacheOperation operation) throws CacheNotExistsException {
		if (operation == null) {
			throw new CacheNotExistsException("Cache operation is null.");
		}
		return getCache(operation.getNamespace());
	}
	
	private Cache getCache(String namespace) throws CacheNotExistsException {
		if (StringUtils.isBlank(namespace)) {
			throw new CacheNotExistsException("Cache namespace is empty.");
		}
		Cache cache = cacheMap.get(namespace);
		if (cache == null) {
			throw new CacheNotExistsException("Cache not found: " + namespace);
		}
		return cache;
	}
	
	private CacheKeyHolder buildHolder() {
		return new CacheKeyHolder();
	}
	
	/**
	 * 记录存储的namespace和key, 用于防止重复操作
	 */
	class CacheKeyHolder {
		
		private Map<String, Set<String>> map = new HashMap<String, Set<String>>();
		
		// 添加待缓存key集合, 返回已经存在的部分
		Set<String> addCacheKeySet(String namespace, Set<String> keySet) {
			Set<String> existed = new HashSet<String>();
			if (StringUtils.isNotBlank(namespace) && CollectionUtils.isNotEmpty(keySet)) {
				Set<String> set = map.get(namespace);
				if (set == null) {
					set = new HashSet<String>();
					map.put(namespace, set);
				}
				for (String key : keySet) {
					if (StringUtils.isBlank(key)) {
						continue;
					}
					if (!set.add(key)) {
						existed.add(key);
					}
				}
			}
			return existed;
		}
		
		// 添加待缓存key, 如存在则返回true, 不存在返回false
		boolean addCacheKey(String namespace, String key) {
			if (StringUtils.isBlank(namespace) || StringUtils.isBlank(key)) {
				return false;
			}
			Set<String> set = map.get(namespace);
			if (set == null) {
				set = new HashSet<String>();
				map.put(namespace, set);
			}
			return set.add(key);
		}
		
		// 返回是否存在待缓存key, 如存在则返回true, 不存在返回false
		boolean containsCacheKey(String namespace, String key) {
			if (StringUtils.isBlank(namespace) || StringUtils.isBlank(key)) {
				return false;
			}
			Set<String> set = map.get(namespace);
			if (set == null) {
				return false;
			}
			return set.contains(key);
		}
	}

}
