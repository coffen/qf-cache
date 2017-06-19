package com.qf.cache.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.qf.cache.anno.CacheField;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 类工具
 * <br>
 * File Name: ClassUtils.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月24日 下午3:36:55 
 * @version: v1.0
 *
 */
public class ClassUtils {	
	
	/**
	 * 获取Class的泛型类, 如不存在则返回Object.class
	 * 
	 * @param clazz
	 * @param index
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Class<?> getSuperClassGenricType(final Class clazz, final int index) {
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			return Object.class;
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class)params[index];
	}
	
	/**
	 * 获取CacheField注解修饰的Field
	 * 
	 * @param clazz
	 * @return
	 */
	public static List<Field> getCacheFields(Class<?> clazz) {
		List<Field> fieldList = new ArrayList<Field>();
		if (clazz == null) {
			return fieldList;
		}
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (!field.isAnnotationPresent(CacheField.class)) {
				continue;
			}
			// 过滤static, transient修饰的字段
			if ((field.getModifiers() & 8) != 0 || (field.getModifiers() & 128) != 0) {
				continue;
			}
			fieldList.add(field);
		}
		return fieldList;
	}

}
