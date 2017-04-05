package com.ordint.tcpears.service.position;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.ordint.tcpears.service.PositionPublisher;

public class PublishingScheduler {
	private final static Logger log = LoggerFactory.getLogger(PublishingScheduler.class);

	private ScheduledExecutorService executor;
	private ScheduledExecutorService executor2;
	@Autowired
	private PositionPublisher positionPublisher;
	
	private boolean useSnakes;
	
	public  PublishingScheduler(boolean useSnakes) {
		this.useSnakes = useSnakes;
	}
	
	@PostConstruct
	protected void init() {
		shedulePositionPublishing();
		if (useSnakes) {
			scheduleSnakePublishing();
		}
	}



	private void shedulePositionPublishing() {
		executor = Executors.newScheduledThreadPool(1);
		log.info("Scheduling position publishing");
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					positionPublisher.publishPositions();
				} catch (Exception e) {
					log.error("error publishing postions", e);
				}
			}
		},
		1000, 47, TimeUnit.MILLISECONDS);
	}
	
	private void scheduleSnakePublishing() {
		executor2 = Executors.newScheduledThreadPool(1);
		log.info("Scheduling snake publishing");
		executor2.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					positionPublisher.publishSnakes();
				} catch (Exception e) {
					log.error("error publishing tracks", e);
				}
			}
		},
		1500, 333, TimeUnit.MILLISECONDS);
	}
}
