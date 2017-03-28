package com.ordint.tcpears.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PositionDistanceInfo {

		private String clientId;
		private double distanceFromStart;
		private double distanceFromFinish;
		private double percentage;
		
		

}
