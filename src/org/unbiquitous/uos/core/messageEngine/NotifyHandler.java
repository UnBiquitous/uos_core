package org.unbiquitous.uos.core.messageEngine;

import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
/**
 * Interface Responsible for handling the service call messages
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface NotifyHandler {
	
	/**
	 * Method responsible for handling a notify message to the appropriate listener.
	 * 
	 * @param notify ServiceCall message Received.
	 * @param device UpDevice which originated the event.
	 */
	public void handleNofify(Notify notify, UpDevice device) throws DriverManagerException;
}
