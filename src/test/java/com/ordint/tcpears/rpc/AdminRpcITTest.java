package com.ordint.tcpears.rpc;

import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ordint.rpc.JsonRpcHttpClient;
import com.ordint.rpc.ProxyUtil;
import com.ordint.tcpears.service.AdministrationService;

@Ignore
public class AdminRpcITTest {

    private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void test() throws Exception {
	    mapper.enable(SerializationFeature.INDENT_OUTPUT);
		//JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://10.10.0.142:6013"));
		//JsonRpcHttpClient client = new JsonRpcHttpClient(new URL("http://78.110.162.226:6013"));
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
			//admin.startRace(150);
			
			//admin.replay("2015-09-16T16:13:21","180", false);
			//
			
			//
			
			//admin.replayRace(3635);
			admin.startTracking("8");
			System.out.println(mapper.writeValueAsString(admin.getGroupTracks()));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		
	}

}
