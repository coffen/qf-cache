package com.qf.cache.local.test;

import java.util.UUID;

import org.junit.Test;

import com.qf.cache.local.SoftHashMap;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: SoftHashMap测试类
 * <br>
 * File Name: SoftHashMapTest.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月23日 下午5:05:08 
 * @version: v1.0
 *
 */
public class SoftHashMapTest {
	
	@Test
	public void testMap() {
		SoftHashMap<String, String> map = new SoftHashMap<String, String>();
	//	Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < 10; i++) {
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
