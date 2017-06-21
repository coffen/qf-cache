package com.qf.cache.sharded.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.sharded.redis.ShardedRedisCache;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

public class ShardedRedisCacheTest {
	
	private Logger log = LoggerFactory.getLogger(ShardedRedisCacheTest.class);
	
	ShardedRedisCache cache;
	String namespace = "com.qf.cache.redis";
	
	@Before
	public void before() throws CacheCreateException {
		List<JedisShardInfo> shardInfoList = new ArrayList<JedisShardInfo>();
		shardInfoList.add(new JedisShardInfo("192.168.48.128", 9001));
		shardInfoList.add(new JedisShardInfo("192.168.48.128", 9002));
		shardInfoList.add(new JedisShardInfo("192.168.48.128", 9003));
		shardInfoList.add(new JedisShardInfo("192.168.48.128", 9004));
		shardInfoList.add(new JedisShardInfo("192.168.48.128", 9005));
		shardInfoList.add(new JedisShardInfo("192.168.48.128", 9006));
		ShardedJedisPool pool = new ShardedJedisPool(new GenericObjectPoolConfig(), shardInfoList);
		cache = ShardedRedisCache.instance(namespace, 0);
		cache.setShardedJedisPool(pool);
	}
	
	@Test
	public void testShardedRedisCache() throws CacheOperateException, InterruptedException {
		Map<String, Person> map = new HashMap<String, Person>();
		map.put("zx", new Person("Zhang Xiang", 171, 145));
		map.put("lkf", new Person("Li Ke Fu", 171, 168));
		map.put("jzq", new Person("Jiang Zhi Qiang", 168, 140));
		cache.put(map, 2000L, null);
		Thread.sleep(1900);
		Map<String, Person> result = cache.get(new String[] { "zx", "lkf", "jzq" }, Person.class);	
		for (Person p : result.values()) {
			log.error(p.getName() + ": " + p);
		}
		cache.evict(new String[] { "zx", "lkf", "jzq" });
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
