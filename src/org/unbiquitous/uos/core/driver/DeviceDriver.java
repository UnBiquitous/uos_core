package org.unbiquitous.uos.core.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.AuthenticationHandler;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.applicationManager.UOSMessageContext;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDriver;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * Driver responsible for providing information about the device.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DeviceDriver implements UosDriver {
	private static Logger logger = Logger.getLogger(DeviceDriver.class);
	
	private static final String DEVICE_KEY = "device";
	private static final String SECURITY_TYPE_KEY = "securityType";
	private static final String DRIVER_LIST_KEY = "driverList";
	private static final String DRIVER_NAME_KEY = "driverName";
	private static final String INTERFACES_KEY = "interfaces";
	private static final String DRIVERS_NAME_KEY = "driversName";

	private Gateway gateway;
	private final UpDriver driver;
	
	public DeviceDriver() {
		driver = new UpDriver(
				"br.unb.unbiquitous.ubiquitos.driver.DeviceDriver");

		// populate listDrivers service
		driver.addService("listDrivers").addParameter(DRIVER_NAME_KEY,
				UpService.ParameterType.OPTIONAL);

		// populate authenticate service
		driver.addService("authenticate").addParameter(SECURITY_TYPE_KEY,
				UpService.ParameterType.MANDATORY);

		// populate goodbye service
		driver.addService("goodbye");

		// populate handshake service
		driver.addService("handshake").addParameter(DEVICE_KEY,
				UpService.ParameterType.MANDATORY);

		// populate tellEquivalentDriver service
		driver.addService("tellEquivalentDriver").addParameter(DRIVER_NAME_KEY, 
				UpService.ParameterType.MANDATORY);
	}

	/**
	 * Service responsible for retrieving the list of Driver Instances present in the underlying device.
	 * This listing service can have its result filtered with the use of the parameters 'serviceName' or 'driverName'.
	 * It responds in a single responseMap within the parameter 'driverList'
	 */
	@SuppressWarnings("unchecked")
	public void listDrivers(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		logger.info("Handling DeviceDriverImpl#listDrivers service");

		List<DriverData> listDrivers = null;
		
		Map<String, Object> parameters = serviceCall.getParameters();
		
		//handle parameters to filter message
		DriverManager driverManager = ((SmartSpaceGateway)this.gateway).getDriverManager();
		if (parameters != null){
			listDrivers = driverManager.listDrivers((String) parameters.get(DRIVER_NAME_KEY),this.gateway.getCurrentDevice().getName());
		}else{
			// In case no parameters informed, list all drivers
			listDrivers = driverManager.listDrivers(null,this.gateway.getCurrentDevice().getName());
		}
		
		//If the current device is doing proxy, filter the list of drivers
		if( ((SmartSpaceGateway)this.gateway).getConnectivityManager().doProxying() ){
			((SmartSpaceGateway)this.gateway).getConnectivityManager().filterDriversList(listDrivers);
		}
		
		// Converts the list of DriverData into Parameters
		
		Map<String, String> driversList = new HashMap<String, String>();
		if (listDrivers != null && !listDrivers.isEmpty()){
			for (DriverData driverData : listDrivers) {
				
				try {
					JSONDriver jsonDriver = new JSONDriver(driverData.getDriver());
					
					driversList.put(driverData.getInstanceID(), jsonDriver.toString());//FIXME : DeviceDriver : the driver should be a JSON and not a String
				} catch (JSONException e) {
					logger.error("Cannot handle Driver with IntanceId : "+driverData.getInstanceID(),e);
				}
			}
		}
		@SuppressWarnings("rawtypes")
		Map responseData = new HashMap();
		
		responseData.put(DRIVER_LIST_KEY, new JSONObject(driversList).toString());
		
		serviceResponse.setResponseData(responseData);
	}
	
	/**
	 * Service responsible for authenticating a device. This Service can be called multiple times for a authentication process
	 * with multiple steps. 
	 * The authentication algorithm is determined by the parameter 'securityType'.
	 */
	public void authenticate(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		
		String securityType = (String) serviceCall.getParameters().get(SECURITY_TYPE_KEY);
		
		// find the Authentication handler responsible for the requested securityType
		AuthenticationHandler ah = ((SmartSpaceGateway) this.gateway).getSecurityManager().getAuthenticationHandler(securityType);
		
		// delegate the authentication process to the AuthenticationHandler responsible
		if (ah != null){
			ah.authenticate(serviceCall, serviceResponse, messageContext);
		}else{
			serviceResponse.setError("No AuthenticationHandler found for the security type : '"+securityType+"' .");
		}
		
	}
	
	/**
	 * This method is responsible for creating a mutual knowledge of two device about it's basic informations.
	 * This information must be informed in the parameter 'device'(<code>UpDevice</code>) by the caller device 
	 * and will be returned in the same parameter with the information of the called device.
	 */
	public void handshake(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext){
		DeviceManager deviceManager = ((SmartSpaceGateway)this.gateway).getDeviceManager();
		
		// Get and Convert the UpDevice Parameter
		String deviceParameter = (String) serviceCall.getParameterString(DEVICE_KEY);
		if (deviceParameter == null){
			serviceResponse.setError("No 'device' parameter informed.");
			return;
		}
		try {
			UpDevice device = new JSONDevice(deviceParameter).getAsObject();
			// TODO : DeviceDriver : validate if the device doing the handshake is the same that is in the parameter
			deviceManager.registerDevice(device);
			serviceResponse
					.addParameter(DEVICE_KEY, 
							new JSONDevice(gateway.getCurrentDevice())
								.toString()
								);
		} catch (Exception e) {
			serviceResponse.setError(e.getMessage());
			logger.error(e);
		} 
	}
	
	/**
	 * This method is responsible for informing that the caller device is leaving the smart-space, so all its data 
	 * must be removed.
	 */
	public void goodbye(ServiceCall serviceCall,ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		((SmartSpaceGateway)gateway).getDeviceManager().deviceLeft(messageContext.getCallerDevice());
	}
	
	/**
	 * This method is responsible for informing the unknown equivalent driverss.
	 */
	public void tellEquivalentDrivers(ServiceCall serviceCall, ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		try {
			
			String equivalentDrivers = (String) serviceCall.getParameter(DRIVERS_NAME_KEY);
			JSONArray equivalentDriversJson = new JSONArray(equivalentDrivers);
			List<JSONDriver> jsonList = new ArrayList<JSONDriver>();
			Map<String,Object> responseData = new HashMap<String, Object>();
			
			for(int i = 0; i < equivalentDriversJson.length(); i++) {
				String equivalentDriver = equivalentDriversJson.getString(i);
				UpDriver driver = ((SmartSpaceGateway)gateway).getDriverManager().getDriverFromEquivalanceTree(equivalentDriver);
				
				if(driver != null) {
					addToEquivalanceList(jsonList, driver);
				}	
			}
			responseData.put(INTERFACES_KEY, new JSONArray(jsonList.toString()).toString());
			serviceResponse.setResponseData(responseData);			
		} catch (JSONException e) {
			logger.error(e);
		}
	}
	
	private void addToEquivalanceList(List<JSONDriver> jsonList, UpDriver upDriver) throws JSONException {
		
		List<String> equivalentDrivers = upDriver.getEquivalentDrivers();
		
		if(equivalentDrivers != null) {
			for (String equivalentDriver : equivalentDrivers) {
				UpDriver driver = ((SmartSpaceGateway)gateway).getDriverManager().getDriverFromEquivalanceTree(equivalentDriver);
				if(driver != null) {
					addToEquivalanceList(jsonList, driver);
				}
			}
		}
		jsonList.add(new JSONDriver(upDriver));
	}

	@Override
	public UpDriver getDriver() {
    	return driver;
	}

	@Override
	public void init(Gateway gateway, String instanceId) {
		this.gateway = gateway;
	}

	@Override
	public void destroy() {}

	@Override
	public List<UpDriver> getParent() {
		return null;
	}

}
