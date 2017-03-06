package com.qf.cache;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存操作接口
 * <br>
 * File Name: CacheOperation.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月2日 上午10:44:58 
 * @version: v1.0
 *
 */
public interface CacheOperation {
	
	/**
	 * 获取缓存名称空间
	 * 
	 * @return
	 */
	public String getNamespace();

}
