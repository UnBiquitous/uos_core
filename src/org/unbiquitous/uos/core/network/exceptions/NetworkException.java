package org.unbiquitous.uos.core.network.exceptions;

public class NetworkException extends RuntimeException{

	private static final long serialVersionUID = 3595546954657100674L;

	public NetworkException() {
		super();
	}

	public NetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	public NetworkException(String message) {
		super(message);
	}

	public NetworkException(Throwable cause) {
		super(cause);
	}

	
	
}
