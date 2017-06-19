package com.qf.cache.anno;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存属性
 * <br>
 * File Name: CacheField.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月13日 下午2:35:56 
 * @version: v1.0
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheField {
	
	/**
	 * 缓存级联保存设置
	 * 
	 * @return
	 */
	CacheCascadeEnum cascade() default CacheCascadeEnum.FULL;
	
	/**
	 * 缓存时以哪个属性做为键值, 设置为当前属性自身或者其它非序列化的属性时报错
	 * 
	 * @return
	 */
	String key() default "";

}
