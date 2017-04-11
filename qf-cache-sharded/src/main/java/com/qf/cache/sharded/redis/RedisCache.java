package com.qf.cache.sharded.redis;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.Cache;
import com.qf.cache.CacheInfo;
import com.qf.cache.CacheSaveConditionEnum;
import com.qf.cache.exception.CacheOperateException;

public class RedisCache implements Cache {
	
	private static final Logger LOG = LoggerFactory.getLogger(RedisCache.class);

	@Override
	public <T> int put(Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException {
		return 0;
	}

	@Override
	public <T> List<T> get(String[] keys, Class<T> clazz) throws CacheOperateException {
		return null;
	}

	@Override
	public int evict(String[] keys) throws CacheOperateException {
		return 0;
	}

	@Override
	public int clear() throws CacheOperateException {
		return 0;
	}

	@Override
	public CacheInfo stat() throws CacheOperateException {
		return null;
	}

	@Override
	public String getNamespace() {
		return null;
	}

}
