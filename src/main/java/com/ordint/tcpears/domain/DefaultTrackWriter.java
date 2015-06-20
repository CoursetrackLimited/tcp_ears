package com.ordint.tcpears.domain;

public class DefaultTrackWriter implements TrackWriter{
	private final static int DEFAULT_CLIENT_COUNT = 20;
	private final static int MAX_CLIENT_COUNT = 6500;
	public final static int DEFAULT_MAX_TRACK = 157025;
	private final static int MAX_TUPLE_SIZE = 54;
	private volatile int maxLength = DEFAULT_MAX_TRACK;
	 
	
	@Override
	public String write(Position position, String existingTrack) {
		return shortenIfRequired(concatTrack(position, existingTrack, false)); 
	}
	
	
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	private String shortenIfRequired(String value) {
		StringBuilder strTracks = new StringBuilder(value);
		if(strTracks.length() > maxLength) {
			int lastTripleIndex = strTracks.lastIndexOf(" ", maxLength -(MAX_TUPLE_SIZE * 3));
			strTracks.delete(lastTripleIndex + 1, strTracks.length() );
		}
		return strTracks.toString();
	}
	
	private String concatTrack(Position position, String existingTrack, boolean alwaysConcat) {
		StringBuilder newPosition = new StringBuilder();
		newPosition.append(position.getLon()).append(",").append(position.getLat())
			.append(",").append(adjustAltitiude(position, existingTrack)).append(" ");
		//only append if its different from the last one
		if(!alwaysConcat) {
			String lastPosition = existingTrack.substring(0, existingTrack.indexOf(" ") + 1);
			if (newPosition.toString().equals(lastPosition)) {
				return existingTrack;
			}
		}
		return newPosition.append(existingTrack).toString();		
	}

	private String adjustAltitiude(Position position, String existingTrack) {
		if (position.getAltitude().equals("-1")) {
            int firstTripleIndex=existingTrack.indexOf(" ");
            if (firstTripleIndex > 0) {
            	String firstTriple=existingTrack.substring(0, firstTripleIndex);
            	return firstTriple.substring(firstTriple.lastIndexOf(",") + 1, firstTriple.length());			
            }
		}
		return position.getAltitude();
	}


	@Override
	public void calculateTrackLength(int clientCount) {
		if (clientCount > DEFAULT_CLIENT_COUNT) {
			if (clientCount > MAX_CLIENT_COUNT) {
				throw new IllegalArgumentException("Too many clients for tracking " + clientCount);
			}
			maxLength = calculateMaxTrackLength(clientCount);
		}
		
	}
	
	
	private int calculateMaxTrackLength(int clientCount) {
		return (int)Math.ceil(((3 *1024 * 1024) - (261 * clientCount)) / clientCount);
	}
	
}
