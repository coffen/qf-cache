package com.qf.cache;

import java.io.IOException;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: 序列化工具
 * <br>
 * File Name: Serializer.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017-02-25 13:59:59 
 * @version: v1.0
 *
 */
public interface Serializer {

	/**
	 * 序列化对象
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public <T> byte[] serialize(T object) throws IOException;

	/**
	 * 反序列化对象
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 */
	public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws IOException;
	
}
