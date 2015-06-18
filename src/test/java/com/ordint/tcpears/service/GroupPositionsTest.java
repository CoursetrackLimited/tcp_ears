package com.ordint.tcpears.service;

import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.memcache.MemcacheHelper;
import com.ordint.tcpears.server.LatLongUtil;
import com.ordint.tcpears.server.Position;

public class GroupPositionsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testUpdate() throws Exception {
		GroupPositions service = new GroupPositions(new MemcacheHelper());
		Position p = new Position(LatLongUtil.supplementMsg("406CCF,024559.06,5104.69986,00003.31440,0.080000", "GR1"));
		service.update(p);
		
		
		StringBuilder strTracks = new StringBuilder("one,two,three four,five,six seve,eight,nine ");
		
		if(strTracks.length() > 30) {
			int lastTripleIndex = strTracks.lastIndexOf(" ", strTracks.length()-20);
			strTracks.delete(lastTripleIndex + 1, strTracks.length());
		}
		System.out.println(strTracks);
		 int firstTripleIndex=strTracks.indexOf(" ");
		String firstTriple=strTracks.substring(0, firstTripleIndex);
		String speed = firstTriple.substring(firstTriple.lastIndexOf(",") + 1, firstTriple.length());		
		System.out.println(speed);
	}

}
