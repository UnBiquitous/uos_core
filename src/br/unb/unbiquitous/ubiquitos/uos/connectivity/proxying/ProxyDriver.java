package br.unb.unbiquitous.ubiquitos.uos.connectivity.proxying;

import br.unb.unbiquitous.ubiquitos.uos.application.UOSMessageContext;
import br.unb.unbiquitous.ubiquitos.uos.driverManager.UosDriver;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;

/**
 * Interface of a Proxy Driver. It masquerades some other proxy on some other device.
 * None of the methods listed here are services. They are used by the middleware for 
 * handling and forwarding the service call to the real provider.
 * 
 * @author Lucas Paranhos Quintella
 *
 */
public interface ProxyDriver extends UosDriver {
	
	/**
	 * Method responsible for forwarding the service call to the real provider. Any service
	 * call made to us is redirected to the real provider by using this method. 
	 * 
	 * @param serviceCall The service call
	 * @param serviceResponse The service response
	 * @param messageContext Our message context of streams respective to the caller device
	 */
	public void forwardServiceCall(ServiceCall serviceCall, ServiceResponse serviceResponse, 
			UOSMessageContext messageContext);
	
	/**
	 * Gets the real provider of the driver we represent.
	 * @return The real provider of the driver
	 */
	public UpDevice getProvider();
	
	
}
