package com.ordint.tcpears.memcache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.MemcachedClient;

public class MemcachedClientImpl extends MemcachedClient implements Memcached {

	public MemcachedClientImpl(InetSocketAddress... ia) throws IOException {
		super(ia);
		
	}

	public MemcachedClientImpl(List<InetSocketAddress> addrs)
			throws IOException {
		super(addrs);
		
	}

	public MemcachedClientImpl(ConnectionFactory cf,
			List<InetSocketAddress> addrs) throws IOException {
		super(cf, addrs);
		
	}

}
