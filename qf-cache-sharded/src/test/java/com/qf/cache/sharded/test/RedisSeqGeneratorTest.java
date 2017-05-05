package com.qf.cache.sharded.test;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qf.cache.sharded.redis.RedisSeqGenerator;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

public class RedisSeqGeneratorTest {
	
	private Logger log = LoggerFactory.getLogger(RedisSeqGeneratorTest.class);
	
	RedisSeqGenerator generator;
	
	@Before
	public void before() throws Exception {
		Set<HostAndPort> nodeSet = new HashSet<HostAndPort>();
		nodeSet.add(new HostAndPort("192.168.48.128", 9001));
		nodeSet.add(new HostAndPort("192.168.48.128", 9002));
		nodeSet.add(new HostAndPort("192.168.48.128", 9003));
		nodeSet.add(new HostAndPort("192.168.48.128", 9004));
		nodeSet.add(new HostAndPort("192.168.48.128", 9005));
		nodeSet.add(new HostAndPort("192.168.48.128", 9006));
		generator = new RedisSeqGenerator(new JedisCluster(nodeSet));
		generator.addGroup("test");
	}
	
	@Test
	public void testRedisSeqGenerator() {
		long seq = generator.generate("test");
		log.error("序列号: {}", seq);
	}
	
}
