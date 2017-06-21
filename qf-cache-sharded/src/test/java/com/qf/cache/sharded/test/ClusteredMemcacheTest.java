package com.qf.cache.sharded.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.exception.CacheOperateException;
import com.qf.cache.sharded.memcache.ClusteredMemcache;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class ClusteredMemcacheTest {
	
	private Logger log = LoggerFactory.getLogger(ClusteredMemcacheTest.class);
	
	ClusteredMemcache cache;
	String namespace = "com.qf.cache.memcache";
	
	@Before
	public void before() throws Exception {
		MemcachedClientBuilder builder = new XMemcachedClientBuilder((AddrUtil.getAddresses("192.168.48.128:11211")));
		MemcachedClient client = builder.build();
		cache = ClusteredMemcache.instance(namespace);
		cache.setMemcachedClient(client);
	}
	
	@Test
	public void test() throws CacheOperateException, InterruptedException {
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
		
		public Person() {}
		
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
