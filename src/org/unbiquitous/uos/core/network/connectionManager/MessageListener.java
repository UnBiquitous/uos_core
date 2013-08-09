package org.unbiquitous.uos.core.network.connectionManager;

import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

/**
 * This interface defines the methods used by a class that handle incoming messages.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface MessageListener {

	/**
	 * Method called by the Connection Manager when a new message is received 
	 * 
	 * @param message Message received by the Connection Manager 
	 * @param clientDevice Device object representing the client device responsible for the message
	 * @return Message to be returned as a response, or null if not needed.
	 */
	public String handleIncomingMessage(String message, NetworkDevice clientDevice) throws NetworkException ;
	
}
