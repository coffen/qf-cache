package com.qf.cache.service.test;

import java.io.Serializable;

import com.qf.cache.anno.CacheCascadeEnum;
import com.qf.cache.anno.CacheField;
import com.qf.cache.anno.CacheType;

@CacheType(namespace = "com.qf.cache.moduleA")
public class ModuleA extends ParentModuleC implements Serializable {

	private static final long serialVersionUID = -3033105164120526482L;
	
	private String title;
	private String url;
	private Integer sort;
	
	private transient String style;
	
	@CacheField(cascade = CacheCascadeEnum.DEPENDS)
	private InnerModuleB moduleB;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Integer getSort() {
		return sort;
	}
	
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	public String getStyle() {
		return style;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}
	
	public InnerModuleB getModuleB() {
		return moduleB;
	}
	
	public void setModuleB(InnerModuleB moduleB) {
		this.moduleB = moduleB;
	}
	
}
