package com.ordint.tcpears.util.prediction;

import com.ordint.tcpears.domain.PositionDistanceInfo;
import com.ordint.tcpears.util.PredictionUtil;

import lombok.Getter;
import lombok.Value;


@Getter
public class RacePosition extends PositionDistanceInfo{
	private int standing;
	public RacePosition(int standing, PositionDistanceInfo pdi) {
		super(pdi.getClientId(), pdi.getDistanceFromStart(), pdi.getDistanceFromFinish(), pdi.getPercentage());
		this.standing = standing;
	}
}
