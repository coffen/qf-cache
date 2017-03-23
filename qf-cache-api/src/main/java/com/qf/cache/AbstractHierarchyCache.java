package com.qf.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;

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
	public int put(String name, Map<String, Object> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		local.put(name, keyValue, expire, condition);
		int count = sharded.put(name, keyValue, expire, condition);
		return count;
	}

	@Override
	public List<Object> get(String name, String[] keys) throws CacheOperateException {
		List<Object> values = local.get(name, keys);
		List<String> remained = new ArrayList<String>();
		List<Integer> indexs = new ArrayList<Integer>();
		if (CollectionUtils.isEmpty(values)) {
			remained.addAll(Arrays.asList(keys));
		}
		else {
			for (int i = 0; i < values.size(); i++) {
				if (values.get(i) == null) {
					remained.add(keys[i]);
					indexs.add(i);
				}
			}
		}
		if (remained.size() > 0) {
			List<Object> shardedValue = sharded.get(name, remained.toArray(new String[0]));
			for (int i = 0; i < remained.size(); i++) {
				values.set(indexs.get(i), shardedValue.get(i));
			}
		}
		return values;
	}

	@Override
	public int evict(String name, String[] keys) throws CacheOperateException {
		local.evict(name, keys);
		sharded.evict(name, keys);
		return keys.length;
	}

	@Override
	public int clear(String name) throws CacheOperateException {
		local.clear(name);
		sharded.clear(name);
		return 1;
	}

	@Override
	public CacheInfo stat(String name) throws CacheOperateException {
		return sharded.stat(name);
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
