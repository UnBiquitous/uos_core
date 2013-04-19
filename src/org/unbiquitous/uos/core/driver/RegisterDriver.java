package org.unbiquitous.uos.core.driver;

import org.unbiquitous.uos.core.application.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * 
 * Driver responsible for the register services.
 * This driver provides most of the features responsible for implementing the service adaptability and spontaneous adaptability.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface RegisterDriver extends UosDriver {

	/**
	 * Service responsible for retrieving the list of Driver Instances present in the Register Database.
	 * This listing service can have its result filtered with the use of the parameters 'serviceName', 'driverName' or 'deviceName' .
	 * It responds in a single responseMap within the parameter 'driverList'
	 */
	public void listDrivers(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);
	
}
