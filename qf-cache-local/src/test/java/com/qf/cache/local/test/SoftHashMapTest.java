package com.qf.cache.local.test;

import java.util.UUID;

import org.junit.Test;

import com.qf.cache.local.SoftHashMap;

public class SoftHashMapTest {
	
	@Test
	public void testMap() {
		SoftHashMap<String, String> map = new SoftHashMap<String, String>();
	//	Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 10000; i++) {
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < 100; j++) {
				sb.append(UUID.randomUUID().toString().replace("-", ""));
			}
			map.put(sb.toString(), sb.reverse().toString());
			
			try {
				Thread.sleep(50L);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
