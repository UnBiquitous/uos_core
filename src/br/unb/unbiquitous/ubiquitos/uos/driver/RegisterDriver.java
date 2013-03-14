package br.unb.unbiquitous.ubiquitos.uos.driver;

import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

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
