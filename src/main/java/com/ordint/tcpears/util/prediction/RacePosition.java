package com.ordint.tcpears.util.prediction;

import com.ordint.tcpears.util.PredictionUtil;

import lombok.Value;


@Value
public class RacePosition {

	private int standing;
	private double metersToFinish;
	private double distanceFromStart;
	private String runnerId;

	public double getMetersFromStart() {
		return distanceFromStart / PredictionUtil.ONE_METER_FACTOR;
	}
}
