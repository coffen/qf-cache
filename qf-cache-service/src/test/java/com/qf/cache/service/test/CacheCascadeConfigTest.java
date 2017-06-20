package com.qf.cache.service.test;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.esotericsoftware.minlog.Log;
import com.qf.cache.exception.CacheCreateException;
import com.qf.cache.operation.CacheGetOperation;
import com.qf.cache.operation.CacheSaveOperation;
import com.qf.cache.service.DefaultCacheContext;
import com.qf.cache.sharded.redis.ClusteredRedisCache;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存级联配置测试
 * <br>
 * File Name: CacheCascadeConfigTest.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月19日 下午3:17:58 
 * @version: v1.0
 *
 */
public class CacheCascadeConfigTest {
	
	JedisCluster cluster;
	
	String namespaceA = "com.qf.cache.moduleA";
	String namespaceB = "com.qf.cache.innerModuleB";
	
	@Before
	public void before() throws CacheCreateException {
		Set<HostAndPort> nodeSet = new HashSet<HostAndPort>();
		nodeSet.add(new HostAndPort("192.168.48.128", 9001));
		nodeSet.add(new HostAndPort("192.168.48.128", 9002));
		nodeSet.add(new HostAndPort("192.168.48.128", 9003));
		nodeSet.add(new HostAndPort("192.168.48.128", 9004));
		nodeSet.add(new HostAndPort("192.168.48.128", 9005));
		nodeSet.add(new HostAndPort("192.168.48.128", 9006));
		cluster = new JedisCluster(nodeSet);
	}
	
	@Test
	public void testParseClazz() {
		DefaultCacheContext cacheContent = new DefaultCacheContext("com.qf.cache.service.test");
		long start = System.currentTimeMillis();
		Log.error("开始时间:" + start);
		try {
			ClusteredRedisCache cacheA = ClusteredRedisCache.instance(namespaceA);
			cacheA.setJedisCluster(cluster);
			cacheContent.addCache(cacheA);
			ClusteredRedisCache cacheB = ClusteredRedisCache.instance(namespaceB);
			cacheB.setJedisCluster(cluster);
			cacheContent.addCache(cacheB);
			for (int i = 0; i < 1000; i++) {
				Map<String, Object> map = new HashMap<String, Object>();
				ModuleA a = new ModuleA();
				a.setId(1L);
				a.setTitle("测试");
				a.setDir("/usr/lib");
				a.setRemark("NONE");
				a.setSort(0);
				a.setUrl("http://127.0.0.1");
				
				InnerModuleB b = new InnerModuleB();
				b.setImg("http://127.0.0.1/qiniu/product");
				b.setKey("aoq88y35sdi92324323");
				b.setWidth(70);
				b.setHeight(70);
				b.setCreatedAt(new Date());
				a.setModuleB(b);
				
				String key = String.valueOf(a.getId());
				map.put(key, a);
				
				CacheSaveOperation saveOperation = new CacheSaveOperation();
				saveOperation.setNamespace(namespaceA);
				saveOperation.setKeyValue(map);
				cacheContent.save(saveOperation);
				
				CacheGetOperation getOperation = new CacheGetOperation();
				getOperation.setNamespace(namespaceA);
				getOperation.setKeys(new String[] { key });
				
//				Map<String, ModuleA> loaded = 
						cacheContent.get(getOperation, ModuleA.class);
//				Log.error(JSON.toJSONString(loaded));	
				
				CacheGetOperation getOperation2 = new CacheGetOperation();
				getOperation2.setNamespace(namespaceB);
				getOperation2.setKeys(new String[] { "1" });
				
//				Map<String, InnerModuleB> loaded2 = 
						cacheContent.get(getOperation2, InnerModuleB.class);
//				Log.error(JSON.toJSONString(loaded2));
			}
			long end = System.currentTimeMillis();
			Log.error("结束时间:" + start + ", 耗时: " + (end - start));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
