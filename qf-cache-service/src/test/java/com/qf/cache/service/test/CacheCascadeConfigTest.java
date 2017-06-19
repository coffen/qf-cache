package com.qf.cache.service.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.esotericsoftware.minlog.Log;
import com.qf.cache.local.EhCache;
import com.qf.cache.operation.CacheGetOperation;
import com.qf.cache.operation.CacheSaveOperation;
import com.qf.cache.service.DefaultCacheContext;

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
	
	@Test
	public void testParseClazz() {
		DefaultCacheContext cacheContent = new DefaultCacheContext("com.qf.cache.service.test");
		try {
			String namespace = "com.qf.cache.test";
			EhCache cache = EhCache.instance(namespace);
			cacheContent.addCache(cache);
			
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
			
			String key = "moduleA";
			map.put(key, a);
			
			CacheSaveOperation saveOperation = new CacheSaveOperation();
			saveOperation.setNamespace(namespace);
			saveOperation.setKeyValue(map);
			cacheContent.save(saveOperation);
			
			CacheGetOperation getOperation = new CacheGetOperation();
			getOperation.setNamespace(namespace);
			getOperation.setKeys(new String[] { key });
			
			Map<String, ModuleA> loaded = cacheContent.get(getOperation, ModuleA.class);
			Log.error(JSON.toJSONString(loaded));			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
