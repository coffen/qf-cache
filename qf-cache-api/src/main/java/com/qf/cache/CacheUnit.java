package com.qf.cache;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.qf.cache.exception.CacheCreateException;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存单元, 缓存通过名称空间隔开不同类型的对象, 一个名称空间对应一个缓存单元
 * <br>
 * File Name: CacheUnit.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-25 11:24:00 
 * @version: v1.0
 *
 */
public class CacheUnit {
	
	private String namespace;					// 名称空间
	private Serializer serializer;				// 序列化工具
	private List<CachePolicy> cachePolicies;	// 缓存策略
	
	public CacheUnit(String namespace) throws CacheCreateException {
		this(namespace, null);
	}
	
	public CacheUnit(String namespace, Serializer serializer) throws CacheCreateException {
		if (StringUtils.isBlank(namespace)) {
			throw new CacheCreateException("缓存名称空间为空");
		}
		this.namespace = namespace;
		this.serializer = serializer;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public Serializer getSerializer() {
		return serializer;
	}
	
	public void addCachePolicy(CachePolicy policy) {
		this.cachePolicies.add(policy);
	}
	
	public List<CachePolicy> getCachePolicies() {
		return Collections.unmodifiableList(cachePolicies);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof String) {
			return ((String)obj).equals(namespace);
		} 
		else if (obj instanceof CacheUnit) {
			CacheUnit cu = (CacheUnit)obj;
			return cu.getNamespace() != null && cu.getNamespace().equals(namespace);
		}
		return false;
	}

}
