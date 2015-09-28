package com.ordint.tcpears.util;

import static java.lang.Double.parseDouble;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;


import com.javadocmd.simplelatlng.LatLng;

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
		int degs = extractDegrees(value, dp);

		double mins = Math.abs(Double.parseDouble(value.substring(upTo0(dp))));

		if (value.charAt(0) == '-')
			return formatDouble(degs -(mins)/60);
		else
			return formatDouble(degs +(mins)/60);
			
			
	}
    private static int upTo0(int dp) {
		return dp -2 < 0 ? 0 : dp -2;

    }
    private static  int extractDegrees(String value, int dp) {
    	if (dp < 3) return 0;
    	String str = value.substring(0, dp-2);
    	if (NumberUtils.isParsable(str))
    		return Integer.parseInt(str);
    	else
    		return 0;
    }
    
    public static String formatDouble(double d) {
    	return new BigDecimal(d).setScale(12, RoundingMode.HALF_DOWN).stripTrailingZeros().toString();
    }
    public static String formatDouble(String d) {
    	return StringUtils.stripEnd(d, "0");
    }
    
    public static LatLng toLatLng(String lat, String lng){
    	
    	return new LatLng(parseDouble(lat), parseDouble(lng));
    	
    }
}
