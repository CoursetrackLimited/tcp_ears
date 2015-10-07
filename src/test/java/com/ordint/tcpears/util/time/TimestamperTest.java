package com.ordint.tcpears.util.time;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
@Ignore
public class TimestamperTest {
	private Clock clock = Clock.systemUTC();
	@Before
	public void setUp() throws Exception {
	}
	
	
	//@Test
	public void shouldListNanos() throws Exception {
		for (int g =0; g<200000;g ++) {
			Timestamper t = Timestamper.nanoTimestamper();
			Set<Long> sd = new HashSet<>();
			ConcurrentHashMap<Long, Long> sds = new ConcurrentHashMap<>();
			for(int i = 0; i < 1000000; i ++) {
				//for(int k = 0; k < 10000; k ++) {} 
				//Thread.currentThread().sleep(0, 100);
				LocalDateTime t1 = LocalDateTime.now(clock);
				LocalDateTime t2 = t.now();
				Duration p = Duration.between(t1, t2);
				System.out.println(t1 + " " + t2 + " " + t2.truncatedTo(ChronoUnit.MICROS)  + " " + p.toMillis());
				//sd.add(p.toMillis());
				sds.putIfAbsent(p.toMillis(), 1l);
				sds.computeIfPresent(p.toMillis(), (k,v) -> v = v +1);
			}
			//System.out.println(sd);
			StringBuilder out = new StringBuilder();
			for(Entry<Long, Long> e :sds.entrySet()){
				out.append("[").append(e.getKey()).append("=").append(e.getValue()).append("],");
			}
			out.setLength(out.length()-1);
			System.out.println(out.toString());
			
		}
	}
	@Test
	public void shouldListNanos2() throws Exception {
		StringBuilder out = new StringBuilder();
		for (int g =0; g<200;g ++) {
			Timestamper t = Timestamper.nanoTimestamper();
			Set<Long> sd = new HashSet<>();
			ConcurrentHashMap<Long, Long> sds = new ConcurrentHashMap<>();
			for(int i = 0; i < 10; i ++) {
				//for(int k = 0; k < 10000; k ++) {} 
				LocalDateTime t1 = LocalDateTime.now(clock);
				LocalDateTime t2 = t.now();
				Duration p = Duration.between(t1, t2);
				//System.out.println(t1 + " " + t2 + " " + t2.truncatedTo(ChronoUnit.MICROS)  + " " + p.toMillis());
				//sd.add(p.toMillis());
				sds.putIfAbsent(p.toMillis(), 1l);
				sds.computeIfPresent(p.toMillis(), (k,v) -> v = v +1);
			}
			//System.out.println(sd);
			
			for(Entry<Long, Long> e :sds.entrySet()){
				out.append("[").append(e.getKey()).append("=").append(e.getValue()).append("],");
			}
			out.setLength(out.length()-1);
			
			
		}
		System.out.println(out.toString());
	}
	
}
