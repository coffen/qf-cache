package com.qf.cache;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 分层式缓存
 * <br>
 * File Name: HierarchyCache.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月2日 上午10:22:58 
 * @version: v1.0
 *
 */
public interface HierarchyCache extends Cache {
	
	/**
	 * 获取本地缓存
	 * 
	 * @return
	 */
	Cache getLocalCache();
	
	/**
	 * 获取分布式缓存
	 * 
	 * @return
	 */
	Cache getShardedCache();

}
