package com.qf.cache;

import java.util.List;
import java.util.Map;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存
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
	 * @param name			缓存名称
	 * @param keyValue   	待缓存键值对
	 * @param expire	 	过期时间
	 * @param condition	 	缓存操作条件
	 * 
	 * @return 成功缓存对象个数
	 */	
	public int put(String name, Map<String, Object> keyValue, Long expire, CacheSaveConditionEnum condition);
	
	/**
	 * <p> 在指定缓存中分页查询键列表
	 * 
	 * @param name			缓存名称
	 * @param start  		开始记录
	 * @param offset		查询个数
	 * 
	 * @return 返回缓存键列表
	 */
	public List<String> keys(String name, Integer start, Integer offset);
	
	/**
	 * <p> 在指定缓存中查询多个缓存键
	 * 
	 * @param name			缓存名称
	 * @param keys  		待查询键值
	 * @param clazz 		对象类型
	 * 
	 * @return 返回缓存对象映射表
	 */
	public <T> Map<Object, T> get(String name, String[] keys, Class<T> clazz);
	
	/**
	 * <p> 从指定缓存中删除多个缓存键值
	 * 
	 * @param name			缓存名称
	 * @param keys   		待删除键值
	 * 
	 * @return 成功删除键值个数
	 */
	public int evict(String name, String[] keys);
	
	/**
	 * <p> 清空缓存
	 * 
	 * @param name			缓存名称
	 * 
	 * @return 成功清空个数
	 */
	public int clear(String name);
	
	/**
	 * <p> 查询缓存统计信息
	 * 
	 * @param name			缓存名称
	 * 
	 * @return CacheInfo
	 */
	public CacheInfo stat(String name);

}
