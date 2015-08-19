package com.ordint.tcpears.service.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.service.PositionPublisher;

@Component
public class PublishingScheduler {
	private final static Logger log = LoggerFactory.getLogger(PublishingScheduler.class);

	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
	@Autowired
	private PositionPublisher positionPublisher;
	
	@PostConstruct
	protected void init() {
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
		1000, 100, TimeUnit.MILLISECONDS);

		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					positionPublisher.publishTracks();
				} catch (Exception e) {
					log.error("error publishing tracks", e);
				}
			}
		},
		1500, 333, TimeUnit.MILLISECONDS);
	}
	
}
