package com.qf.cache;

import com.qf.cache.operation.CacheClearOperation;
import com.qf.cache.operation.CacheEvictOperation;
import com.qf.cache.operation.CacheGetOperation;
import com.qf.cache.operation.CacheKeysOperation;
import com.qf.cache.operation.CacheSaveOperation;
import com.qf.cache.operation.CacheStatOperation;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 缓存上下文接口
 * <br>
 * File Name: CacheContext.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-26 13:41:02 
 * @version: v1.0
 *
 */
public interface CacheContext {
	
	public void save(CacheSaveOperation operation);
	
	public void keys(CacheKeysOperation operation);
	
	public void get(CacheGetOperation operation);
	
	public void evict(CacheEvictOperation operation);
	
	public void clear(CacheClearOperation operation);
	
	public void stat(CacheStatOperation operation);
	
	public void execute(CacheOperation operation);
	
}