package com.qf.cache.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.qf.cache.Serializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Jdk序列化工具
 * <br>
 * File Name: JdkSerializer.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年5月11日 下午3:40:50 
 * @version: v1.0
 *
 */
public class JdkSerializer implements Serializer {

	@Override
	public <T> byte[] serialize(T object) throws IOException {
		if (!(object instanceof Serializable)) {
			throw new IOException();
		}
		byte[] result = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(bos)) {
	        oos.writeObject(object);
	        result = bos.toByteArray();
		}
		catch (Exception e) {
			throw new IOException(e);
		}
        return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws IOException {
		T t = null;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				ObjectInputStream ois = new ObjectInputStream(bis)) {
			Object obj = ois.readObject();
			if (obj != null) {
				t = (T)obj; 
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
		return t;
	}

}
