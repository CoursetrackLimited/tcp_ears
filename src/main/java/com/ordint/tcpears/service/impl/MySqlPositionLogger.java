package com.ordint.tcpears.service.impl;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.service.PositionLogger;
@Component
public class MySqlPositionLogger implements PositionLogger {
	private final static Logger log = LoggerFactory.getLogger(MySqlPositionLogger.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private final static String INSERT = "INSERT INTO `positionHistory` (`altitude`,`clientId`,`groupId`, `heading`,`horizontalAccuracy`,`lat`,`lon`,`speed`,`status`,`gpsTimeStamp`,`timeReceived`,`verticalAccuracy`,`lag`,`vehicleType`,`source`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	//$sql='INSERT INTO `locationHistory` (`recievedDateTime`,`groupId`,`clientId`,`sourceType`,`sourceSubType`,`timeStamp`,`impLat`,`northIndicator`,`impLon`,`eastIndicator`,`altitude`,`status`,`accuracyX`,`accuracyY`,`SOG`,`COG`,`VD`,`GPSAge`,`HDOP`,`VDOP`,`SVsUsed`,`DRStatus`,`lat`,`lon`) VALUES (';

	private ConcurrentLinkedQueue<Object[]> batchOne = new ConcurrentLinkedQueue<>();
	private ConcurrentLinkedQueue<Object[]> batchTwo = new ConcurrentLinkedQueue<>();
	private AtomicInteger toggleInt = new AtomicInteger(1);
	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
	public MySqlPositionLogger() {
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					writeToDb();
				} catch (Exception e) {
					log.error("error writing postions to db", e);
				}
			}
		},
		1, 1, TimeUnit.SECONDS);		
	}

	public MySqlPositionLogger(JdbcTemplate jdbcTempalate) {

		this.jdbcTemplate = jdbcTempalate;
	}

	/* (non-Javadoc)
	 * @see com.ordint.tcpears.service.impl.PositionLogger#log(com.ordint.tcpears.domain.Position, java.lang.String, java.lang.String)
	 */
	@Override
	public void log(Position p,String vehicleType, String source) {
		if (firstBatch()) {
			batchOne.add(buildParams(p, vehicleType, source));
		} else {
			batchTwo.add(buildParams(p, vehicleType, source));
		}
		
	}
	
	private Object[] buildParams(Position p, String vehicleType, String source) {
		
		return new Object[] {
				p.getAltitude(),
				p.getClientDetails().getCurrentName(),
				p.getGroupId(),
				p.getHeading(),
				p.getHorizontalAccuracy(),
				p.getLat(),
				p.getLon(),
				p.getSpeed(),
				p.getStatus(),
				p.getTimestamp(),
				p.getTimeCreated().toString(),				
				p.getVerticalAccuracy(),
				p.getLag(),
				vehicleType,
				source
				
		};
	}
	
	protected void writeToDb() {
		if (toggle()) {		
			doInsert(batchOne);
		} else {
			doInsert(batchTwo);
		}
		
	}
	
	private boolean firstBatch() {
		return toggleInt.get() % 2 != 0;
	}
	
	private boolean toggle() {
		return toggleInt.incrementAndGet() % 2 == 0;
	}
	
	private void doInsert(ConcurrentLinkedQueue<Object[]> batch) {		
		if (!batch.isEmpty()) {
			jdbcTemplate.batchUpdate(INSERT, new ArrayList<>(batch));
			batch.clear();
		}
	}
	
}
