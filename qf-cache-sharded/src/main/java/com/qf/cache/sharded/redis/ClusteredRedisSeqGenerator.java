package com.qf.cache.sharded.redis;

import redis.clients.jedis.JedisCluster;

public class ClusteredRedisSeqGenerator {
	
	private final JedisCluster jedisCluster;
	
	public ClusteredRedisSeqGenerator(JedisCluster cluster) {
		this.jedisCluster = cluster;
	}
	
	public long generate() {
		return 0;
		
	}

}
