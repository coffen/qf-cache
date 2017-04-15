package com.qf.cache.sharded.redis;

import com.qf.cache.Serializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Redis缓存配置
 * <br>
 * File Name: ClusteredRedisCacheConfig.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年4月15日 下午12:09:23 
 * @version: v1.0
 *
 */
public class RedisCacheConfig {
	
	private Serializer serializer;
	private Integer db;
	private String namespace;
	
	public Serializer getSerializer() {
		return serializer;
	}
	
	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}
	
	public Integer getDb() {
		return db;
	}
	
	public void setDb(Integer db) {
		this.db = db;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
