package com.qf.cache.local.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	String namespace = "com.qf.cache.eh";
	
	@Before
	public void before() throws CacheCreateException {
		cache = EhCache.instance(namespace, 60 * 24);
	}
	
	@Test
	public void testEhCache() throws CacheOperateException, InterruptedException {
		Map<String, Person> map = new HashMap<String, Person>();
		map.put("zx", new Person("Zhang Xiang", 171, 145));
		map.put("lkf", new Person("Li Ke Fu", 171, 168));
		map.put("jzq", new Person("Jiang Zhi Qiang", 168, 140));
		cache.put(map, 2000L, null);
		Thread.sleep(1900);
		List<Person> list = cache.get(new String[] { "zx", "lkf", "jzq" }, Person.class);	
		for (Person p : list) {
			log.error(p.getName() + ": " + p);
		}
	}
	
	class Person {
		
		public Person(String name, float height, float weight) {
			this.name = name;
			this.height = height;
			this.weight = weight;
		}
		
		String name;
		float height;
		float weight;
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public float getHeight() {
			return height;
		}
		
		public void setHeight(float height) {
			this.height = height;
		}
		
		public float getWeight() {
			return weight;
		}
		
		public void setWeight(float weight) {
			this.weight = weight;
		}
		
		@Override
		public String toString() {
			return "name: " + name + " height: " + height + " weight: " + weight;
		}
		
	}

}
