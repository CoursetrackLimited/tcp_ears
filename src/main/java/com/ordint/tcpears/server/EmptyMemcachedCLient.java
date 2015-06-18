package com.ordint.tcpears.server;

import java.util.concurrent.Future;

import com.ordint.sportradar.memcached.Memcached;

public class EmptyMemcachedCLient implements Memcached {

	@Override
	public long incr(String key, int by) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Future<Boolean> set(String key, int exp, Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
