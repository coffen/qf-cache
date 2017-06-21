package com.qf.cache.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.qf.cache.Cache;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheSaveConditionEnum;
import com.qf.cache.HierarchyCache;
import com.qf.cache.exception.CacheOperateException;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 分层式缓存抽象类
 * <br>
 * File Name: AbstractHierarchyCache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-03-12 22:02:17 
 * @version: v1.0
 *
 */
public abstract class AbstractHierarchyCache implements HierarchyCache {
	
	private final Cache local;
	private final Cache sharded;
	
	public AbstractHierarchyCache(Cache local, Cache sharded) {
		this.local = local;
		this.sharded = sharded;
	}

	@Override
	public <T> int put(Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		local.put(keyValue, expire, condition);
		int count = sharded.put(keyValue, expire, condition);
		return count;
	}

	@Override
	public <T> Map<String, T> get(String[] keys, Class<T> clazz) throws CacheOperateException {
		Map<String, T> result = local.get(keys, clazz);
		if (result == null || result.size() != keys.length) {
			throw new CacheOperateException(getCacheUnit().getNamespace(), "本地缓存返回结果为空或与Keys参数数组长度不匹配");
		}
		List<String> remained = new ArrayList<String>();
		for (Entry<String, T> entry : result.entrySet()) {
			if (entry.getValue() == null) {
				remained.add(entry.getKey());
			}
		}
		if (remained.size() > 0) {
			Map<String, T> shardedValue = sharded.get(remained.toArray(new String[0]), clazz);
			if (shardedValue == null || shardedValue.size() != remained.size()) {
				throw new CacheOperateException(getCacheUnit().getNamespace(), "分布式缓存返回结果为空或与Keys参数数组长度不匹配");
			}
			for (int i = 0; i < remained.size(); i++) {
				result.put(remained.get(i), shardedValue.get(remained.get(i)));
			}
		}
		return result;
	}

	@Override
	public int evict(String[] keys) throws CacheOperateException {
		local.evict(keys);
		sharded.evict(keys);
		return keys.length;
	}

	@Override
	public int clear() throws CacheOperateException {
		local.clear();
		sharded.clear();
		return 1;
	}

	@Override
	public CacheInfo stat() throws CacheOperateException {
		return sharded.stat();
	}

	@Override
	public Cache getLocalCache() {
		return local;
	}

	@Override
	public Cache getShardedCache() {
		return sharded;
	}

}
