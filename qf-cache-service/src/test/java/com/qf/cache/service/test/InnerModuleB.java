package com.qf.cache.service.test;

import java.io.Serializable;
import java.util.Date;

import com.qf.cache.anno.CacheCascadeEnum;
import com.qf.cache.anno.CacheField;
import com.qf.cache.anno.CacheType;

@CacheType(namespace = "com.qf.cache.innerModuleB")
public class InnerModuleB implements Serializable {

	private static final long serialVersionUID = 2404648958155174025L;
	
	private Long moduleId;
	
	private String img;
	private String key;
	
	private Integer width;
	private Integer height;
	
	@CacheField(cascade = CacheCascadeEnum.NONE)
	private Date createdAt;
	
	@CacheField(cascade = CacheCascadeEnum.DEPENDS, key="moduleId")
	private ModuleA moduleA;
	
	public Long getModuleId() {
		return moduleId;
	}
	
	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getImg() {
		return img;
	}
	
	public void setImg(String img) {
		this.img = img;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public Integer getWidth() {
		return width;
	}
	
	public void setWidth(Integer width) {
		this.width = width;
	}
	
	public Integer getHeight() {
		return height;
	}
	
	public void setHeight(Integer height) {
		this.height = height;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
