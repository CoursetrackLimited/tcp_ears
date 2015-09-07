package com.ordint.tcpears.domain;

import org.apache.commons.lang3.StringUtils;

/**
 * The output of this OutputWritre matches what is currently
 * expected by the map.php page
 * @author Tom
 *
 */
public class DefaultOutputWriter implements OutputWriter {

	@Override
	public String write(Position p) {
		String[] cells = new String[22];
		int i = cells.length;
		cells[1] = p.getClientId();
		cells[i - 19] = p.getGPSTimestamp();
		cells[i - 13] = p.getStatus();
		cells[i - 12] = p.getHorizontalAccuracy();
		cells[i - 6] = p.getSpeed();
		cells[8] = p.getAltitude();
		cells[i - 1] = p.getLon();
		cells[i - 2] = p.getLat();
		cells[i - 3] = String.valueOf(p.getLag());
		return StringUtils.join(cells, ",");

	}

}
