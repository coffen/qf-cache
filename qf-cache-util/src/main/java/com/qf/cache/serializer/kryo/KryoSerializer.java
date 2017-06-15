package com.qf.cache.serializer.kryo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.KryoPool;
import com.qf.cache.Serializer;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: Kryo序列化器
 * <br>
 * File Name: KryoSerializer.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月24日 上午11:52:29 
 * @version: v1.0
 *
 */
public class KryoSerializer implements Serializer {
	
	private static KryoPool pool = KryoPoolFactory.getKryoPoolInstance();

	@Override
	public <T> byte[] serialize(T object) throws IOException {
		if (object == null) {
			return null;
		}
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        Output output = new Output(byteOut);
        pool.borrow().writeObject(output, object);
		byte[] bs = output.toBytes();
		output.close();
		return bs;
	}

	@Override
	public <T> T deSerialize(byte[] bytes, Class<T> clazz) throws IOException {
		if (bytes == null) {
			return null;
		}
		ByteArrayInputStream byteInput = new ByteArrayInputStream(bytes);
        Input input = new Input(byteInput);
		T obj = pool.borrow().readObject(input, clazz);
		input.close();
		return obj;
	}

}
