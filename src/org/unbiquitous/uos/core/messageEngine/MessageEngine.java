package org.unbiquitous.uos.core.messageEngine;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.SecurityManager;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.EncapsulatedMessage;
import org.unbiquitous.uos.core.messageEngine.messages.Message;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.messageEngine.messages.json.JSONEncapsulatedMessage;
import org.unbiquitous.uos.core.messageEngine.messages.json.JSONNotify;
import org.unbiquitous.uos.core.messageEngine.messages.json.JSONServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.json.JSONServiceResponse;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.connectionManager.MessageListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

/**
 * This class is responsible for handling with messages received from other devices and defining which type are they,
 * then encapsulating it in the appropriate format.  
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class MessageEngine implements MessageListener {

	Logger logger = Logger.getLogger(MessageEngine.class);
	
	private ServiceCallHandler serviceCallHandler;
	private NotifyHandler notifyHandler;
	private SecurityManager securityManager;
	private DeviceManager deviceManager;	
	private ConnectionManagerControlCenter connectionManagerControlCenter;
	private MessageHandler messageHandler;
	
	public void init(
						ServiceCallHandler serviceCallHandler, 
						NotifyHandler notifyHandler,
						SecurityManager securityManager,
						ConnectionManagerControlCenter connectionManagerControlCenter,
						MessageHandler messageHandler) {
		this.serviceCallHandler = serviceCallHandler;
		this.notifyHandler = notifyHandler;
		this.securityManager = securityManager;
		this.connectionManagerControlCenter = connectionManagerControlCenter;
		this.messageHandler = messageHandler;
		
	}
	
	@Override
	public String handleIncomingMessage(String message,NetworkDevice clientDevice) throws NetworkException{
		
		if (message == null || clientDevice == null)
			return null;
		
		try {
			//TODO: This JSON Conversion can be done only once
			JSONObject json = new JSONObject(message);
			Message.Type messageType = retrieveMessageType(json);
			if (messageType != null){
				if (messageType == Message.Type.SERVICE_CALL_REQUEST){
					logger.info("Incoming Service Call");
					UOSMessageContext messageContext = new UOSMessageContext();
					messageContext.setCallerDevice(clientDevice);
					return handleServiceCall(message, messageContext);
				}else if (messageType == Message.Type.NOTIFY){
					logger.info("Incoming Notify");
					handleNotify(message,clientDevice);
					return null;
				}else if (messageType == Message.Type.ENCAPSULATED_MESSAGE){
					logger.info("Incoming Encapsulated Message");
					return handleEncapsulatedMessage(message,clientDevice);
				}
			}
		} catch (JSONException e) {
			logger.info("Failure to handle the incoming message",e);
			Notify event = new Notify();
			event.setError("Failure to handle the incoming message");
			try {return new JSONNotify(event).toString();} 
			catch (JSONException z) {logger.error("Never Happens");}
		}
		return null;
	}

	/**
	 * Redirect ServiceCalls to the adequate responsible entity.
	 */
	private String handleServiceCall(String message, UOSMessageContext messageContext) throws MessageEngineException{
		try {
			JSONServiceCall jsonServiceCall = new JSONServiceCall(message);
			ServiceCall serviceCall = jsonServiceCall.getAsObject();
			
			//TODO: Bassani - if the service call is a (Un)Register Service Call
			//					give it to the EventManager.
			//
			if(serviceCall.getService().equals("registerListener") || 
					serviceCall.getService().equals("unregisterListener")){
				
			}
			ServiceResponse response = serviceCallHandler.handleServiceCall(serviceCall, messageContext);
			logger.info("Returning service response");
			
			JSONServiceResponse jsonResponse = new JSONServiceResponse(response);
			return jsonResponse.toString();
		} catch (Exception e) {
			logger.error("Internal Failure", e);
			ServiceResponse errorResponse = new ServiceResponse();
			errorResponse.setError(e.getMessage() == null ?"Internal Error":e.getMessage());
			try {
				JSONServiceResponse jsonResponse = new JSONServiceResponse(errorResponse);
				return jsonResponse.toString();
			} catch (JSONException e1) {
				// Never Should Happens
				throw new MessageEngineException("Unexpected Error",e1);
			}
		}
	}
	
	private void handleNotify(String message,NetworkDevice clientDevice) throws MessageEngineException{
		try {
			Notify notify = new JSONNotify(message).getAsObject();
			
			notifyHandler.handleNofify(notify,
					deviceManager.retrieveDevice(
							connectionManagerControlCenter.getHost(
										clientDevice.getNetworkDeviceName()), 
										clientDevice.getNetworkDeviceType())
							);
			
		} catch (Exception e) {
			logger.error("Internal Failure. Notify cannot be handled.", e);
		} 
	}
	
	private String handleEncapsulatedMessage(String message,NetworkDevice clientDevice) throws MessageEngineException{
		try {
			JSONEncapsulatedMessage jsonEncapsulatedMessage = new JSONEncapsulatedMessage(message);
			EncapsulatedMessage encapsulatedMessage = jsonEncapsulatedMessage.getAsObject();
			
			String securityType = encapsulatedMessage.getSecurityType();
			
			TranslationHandler tHandler = securityManager.getTranslationHandler(securityType);
			
			logger.debug("clientDevice.getNetworkDeviceName: "+clientDevice.getNetworkDeviceName());
			
			//Translate Network Name into Device Name
			//TODO: MessageEngine : This is a violation of responsibility among the layers. Names should be already in the correct format
			UpDevice upClientDevice = deviceManager.retrieveDevice(connectionManagerControlCenter.getHost(clientDevice.getNetworkDeviceName()), clientDevice.getNetworkDeviceType());
			
			String deviceName = null;
			
			if (upClientDevice != null ){
				deviceName = upClientDevice.getName();
			}
			
			String innerRequest = tHandler.decode(encapsulatedMessage.getInnerMessage(),deviceName);
			
			String innerResponse = handleIncomingMessage(innerRequest,clientDevice);
			
			if (innerResponse != null){
				String encodedMessage= tHandler.encode(innerResponse,deviceName);
				
				EncapsulatedMessage encapsulatedResponse = new EncapsulatedMessage(); 
				
				encapsulatedResponse.setInnerMessage(encodedMessage);
				encapsulatedResponse.setSecurityType(securityType);
				 
				JSONEncapsulatedMessage jsonEncapsulatedResponse = new JSONEncapsulatedMessage(encapsulatedResponse);
				
				return jsonEncapsulatedResponse.toString();
			}
		} catch (Exception e) {
			logger.error("Problems handling EncapsulatedMessage: ",e);
		} 
		return null;
	}
	
	/**
	 * Retrieve the message type for the message informed
	 * 
	 * @param json JSONObject representing the message received.
	 * @return Message.Type Object of the type for the message informed.
	 */
	private Message.Type retrieveMessageType(JSONObject json) {
		try {
			return Message.Type.valueOf(json.optString("type"));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	/**
	 * Sends a notify message to the device informed.
	 * 
	 * @param notify Notify message to be sent.
	 * @param device Device which is going to receive the notofy event
	 * @throws MessageEngineException
	 */
	public void notifyEvent(Notify notify, UpDevice device) throws MessageEngineException{
		messageHandler.notifyEvent(notify, device);
	}
	/**
	 * Method responsible for calling a service according to the ServiceCall informed.
	 * 
	 * @param deviceName Device providing the service to be called.
	 * @param serviceCall Objetc representig the service call to be placed.
	 * @return Service Response for the called service.
	 * @throws MessageEngineException
	 */
	public ServiceResponse callService(UpDevice device,ServiceCall serviceCall) throws MessageEngineException{
		return messageHandler.callService(device, serviceCall);
	}

	/**
	 * @param deviceManager the deviceManager to set
	 */
	public void setDeviceManager(DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
	}
}
