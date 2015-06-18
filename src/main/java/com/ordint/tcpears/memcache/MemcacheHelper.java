package com.ordint.tcpears.memcache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import net.spy.memcached.MemcachedClient;

import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;
import org.joda.time.DateTime;


public class MemcacheHelper {
	
	

	
	private Memcached memcachedClient;
	
	public MemcacheHelper() {
		memcachedClient = new DevMemcachedClient();
	}
	public MemcacheHelper(String hostname, int port) throws IOException {
		memcachedClient = new MemcachedClientImpl(new InetSocketAddress(hostname, port));
	}
	
	private final ObjectMapper objectMapper = new ObjectMapperImpl();
	
	public Map<String,String> getMap(String namespace, String objectKey) throws  IOException {
		Object json = getObject(namespace, objectKey);
		if (json == null) {
			return new HashMap<String,String>();
		}
		return objectMapper.readValue(json.toString(), Map.class);
		//return (Map<String, String>) json;
	}
	public Future<Boolean> set(String namespace, String objectKey, Map<?,?> map) throws IOException {
		String key = getNamespaceKey(namespace, objectKey);
		
		String json = objectMapper.writeValueAsString(map);
		return memcachedClient.set(key, 0, json);
	}	
	public Future<Boolean> set(String namespace, String objectKey, Object object) {
		String key = getNamespaceKey(namespace, objectKey);
		return memcachedClient.set(key, 0, object);
	}
	
	public Object getObject(String namespace, String objectKey ) {
		return memcachedClient.get(getNamespaceKey(namespace, objectKey));
	}
	
	public String getNamespaceKey(String namespace, String objectKey) {
		//return namespace + objectKey;
	
		 String namespaceKeyOffset = (String) memcachedClient.get(namespace);
		 if(namespaceKeyOffset == null) {
			 namespaceKeyOffset = initNamespace(namespace);
		 }
		 return getItemKey(namespace,namespaceKeyOffset, objectKey);
		 
	}

	private String initNamespace(String namespace) {
		
		return initNamespaceNsk(namespace, getNextKey(namespace));
	}
	private String initNamespaceNsk(String namespace, String nextKey) {
		memcachedClient.set(namespace, 0, nextKey);
		return nextKey;
	}
	private String getNextKey(String namespace) {
		
		return "--DateStamp=" + DateTime.now() + "--";
	}
	private String getItemKey(String namespace, String namespaceKeyOffset, String objectKey) {
		return String.format("%s_%s_%s", namespace, namespaceKeyOffset, objectKey);
	}
}
