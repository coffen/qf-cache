package com.qf.cache.service.exception;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存注解类解析错误
 * <br>
 * File Name: ClassParseException.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月19日 下午4:55:23 
 * @version: v1.0
 *
 */
public class ClassParseException extends Exception {

	private static final long serialVersionUID = -5049512053643573840L;
	
	private String clazz;
	private String errorMsg;
	
	public ClassParseException(String clazz, String error) {
		this.clazz = clazz;
		this.errorMsg = error;
	}
	
	public String getClazz() {
		return clazz;
	}
	
	public String getErrorMsg() {
		return errorMsg;
	}

}
