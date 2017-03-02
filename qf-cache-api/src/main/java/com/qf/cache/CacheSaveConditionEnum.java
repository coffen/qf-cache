package com.qf.cache;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存保存条件枚举
 * <br>
 * File Name: CacheSaveConditionEnum.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-26 14:24:41 
 * @version: v1.0
 *
 */
public enum CacheSaveConditionEnum {
	
	IF_EXISTS,		// 缓存对象已存在时才缓存
	IF_NOT_EXISTS,	// 缓存对象不存在时才缓存
	ALWAYS;			// 总是缓存

}
