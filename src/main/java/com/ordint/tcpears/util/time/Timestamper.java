package com.ordint.tcpears.util.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class Timestamper {
	
	private ThreadLocal<Timestamp> timestamp;
	
	
	
	private Timestamper(TimeProvider timeProvider) {
		timestamp = new ThreadLocal<Timestamp>() {
			  @Override
			  protected Timestamp initialValue() {
			   return new Timestamp(timeProvider);
			  }
		};		
	}

	public static Timestamper nanoTimestamper() {
		return new Timestamper(new NanoTimeProvider());
	}
	public static Timestamper fixedTimestamper() {
		return new Timestamper(new FixedTimeProvider());
	}
	public static Timestamper defaultTimestamper() {
		return new Timestamper(new FixedTimeProvider());
	}	
	
	public LocalDateTime now() {
		return LocalDateTime.ofInstant(timestamp.get().now(), ZoneId.of("UTC"));
	}

}
class DefaultTimeProvider implements TimeProvider {
	private Clock clock = Clock.systemUTC();
	@Override
	public Instant now() {
		return Instant.now(clock);
	}

	@Override
	public long nanoSeconds() {
		return 0;
	}

}


class NanoTimeProvider implements TimeProvider {
	private Clock clock = Clock.systemUTC();
	@Override
	public Instant now() {
		return Instant.now(clock);
	}

	@Override
	public long nanoSeconds() {
		return System.nanoTime();
	}

}

class FixedTimeProvider implements TimeProvider {
	final long nano;
	Instant now;
	FixedTimeProvider() {
		now = Instant.now();
		nano = System.nanoTime();
	}
	@Override
	public Instant now() {
		return now;
	}

	@Override
	public long nanoSeconds() {
		return nano;
	}

}
final class Timestamp {

	   private long startNanoseconds;
	   private Instant start;
	   private TimeProvider timeProvider;
	   private long resetCounter;

	   protected Timestamp(TimeProvider timeProvider) {
		  this.timeProvider = timeProvider;
		  reset();
	   }

	   private void reset() {
			  startNanoseconds = timeProvider.nanoSeconds() ;
			  start = timeProvider.now();
			  resetCounter = start.toEpochMilli();
		
	}

	public Instant now(){
		  if (timeProvider.now().toEpochMilli() - resetCounter > 50) reset();
	      long nanosToAdd = timeProvider.nanoSeconds() - startNanoseconds;
	      startNanoseconds = timeProvider.nanoSeconds();
	      start = start.plusNanos(nanosToAdd);
	      
	      return start;
	   }
	}