package org.unbiquitous.uos.core.driverManager;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.application.UOSMessageContext;
import org.unbiquitous.uos.core.application.UosApplication;
import org.unbiquitous.uos.core.connectivity.proxying.ProxyDriver;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall.ServiceType;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

/**
 * Utilitary class responsible for calling services using reflection methods. 
 * 
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class ReflectionServiceCaller {
	
	private static Logger logger = Logger.getLogger(ReflectionServiceCaller.class);
	
	ConnectionManagerControlCenter connectionManagerControlCenter;
	
	public ReflectionServiceCaller(ConnectionManagerControlCenter connectionManagerControlCenter) {
		this.connectionManagerControlCenter = connectionManagerControlCenter;
	}

	/**
	 * Method responsible to call a service on a driver instance object based on reflection methods
	 * 
	 * @param serviceCall Service Call Request Object containing which service to call. 
	 * @param instanceDriver Object of the instance of the driver that must contain the service.
	 * @param messageContext ?
	 * @return The service Response to return to the caller device.
	 * @throws DriverManagerException
	 */
	public ServiceResponse callServiceOnDriver(ServiceCall serviceCall, Object instanceDriver, UOSMessageContext messageContext) throws DriverManagerException{
		if (instanceDriver != null){
			try {
				Method serviceMethod = findMethod(serviceCall, instanceDriver);
				if (serviceMethod != null) {
					logger.info("Calling service ("+ serviceCall.getService()+ ") on Driver (" + serviceCall.getDriver()
								+ ") in instance ("+ serviceCall.getInstanceId() + ")");
					
					handleStreamCall(serviceCall, messageContext);
					ServiceResponse response = new ServiceResponse();
					serviceMethod.invoke(instanceDriver,serviceCall,response,messageContext);
					
					logger.info("Finished service call.");
					return response;
				}else{
					String message = "No Service Implementation found for service "+serviceCall.getService()+" on driver :" +serviceCall.getDriver() +"(@"+serviceCall.getInstanceId()+")";
					logger.error(message);
					throw new DriverManagerException(message);
				}
			} catch (Exception e) {
				logInternalError(serviceCall, e);
				return null;
			} 
		}else{
			logger.error("No Intance Driver Found for this ServiceCall");
			throw new DriverManagerException("No Intance Driver Found for this ServiceCall");
		}
	}

	private void handleStreamCall(ServiceCall serviceCall,
			UOSMessageContext messageContext) throws NetworkException,
			IOException {
		if(serviceCall.getServiceType().equals(ServiceType.STREAM)){
			NetworkDevice networkDevice = messageContext.getCallerDevice();
			
			String host = connectionManagerControlCenter.getHost(networkDevice.getNetworkDeviceName());
			for(int i = 0; i < serviceCall.getChannels(); i++){
				ClientConnection con = connectionManagerControlCenter.openActiveConnection(host+":"+serviceCall.getChannelIDs()[i], serviceCall.getChannelType());
				messageContext.addDataStreams(con.getDataInputStream(), con.getDataOutputStream());
			}
		}
	}

	private Method findMethod(ServiceCall serviceCall, Object instanceDriver) {
		String serviceName = serviceCall.getService();;
		
		if(instanceDriver instanceof ProxyDriver)	serviceName = "forwardServiceCall";
		
		for (Method m : instanceDriver.getClass().getMethods()){
			if (m.getName().equalsIgnoreCase(serviceName))	return m;
		}
		return null;
	}
	
	/**
	 * Utility Method for logging internal failures and encapsulating it into DriverManagerException's.
	 * 
	 * @param serviceCall Corresponding Service Call
	 * @param e Internal Failure Found
	 * @throws DriverManagerException Error Encapsulated
	 */
	private void logInternalError(ServiceCall serviceCall, Exception e) throws DriverManagerException{
		logger.error("Internal Failure", e);
		throw new DriverManagerException("Internal Error calling service ("
				+ serviceCall.getService()
				+ ") on Driver (" + serviceCall.getDriver()
				+ ") in instance ("
				+ serviceCall.getInstanceId() + ")",e);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ServiceResponse callServiceOnApp(UosApplication app,ServiceCall call) {
		ServiceResponse response = new ServiceResponse();
		try {
			Method method = app.getClass().getMethod(call.getService(), Map.class);
			Map responseMap = (Map) method.invoke(app, call.getParameters());
			response.setResponseData(responseMap);
			return response;
		} catch (Exception e) {
			logger.error("Internal Failure", e);
			response.setError("Not possible to make call because "+e.getMessage());
		} 
		return response;
	}
}
