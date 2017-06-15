package com.qf.cache.serializer;

import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.qf.cache.Serializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Fastjson序列化工具
 * <br>
 * File Name: FastjsonSerializer.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年5月11日 上午10:56:43 
 * @version: v1.0
 *
 */
public class FastjsonSerializer implements Serializer {

	@Override
	public <T> byte[] serialize(T object) throws IOException {
		byte[] bytes = null;
		String json = JSON.toJSONString(object);
		if (json != null) {
			bytes = json.getBytes();
		}
		return bytes;
	}

	@Override
	public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws IOException {
		T t = null;
		if (bytes != null && clazz != null) {
			t = JSON.parseObject(new String(bytes), clazz);
		}
		return t;
	}

}
