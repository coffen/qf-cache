package com.qf.cache.local;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.collections4.CollectionUtils;

/**
 * 
 * <p>
 * Project Name: C2C商城
 * <br>
 * Description: SoftHashMap
 * <br>
 * File Name: SoftHashMap.java
 * <br>
 * Copyright: Copyright (C) 2015 All Rights Reserved.
 * <br>
 * Company: 杭州偶尔科技有限公司
 * <br>
 * @author 穷奇
 * @create time：2017年3月16日 上午11:41:12 
 * @version: v1.0
 *
 */
public class SoftHashMap<K, V> extends AbstractMap<K, V> implements Map<K,V> {
	
	private Map<K, SoftValue> map;
	private ReferenceQueue<V> queue = new ReferenceQueue<V>();
	
	public SoftHashMap() {
		map = new HashMap<K, SoftValue>();		
		init();
	}
	
    public SoftHashMap(int initialCapacity) {
    	map = new HashMap<K, SoftValue>(initialCapacity);
    	init();
    }
    
    public SoftHashMap(int initialCapacity, float loadFactor) {
    	map = new SoftHashMap<K, SoftValue>(initialCapacity, loadFactor);
    	init();
    }
    
    private void init() {
    	new Timer().schedule(new ExpungeSoftValueTask(), 5 * 1000, 30 * 1000);
    }
	
	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return null;
	}
	
	@Override
	public Collection<V> values() {
		List<V> list = new ArrayList<V>();
		Collection<SoftValue> valueColl = map.values();
		if (CollectionUtils.isNotEmpty(valueColl)) {
			for (SoftValue sv : valueColl) {
				list.add(sv.get());
			}
		}
		return list;
	}
	
	@Override
	public V get(Object key) {
		V returned = null;
		SoftValue pre = map.get(key);
		if (pre != null) {
			returned = pre.get();
		}
		return returned;
	}
	
	@Override
	public V put(K key, V value) {
		V returned = null;
		SoftValue pre = map.put(key, new SoftValue(key, value));
		if (pre != null) {
			return pre.get();
		}
		return returned;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		if (map != null && map.size() > 0) {
			for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public V remove(Object key) {
		V returned = null;
		SoftValue pre = map.remove(key);
		if (pre != null) {
			returned = pre.get();
		}
		return returned;
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public void clear() {
		map.clear();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
        return false;
	}
	
	private class SoftValue extends SoftReference<V> {

		K key;
		
		SoftValue(K key, V referent) {
			super(referent, queue);	
			this.key = key;
		}
		
		public K getKey() {
			return key;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
            if (!(obj instanceof SoftHashMap.SoftValue)) {
                return false;
            }
            SoftValue sv = (SoftValue)obj;
            K key1 = getKey();
            K key2 = sv.getKey();
            if (key1 == key2 || (key1 != null && key1.equals(key2))) {
            	return true;
            }
            return false;
		}
		
        public int hashCode() {
            K k = getKey();
            return k == null ? 0 : k.hashCode();
        }
		
	}
	
	private class ExpungeSoftValueTask extends TimerTask {

		@Override
		@SuppressWarnings("unchecked")
		public void run() {		
	        for (Object obj; (obj = queue.poll()) != null; ) {
	            synchronized (queue) {
	            	SoftValue sv = (SoftValue)obj;
	            	if (sv.getKey() != null) {
	            		map.remove(sv.getKey());
	            	}
	            	sv = null;
	            }
	        }
		}
		
	}

}
