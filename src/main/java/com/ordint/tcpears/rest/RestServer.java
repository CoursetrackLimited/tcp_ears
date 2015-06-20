package com.ordint.tcpears.rest;

import java.io.IOException;

import IceBreakRestServer.IceBreakRestServer;

import com.ordint.tcpears.service.AdministrationService;

public class RestServer implements Runnable{
	private RpcInvoker invoker;
	public RestServer(AdministrationService adminService) {
		 invoker = new RpcInvoker(adminService);
	}

	@Override
	public void run()  {

	    // Declare the IceBreak HTTP REST server class
	    IceBreakRestServer rest;   

	    try { 

	      // Instantiate it once 
	      rest  = new IceBreakRestServer();  
	      rest.setPort(65000);
	     
	      while (true) {
	    	System.out.println("Incomming!!!!!! " );       
	        // Now wait for any HTTP request  
	        // the "config.properties" file contains the port we are listening on 
	        rest.getHttpRequest();
	        invoker.invoke(rest.payload);

	      }
	    }
	    catch (IOException ex) {
	      System.out.println(ex.getMessage());
	    }
	  }
}
