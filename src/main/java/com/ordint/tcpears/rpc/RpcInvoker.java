package com.ordint.tcpears.rpc;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.boon.core.value.ValueList;
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
	public Object invoke(String rpcString) {
			
		Map<String, Object> request = (Map<String, Object>) mapper.fromJson(rpcString);
		String methodName = request.get("method").toString();
		String id = request.get("id").toString();
		Method m = methods.get(methodName);
		List<Object> params = (List<Object>) request.get("params");
		Object[] args =  params.toArray(new Object[params.size()]);
		if (params.get(0) instanceof ValueList) {
			ValueList v = (ValueList) params.get(0);
			args[0] =  v.toArray(new String[0]);
		}
		//args = new Object[] {new String[] {"TC1", "TC2"}, "1"};

		return ReflectionUtils.invokeMethod(m, adminService, args);
				
	}
	
	protected void blurg(String rpcString) {
	
		
	}	
	

}
