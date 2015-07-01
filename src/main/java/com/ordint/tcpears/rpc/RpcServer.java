package com.ordint.tcpears.rpc;

import java.io.IOException;

import com.ordint.tcpears.service.AdministrationService;

public class RpcServer implements Runnable{
	private RpcInvoker invoker;
	private int port;
	public RpcServer(AdministrationService adminService, int port) {
		 invoker = new RpcInvoker(adminService);
		 this.port = port;
	}

	@Override
	public void run()  {

	    // Declare the IceBreak HTTP REST server class
	    IceBreakerServer server;   

	    try { 

	      // Instantiate it once 
	    server  = new IceBreakerServer();  
	    server.setPort(port);
	     
	      while (true) {
	    	System.out.println("Incomming!!!!!! " );       
	        // Now wait for any HTTP request  
	        // the "config.properties" file contains the port we are listening on 
	    	server.getHttpRequest();
	        invoker.invoke(server.payload);
	       

	      }
	    }
	    catch (IOException ex) {
	      System.out.println(ex.getMessage());
	    }
	  }
}
