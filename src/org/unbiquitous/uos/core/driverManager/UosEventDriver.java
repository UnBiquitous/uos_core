package org.unbiquitous.uos.core.driverManager;

import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

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
	public void registerListener(Call serviceCall, Response serviceResponse, CallContext messageContext);
	
	/**
	 * Service responsible for removing the caller device as a listener for the event under the key informed 
	 * in the 'eventKey' parameter for the implementing driver. If no key is informed the current device will be
	 * removed as listener from all event queues. 
	 */
	public void unregisterListener(Call serviceCall, Response serviceResponse, CallContext messageContext);
}
