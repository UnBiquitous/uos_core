package org.unbiquitous.uos.core.messageEngine;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.SecurityManager;
import org.unbiquitous.uos.core.UOSComponent;
import org.unbiquitous.uos.core.UOSComponentFactory;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Capsule;
import org.unbiquitous.uos.core.messageEngine.messages.Message;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
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
public class MessageEngine implements MessageListener , UOSComponent{

	Logger logger = UOSLogging.getLogger();
	
	private ServiceCallHandler serviceCallHandler;
	private NotifyHandler notifyHandler;
	private SecurityManager securityManager;
	private DeviceManager deviceManager;	
	private ConnectionManagerControlCenter connectionManagerControlCenter;
	private MessageHandler messageHandler;

	private InitialProperties properties;
	
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
					CallContext messageContext = new CallContext();
					messageContext.setCallerNetworkDevice(clientDevice);
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
			logger.log(Level.INFO,"Failure to handle the incoming message",e);
			Notify event = new Notify();
			event.setError("Failure to handle the incoming message");
			try {return event.toJSON().toString();} 
			catch (JSONException z) {logger.severe("Never Happens");}
		}
		return null;
	}

	/**
	 * Redirect ServiceCalls to the adequate responsible entity.
	 */
	private String handleServiceCall(String message, CallContext messageContext) throws MessageEngineException{
		try {
			Call serviceCall = Call.fromJSON(new JSONObject(message)); 
			Response response = serviceCallHandler.handleServiceCall(serviceCall, messageContext);
			logger.info("Returning service response");
			
			return response.toJSON().toString();
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Internal Failure", e);
			Response errorResponse = new Response();
			errorResponse.setError(e.getMessage() == null ?"Internal Error":e.getMessage());
			try {
				return errorResponse.toJSON().toString();
			} catch (JSONException e1) {
				// Never Should Happens
				throw new MessageEngineException("Unexpected Error",e1);
			}
		}
	}
	
	private void handleNotify(String message,NetworkDevice clientDevice) throws MessageEngineException{
		try {
			Notify notify = Notify.fromJSON(new JSONObject(message));
			
			notifyHandler.handleNofify(notify,
					deviceManager.retrieveDevice(
							connectionManagerControlCenter.getHost(
										clientDevice.getNetworkDeviceName()), 
										clientDevice.getNetworkDeviceType())
							);
			
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Internal Failure. Notify cannot be handled.", e);
		} 
	}
	
	private String handleEncapsulatedMessage(String message,NetworkDevice clientDevice) throws MessageEngineException{
		try {
			Capsule encapsulatedMessage = Capsule.fromJSON(new JSONObject(message)); 
			
			String securityType = encapsulatedMessage.getSecurityType();
			
			TranslationHandler tHandler = securityManager.getTranslationHandler(securityType);
			
			logger.fine("clientDevice.getNetworkDeviceName: "+clientDevice.getNetworkDeviceName());
			
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
				
				Capsule encapsulatedResponse = new Capsule(); 
				
				encapsulatedResponse.setInnerMessage(encodedMessage);
				encapsulatedResponse.setSecurityType(securityType);
				 
				return encapsulatedResponse.toJSON().toString();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Problems handling EncapsulatedMessage: ",e);
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
	public void notify(Notify notify, UpDevice device) throws MessageEngineException{
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
	public Response callService(UpDevice device,Call serviceCall) throws MessageEngineException{
		return messageHandler.callService(device, serviceCall);
	}

	/************************ USO COmpoment ***************************/
	
	@Override
	public void create(InitialProperties properties) {
		this.properties = properties;
	}
	
	@Override
	public void init(UOSComponentFactory factory) {
		this.serviceCallHandler = factory.get(AdaptabilityEngine.class);// FIXME: AdaptabilityEngine should register
		this.notifyHandler = factory.get(AdaptabilityEngine.class);// FIXME: AdaptabilityEngine should register
//		this.deviceManager = factory.get(DeviceManager.class);// FIXME: DeviceManager should register
		this.securityManager = factory.get(SecurityManager.class);
		this.connectionManagerControlCenter = factory.get(ConnectionManagerControlCenter.class);
		this.connectionManagerControlCenter.setListener(this);
				
		MessageHandler messageHandler = new MessageHandler(properties, 
										connectionManagerControlCenter,
										securityManager,
										factory.get(ConnectivityManager.class)
													);
		this.messageHandler = messageHandler;
	}
	
	@Override
	public void start() {}
	
	@Override
	public void stop() {}

	
	//FIXME: remove this method
	public void setDeviceManager(DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
	}
	
	
}
