package com.ordint.tcpears.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.PositionService;
import com.ordint.tcpears.service.ReplayService;


@Component
public class MySqlReplayService implements ReplayService {
	private final static Logger log = LoggerFactory.getLogger(MySqlReplayService.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ClientDetailsResolver clientDetailsResolver;
	@Autowired
	private ClientManager clientManager;
	@Autowired
	private PositionServiceImpl positionService;
	@Autowired
	private PositionDecorators positionDecorators;

	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	protected DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("Hmmss.SS");
	
	private Map<String, ReplayDetails> runningReplays = new HashMap<>();
	
	public MySqlReplayService() {
	
	}

	public MySqlReplayService(ExecutorService executor) {
		this.executor = executor;
		
	}	


	@Override
	public String replayFrom(LocalDateTime startDateTime, int numberOfSeconds, boolean useOriginalTimestamp) {
		
		
		String start = startDateTime.toString();
		String end = startDateTime.plusSeconds(numberOfSeconds).toString();
		log.debug("REplaying from {} to {}", start, end);
		
		Future<?> replayFuture = executor.submit(new Runnable() {
			
			@Override
			public void run() {
				
				List<Position> replay = jdbcTemplate.query("select * from positionHistory where timeReceived > ? and timeReceived < ? order by gpsTimestamp asc",
						new Object[] {start,end},
						new RowMapper<Position>() {
							@Override
							public Position mapRow(ResultSet rs, int rowNum) throws SQLException {
								LocalDateTime timeCreated = LocalDateTime.parse(rs.getString("timeReceived"));
								LocalDateTime gpsTime = LocalDateTime.parse(rs.getString("gpsTimestamp"));
								long lag = rs.getLong("lag");
								return Position.builder()
									.clientDetails(clientDetailsResolver.resolveClientDetails(rs.getString("clientId")))
									.heading(rs.getString("heading"))
									.horizontalAccuracy(rs.getString("horizontalAccuracy"))
									.lat(rs.getString("lat"))
									.lon(rs.getString("lon"))
									.speed(rs.getString("speed"))
									.status(rs.getString("status"))
									.lag(0)
									.timestampFromDateTime(gpsTime)
									.timeCreated(gpsTime)
									.verticalAccuracy(rs.getString("verticalAccuracy"))
									.altitude(rs.getString("altitude"))
									.build();
								
							}
						});
				
		

				LocalDateTime lastTime = replay.get(0).getTimeCreated();
				for(Position p : replay) {
					//wait for the clock to tick over
					long milli = ChronoUnit.MILLIS.between( lastTime, p.getTimeCreated());
					if (milli < 20) {
						//gap is too short for thread sleep, delay with a while loop
						long interval = ChronoUnit.NANOS.between( lastTime, p.getTimeCreated());
						long startPoint = System.nanoTime();
						long stop = 0;
						do {
							stop = System.nanoTime();
	
						} while(startPoint + interval >= stop);
					} else {
						try {
							Thread.sleep(milli);
						} catch (InterruptedException e) {
							
						}
					}
					if (Thread.currentThread().isInterrupted()) {
						break;
					}

					
					if(useOriginalTimestamp) {
						clientManager.updatePostion(p);
					} else {
						//build new postiton with updated timestamps
						LocalDateTime timestamp = LocalDateTime.now(Clock.systemUTC());
						Position position =Position.builder()
							.clientDetails(p.getClientDetails())
							.heading(p.getHeading())
							.horizontalAccuracy(p.getHorizontalAccuracy())
							.lat(p.getLat())
							.lon(p.getLon())
							.speed(p.getSpeed())
							.status(p.getStatus())
							.timestampFromDateTime(timestamp.minus(p.getLag(), ChronoUnit.MILLIS))
							.timeCreated(timestamp)
							.verticalAccuracy(p.getVerticalAccuracy())
							.altitude(p.getAltitude())
							.build();
						positionService.update(position);
					}
					//System.out.println("P= " + p.getTimeCreated() + " " + interval);
					lastTime = p.getTimeCreated();
				}
				positionDecorators.clearDecorator("");
				log.info("Finished replay!");
				
			}
			
			
		});
		String id = String.valueOf(replayFuture.hashCode());
		runningReplays.put(id, new ReplayDetails(startDateTime.toString(), replayFuture));
		log.info("Replay id: {}", id);
		return id;
	}

	@Override
	public boolean endReplay(String replayId) {
		ReplayDetails replay = runningReplays.remove(replayId);
		if (replay != null) {
			return replay.replayFuture.cancel(true);
		}
		return false;
	}
	
	public void clearReplayTrack(String replayId) {
		
	}


	
	static class ReplayDetails {
		final String startTime;
		final Future<?> replayFuture;
		ReplayDetails(String startTime, Future<?> replayFuture) { 
			this.startTime = startTime;
			this.replayFuture = replayFuture;
		
		}
	}

}
