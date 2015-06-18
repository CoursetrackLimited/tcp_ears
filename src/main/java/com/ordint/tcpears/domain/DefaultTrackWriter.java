package com.ordint.tcpears.domain;

public class DefaultTrackWriter implements TrackWriter{
	private static final int MAX_TRACK_LENGTH = 1000;
	
	@Override
	public String write(Position2 position, String existingTrack) {
		return concatTrack(position, existingTrack, false);
	}
	
	
	private String concatTrack(Position2 position, String existingTrack, boolean alwaysConcat) {
		String track = shortenExistingTrackIfRequired(existingTrack);
		StringBuilder newPosition = new StringBuilder();
		newPosition.append(position.getLon()).append(",").append(position.getLat())
			.append(",").append(adjustAltitiude(position, track)).append(" ");
		//only append if its different from the last one
		if(!alwaysConcat) {
			String lastPosition = track.substring(0, track.indexOf(" ") + 1);
			if (newPosition.toString().equals(lastPosition)) {
				return track;
			}
		}
		return newPosition.append(track).toString();		
	}
	private String shortenExistingTrackIfRequired(String value) {
		StringBuilder strTracks = new StringBuilder(value);
		if(strTracks.length() > MAX_TRACK_LENGTH) {
			int lastTripleIndex = strTracks.lastIndexOf(" ", strTracks.length()-100);
			strTracks.delete(lastTripleIndex + 1, strTracks.length() );
		}
		return strTracks.toString();
	}
	private String adjustAltitiude(Position2 position, String existingTrack) {
		if (position.getAltitude().equals("-1")) {
            int firstTripleIndex=existingTrack.indexOf(" ");
            if (firstTripleIndex > 0) {
            	String firstTriple=existingTrack.substring(0, firstTripleIndex);
            	return firstTriple.substring(firstTriple.lastIndexOf(",") + 1, firstTriple.length());			
            }
		}
		return position.getAltitude();
	}
}
