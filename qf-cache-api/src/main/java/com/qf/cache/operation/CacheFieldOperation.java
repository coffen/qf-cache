package com.qf.cache.operation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.qf.cache.CacheOperation;
import com.qf.cache.CacheSaveConditionEnum;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 类属性缓存操作
 * <br>
 * File Name: CacheFieldOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月20日 上午9:24:54 
 * @version: v1.0
 *
 */
public class CacheFieldOperation implements CacheOperation {

	private Field field;
	
	private String namespace;
	private String key;
	
	public CacheFieldOperation(Field field, String namespace, String key) {
		this.field = field;
		
		this.namespace = namespace;
		this.key = key;
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getFieldName() {
		return field == null ? null : field.getName();
	}
	
	public Class<?> getFieldClass() {
		return field == null ? null : field.getType();
	}
	
	// 生成级联属性的缓存操作
	public CacheSaveOperation generateCacheSaveOperation(String key, Object obj, Long expire, CacheSaveConditionEnum condition) throws IllegalArgumentException, IllegalAccessException {
		if (StringUtils.isBlank(key) || obj == null) {
			return null;
		}
		Object fieldObject = field.get(obj);
		if (fieldObject == null) {
			return null;
		}
		CacheSaveOperation operation = new CacheSaveOperation();
		operation.setExpire(expire);
		operation.setCondition(condition);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(key, fieldObject);
		operation.setKeyValue(data);
		return operation;
	}
	
	public CacheGetOperation generateCacheGetOperation() {
		CacheGetOperation operation = new CacheGetOperation();
		operation.setNamespace(namespace);
		operation.setKeys(new String[] { key });
		return operation;
	}
	
	public void setValue(Object obj, Object fieldValue) throws IllegalArgumentException, IllegalAccessException {
		field.set(obj, fieldValue);
	}

}
