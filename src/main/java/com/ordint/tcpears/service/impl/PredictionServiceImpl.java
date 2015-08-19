package com.ordint.tcpears.service.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ordint.tcpears.domain.Position;
import com.ordint.tcpears.util.prediction.PositionPredictor;
import com.ordint.tcpears.util.prediction.StaticTrackPathBuilder;
import com.ordint.tcpears.util.prediction.TrackBasedPredictor;


@Component
public class PredictionServiceImpl {
	private final static Logger log = LoggerFactory.getLogger(PredictionServiceImpl.class);
	private PositionPredictor predictor = new TrackBasedPredictor (new StaticTrackPathBuilder().build("KEMPTON_740"));
	private int maxPredictionTime = 10000;
	private int numberOfPoints = 5;

	public  ConcurrentMap<String, String> predictPositions(String groupId, List<Position> positions) {
		ConcurrentMap<String, String> predictions = new ConcurrentHashMap<>();
		for(Position p : positions) {
			int currentLag = (int) p.getCurrentLag();
			if ( p.getCurrentLag() > maxPredictionTime) {
				currentLag = maxPredictionTime;
			}
			if (p.getSpeedValue() > 2) {
				try {
					predictions.put(p.getClientId(), getPredictor(groupId).predict(p, currentLag, numberOfPoints, null));
				} catch (Exception e) {
					log.warn("Unable to predict for point {}",p);
					log.warn("PostitionPredictor threw",e);
				}
			}
		}
		return predictions;
	}
	

	private PositionPredictor getPredictor(String groupId) {
		return predictor;
	}

}
