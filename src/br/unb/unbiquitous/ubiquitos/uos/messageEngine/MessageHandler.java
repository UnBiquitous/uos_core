package br.unb.unbiquitous.ubiquitos.uos.messageEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ResourceBundle;

import br.unb.unbiquitous.json.JSONObject;
import br.unb.unbiquitous.ubiquitos.Logger;
import br.unb.unbiquitous.ubiquitos.network.connectionManager.ConnectionManagerControlCenter;
import br.unb.unbiquitous.ubiquitos.network.model.connection.ClientConnection;
import br.unb.unbiquitous.ubiquitos.uos.adaptabitilyEngine.ServiceCallException;
import br.unb.unbiquitous.ubiquitos.uos.connectivity.ConnectivityManager;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpDevice;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.dataType.UpNetworkInterface;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.EncapsulatedMessage;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.Notify;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.ServiceResponse;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json.JSONEncapsulatedMessage;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json.JSONNotify;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json.JSONServiceCall;
import br.unb.unbiquitous.ubiquitos.uos.messageEngine.messages.json.JSONServiceResponse;
import br.unb.unbiquitous.ubiquitos.uos.security.AuthenticationHandler;
import br.unb.unbiquitous.ubiquitos.uos.security.SecurityManager;

/**
 * Method responsible for translating messages of service call, Service Response and Notify. 
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class MessageHandler {
	
	private static Logger logger = Logger.getLogger(MessageHandler.class);
	
	//TODO: must be parameters (or not even be here)
	private int maxRetries = 30;
	private int waitTime = 100;
	
	/*************************************
	 * ATTRIBUTES
	 *************************************/
	
	private ConnectionManagerControlCenter connectionManagerControlCenter;
	private SecurityManager securityManager;
	private ConnectivityManager connectivityManager;
	
	
	/*************************************
	 * CONSTRUCTORS
	 *************************************/
	
	public MessageHandler(
				ResourceBundle bundle,
				ConnectionManagerControlCenter connectionManagerControlCenter,
				SecurityManager securityManager,
				ConnectivityManager connectivityManager
			){
		this.connectionManagerControlCenter = connectionManagerControlCenter;
		this.securityManager = securityManager;
		this.connectivityManager = connectivityManager;
		if (bundle != null && bundle.containsKey("ubiquitos.message.response.timeout")){
			maxRetries = Integer.parseInt(bundle.getString("ubiquitos.message.response.timeout"));
		}else{
			maxRetries = 30;
		}
		if (bundle != null && bundle.containsKey("ubiquitos.message.response.retry")){
			waitTime = Integer.parseInt(bundle.getString("ubiquitos.message.response.retry"));
		}else{
			waitTime = 100;
		}
	}
	
	/**
	 * Method responsible for calling a service according to the ServiceCall informed.
	 * 
	 * @param deviceName Device providing the service to be called.
	 * @param serviceCall Objetc representig the service call to be placed.
	 * @return Service Response for the called service.
	 * @throws ServiceCallException
	 */
	public ServiceResponse callService(UpDevice device,ServiceCall serviceCall) throws MessageEngineException{
		if (	device == null || serviceCall == null ||
				serviceCall.getDriver() == null || serviceCall.getDriver().isEmpty() ||
				serviceCall.getService() == null || serviceCall.getService().isEmpty()){
			throw new IllegalArgumentException("Either the Device or Service is invalid.");
		}
		
		try {
			JSONObject  jsonCall = new JSONServiceCall(serviceCall);
			if (serviceCall.getSecurityType() != null ){
				return (new JSONServiceResponse(sendEncapsulated(jsonCall.toString(), serviceCall.getSecurityType(), device))).getAsObject();
			}
			String returnedMessage = send(jsonCall.toString(), device,true);
			if (returnedMessage != null)
				return (new JSONServiceResponse(returnedMessage)).getAsObject();
		} catch (Exception e) {
			throw new MessageEngineException(e);
		} 
		return null;
	}

	private String sendEncapsulated(String message, String securityType, UpDevice target) throws Exception{
		logger.debug("Authentication needed for type : '"+securityType+"'");
		
		AuthenticationHandler ah = securityManager.getAuthenticationHandler(securityType);
		if (ah == null)
			throw new MessageEngineException("No AuthenticationHandler found for the specified security type.");
		TranslationHandler tHandler = securityManager.getTranslationHandler(securityType);
		if (tHandler == null)
			throw new MessageEngineException("No TranlationHandler found for the specified security type.");
			
		ah.authenticate(target, this);
		
		logger.debug("Proceed to encode original message");

		message = tHandler.encode(message, target.getName());
		message = new JSONEncapsulatedMessage(new EncapsulatedMessage(securityType,message)).toString();
		message = send(message, target,true);
			
		return tHandler.decode(new JSONEncapsulatedMessage(message).getAsObject().getInnerMessage(), target.getName());
	}
	
	/**
	 * Sends a notify message to the device informed.
	 * 
	 * @param notify Notify message to be sent.
	 * @param device Device which is going to receive the notify event
	 * @throws MessageEngineException
	 */
	public void notifyEvent(Notify notify, UpDevice device) throws MessageEngineException{
		if (	device == null || notify == null ||
				notify.getDriver() == null || notify.getDriver().isEmpty() ||
				notify.getEventKey() == null || notify.getEventKey().isEmpty()){
			throw new IllegalArgumentException("Either the Device or Service is invalid.");
		}
		try {
			String message = new JSONNotify(notify).toString();
			send(message, device,false);
		} catch (Exception e) {
			throw new MessageEngineException(e);
		} 
		
	}
	
	private String send(String message, UpDevice target, boolean waitForResponse) throws Exception{
		UpNetworkInterface netInt = connectivityManager.getAppropriateInterface(target);
		ClientConnection connection = connectionManagerControlCenter.openActiveConnection(netInt.getNetworkAddress(), netInt.getNetType());
					
		OutputStream outputStream = connection.getDataOutputStream();
		InputStream inputStream = connection.getDataInputStream();
		
		String response = sendReceive(message, outputStream,inputStream,waitForResponse);
		
		connection.closeConnection();
		
		if (!waitForResponse || response.isEmpty()){
			return null;
		}
		return response;
	}
	
	/**
	 * Method responsible for handling the sending of a request and the receiving of its response
	 * 
	 * @param jsonCall JSON Object of the service call to be sent
	 * @param outputStream OutputStream Object to write into 
	 * @param inputStream InputStream Object to read from
	 * @return String of the response read
	 * @throws IOException
	 * @throws InterruptedException
	 */
	//FIXME: This is NetworkLayer work
	private String sendReceive(String call,OutputStream outputStream, InputStream inputStream, boolean waitForResponse)
			throws IOException, InterruptedException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		
		writer.write(call);
		writer.write('\n');
		writer.flush();
		
		if (waitForResponse){
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			
			StringBuilder builder = new StringBuilder();
			for(int i = 0; i < maxRetries; i++){
				if (reader.ready()){
					for(Character c = (char)reader.read();c != '\n';c = (char)reader.read()){
						builder.append(c);
					}
					break;
				}
				Thread.sleep(waitTime);
			}
			
			logger.debug("Received message : " + builder);
			return builder.toString();
		}
		return null;
	}
}
