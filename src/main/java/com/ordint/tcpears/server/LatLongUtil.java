package com.ordint.tcpears.server;

import static org.apache.commons.lang3.StringUtils.leftPad;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.lang3.StringUtils;

public class LatLongUtil {
    
	public static String supplementMsg(String msg, String group){
		 String name, timeStamp, lat, lon, heading, horizontalAccuracy, verticalAccuracy, alt, status;
        //0001,112630.10,51.23270,-0.19645,1.09800\r
      
        //need to fill in the other data
        //GR1,406C43,$PUBX,99,123423.19,51.4308205,N,0.11955,E,2000,D,0.700000,0.600000,0.041155555552,-1,-1,,0.041155555552,-1,-1,-1,
        String c=",";
        String[] parts= msg.split(",");
        name=parts[0];
        timeStamp=parts[1];
        lat=posToDec(parts[2]);
        lon=posToDec(parts[3]);
        char ei = 'E';
        char ni = 'N';
        if (Double.parseDouble(lon)<0){ei='W';} 
        if (Double.parseDouble(lat)<0){ni='S';}

        String  sog= formatDouble(Double.parseDouble(parts[4])*0.5144444444);
        if (parts.length==10){
            //its a long message
            //ident      ,time     ,lat     ,lon     ,sog    ,heading  ,laacc   ,lonacc,alt,    ,status
            //-1401086360,203137.20,50.92890,-1.38315,0.03000,305.68000,0.80000,0.60000,19.60000,D
            heading=parts[5];
            horizontalAccuracy=parts[6];
            verticalAccuracy=parts[7];
            alt=feetToMeters(parts[8]);
            status=parts[9];
        } else {
            horizontalAccuracy="-1";
            verticalAccuracy="-1";
            status="-1";
            alt="-1";
        }
        StringBuilder out = new StringBuilder();
        out.append(group).append(c).append(name).append(c).append("$PUBX,99,").append(timeStamp).append(c).append(lat).append(c)
        	.append(ni).append(c).append(lon).append(c).append(ei).append(c).append(alt).append(c).append(status).append(c)
        	.append(horizontalAccuracy).append(c).append(verticalAccuracy).append(c).append(sog).append(",-1,-1,,").append(sog)
        	.append(",-1,-1,-1");

        return out.toString();
    }
    protected static String feetToMeters(String value) {
    	double feet = Double.parseDouble(value);
    	return formatDouble(feet * 0.3048);
    }
	protected static String posToDec(String value) {
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
    private static String formatDouble(double d) {
    	return new BigDecimal(d).setScale(12, RoundingMode.HALF_DOWN).stripTrailingZeros().toString();
    }
    
    
}
