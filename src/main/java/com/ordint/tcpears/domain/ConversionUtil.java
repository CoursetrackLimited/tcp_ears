package com.ordint.tcpears.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;

public final class ConversionUtil {

	private ConversionUtil() {}
	
    public static String feetToMeters(String value) {
    	double feet = Double.parseDouble(value);
    	return formatDouble(feet * 0.3048);
    }
	
    public static String posToDec(String value) {
		//convert ddmm.mmmmm to decimal
		//convert dddmm.mmmmm to decimal
		int dp = value.indexOf(".");
		
		double mins = Math.abs(Double.parseDouble(value.substring(dp-2)));
		String degStr = value.substring(0, dp-2);
		
		int degs = StringUtils.isNumeric(degStr) ? Integer.parseInt(degStr) : 0;	
		if (value.charAt(0) == '-')
			return formatDouble(degs -(mins)/60);
		else
			return formatDouble(degs +(mins)/60);
			
			
	} 
    public static String formatDouble(double d) {
    	return new BigDecimal(d).setScale(12, RoundingMode.HALF_DOWN).stripTrailingZeros().toString();
    }
}
