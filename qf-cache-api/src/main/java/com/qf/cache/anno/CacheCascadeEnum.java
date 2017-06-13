package com.qf.cache.anno;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存级联操作条件
 * <br>
 * File Name: CacheCascadeEnum.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年6月13日 下午2:35:06 
 * @version: v1.0
 *
 */
public enum CacheCascadeEnum {
	
	FULL,		// 被引用对象作为引用对象的一部分缓存, 如果被引用对象对应类有设置名称空间还需单独缓存
	ISOLATE,	// 被引用对象不作为引用对象的一部分缓存, 如果被引用对象对应类有设置名称空间需单独缓存
	DEPENDS,	// 被引用对象是否作为引用对象的一部分, 取决于被引用对象是否设置名称空间; 如设置了名称空间则不缓存在引用对象中, 反之则缓存
	NONE;		// 无论什么条件都不缓存被引用对象

}
