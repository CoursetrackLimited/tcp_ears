package com.ordint.tcpears.server;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class LatLongUtilTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
		public void testSupplementMsgWithShortInput() throws Exception {
	/*
	 * 400A7C,110338.40,5101.63261,-00000.49920,0.080000
	GR1,400A7C,$PUBX,99,110338.40,51.027210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1
	
	406C43,123423.19,5125.84923,00007.17300,0.080000,119.430000,0.700000,0.600000,2000,D
	GR1,406C43,$PUBX,99,123423.19,51.4308205,N,0.11955,E,2000,D,0.700000,0.600000,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1		
	 */
			String input = "400A7C,110338.40,5101.63261,-0.49920,0.080000";
			String expected ="GR1,400A7C,$PUBX,99,110338.40,51.027210166667,N,-0.00832,W,-1,-1,-1,-1,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
			
			assertThat(LatLongUtil.supplementMsg(input, "GR1"), equalTo(expected));
			
		}
	@Test
	public void testSuppplemntMsgWithLongInput() throws Exception {
		String input = "400678,105413.15,5146.81252,-00019.47240,0.080000,119.430000,0.700000,0.600000,19925,D";
		
		String expected ="GR1,400678,$PUBX,99,105413.15,51.780208666667,N,-0.32454,W,6073.14,D,0.700000,0.600000,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1";
		assertThat(LatLongUtil.supplementMsg(input, "GR1"), equalTo(expected));
	}
	@Test
	public void testPosIn() {
        String latIn = "5101.63261";
        String lonIn = "-00000.49920";
        
        String latOut = LatLongUtil.posToDec("5101.63261");
        String lonOut = LatLongUtil.posToDec("00000.49920");
        //5101.63261N=51.027210166667 00000.49920W=-0.00832
       
        System.out.println(latOut + " " + lonOut);
        
        
	}
	

}
