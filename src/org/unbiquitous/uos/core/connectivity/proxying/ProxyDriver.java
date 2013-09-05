package org.unbiquitous.uos.core.connectivity.proxying;

import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

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
	public void forwardServiceCall(Call serviceCall, Response serviceResponse, 
			CallContext messageContext);
	
	/**
	 * Gets the real provider of the driver we represent.
	 * @return The real provider of the driver
	 */
	public UpDevice getProvider();
	
	
}
