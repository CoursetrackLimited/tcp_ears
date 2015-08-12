package com.ordint.tcpears.domain;



import org.apache.commons.lang3.StringUtils;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

public class SimplePredictingTrackWriter extends DefaultTrackWriter {

	@Override
	protected String concatTrack(Position position, String existingTrack, boolean alwaysConcat) {
		//if we have 0 existing points we call super
		if(StringUtils.isEmpty(existingTrack)) {
			return super.concatTrack(position, existingTrack, alwaysConcat);
		}
		//if we have > 2 points we delete the most recent point (as it was a prediction)
		String mostRecentPosition = getMostRecentPosition(existingTrack);
		
		if (existingTrack.length() - mostRecentPosition.length() > 2) {
			existingTrack = existingTrack.substring(mostRecentPosition.length(), existingTrack.length());
		    mostRecentPosition = getMostRecentPosition(existingTrack);
		}
		
		//add new point and calculate predicted point and add that
		StringBuilder newPosition = buildTrackPosition(position, existingTrack);
		
		if(!alwaysConcat) {
			if (newPosition.toString().equals(mostRecentPosition)) {
				return existingTrack;
			}
		}
		
		newPosition.append(existingTrack);
		
		Position predicted = buildPredictedPosition(position, mostRecentPosition, newPosition.toString());
		
		StringBuilder predictedPostion = buildTrackPosition(predicted, newPosition.toString());
		
		return predictedPostion.append(newPosition).toString();
	}
	
	protected Position buildPredictedPosition(Position position, String mostRecentPostiton, String existingTrack) {
		String cells[] = StringUtils.split(mostRecentPostiton, ",");
		LatLng lastPosition = new LatLng(Double.parseDouble(cells[1]), Double.parseDouble(cells[0]));
		LatLng predicted = predict(position, lastPosition);
		return Position.builder()
				.altitude(adjustAltitiude(position, existingTrack))
				.lat(String.valueOf(predicted.getLatitude()))
				.lon(String.valueOf(predicted.getLongitude()))
				.timestamp(position.getTimestamp())
				.build();
	}
	
	protected LatLng predict(Position p, LatLng lastPostion) {
		LatLng currentPostion = new LatLng(Double.parseDouble(p.getLat()), Double.parseDouble(p.getLon()));
		double bearing = LatLngTool.initialBearing(lastPostion, currentPostion);
		double distance = Double.parseDouble(p.getSpeed()) * p.getLag() / 1000;
		
		return LatLngTool.travel(currentPostion, bearing, distance, LengthUnit.METER);
	}
	
	protected String getMostRecentPosition(String existingTrack) {
        int firstTripleIndex=existingTrack.indexOf(" ");
        if (firstTripleIndex > 0) {
        	return existingTrack.substring(0, firstTripleIndex + 1);			
        } else {
        	return null;
        }
	}
}
