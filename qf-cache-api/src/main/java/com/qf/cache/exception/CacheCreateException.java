package com.qf.cache.exception;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存创建错误
 * <br>
 * File Name: CacheCreateException.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-25 11:24:46 
 * @version: v1.0
 *
 */
public class CacheCreateException extends Exception {

	private static final long serialVersionUID = -5652831214571953355L;
	
	private String errorMsg;
	
	public CacheCreateException(String error) {
		this.errorMsg = error;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

}
