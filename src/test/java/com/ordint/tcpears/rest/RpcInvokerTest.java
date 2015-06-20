package com.ordint.tcpears.rest;

import java.awt.List;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.ordint.tcpears.domain.ClientDetails;

public class RpcInvokerTest {

	
	
	@Before
	public void setUp() throws Exception {
	}

	
	public void testInvoke() throws Exception {
		RpcInvoker invoker = new RpcInvoker(null);
		String rpcCall = "{\"jsonrpc\":\"2.0\",\"method\":\"login\",\"params\":[\"1\",\"val1\",\"13\"],\"id\":\"25203cbb83e273cbfc001fd5da67a559\"}";
		invoker.invoke(rpcCall);
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
