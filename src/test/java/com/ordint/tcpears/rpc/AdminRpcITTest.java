package com.ordint.tcpears.rpc;

import java.net.URL;

import org.junit.Test;

import com.ordint.rpc.JsonRpcHttpClient;
import com.ordint.rpc.ProxyUtil;
import com.ordint.tcpears.service.AdministrationService;

public class AdminRpcITTest {

	@Test
	public void test() throws Exception {
		//JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://10.10.0.148:6013"));
		//JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://78.110.162.225:6013"));
		JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://localhost:6013"));
		
		System.out.println("REsult = " ); 
		AdministrationService admin = ProxyUtil.createClientProxy(AdminRpcITTest.class.getClassLoader(), AdministrationService.class, client);
		
		//System.out.println(admin.replay("2015-08-25T13:28:11.811", "10"));
		
		
		//System.out.println("REsult = " + admin.groupClientsByGroup());
		try {
			//admin.finishRace(132);
			//System.out.println(admin.replayRace(134));
			//admin.refreshClientDetails();
			//2015-09-22 17:41:39
			//admin.startRace(raceId);
			
			//admin.replay("2015-09-16T16:13:21","180", false);
			admin.replayRace(140);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
	}

}
