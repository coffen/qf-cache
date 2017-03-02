package com.qf.cache.operation;

import java.io.Serializable;
import java.util.Map;

import com.qf.cache.CacheOperation;
import com.qf.cache.CacheSaveConditionEnum;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存保存操作
 * <br>
 * File Name: CacheSaveOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月2日 上午10:46:35 
 * @version: v1.0
 *
 */
public class CacheSaveOperation implements CacheOperation, Serializable {
	
	private static final long serialVersionUID = -2157371986540008460L;
	
	private String namespace;	
	private Map<String, Object> keyValue; 
	private Long expire;
	private CacheSaveConditionEnum condition;
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public Map<String, Object> getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(Map<String, Object> keyValue) {
		this.keyValue = keyValue;
	}

	public Long getExpire() {
		return expire;
	}

	public void setExpire(Long expire) {
		this.expire = expire;
	}

	public CacheSaveConditionEnum getCondition() {
		return condition;
	}

	public void setCondition(CacheSaveConditionEnum condition) {
		this.condition = condition;
	}	

}
