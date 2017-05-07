package com.qf.cache.sharded.memcache;

import com.qf.cache.Serializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Memcache配置
 * <br>
 * File Name: MemcacheConfig.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年5月7日 下午3:18:11 
 * @version: v1.0
 *
 */
public class MemcacheConfig {
	
	private Serializer serializer;
	private String namespace;
	
	public Serializer getSerializer() {
		return serializer;
	}
	
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
