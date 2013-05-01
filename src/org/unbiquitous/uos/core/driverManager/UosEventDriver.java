package org.unbiquitous.uos.core.driverManager;

import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * Interface representing the UosDrivers that can report events in the smart-space 
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface UosEventDriver extends UosDriver {

	/**
	 * Service responsible for registering the caller device as a listener for the event under the key informed 
	 * in the 'eventKey' parameter for the implementing driver.
	 */
	public void registerListener(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);
	
	/**
	 * Service responsible for removing the caller device as a listener for the event under the key informed 
	 * in the 'eventKey' parameter for the implementing driver. If no key is informed the current device will be
	 * removed as listener from all event queues. 
	 */
	public void unregisterListener(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);
}
