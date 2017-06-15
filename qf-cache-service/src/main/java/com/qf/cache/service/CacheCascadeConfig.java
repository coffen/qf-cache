package com.qf.cache.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.anno.CacheCascadeEnum;
import com.qf.cache.anno.CacheField;
import com.qf.cache.anno.CacheType;
import com.qf.cache.util.ClassUtils;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存级联配置
 * <br>
 * File Name: CacheCascadeConfig.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月15日 下午4:19:28 
 * @version: v1.0
 *
 */
public class CacheCascadeConfig {
	
	private static Logger log = LoggerFactory.getLogger(CacheCascadeConfig.class);
	
	private Map<Class<?>, ClassWrap> clazzMapping = new HashMap<Class<?>, ClassWrap>();
	private Map<Class<?>, FieldWrap> fieldMapping = new HashMap<Class<?>, FieldWrap>();
	
	// 添加有CacheType注解的类
	public void parse(List<Class<?>> clazzList) {
		if (CollectionUtils.isEmpty(clazzList)) {
			return;
		}
		for (Class<?> clazz : clazzList) {
			ClassWrap clazzWrap = parseClass(clazz);
			if (clazzWrap != null) {
				
			}
		}
		
	}
	
	// 解析缓存类设定
	private ClassWrap parseClass(Class<?> clazz) {
		if (fieldMapping.containsKey(clazz)) {
			log.error("{}已经解析过Class", clazz);
			return null;
		}
		if (!clazz.isAnnotationPresent(CacheType.class)) {
			log.error("{}没有CacheType注解", clazz);
			return null;
		}
		String[] namespaces = clazz.getAnnotation(CacheType.class).namespace();
		if (!checkNamespaces(namespaces)) {
			log.error("{}名称空间设定为空", clazz);
			return null;
		}
		ClassWrap wrap = new ClassWrap();
		wrap.setNamespace(namespaces);
		return wrap;
	}
	
	// 解析缓存变量设定
	private List<FieldWrap> parseField(Class<?> clazz) {
		if (clazzMapping.containsKey(clazz)) {
			log.error("{}已经解析过Field", clazz);
			return null;
		}
		if (!clazz.isAnnotationPresent(CacheType.class)) {
			log.error("{}没有CacheType注解", clazz);
			return null;
		}
		List<Field> fieldList = ClassUtils.getCacheFields(clazz);		
		List<FieldWrap> wrapList = new ArrayList<FieldWrap>();
		if (CollectionUtils.isNotEmpty(fieldList)) {
			for (Field f : fieldList) {
				if (!f.isAnnotationPresent(CacheField.class)) {
					continue;
				}
				FieldWrap warp = new FieldWrap();
				Class<?> fieldClazz = f.getClass();
				CacheField cf = f.getAnnotation(CacheField.class);
				warp.setBelongClazz(clazz);
				warp.setField(f);
				warp.setCascadeType(cf.cascade());
				if (!(fieldClazz.isArray() || fieldClazz.isPrimitive()) && clazzMapping.get(fieldClazz) != null) {
					ClassWrap wrap = clazzMapping.get(fieldClazz);
					warp.setNamespace(wrap.getNamespace());
				}
				wrapList.add(warp);
			}
		}
		return wrapList;
	}
	
	private boolean checkNamespaces(String[] namespaces) {
		if (namespaces == null || namespaces.length == 0) {
			return false;
		}
		int count = 0;
		for (String ns : namespaces) {
			if (StringUtils.isNotBlank(ns)) {
				count++;
			}
		}
		return count > 0;
	}
	
	/**
	 * 类封装, 包括字节码类, 名称空间
	 */
	private class ClassWrap {
		
		private String[] namespace;
		private Class<?> serializeClazz; // 处理过的生成类
		
		public String[] getNamespace() {
			return namespace;
		}
		
		public void setNamespace(String[] namespace) {
			this.namespace = namespace;
		}
		
		public Class<?> getSerializeClazz() {
			return serializeClazz;
		}
		
		public void setSerializeClazz(Class<?> serializeClazz) {
			this.serializeClazz = serializeClazz;
		}

	}
	
	/**
	 * 类成员变量封装, 包括变量对应类, 类指定名称空间和级联缓存方式
	 */
	private class FieldWrap {
		
		private Field field;
		private Class<?> belongClazz;
		private String[] namespace;
		private CacheCascadeEnum cascadeType;
		
		public Field getField() {
			return field;
		}
		
		public void setField(Field field) {
			this.field = field;
		}
		
		public Class<?> getBelongClazz() {
			return belongClazz;
		}
		
		public void setBelongClazz(Class<?> belongClazz) {
			this.belongClazz = belongClazz;
		}
		
		public String[] getNamespace() {
			return namespace;
		}
		
		public void setNamespace(String[] namespace) {
			this.namespace = namespace;
		}
		
		public CacheCascadeEnum getCascadeType() {
			return cascadeType;
		}
		
		public void setCascadeType(CacheCascadeEnum cascadeType) {
			this.cascadeType = cascadeType;
		}

	}	

}
