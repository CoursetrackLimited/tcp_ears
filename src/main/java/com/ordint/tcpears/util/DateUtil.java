package com.ordint.tcpears.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class DateUtil {
	
	private DateUtil(){}
	
	public final static LocalDateTime ukLocalDateTimeToUTC(LocalDateTime datetime) {
		return ZonedDateTime.of(datetime, ZoneId.of("Europe/London"))
		.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
	}

}
