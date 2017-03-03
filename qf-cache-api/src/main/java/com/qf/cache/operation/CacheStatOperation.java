package com.qf.cache.operation;

import java.io.Serializable;

import com.qf.cache.CacheOperation;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存统计操作
 * <br>
 * File Name: CacheStatOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月3日 下午2:21:35 
 * @version: v1.0
 *
 */
public class CacheStatOperation implements CacheOperation, Serializable {

	private static final long serialVersionUID = -6190889487063521448L;
	
	private String namespace;
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
