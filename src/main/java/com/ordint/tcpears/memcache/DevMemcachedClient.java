package com.ordint.tcpears.memcache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DevMemcachedClient implements Memcached {
	private static final Logger log = LoggerFactory.getLogger(DevMemcachedClient.class);	
	private ConcurrentHashMap<String, Object> cached = new ConcurrentHashMap<>();
		
			
	public DevMemcachedClient() {
		
	}

	@Override
	public long incr(String key, int by) {
		//log.info("inc({}, {}", key, by);

		return 0;
		
	}


	@Override
	public Future<Boolean> set(final String key, int exp, final Object o) {
		log.info("set ({}, {})", key, o);
		cached.put(key, o);
		return null;		
		
	}
	

	@Override
	public Object get(String key) {
		Object val = cached.get(key);
		log.info("get {} = {}", key, val);
		return val;
	}
	
	
}
