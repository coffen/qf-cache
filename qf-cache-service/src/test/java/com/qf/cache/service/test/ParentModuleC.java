package com.qf.cache.service.test;

import java.io.Serializable;

import com.qf.cache.anno.CacheCascadeEnum;
import com.qf.cache.anno.CacheField;
import com.qf.cache.anno.CacheType;

@CacheType(namespace = "com.qf.cache.ParentModuleC")
public class ParentModuleC implements Serializable {

	private static final long serialVersionUID = -4023723756436136441L;
	
	private Long id;
	private String dir;
	
	@CacheField(cascade = CacheCascadeEnum.ISOLATE)
	private String remark;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDir() {
		return dir;
	}
	
	public void setDir(String dir) {
		this.dir = dir;
	}
	
	public String getRemark() {
		return remark;
	}
	
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
}
