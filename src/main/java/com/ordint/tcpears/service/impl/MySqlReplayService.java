package com.ordint.tcpears.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
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
	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	protected DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("HHmmss.SS");
	
	private Map<String, Future<?>> runningReplays = new HashMap<>();
	
	public MySqlReplayService() {
	
	}

	public MySqlReplayService(ExecutorService executor) {
		this.executor = executor;
		
	}	


	@Override
	public String replayFrom(LocalDateTime startDateTime, int numberOfSeconds) {
		
		
		String start = startDateTime.toString();
		String end = startDateTime.plusSeconds(numberOfSeconds).toString();
		log.debug("REplaying from {} to {}", start, end);
		List<Position> replay = jdbcTemplate.query("select * from positionHistory where timeReceived > ? and timeReceived < ? order by timeReceived asc",
				new Object[] {start,end},
				new RowMapper<Position>() {
					@Override
					public Position mapRow(ResultSet rs, int rowNum) throws SQLException {
						return Position.builder()
							.clientDetails(clientDetailsResolver.resolveClientDetails(rs.getString("clientId")))
							.heading(rs.getString("heading"))
							.horizontalAccuracy(rs.getString("horizontalAccuracy"))
							.lat(rs.getString("lat"))
							.lon(rs.getString("lon"))
							.speed(rs.getString("speed"))
							.status(rs.getString("status"))
							.lag(rs.getLong("lag"))
							.timestamp(rs.getString("gpsTimestamp"))
							.timeCreated(LocalDateTime.parse(rs.getString("timeReceived")))
							.verticalAccuracy(rs.getString("verticalAccuracy"))
							.altitude(rs.getString("altitude"))
							.build();
						
					}
				});
		
		
		Future<?> replayFuture = executor.submit(new Runnable() {
		
			@Override
			public void run() {
				LocalDateTime lastTime = replay.get(0).getTimeCreated();
				for(Position p : replay) {
					//wait for the clock to tick over
					long milli = ChronoUnit.MILLIS.between( lastTime, p.getTimeCreated());
					if (milli < 20) {
						long interval = ChronoUnit.NANOS.between( lastTime, p.getTimeCreated());
						long startPoint = System.nanoTime();
						long stop = 0;
						do {
							stop = System.nanoTime();
	
						} while(startPoint + interval >= stop);
					} else {
						try {
							Thread.currentThread().sleep(milli);
						} catch (InterruptedException e) {
							
						}
					}
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					//build new postiton with updated timestamps
					LocalDateTime timestamp = LocalDateTime.now();
					Position position =Position.builder()
						.clientDetails(p.getClientDetails())
						.heading(p.getHeading())
						.horizontalAccuracy(p.getHorizontalAccuracy())
						.lat(p.getLat())
						.lon(p.getLon())
						.speed(p.getSpeed())
						.status(p.getStatus())
						.timestamp(timestampFormatter.format(timestamp.minusNanos(p.getLag() * 1_000_000)))
						.timeCreated(timestamp)
						.verticalAccuracy(p.getVerticalAccuracy())
						.altitude(p.getAltitude())
						.build();
					
					clientManager.updatePostion(position);
					//System.out.println("P= " + p.getTimeCreated() + " " + interval);
					lastTime = p.getTimeCreated();
				}
				
			}
		});
		String id = String.valueOf(replayFuture.hashCode());
		runningReplays.put(id, replayFuture);
		log.info("Replay id: {}", id);
		return id;
	}

	@Override
	public boolean endReplay(String replayId) {
		Future<?> replay = runningReplays.remove(replayId);
		if (replay != null) {
			return replay.cancel(true);
		}
		return false;
	}


	
	

}
