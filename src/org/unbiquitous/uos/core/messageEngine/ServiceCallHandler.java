package org.unbiquitous.uos.core.messageEngine;

import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/**
 * Interface Responsible for handling the service call messages
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface ServiceCallHandler {

	/**
	 * Method responsible for handling a service call in the appropriate instance driver
	 * 
	 * @param serviceCall ServiceCall message Received.
	 * @return serviceResponse returned from the service.
	 */
	public Response handleServiceCall(Call serviceCall, CallContext messageContext) throws DriverManagerException;
}
