/**
 * 
 */
package org.unbiquitous.uos.core.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unbiquitous.json.JSONArray;
import org.unbiquitous.json.JSONException;
import org.unbiquitous.json.JSONObject;
import org.unbiquitous.uos.core.Logger;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.application.UOSMessageContext;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService.ParameterType;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.json.JSONDriver;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceCall;
import org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse;

/**
 * Default Implementation of the RegisterDriver.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class RegisterDriverImpl implements RegisterDriver {
	
	private static final Logger logger = Logger.getLogger(RegisterDriver.class);

	private static final String DEVICE_NAME_KEY = "deviceName";
	private static final String DRIVER_NAME_KEY = "driverName";
	private static final String SERVICE_NAME_KEY = "serviceName";
	
	private Gateway gateway;
	
	/** 
	 * @see org.unbiquitous.uos.core.driver.RegisterDriver#listDrivers(org.unbiquitous.uos.core.messageEngine.messages.ServiceCall, org.unbiquitous.uos.core.messageEngine.messages.ServiceResponse, br.unb.unbiquitous.ubiquitos.uos.context.UOSMessageContext)
	 */
	@Override
	public void listDrivers(ServiceCall serviceCall,
			ServiceResponse serviceResponse, UOSMessageContext messageContext) {
		
		
		// Parameters 
		String driverName = null;
		String deviceName = null;
		
		List<DriverData> returnedList = ((SmartSpaceGateway)this.gateway).getDriverManager().listDrivers(driverName, deviceName);
		
		String deviceCaller = null;
		try{
			JSONDevice jsonDevice = new JSONDevice((String) serviceCall.getParameter("device"));
			deviceCaller = (jsonDevice.getAsObject()).getName();
		} catch (JSONException e) {
			logger.error(e);
		}
		
		// Filter proxying drivers
		((SmartSpaceGateway)this.gateway).getConnectivityManager().filterRemoteDriversList(deviceCaller, returnedList);
		
		// convert the result in a JSON representation
		if (returnedList != null && !returnedList.isEmpty()){
			List<JSONObject> listJson = new ArrayList<JSONObject>();
			//each Driver Data must be encapsulated in a json object
			for (DriverData rdd : returnedList) {
				try {
					JSONObject json = new JSONObject();
					
					json.put("driver", new JSONDriver(rdd.getDriver()).toString());
					json.put("device", new JSONDevice(rdd.getDevice()).toString());
					json.put("instanceID", rdd.getInstanceID());
					
					listJson.add(json);
				} catch (JSONException e) {
					logger.error(e);
				}
			}
			//The JSON List must be represented in a JSON Way with the JSON Array 
			JSONArray jsonArray = new JSONArray(listJson);
			Map<String, String> responseData = new HashMap<String, String>();
			responseData.put("driverList", jsonArray.toString());
			serviceResponse.setResponseData(responseData);
		}
		
	}

	/**
	 * @see org.unbiquitous.uos.core.driverManager.UosDriver#getDriver()
	 */
	@Override
	public UpDriver getDriver() {
		
		UpDriver registerDriver = new UpDriver("br.unb.unbiquitous.ubiquitos.uos.driver.RegisterDriver");
		
		registerDriver
			.addService("listDrivers")
				.addParameter(SERVICE_NAME_KEY, ParameterType.OPTIONAL)
				.addParameter(DRIVER_NAME_KEY, ParameterType.OPTIONAL)
				.addParameter(DEVICE_NAME_KEY, ParameterType.OPTIONAL);
		
		return registerDriver;
	}

	/** 
	 * @see org.unbiquitous.uos.core.driverManager.UosDriver#init(br.unb.unbiquitous.ubiquitos.uos.context.UOSApplicationContext)
	 */
	@Override
	public void init(Gateway gateway, String instanceId) {
		this.gateway = gateway;
	}

	/**
	 * @see org.unbiquitous.uos.core.driverManager.UosDriver#destroy()
	 */
	@Override
	public void destroy() {}

	@Override
	public List<UpDriver> getParent() {
		return null;
	}

}
