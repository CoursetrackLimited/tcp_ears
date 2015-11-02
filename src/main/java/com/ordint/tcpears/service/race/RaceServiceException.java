package com.ordint.tcpears.service.race;

public class RaceServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2751687618021627553L;

	public RaceServiceException() {
	}

	public RaceServiceException(String message) {
		super(message);
	}

	public RaceServiceException(Throwable cause) {
		super(cause);
	}

	public RaceServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public RaceServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
