package com.ordint.tcpears.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ordint.tcpears.service.position.ClientManagerImpl;

public class DefaultSnakeWriter implements SnakeWriter{
	
	private final static Logger log = LoggerFactory.getLogger(DefaultSnakeWriter.class);
	
	private final static int DEFAULT_CLIENT_COUNT = 20;
	private final static int MAX_CLIENT_COUNT = 6500;
	public final static int DEFAULT_MAX_TRACK = 157025;
	private final static int MAX_TUPLE_SIZE = 54;
	private volatile int maxLength = DEFAULT_MAX_TRACK;
	 
	
	@Override
	public String write(Position position, String existingSnake) {
		return shortenIfRequired(concatSnake(position, existingSnake, false)); 
	}
	
	
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	protected String shortenIfRequired(String value) {
		StringBuilder strSnakes = new StringBuilder(value);
		if(strSnakes.length() > maxLength) {
			int lastTripleIndex = strSnakes.lastIndexOf(" ", maxLength -(MAX_TUPLE_SIZE * 3));
			strSnakes.delete(lastTripleIndex + 1, strSnakes.length() );
		}
		return strSnakes.toString();
	}
	
	protected String concatSnake(Position position, String existingSnake, boolean alwaysConcat) {
		StringBuilder newPosition = buildSnake(position, existingSnake);
		//only append if its different from the last one
		if(!alwaysConcat) {
			String lastPosition = existingSnake.substring(0, existingSnake.indexOf(" ") + 1);
			if (newPosition.toString().equals(lastPosition)) {
				return existingSnake;
			}
		}
		return newPosition.append(existingSnake).toString();		
	}
	
	protected StringBuilder buildSnake(Position position, String existingSnake) {
		StringBuilder newPosition = new StringBuilder();
		newPosition.append(position.getLon()).append(",").append(position.getLat())
			.append(",").append(adjustAltitiude(position, existingSnake)).append(" ");
		return newPosition;
	} 
	protected String adjustAltitiude(Position position, String existingSnake) {
		if (position.getAltitude().equals("-1")) {
            int firstTripleIndex=existingSnake.indexOf(" ");
            if (firstTripleIndex > 0) {
            	String firstTriple=existingSnake.substring(0, firstTripleIndex);
            	return firstTriple.substring(firstTriple.lastIndexOf(",") + 1, firstTriple.length());			
            }
		}
		return position.getAltitude();
	}

	@Override
	public void calculateSnakeLength(int clientCount) {
		if (clientCount > DEFAULT_CLIENT_COUNT) {
			if (clientCount > MAX_CLIENT_COUNT) {
				throw new IllegalArgumentException("Too many clients for tracking " + clientCount);
			}
			maxLength = calculateMaxSnakeLength(clientCount);
		}
		
	}
	
	private int calculateMaxSnakeLength(int clientCount) {
		return (int)Math.ceil(((3 *1024 * 1024) - (261 * clientCount)) / clientCount);
	}
	
}
