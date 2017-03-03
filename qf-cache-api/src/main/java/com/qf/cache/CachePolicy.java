package com.qf.cache;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存策略接口
 * <br>
 * File Name: CachePolicy.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-25 11:24:29 
 * @version: v1.0
 *
 */
public interface CachePolicy<T extends CacheOperation> {
	
	/**
	 * 修改缓存操作
	 * 
	 * @param operation
	 * @return
	 */
	public T apply(T operation);
	
}
