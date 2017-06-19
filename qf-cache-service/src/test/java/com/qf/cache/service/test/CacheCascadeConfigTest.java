package com.qf.cache.service.test;

import org.junit.Test;

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
		new DefaultCacheContext("com.qf.cache.service.test");
	}

}
