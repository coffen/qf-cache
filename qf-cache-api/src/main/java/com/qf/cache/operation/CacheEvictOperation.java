package com.qf.cache.operation;

import java.io.Serializable;

import com.qf.cache.CacheOperation;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存移除操作
 * <br>
 * File Name: CacheEvictOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月2日 上午10:58:54 
 * @version: v1.0
 *
 */
public class CacheEvictOperation implements CacheOperation, Serializable {

	private static final long serialVersionUID = -339370065714648958L;
	
	private String namespace;
	private String[] keys;
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public String[] getKeys() {
		return keys;
	}
	
	public void setKeys(String[] keys) {
		this.keys = keys;
	}

}
