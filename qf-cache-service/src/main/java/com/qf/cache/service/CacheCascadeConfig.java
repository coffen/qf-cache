package com.qf.cache.service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.beans.BeanCopier;

import com.qf.cache.anno.CacheCascadeEnum;
import com.qf.cache.anno.CacheField;
import com.qf.cache.anno.CacheType;
import com.qf.cache.service.exception.ClassParseException;
import com.qf.cache.util.ClassUtils;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AccessFlag;

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
	private Map<Class<?>, Class<?>> reverseMapping = new HashMap<Class<?>, Class<?>>();
	private Map<Class<?>, List<FieldWrap>> fieldMapping = new HashMap<Class<?>, List<FieldWrap>>();
	
	private ClassLoader clazzLoader = getClass().getClassLoader();
	
	// 解析有CacheType注解的类
	public void parse(List<String> clazzNameList) throws ClassParseException {
		if (CollectionUtils.isEmpty(clazzNameList)) {
			return;
		}
		List<Class<?>> clazzList = loadClass(clazzNameList);
		for (Class<?> clazz : clazzList) {
			ClassWrap clazzWrap = parseClass(clazz);
			if (clazzWrap != null) {
				clazzMapping.put(clazz, clazzWrap);
			}
		}
		for (Class<?> clazz : clazzList) {
			List<FieldWrap> fieldWrapList = parseField(clazz);
			if (CollectionUtils.isNotEmpty(fieldWrapList)) {
				fieldMapping.put(clazz, fieldWrapList);
			}
		}
		for (Entry<Class<?>, List<FieldWrap>> entry : fieldMapping.entrySet()) {
			buildTargetClass(entry.getKey(), entry.getValue());
		}
	}
	
	// 复制对象
	public Object copyBean(Object obj) {
		Object copyed = null;
		if (obj != null) {
			Class<?> srcClazz = obj.getClass();
			ClassWrap wrap = clazzMapping.get(srcClazz);
			if (wrap != null && wrap.getSerializeClazz() != null && wrap.getCopier() != null) {
				try {
					copyed = wrap.getSerializeClazz().newInstance();
					wrap.getCopier().copy(obj, copyed, null);
				}
				catch(Exception e) {
					log.error("复制Bean失败", e);
				}
			}
		}
		return copyed;
	}
	
	// 复制对象
	public Object reverseCopyBean(Object obj) {
		Object copyed = null;
		if (obj != null) {
			Class<?> serializedClazz = obj.getClass();
			Class<?> srcClazz = reverseMapping.get(serializedClazz);
			ClassWrap wrap = clazzMapping.get(srcClazz);
			if (wrap != null) {
				try {
					copyed = srcClazz.newInstance();
					wrap.getReverseCopier().copy(obj, copyed, null);
				}
				catch(Exception e) {
					log.error("复制Bean失败", e);
				}
			}
		}
		return copyed;
	}
	
	public Class<?> getTargetClass(Class<?> srcClazz) {
		Class<?> targetClazz = null;
		ClassWrap wrap = clazzMapping.get(srcClazz);
		if (wrap != null) {
			targetClazz = wrap.getSerializeClazz();
		}
		return targetClazz;
	}
	
	private List<Class<?>> loadClass(List<String> clazzNameList) throws ClassParseException {
		List<Class<?>> loadedClazzList = new ArrayList<Class<?>>();
		String clazz = null;
		try {
			for (String clazzName : clazzNameList) {
				clazz = clazzName;
				loadedClazzList.add(clazzLoader.loadClass(clazzName));
			}
		}
		catch (ClassNotFoundException e) {
			throw new ClassParseException(clazz, e.getMessage());
		}
		return loadedClazzList;
	}
	
	// 解析缓存类设定
	private ClassWrap parseClass(Class<?> clazz) {
		if (clazzMapping.containsKey(clazz)) {
			log.error("{}已经解析过Class", clazz);
			return null;
		}
		if (!clazz.isAnnotationPresent(CacheType.class)) {
			log.error("{}没有CacheType注解", clazz);
			return null;
		}
		String[] namespaces = clazz.getAnnotation(CacheType.class).namespace();
		if (!checkNamespaces(namespaces)) {
			log.error("{}名称空间设置为空", clazz);
			return null;
		}
		ClassWrap wrap = new ClassWrap();
		wrap.setNamespace(namespaces);
		return wrap;
	}
	
	// 解析缓存变量设定
	private List<FieldWrap> parseField(Class<?> clazz) throws ClassParseException {
		if (fieldMapping.containsKey(clazz)) {
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
				Class<?> fieldClazz = f.getType();
				CacheField cf = f.getAnnotation(CacheField.class);
				if (StringUtils.isNotBlank(cf.key())) {
					Field keyField = ClassUtils.getKeyFields(clazz, cf.key());
					if (keyField == null) {
						throw new ClassParseException(clazz.getName(), f.getName() + ": CacheField注解key值无效");
					}
					warp.setKeyField(keyField);
				}
				warp.setFieldClazz(fieldClazz);
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
	
	private Class<?> buildTargetClass(Class<?> clazz, List<FieldWrap> fieldWrapList) {
		ClassWrap clazzWrap = clazzMapping.get(clazz);
		if (clazzWrap == null) {
			log.error("类尚未解析: {}", clazz);
			return null;
		}
		if (clazzWrap.getSerializeClazz() != null) {
			return clazzWrap.getSerializeClazz();
		}
		if (CollectionUtils.isEmpty(fieldWrapList)) {
			clazzWrap.setSerializeClazz(clazz);
			return clazz;
		}
		Class<?> parentClazz = clazz.getSuperclass();
		ClassWrap parentClazzWrap = clazzMapping.get(parentClazz);
		// 设置父代理类
		Class<?> parentSerializeClazz = null;
		if (parentClazzWrap != null) {
			parentSerializeClazz = parentClazzWrap.getSerializeClazz();
			if (parentSerializeClazz == null) {
				parentSerializeClazz = buildTargetClass(parentClazz, fieldMapping.get(parentClazz));
			}
		}
		// 遍历获取须设置为transient的Field
		List<Field> noneSerializedFields = new ArrayList<Field>();
		for (FieldWrap fw : fieldWrapList) {
			if (fw.getCascadeType() == CacheCascadeEnum.NONE || fw.getCascadeType() == CacheCascadeEnum.ISOLATE || (fw.getCascadeType() == CacheCascadeEnum.DEPENDS && fw.getNamespace() != null)) {
				noneSerializedFields.add(fw.getField());
			}
		}
		// 构造代理类
		if (parentSerializeClazz == null && noneSerializedFields.size() == 0) {
			clazzWrap.setSerializeClazz(clazz);
			return clazz;
		}
		try {
			ClassPool classPool = ClassPool.getDefault();
			CtClass ctClazz = classPool.getAndRename(clazz.getCanonicalName(), clazz.getCanonicalName() + "-SerializeProxy");
			if (parentSerializeClazz != null) {
				ctClazz.setSuperclass(classPool.getCtClass(parentSerializeClazz.getCanonicalName()));
			}
			for (Field f : noneSerializedFields) {
				CtField ctField = ctClazz.getField(f.getName());
				ctField.setModifiers(AccessFlag.TRANSIENT);
			}
			Class<?> targetClazz = ctClazz.toClass(clazz.getClassLoader(), clazz.getProtectionDomain());
			clazzWrap.setSerializeClazz(targetClazz);
			clazzWrap.setCopier(BeanCopier.create(clazz, targetClazz, false));
			clazzWrap.setReverseCopier(BeanCopier.create(targetClazz, clazz, false));
			
			reverseMapping.put(targetClazz, clazz);	// 设置类的反向映射关系
			
			return targetClazz;
		}
		catch (Exception e) {
			log.error("创建代理类错误: " + clazz, e);
			return null;
		}
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
	class ClassWrap {
		
		private String[] namespace;
		private Class<?> serializeClazz; 	// 动态生成的类
		private BeanCopier copier;		 	// 对象复制器
		private BeanCopier reverseCopier;	// 反向复制器
		
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
		
		public BeanCopier getCopier() {
			return copier;
		}
		
		public void setCopier(BeanCopier copier) {
			this.copier = copier;
		}
		
		public BeanCopier getReverseCopier() {
			return reverseCopier;
		}
		
		public void setReverseCopier(BeanCopier reverseCopier) {
			this.reverseCopier = reverseCopier;
		}

	}
	
	/**
	 * 类成员变量封装, 包括变量对应类, 类指定名称空间和级联缓存方式
	 */
	class FieldWrap {
		
		private Field field;
		private Field keyField;
		private Class<?> fieldClazz;
		private Class<?> belongClazz;
		private String[] namespace;
		private CacheCascadeEnum cascadeType;
		
		public Field getField() {
			return field;
		}
		
		public void setField(Field field) {
			this.field = field;
		}
		
		public Field getKeyField() {
			return keyField;
		}
		
		public void setKeyField(Field keyField) {
			this.keyField = keyField;
		}
		
		public Class<?> getFieldClazz() {
			return fieldClazz;
		}
		
		public void setFieldClazz(Class<?> fieldClazz) {
			this.fieldClazz = fieldClazz;
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
