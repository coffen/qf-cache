package com.qf.cache.service.test.impl;

import java.io.Serializable;

import com.qf.cache.anno.CacheType;

@CacheType(namespace = "com.qf.cache.ModuleD")
public class ModuleD implements Serializable {

	private static final long serialVersionUID = -3033105164120526482L;
	
	private Boolean unique;
	
	public Boolean getUnique() {
		return unique;
	}
	
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}
	
}
