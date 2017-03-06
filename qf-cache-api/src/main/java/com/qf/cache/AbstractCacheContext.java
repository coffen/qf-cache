package com.qf.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.qf.cache.exception.CacheNotExistsException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.operation.CacheClearOperation;
import com.qf.cache.operation.CacheEvictOperation;
import com.qf.cache.operation.CacheGetOperation;
import com.qf.cache.operation.CacheKeysOperation;
import com.qf.cache.operation.CacheSaveOperation;
import com.qf.cache.operation.CacheStatOperation;

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
public class AbstractCacheContext implements CacheContext {
	
	private Map<CacheUnit, Cache> cacheMap = new HashMap<CacheUnit, Cache>();

	@Override
	public void save(CacheSaveOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
	}

	@Override
	public List<String> keys(CacheKeysOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
	}

	@Override
	public Map<String, Object> get(CacheGetOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
	}

	@Override
	public void evict(CacheEvictOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
	}

	@Override
	public void clear(CacheClearOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
	}

	@Override
	public CacheInfo stat(CacheStatOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
	}

	@Override
	public void execute(CacheOperation operation) throws CacheNotExistsException, CacheOperateException {
		Cache cache = getCache(operation);
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
