package com.ordint.tcpears.rpc;

import java.net.URL;

import com.ordint.rpc.JsonRpcHttpClient;
import com.ordint.rpc.ProxyUtil;
import com.ordint.tcpears.service.AdministrationService;

public class AdminRpcITTest {

	//@Test
	public void test() throws Exception {
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://localhost:6013"));
		
		
		AdministrationService admin = ProxyUtil.createClientProxy(AdminRpcITTest.class.getClassLoader(), AdministrationService.class, client);
		
		System.out.println(admin.replay("2015-08-25T13:28:11.811", "10"));
	}

}
