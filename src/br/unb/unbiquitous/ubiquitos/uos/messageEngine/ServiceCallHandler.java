package br.unb.unbiquitous.ubiquitos.uos.messageEngine;

import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.DriverManagerException;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

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
	public ServiceResponse handleServiceCall(ServiceCall serviceCall, UOSMessageContext messageContext) throws DriverManagerException;
}
