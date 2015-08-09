package com.ordint.tcpears.memcache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;



public class MemcacheHelper {
	
	

	
	private Memcached memcachedClient;
	
	public MemcacheHelper() {
		memcachedClient = new DevMemcachedClient();
	}
	public MemcacheHelper(String hostname, int port) throws IOException {
		memcachedClient = new MemcachedClientImpl(new InetSocketAddress(hostname, port));
	}
	
	private final ObjectMapper objectMapper = new ObjectMapperImpl();
	
	@SuppressWarnings("unchecked")
	public Map<String,String> getMap(String namespace, String objectKey) throws  IOException {
		Object json = getObject(namespace, objectKey);
		if (json == null) {
			return new HashMap<String,String>();
		}
		return objectMapper.readValue(json.toString(), Map.class);
		//return (Map<String, String>) json;
	}
	public Future<Boolean> set(String namespace, String objectKey, Map<?,?> map) throws IOException {
		String json = objectMapper.writeValueAsString(map);
		return set(namespace, objectKey, json);
	}
	public Future<Boolean> set(String namespace, String objectKey, Map<?,?> map, int timeout) throws IOException {
		String json = objectMapper.writeValueAsString(map);
		return set(namespace, objectKey, json, timeout);
	}
	public Future<Boolean> set(String namespace, String objectKey, Object object, int timeout) {
		String key = getNamespaceKey(namespace, objectKey);
		return memcachedClient.set(key, timeout, object);
	}
	public Future<Boolean> set(String namespace, String objectKey, Object object) {
		String key = getNamespaceKey(namespace, objectKey);
		return memcachedClient.set(key, 0, object);
	}
	
	public void clear(String namespace, String objectKey) {
		memcachedClient.delete(getNamespaceKey(namespace, objectKey));
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
		
		return "--DateStamp=" + LocalDateTime.now() + "--";
	}
	private String getItemKey(String namespace, String namespaceKeyOffset, String objectKey) {
		return String.format("%s_%s_%s", namespace, namespaceKeyOffset, objectKey);
	}
}
