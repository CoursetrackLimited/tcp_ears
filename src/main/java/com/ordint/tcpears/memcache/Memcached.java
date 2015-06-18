package com.ordint.tcpears.memcache;

import java.util.concurrent.Future;

public interface Memcached {
	
	long incr(String key, int by);
	
	Future<Boolean> set(String key, int exp, Object o);
	
	Object get(String key);
	

}
