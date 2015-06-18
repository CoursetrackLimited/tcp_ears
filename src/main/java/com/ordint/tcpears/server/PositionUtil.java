package com.ordint.tcpears.server;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.sportradar.memcached.Memcached;
import com.ordint.tcpears.memcache.MemcacheHelper;

public class PositionUtil {
	//private final static Logger log = LoggerFactory.getLogger(PositionUtil.class);
	private MemcacheHelper memcacheHelper;
	public PositionUtil(MemcacheHelper memcacheHelper) {
		this.memcacheHelper = memcacheHelper;
	}
	public void updatePosition(String message) throws JsonParseException, JsonMappingException, IOException {
		Instant start = Instant.now();
		String keys = "groupId,clientId,type,PUBXType,UTC,impLat,Northing Indicator,impLon,Easting Indicator,Alt,Status, " +
	            "HorizontalAccuracy,VerticalAccuracy,SOG,COG,VD,GPSAge," +
	            "HDOP,VDOP,SVsUsed,DR Status";
		String[] vals = message.split(",");
		HashMap<String,String> params = new HashMap<>();
		int i =0;
		for(String key : keys.split(",")) {
			params.put(key, vals[i++]);
		}
		
		String groupId = params.get("groupId");
		String clientId = params.get("clientId");
		String speed = params.get("SOG");
		String decLat = formatTo(params.get("impLat"),10);
		String decLong = formatTo(params.get("impLon"),10);
		StringBuilder out = new StringBuilder();
		//add the lat long formatted to 10 dp
		out.append(message).append(",").append(decLat).append(",").append(decLong);
		String latestPosition = out.toString();
		String groupKeyName = "/ggps/locations/" + groupId;
		String clientKeyName="/ggps/location/" + clientId;
		
		
		Map<String,String> positionsMap =  memcacheHelper.getMap(groupKeyName, groupKeyName);

		positionsMap.put(clientId, latestPosition);
		memcacheHelper.set(clientKeyName, clientKeyName, latestPosition);
		memcacheHelper.set(groupKeyName, groupKeyName, positionsMap);
		
		
		//updateTracks(clientId, speed, decLat, decLong, groupId);
		Instant end = Instant.now();
		//log.debug(Duration.between(start, end).toString());
	}
	private void updateTracks(String clientId, String speed, String decLat, String decLong, String groupId)
			throws IOException {
		String groupTracksName="/ggps/tracks/" + groupId;
		Map<String,String> tracksMap =  memcacheHelper.getMap(groupTracksName, groupTracksName);
		StringBuilder strTracks = new StringBuilder();
		
		if(tracksMap.containsKey(clientId)) {
			strTracks.append(tracksMap.get(clientId));
			if(strTracks.length() > 36096) {
				String str = strTracks.toString();
				int lastTripleIndex = str.substring(0, str.length()-100).lastIndexOf(" ");
				strTracks.delete(lastTripleIndex + 1, strTracks.length() );
			}
		}
		
		if (speed.equals("-1")) {
            int firstTripleIndex=strTracks.indexOf(" ");
            String firstTriple=strTracks.substring(0, firstTripleIndex);
           
            speed = firstTriple.substring(firstTriple.lastIndexOf(",") + 1, firstTriple.length());			
			
		}
		StringBuilder tmp = new StringBuilder();
		tmp.append(decLat).append(",").append(decLong).append(",").append(speed).append(" ").append(strTracks);
		
		tracksMap.put(clientId, tmp.toString());
		

		memcacheHelper.set(groupTracksName, groupTracksName, tracksMap);
	}
	

	private static String formatTo(String number, int dp) {
		return new BigDecimal(number).setScale(dp, RoundingMode.HALF_DOWN).toString();
	}
	
}
