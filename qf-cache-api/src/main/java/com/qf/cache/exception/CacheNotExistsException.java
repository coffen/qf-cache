package com.qf.cache.exception;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存不存在异常
 * <br>
 * File Name: CacheNotExistsException.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月3日 下午2:31:52 
 * @version: v1.0
 *
 */
public class CacheNotExistsException extends Exception {

	private static final long serialVersionUID = -5652831214571953355L;
	
	private String namespace;
	
	public CacheNotExistsException(String namespace) {
		this.namespace = namespace;
	}
	
	public String getCacheNamespace() {
		return namespace;
	}

}
