package org.unbiquitous.uos.core.connectivity;

import org.unbiquitous.uos.core.network.exceptions.NetworkException;

/**
 * Class that represents a connectivity exception. Since connectivity
 * is a network problem, it extends the uos network exception.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public class ConnectivityException extends NetworkException{
	
	private static final long serialVersionUID = -431641697806422005L;

	public ConnectivityException(String message) {
		super(message);
	}
	
}
