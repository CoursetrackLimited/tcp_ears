package com.ordint.tcpears.util.prediction;

import org.junit.Before;
import org.junit.Test;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.util.PredictionUtil;

public class TrackBasedPredictorTest {
	
	TrackPathBuilder trackPathBuilder = new StaticTrackPathBuilder();
	
	@Before
	public void setUp() throws Exception {
	}
	
	String test="-0.4011335298785435,51.41733331888029,0 -0.4009605263299365,51.41740987031915,0 -0.4009160724577032,51.41752982920541,0 -0.400986172888893,51.4176800395204,0 -0.4011784167884824,51.41775966655149,0 -0.4015138125599982,51.41779422064939,0 -0.4016641846663749,51.41768737787782,0 -0.4017091643317772,51.41756945868331,0 -0.401690492828074,51.41746946132648,0 -0.4015930222785025,51.41739066872717,0 -0.4014789255348294,51.41734640019065,0 -0.4013066369003704,51.41732309129238,0"; 

	@Test
	public void test() {
		
		TrackBasedPredictor util = new TrackBasedPredictor(trackPathBuilder.build("KEMPTON"));
		
		Position p = Position.builder()
				.altitude("10")
				.speed("15")
				.lon("-0.406510")
				.lat("51.41973")
				.build();
		Position p2 = Position.builder()
				.altitude("10")
				.speed("15")
				.lon("-0.406527")
				.lat("51.41977")
				.build();
		Position p3= Position.builder()
				.altitude("10")
				.speed("15")
				.lon("-0.406527")
				.lat("51.41989")
				.build();
	
		long t = System.currentTimeMillis();
		int i =0;
		for (; i< 2; i++) {
			util.predict(p,  5000, 29, null);
			util.predict(p2,  5000, 29, null);
			util.predict(p3,  5000, 29, null);
			System.out.println(util.predict(p,  5000, 29, null));
			System.out.println(util.predict(p2,  5000, 29, null));
			System.out.println(util.predict(p3,  5000, 29, null));
		}
		double t1 = System.currentTimeMillis() -t;
		System.out.println( t1);
		System.out.println( t1/ i);
		
		
		
	}
	


}
