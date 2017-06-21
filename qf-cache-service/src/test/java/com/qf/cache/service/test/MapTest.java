package com.qf.cache.service.test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;

public class MapTest {

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("C", "Coffen");
		map.put("B", "Bill");
		map.put("A", "Anthone");
		
		Set<String> set = new HashSet<String>();
		set.add("A");
		set.add("C");
		
		map.keySet().removeAll(set);
		
		System.out.println(JSON.toJSONString(map));
		System.out.println(map.size());
	}

}
