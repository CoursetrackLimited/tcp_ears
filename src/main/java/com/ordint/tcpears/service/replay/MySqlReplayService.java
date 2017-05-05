package com.ordint.tcpears.service.replay;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.ordint.tcpears.domain.lombok.Position;
import com.ordint.tcpears.service.ClientDetailsResolver;
import com.ordint.tcpears.service.ClientManager;
import com.ordint.tcpears.service.ReplayService;
import com.ordint.tcpears.service.race.DefaultRaceService;



public class MySqlReplayService implements ReplayService {
	private final static Logger log = LoggerFactory.getLogger(MySqlReplayService.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ClientDetailsResolver clientDetailsResolver;
	@Autowired
	private ClientManager clientManager;

	@Autowired
	private DefaultRaceService raceService;

	
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	protected DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("Hmmss.SS");
	
	private Map<String, ReplayDetails> runningReplays = new HashMap<>();
	private GpsTimeReplayer currentReplay;
	
	public MySqlReplayService() {
	
	}

	public MySqlReplayService(ExecutorService executor) {
		this.executor = executor;
		
	}	


	@Override
	public String replayFrom(LocalDateTime startDateTime, int numberOfSeconds, boolean useOriginalTimestamp,
			String replayId) {
		
		Object start = startDateTime.toString();
		Object end = startDateTime.plusSeconds(numberOfSeconds).toString();
		log.info("REplaying from {} to {}", start, end);
		
		SqlRowSet rs = jdbcTemplate.queryForRowSet("select * from replays where replay_id=?", replayId);
		if (rs.next()) {
			start = rs.getLong(2);
			end = rs.getLong(3);
		}
		currentReplay = new GpsTimeReplayer(replayId, start, end);
		Future<?> replayFuture = executor.submit(currentReplay);

		runningReplays.put(replayId, new ReplayDetails(startDateTime.toString(), replayFuture));
		log.info("Replay id: {}", replayId);
		return replayId;
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



	@Override
	public String replayFrom(LocalDateTime parse, int parseInt, boolean userOriginalTimeStamp) {
		
		return replayFrom(parse, parseInt, userOriginalTimeStamp, "" + System.currentTimeMillis());
	}
	
	class GpsTimeReplayer implements Callable<String>{
		
		private String replayId;
		private Object[] params;
		private RowMapper<Position> rowMapper = new PositionRowMapper(clientDetailsResolver);
		private volatile boolean pause;
		
		
		public GpsTimeReplayer() {
			// TODO Auto-generated constructor stub
		}
		public GpsTimeReplayer(String replayId, Object... params) {
			super();
			this.replayId = replayId;
			this.params = params;
		}		
		public boolean stopStart() {
			pause = !pause;
			return pause;
		}
		
		@Override
		public String call() throws Exception {
			String sql = "SELECT * FROM positionHistory WHERE positionHistoryId > ? AND positionHistoryId <  ? ORDER BY gpsTimestamp ASC";
			int duplicateCount = 0;
			if (params[0] instanceof String) {
				log.info("Getting replay positionids..");
				SqlRowSet rs =jdbcTemplate.queryForRowSet("SELECT MIN(positionHistoryId),MAX(positionHistoryId) FROM positionHistory where timeReceived > ? and timeReceived < ?", params);
				if(rs.next()) {
					params[0] = rs.getObject(1);
					params[1] = rs.getObject(2);
					log.info("Inserting start and finish");
					jdbcTemplate.update("insert into replays (replay_id, start, finish) values (?,?,?) on duplicate key update start=?, finish=?",
							replayId, params[0], params[1], params[0], params[1]);
				} else {
					log.error("No positions found between {}, {}", params[0], params[1]);
					raceService.replayEnded(replayId);
					return "no points";
				}
			}
			List<Position> replay = jdbcTemplate.query(sql, params, rowMapper);
			LocalDateTime lastTime = replay.get(0).getTimeCreated();
			for(int i = 0; i < replay.size()-2; i ++) {
				//wait for the clock to tick over
				Position p = replay.get(i);
				if(isDuplicate(p, replay.get(i + 1))) {
					i++;
					duplicateCount++;
					p = replay.get(i);
				}
				tickOver(lastTime, p.getTimeCreated());
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				clientManager.updatePostion(p);
				lastTime = p.getTimeCreated();
			}
			log.info("replay {} finished with {} duplicates", replayId, duplicateCount);
			raceService.replayEnded(replayId);
			return "";
		}
		
		private boolean isDuplicate(Position p1, Position p2) {
			return p1.getClientId().equals(p2.getClientId()) && p1.getGPSTimestamp().equals(p2.getGPSTimestamp());
		}

		protected void tickOver(LocalDateTime lastTime, LocalDateTime currentTime) {
			long milli = ChronoUnit.MILLIS.between( lastTime, currentTime);
			if (milli < 20) {
				//gap is too short for thread sleep, delay with a while loop
				long interval = ChronoUnit.NANOS.between( lastTime, currentTime);
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
			
			pause();
		}
		
		private void pause() {
			while(pause) {}
		}



		

	}



	@Override
	public boolean stopStartReplay() {
		if (currentReplay != null) {
			return currentReplay.stopStart();
		}
		return false;
	}
	
	

}

