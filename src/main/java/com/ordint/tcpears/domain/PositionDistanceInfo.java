package com.ordint.tcpears.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class PositionDistanceInfo {

		private String clientId;
		private double distanceFromStart;
		private double distanceFromFinish;
		private double percentage;

}
