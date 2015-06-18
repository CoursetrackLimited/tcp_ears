package com.ordint.tcpears.server;

import java.util.HashMap;
import java.util.Map;

import org.boon.json.ObjectMapper;
import org.boon.json.implementation.ObjectMapperImpl;
import org.junit.Before;
import org.junit.Test;

public class PositionUtilTest {

	@Before
	public void setUp() throws Exception {
	}
	
	
	@Test
	public void testThing() {
		
		StringBuilder track = new StringBuilder("123.3213,1232,22,12 123.444,223.12,32 11.111,122.22,4 ");
		
		if(track.length() > 45) {
			String str = track.toString();
			int lastTripleIndex = str.substring(0, str.length()-20).lastIndexOf(" ");
			track.delete(lastTripleIndex + 1, track.length() );
		}
		System.out.println(track + "|");
	}
	
	@Test 
	public void serializeMap() {
		
		Map<String, String> map = new HashMap<String, String>();
		
		map.put("key1", "comma,speratated,list");
		map.put("key2", "comma2,speratated2,list2");
		
		ObjectMapper mapper = new ObjectMapperImpl();
		
		String out = mapper.writeValueAsString(map);
		
		System.out.println(out);
		
		Map<String, String> map2 = mapper.readValue(out, Map.class);
	}
}
