package com.qf.cache.operation;

import java.io.Serializable;

import com.qf.cache.CacheOperation;

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
