package com.qf.cache.sharded.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.sharded.redis.ClusteredRedisCache;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class ClusteredRedisCacheTest {
	
	private Logger log = LoggerFactory.getLogger(ClusteredRedisCacheTest.class);
	
	ClusteredRedisCache cache;
	String namespace = "com.qf.cache.redis";
	
	@Before
	public void before() throws CacheCreateException {
		Set<HostAndPort> nodeSet = new HashSet<HostAndPort>();
		nodeSet.add(new HostAndPort("192.168.48.128", 9001));
		nodeSet.add(new HostAndPort("192.168.48.128", 9002));
		nodeSet.add(new HostAndPort("192.168.48.128", 9003));
		nodeSet.add(new HostAndPort("192.168.48.128", 9004));
		nodeSet.add(new HostAndPort("192.168.48.128", 9005));
		nodeSet.add(new HostAndPort("192.168.48.128", 9006));
		JedisCluster cluster = new JedisCluster(nodeSet);
		cache = ClusteredRedisCache.instance(namespace);
		cache.setJedisCluster(cluster);
	}
	
	@Test
	public void testClusteredRedisCache() throws CacheOperateException, InterruptedException {
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
