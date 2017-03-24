package com.qf.cache;

import java.util.Map;

import com.qf.cache.exception.CacheNotExistsException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.operation.CacheClearOperation;
import com.qf.cache.operation.CacheEvictOperation;
import com.qf.cache.operation.CacheGetOperation;
import com.qf.cache.operation.CacheSaveOperation;
import com.qf.cache.operation.CacheStatOperation;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存上下文接口
 * <br>
 * File Name: CacheContext.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-26 13:41:02 
 * @version: v1.0
 *
 */
public interface CacheContext {
	
	/**
	 * 缓存保存操作
	 * 
	 * @param operation
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	public <T> void save(CacheSaveOperation<T> operation) throws CacheNotExistsException, CacheOperateException;
	
	/**
	 * 获取缓存对象操作
	 * 
	 * @param operation
	 * @return
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	public <T> Map<String, T> get(CacheGetOperation<T> operation) throws CacheNotExistsException, CacheOperateException;
	
	/**
	 * 删除缓存操作
	 * 
	 * @param operation
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	public void evict(CacheEvictOperation operation) throws CacheNotExistsException, CacheOperateException;
	
	/**
	 * 清空缓存操作
	 * 
	 * @param operation
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	public void clear(CacheClearOperation operation) throws CacheNotExistsException, CacheOperateException;
	
	/**
	 * 缓存信息统计操作
	 * 
	 * @param operation
	 * @return
	 * @throws CacheNotExistsException
	 * @throws CacheOperateException
	 */
	public CacheInfo stat(CacheStatOperation operation) throws CacheNotExistsException, CacheOperateException;
	
}