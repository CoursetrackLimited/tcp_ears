package com.ordint.tcpears.rest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.boon.json.JsonParser;
import org.boon.json.JsonParserFactory;
import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;
import org.springframework.util.ReflectionUtils;

import com.ordint.tcpears.service.AdministrationService;


public class RpcInvoker {
	
	private ObjectMapper mapper = new ObjectMapperImpl();
	
	private AdministrationService adminService;
	private ConcurrentMap<String, Method> methods = new ConcurrentHashMap<>();
	
	public RpcInvoker(AdministrationService adminService) {
		this.adminService = adminService;
		for(Method m :AdministrationService.class.getMethods()) {
			methods.putIfAbsent(m.getName(), m);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void invoke(String rpcString) {
		
		
		Map<String, Object> request = (Map<String, Object>) mapper.fromJson(rpcString);
		String methodName = request.get("method").toString();
		
		Method m = methods.get(methodName);
		List<Object> things = (List<Object>) request.get("params");
		ReflectionUtils.invokeMethod(m, adminService, things.toArray(new Object[things.size()]));
				
	}

}
