package br.unb.unbiquitous.ubiquitos.uos.driverManager;

import java.util.List;

import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.Gateway;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDriver;

/**
 * Interface representing the UbiquitOSDrivers.
 * 
 * All service methods must be in the following way:
 * 
 * 		public void serviceName(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);
 * 
 * 		serviceCall : represent the service call request which originated the current call.
 * 		serviceResponse : represent the service call response which will be returned after the current call.
 * 		messageContext : represent the context of this conversation. 
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface UosDriver {

	/**
	 * Method responsible for returning the Driver Interface which the implementing class refers to.
	 * 
	 * @return Driver containing the Interface correspondent to the implementation.
	 */
	public UpDriver getDriver();
	
	/**
	 * Method responsible for returning the Interface of each parent driver.
	 * 
	 * @return List containing the Interface of each parent driver.
	 */
	public List<UpDriver> getParent();
	
	/**
	 * Method responsible for executing initialization tasks for the Driver
	 */
	public void init(Gateway gateway, String instanceId);
	
	/**
	 * Method responsible for executing clean-up tasks for the Driver
	 */
	public void destroy();
	
}
