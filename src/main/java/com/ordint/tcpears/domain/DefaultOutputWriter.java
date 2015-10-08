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
		int i = cells.length;
		cells[1] = p.getClientId();
		cells[i - 19] = p.getGPSTimestamp();
		cells[i - 13] = p.getStatus();
		cells[i - 11] = String.valueOf(p.getStanding());
		cells[i - 12] = p.getHorizontalAccuracy();
		cells[i - 6] = p.getSpeed();
		cells[8] = p.getAltitude();
		cells[i - 1] = p.getLon();
		cells[i - 2] = p.getLat();
		cells[i - 3] = String.valueOf(p.getLag());
		cells[4] = p.getHeading();
		return StringUtils.join(cells, ",");

	}

}
