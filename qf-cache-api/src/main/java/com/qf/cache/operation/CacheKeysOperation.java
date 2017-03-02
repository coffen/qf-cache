package com.qf.cache.operation;

import java.io.Serializable;

import com.qf.cache.CacheOperation;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存键值列表操作
 * <br>
 * File Name: CacheKeysOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月2日 上午10:57:59 
 * @version: v1.0
 *
 */
public class CacheKeysOperation implements CacheOperation, Serializable {

	private static final long serialVersionUID = 8606908724177500729L;
	
	private String namespace;
	private Integer start;
	private Integer offset;
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	
	public Integer getStart() {
		return start;
	}
	
	public void setStart(Integer start) {
		this.start = start;
	}
	
	public Integer getOffset() {
		return offset;
	}
	
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

}
