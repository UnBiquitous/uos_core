package br.unb.unbiquitous.ubiquitos.uos.messageEngine;

import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManagerException;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;
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
