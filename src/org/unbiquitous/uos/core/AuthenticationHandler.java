package org.unbiquitous.uos.core;

import org.unbiquitous.uos.core.application.UOSMessageContext;
import org.unbiquitous.uos.core.messageEngine.MessageHandler;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * 
 * Interface responsible for abstracting the operation of authentication.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public interface AuthenticationHandler {

	/**
	 * This method is responsible for handling the process of authentication in the validator's (B) side of an authentication.
	 * This method can be called multiple times for a authentication process with multiple steps 
	 * 
	 * @param serviceCall <code>ServiceCall</code> for the current step of authentication.
	 * @param serviceResponse <code>ServiceResponse</code> with the response of the execution of the current step.
	 * @param messageContext <code>UOSMessageContext</code>with the information about the sender (like its ID).
	 */
	public void authenticate(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext);

	/**
	 * This method is responsible for handling the process of authentication in the caller's (A) side of an authentication.
	 * 
	 * @param deviceName <code>String</code> with the name whom it's going to be authenticated.
	 * @param messageHandler <code>ServiceHandler</code> responsible for handling the service calls to the authenticate service.
	 * @param currentDevice <code>UpDevice</code> data of current device
	 */
	public void authenticate(UpDevice upDevice, MessageHandler messageHandler);
	
	/**
	 * @return Returns the <code>String</code> id of the security type of the authentication.
	 */
	public String getSecurityType();
}
