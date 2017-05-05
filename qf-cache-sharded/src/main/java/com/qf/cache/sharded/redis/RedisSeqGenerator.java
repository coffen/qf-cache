package com.qf.cache.sharded.redis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.JedisCluster;

public class RedisSeqGenerator {
	
	private static Logger log = LoggerFactory.getLogger(RedisSeqGenerator.class);
	
	private final JedisCluster jedisCluster;
	private String scriptPath = "/lua/redis-script.lua";
	
	private Map<String, String> mapping = new HashMap<String, String>();
	private String script;
	
	public RedisSeqGenerator(JedisCluster cluster) throws Exception {
		this.jedisCluster = cluster;
		script = loadScript();
	}
	
	public void addGroup(String group) throws Exception {
		if (StringUtils.isBlank(group)) {
			log.error("添加序列号分组失败, 参数为空");
			return;
		}
		String groupKey = buildGroupKey(group);
		String shal = this.jedisCluster.scriptLoad(script, groupKey);
		if (StringUtils.isNotBlank(shal)) {
			mapping.put(groupKey, shal);
		}
	}
	
	public long generate(String group) {
		if (StringUtils.isBlank(group)) {
			log.error("生成序列号失败, 参数为空");
			return -1;
		}
		String groupKey = buildGroupKey(group);
		String shal = mapping.get(groupKey);
		if (StringUtils.isBlank(shal)) {
			log.error("生成序列号失败, 分组尚未添加: {}", group);
			return -1;
		}
		Object result = this.jedisCluster.evalsha(shal, 1, groupKey);
		log.error(JSON.toJSONString(result));
		return 0;
	}
	
	private String loadScript() throws Exception {
		String basePath = getClass().getClassLoader().getResource("").toString();
		if (scriptPath.startsWith("/")) {
			scriptPath = scriptPath.substring(1);
		}
		URL url = new URL(basePath + scriptPath);
		BufferedReader reader = new BufferedReader(new FileReader(url.getFile()));
		StringBuilder builder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		reader = null;
		return builder.toString();
	}
	
	private String buildGroupKey(String group) {
		return "_" + group;
	}

}
