package com.qf.cache.service;

import java.util.ArrayList;
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
		CacheKeyHolder holder = buildKeyHolder();
		save(operation, holder);
	}
	
	/**
	 * 首先按CacheSaveOperation设定的namespace保存对象, 然后按缓存对象类的CacheType注解指定的namespace保存
	 * 
	 * CacheKeyHolder用于存储当前操作已缓存的对象, 防止类相互引用时出现的死循环操作
	 * 
	 * @param operation
	 * @param holder
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
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
					// 添加成功, 则进行缓存操作
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
	
	/**
	 * 复制原对象, 生成代理类的对象, 序列化器根据代理类来决定过滤哪些属性
	 * 
	 * @param operation
	 * @return
	 */
	private Map<String, Object> convertKeyValue(CacheSaveOperation operation) {
		Map<String, Object> map = new HashMap<String, Object>();
		if (operation != null && operation.getKeyValue() != null) {
			for (Entry<String, Object> entry : operation.getKeyValue().entrySet()) {
				map.put(entry.getKey(), config.copyBean(entry.getValue()));
			}
		}
		return map;
	}
	
	/**
	 * <p>级联缓存类属性</p> 
	 * 
	 * <p>类属性的级联操作首先需要设定关联属性（keyField）, ShopInfo是Product对象中待级联缓存的属性, 
	 * shopId的值（需转换为String）就是ShopInfo缓存的key; 如果关联属性未指定或者值为空, 则ShopInfo无需缓存</p>
	 * 
	 * Product
	 *  |-- shopId (Key)
	 *  |-- ShopInfo (CacheField)
	 * 
	 * @param operation
	 * @param holder
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
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
	public <T> Map<String, T> get(CacheGetOperation operation, Class<T> clazz) throws CacheNotExistsException, CacheOperateException {
		if (operation == null || operation.getKeys() == null || operation.getKeys().length == 0) {
			throw new CacheOperateException(operation == null ? "" : operation.getNamespace(), "Cache batch get result error: operation is null or valid.");
		}
		CacheObjectHolder holder = buildObjectHolder();
		return get(operation, clazz, holder);
	}
	
	/**
	 * 首先按CacheGetOperation指定的namespace加载缓存, 如未设定则按缓存对象类的CacheType注解指定的namespace加载
	 * 读取的缓存对象由于是按代理类进行序列化的, 需要反向复制为原生类的对象
	 * 
	 * @param operation
	 * @param clazz
	 * @param holder
	 * @return
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	@SuppressWarnings("unchecked")
	private <T> Map<String, T> get(CacheGetOperation operation, Class<T> clazz, CacheObjectHolder holder) throws CacheNotExistsException, CacheOperateException {
		if (clazz == null) {
			throw new CacheOperateException(operation.getNamespace(), "Cache batch get result error: target class is null.");
		}
		String namespace = operation.getNamespace();
		String[] keys = operation.getKeys();
		Class<?> serializeClazz = config.getTargetSerializedClass(clazz);
		if (serializeClazz == null) {
			serializeClazz = Object.class;
		}
		if (StringUtils.isBlank(namespace)) {
			String[] namespaces = config.getTargetNamespace(clazz);
			if (namespaces == null || namespaces.length == 0) {
				throw new CacheOperateException(operation.getNamespace(), "Cache batch get result error: namespace is null or class has no CacheType anno.");
			}
			namespace = namespaces[0];
		}
		// 查询CacheObjectHolder不存在的Key列表, 从缓存中加载
		String[] needToGetKeys = holder.getNoneCachedKeys(namespace, keys);
		if (needToGetKeys != null && needToGetKeys.length > 0) {
			Cache cache = getCache(namespace);
			Map<String, ?> needToGetObjects = cache.get(needToGetKeys, serializeClazz);			
			if (needToGetObjects == null || needToGetObjects.size() != needToGetKeys.length) {
				throw new CacheOperateException(namespace, "Cache batch get result error: operation is null or not conform the input keys length.");
			}
			holder.addCacheObjectSet(namespace, needToGetObjects);
		}
		Map<String, T> result = new HashMap<String, T>();
		for (int i = 0; i < keys.length; i++) {
			Object obj = holder.getCachedObject(namespace, keys[i]);
			Object copyed = config.reverseCopyBean(obj);
			holder.addCacheObject(namespace, keys[i], copyed);	// 需在级联操作前将holder中缓存对象替换为反向复制的对象
			fulfillFields(copyed, clazz, holder);
			T t = null;
			if (copyed != null) {
				t = (T)copyed;
			}
			result.put(keys[i], t);
		}
		return result;
	}
	
	/**
	 * 级联加载对象中有CacheField注解的属性
	 * 
	 * @param obj
	 * @param clazz
	 * @param holder
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	private void fulfillFields(Object obj, Class<?> clazz, CacheObjectHolder holder) throws CacheNotExistsException, CacheOperateException {
		List<CacheFieldOperation> operationList = config.getFieldsOperation(obj, clazz);
		if (CollectionUtils.isNotEmpty(operationList)) {
			for (CacheFieldOperation fieldOperation : operationList) {
				if (fieldOperation == null) {
					continue;
				}
				String namespace = fieldOperation.getNamespace();
				String key = fieldOperation.getKey();
				if (!holder.containsCacheKey(namespace, key)) {
					CacheGetOperation getOperation = fieldOperation.generateCacheGetOperation();
					Map<String, ?> fieldValue = get(getOperation, fieldOperation.getFieldClass(), holder);
					if (fieldValue == null || fieldValue.size() != 1) {
						throw new CacheOperateException(namespace, "Cache batch get result error: operation is null or not conform the input keys length.");
					}
				}
				Object fieldObject = holder.getCachedObject(namespace, key);
				if (fieldObject != null) {
					try {
						fieldOperation.setValue(obj, fieldObject);
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
	
	private CacheKeyHolder buildKeyHolder() {
		return new CacheKeyHolder();
	}
	
	private CacheObjectHolder buildObjectHolder() {
		return new CacheObjectHolder();
	}
	
	/**
	 * 记录存储的namespace和key, 用于防止缓存写入时重复保存的问题
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
	
	/**
	 * 记录存储的namespace和key/value, 用于防止缓存写入时重复读取的问题
	 */
	class CacheObjectHolder {
		
		private Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		
		// 获取本地缓存对象
		Object getCachedObject(String namespace, String key) {
			Object cached = null;
			if (StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(key)) {
				Map<String, Object> valueMap = map.get(namespace);
				if (valueMap != null) {
					cached = valueMap.get(key);
				}
			}
			return cached;
		}
		
		// 获取本地缓存对象集
		Map<String, Object> getCachedObjectMap(String namespace, String[] keys) {
			Map<String, Object> cached = new HashMap<String, Object>();
			if (StringUtils.isNotBlank(namespace) && keys != null && keys.length > 0) {
				Map<String, Object> valueMap = map.get(namespace);
				if (valueMap != null) {
					for (String key : keys) {
						if (valueMap.containsKey(key)) {
							cached.put(key, valueMap.get(key));
						}
					}
				}
			}
			return cached;
		}
		
		// 获取本地缓存的key集合
		String[] getCachedKeys(String namespace, String[] keys) {
			List<String> existed = new ArrayList<String>();
			if (StringUtils.isNotBlank(namespace) && keys != null && keys.length > 0) {
				Map<String, Object> valueMap = map.get(namespace);
				if (valueMap != null) {
					for (String key : keys) {
						if (valueMap.containsKey(key)) {
							existed.add(key);
						}
					}
				}
			}
			return existed.toArray(new String[0]);
		}
		
		// 获取未本地缓存的key集合
		String[] getNoneCachedKeys(String namespace, String[] keys) {
			List<String> remained = new ArrayList<String>();
			if (StringUtils.isNotBlank(namespace) && keys != null && keys.length > 0) {
				Map<String, Object> valueMap = map.get(namespace);
				if (valueMap != null) {
					for (String key : keys) {
						if (!valueMap.containsKey(key)) {
							remained.add(key);
						}
					}
				}
				else {
					return keys;
				}
			}
			return remained.toArray(new String[0]);
		}
		
		// 添加待缓存key集合, 返回已经存在的部分
		void addCacheObjectSet(String namespace, Map<String, ?> objectMap) {
			if (StringUtils.isNotBlank(namespace) && objectMap != null && objectMap.size() > 0) {
				Map<String, Object> valueMap = map.get(namespace);
				if (valueMap == null) {
					valueMap = new HashMap<String, Object>();
					map.put(namespace, valueMap);
				}
				valueMap.putAll(objectMap);
			}
		}
		
		// 添加待缓存key, 如存在则返回true, 不存在返回false
		void addCacheObject(String namespace, String key, Object obj) {
			if (StringUtils.isNotBlank(namespace) && StringUtils.isNotBlank(key)) {
				Map<String, Object> valueMap = map.get(namespace);
				if (valueMap == null) {
					valueMap = new HashMap<String, Object>();
					map.put(namespace, valueMap);
				}
				valueMap.put(key, obj);
			}
		}
		
		// 返回是否存在待缓存key, 如存在则返回true, 不存在返回false
		boolean containsCacheKey(String namespace, String key) {
			if (StringUtils.isBlank(namespace) || StringUtils.isBlank(key)) {
				return false;
			}
			Map<String, Object> valueMap = map.get(namespace);
			if (valueMap == null) {
				return false;
			}
			return valueMap.containsKey(key);
		}
	}

}
