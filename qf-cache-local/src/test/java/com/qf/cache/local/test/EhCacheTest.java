package com.qf.cache.local.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.local.EhCache;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: EhCache测试类
 * <br>
 * File Name: EhCacheTest.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月24日 上午10:22:15 
 * @version: v1.0
 *
 */
public class EhCacheTest {
	
	private Logger log = LoggerFactory.getLogger(EhCacheTest.class);
	
	EhCache cache;
	String namespace;
	
	@Before
	public void before() throws CacheCreateException {
		namespace = "com.qf.cache.eh";
		
		CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder().build();
		manager.init();
		CacheConfiguration<String, byte[]> config = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class, ResourcePoolsBuilder.heap(1000)).build();
		manager.createCache(namespace, config);
		cache = new EhCache(manager);
	}
	
	@Test
	public void testEhCache() throws CacheOperateException, InterruptedException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("name", 7);
		map.put("height", 171);
		map.put("weight", 200);
		cache.put("com.qf.cache.eh", map, 2000L, null);
		Thread.sleep(1900);
		List<Integer> list = cache.get(namespace, new String[] { "name", "height", "weight" }, Integer.class);	
		for (Integer intObject : list) {
			log.error(intObject + ":");
		}
	}

}
