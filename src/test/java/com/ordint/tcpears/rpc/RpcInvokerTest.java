package com.ordint.tcpears.rpc;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ordint.tcpears.service.AdministrationService;
@RunWith(MockitoJUnitRunner.class)
public class RpcInvokerTest {
	
	@Mock
	private AdministrationService administrationService;
	private final static String RPC_CALL = "{\"jsonrpc\":\"2.0\",\"method\":\"%s\",\"params\":[%s],\"id\":\"25203cbb83e273cbfc001fd5da67a559\"}";
	
	@InjectMocks
	private RpcInvoker invoker;
	
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void shouldStartTracking() throws Exception {
		
		String rpcCall = String.format(RPC_CALL, "startTracking", "\"1\"");
		
		invoker.invoke(rpcCall);
		
		Mockito.verify(administrationService).startTracking("1");
		
	}
	@Test
	public void shouldStopTracking() throws Exception {
		
		String rpcCall = String.format(RPC_CALL, "stopTracking", "\"1\"");
		
		invoker.invoke(rpcCall);
		
		Mockito.verify(administrationService).stopTracking("1");
		
	}	
	@Test
	public void shouldSetClientsGroup() throws Exception {
		
		String rpcCall = String.format(RPC_CALL, "setClientsGroup", "[\"TC1\",\"TC2\"],\"1\"");
		
		invoker.invoke(rpcCall);
		
		Mockito.verify(administrationService).setClientsGroup(new String[] {"TC1",  "TC2"}, "1");		
	}
	@Test
	public void test() {
		String[] pram = {"one","two","three"};
		
		Map<String, Object> params = new HashMap<>();
		
		params.put("method", "someMethod");
		params.put("params", Arrays.asList(pram, "groupId"));
		
		ObjectMapper mapper = new ObjectMapperImpl();
		String s = mapper.writeValueAsString(params);
		System.out.println(s);
		
		Map<String, Object> reverse = mapper.parser().parseMap(s);
		
		System.out.println(((java.util.List<Serializable>) reverse.get("params")).get(0));
		
	}

}
