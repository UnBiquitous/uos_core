package org.unbiquitous.uos.core.messageEngine;

import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

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
	public ServiceResponse handleServiceCall(ServiceCall serviceCall, CallContext messageContext) throws DriverManagerException;
}
