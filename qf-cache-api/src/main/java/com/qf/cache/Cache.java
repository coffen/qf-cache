package com.qf.cache;

import java.util.Map;

import com.qf.cache.exception.CacheOperateException;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存接口
 * <br>
 * File Name: Cache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-25 11:30:22 
 * @version: v1.0
 *
 */
public interface Cache {
	
	/**
	 * <p> 遍历存入多个缓存键值对
	 * 
	 * @param keyValue   	待缓存键值对
	 * @param expire	 	过期时间
	 * @param condition	 	缓存操作条件
	 * 
	 * @return 成功缓存对象个数
	 */	
	public <T> int put(Map<String, T> keyValue, Long expire, CacheSaveConditionEnum condition) throws CacheOperateException;
	
	/**
	 * <p> 在指定缓存中查询多个缓存键
	 * 
	 * @param keys			查询缓存键值
	 * @return
	 */
	public <T> Map<String, T> get(String[] keys, Class<T> clazz) throws CacheOperateException;
	
	/**
	 * <p> 从指定缓存中删除多个缓存键值
	 * 
	 * @param keys   		待删除键值
	 * 
	 * @return 成功删除键值个数
	 */
	public int evict(String[] keys) throws CacheOperateException;
	
	/**
	 * <p> 清空缓存
	 *
	 * @return 成功清空个数
	 */
	public int clear() throws CacheOperateException;
	
	/**
	 * <p> 查询缓存统计信息
	 * 
	 * @return CacheInfo
	 */
	public CacheInfo stat() throws CacheOperateException;
	
	/**
	 * <p> 返回缓存单元
	 * 
	 * @return
	 */
	public CacheUnit getCacheUnit();

}
