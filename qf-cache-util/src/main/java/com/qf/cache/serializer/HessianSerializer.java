package com.qf.cache.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.qf.cache.Serializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Hession序列化工具
 * <br>
 * File Name: HessianSerializer.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年5月11日 下午3:43:15 
 * @version: v1.0
 *
 */
public class HessianSerializer implements Serializer {
	
	private HessianProxyFactory factory = new HessianProxyFactory(this.getClass().getClassLoader()); 
	
	@Override
	public <T> byte[] serialize(T object) throws IOException {
		byte[] result = null;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			AbstractHessianOutput output = factory.getHessianOutput(bos);
			output.writeObject(object);
			result = bos.toByteArray();
			output.close();
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
		try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
			AbstractHessianInput input = factory.getHessian1Input(bis);
			Object obj = input.readObject(clazz);
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
