package com.ordint.tcpears.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * The output of this OutputWritre matches what is currently
 * expected by the map.php page
 * @author Tom
 *
 */
public class DefaultOutputWriter implements OutputWriter {
	/**
	 * 		  ,989780014 ,       ,2015-09-22T17:44:26.600,-1       ,       ,       ,       ,13.20000   ,-1      ,-1                  ,0         ,       ,       ,       ,       ,20.682 ,       ,       ,0    ,51.421332,-0.404618166667
	 * {spare},{clientId},{spare},{gpsTimestamp}         ,{heading},{spare},{spare},{spare},{altitiude},{status},{horizontalAccuracy},{standing},{spare},{spare},{spare},{spare},{speed},{spare},{spare},{lag},{lat}    ,{lon}
	 * 
	 */
	@Override
	public String write(Position p) {
		String[] cells = new String[22];
		cells[1] = p.getClientId();
		cells[3] = p.getGPSTimestamp();
		cells[4] = p.getHeading();
		cells[5] = p.getClientDetails().getRunnerIdent();
		cells[8] = p.getAltitude();
		cells[9] = p.getStatus();
		cells[10] = p.getHorizontalAccuracy();
		cells[11] = String.valueOf(p.getStanding());
		cells[12] = String.valueOf(getDistanceFromFinish(p.getDistanceInfo()));
		cells[16] = p.getSpeed();
		cells[19] = String.valueOf(p.getLag());
		cells[20] = p.getLat();
		cells[21] = p.getLon();
		
		
		
		return StringUtils.join(cells, ",");

	}
	
	private double getDistanceFromFinish(PositionDistanceInfo distanceInfo) {
		if (distanceInfo == null) {
			return -1;
		} else {
			return distanceInfo.getDistanceFromFinish();
		}
	}

}
