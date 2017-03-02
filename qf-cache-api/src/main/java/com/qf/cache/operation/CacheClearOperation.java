package com.qf.cache.operation;

import java.io.Serializable;

import com.qf.cache.CacheOperation;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存清除操作
 * <br>
 * File Name: CacheClearOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月2日 上午10:59:14 
 * @version: v1.0
 *
 */
public class CacheClearOperation implements CacheOperation, Serializable {
	
	private static final long serialVersionUID = -4811855590038275898L;
	
	private String namespace;
	
	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
