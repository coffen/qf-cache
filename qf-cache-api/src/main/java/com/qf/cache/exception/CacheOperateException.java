package com.qf.cache.exception;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存操作异常
 * <br>
 * File Name: CacheOperateException.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月3日 下午2:34:24 
 * @version: v1.0
 *
 */
public class CacheOperateException extends Exception {

	private static final long serialVersionUID = -5652831214571953355L;
	
	private String namespace;
	private String operationError;
	
	public CacheOperateException(String namespace, String error) {
		this.namespace = namespace;
		this.operationError = error;
	}
	
	public String getCacheNamespace() {
		return namespace;
	}
	
	public String getOperationError() {
		return operationError;
	}

}
