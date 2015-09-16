package org.unbiquitous.uos.core.driver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.AuthenticationHandler;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.NotifyException;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverData;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.driverManager.DriverModel;
import org.unbiquitous.uos.core.driverManager.UosDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDriver;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Notify;
import org.unbiquitous.uos.core.messageEngine.messages.Response;
import org.unbiquitous.uos.core.network.model.NetworkDevice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Driver responsible for providing information about the device.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DeviceDriver implements UosDriver {
	private static Logger logger = UOSLogging.getLogger();
	private static final ObjectMapper mapper = new ObjectMapper();

	public static final String DEVICE_ENTERED_EVENT_KEY = "deviceEntered";
	public static final String DEVICE_LEFT_EVENT_KEY = "deviceLeft";
	
	private static final String DEVICE_KEY = "device";
	private static final String SECURITY_TYPE_KEY = "securityType";
	private static final String DRIVER_LIST_KEY = "driverList";
	private static final String DRIVER_NAME_KEY = "driverName";
	private static final String INTERFACES_KEY = "interfaces";
	private static final String DRIVERS_NAME_KEY = "driversName";

	private Gateway gateway;
	private final UpDriver driver;
	private Map<String, UpDevice> listeners;

	public DeviceDriver() {
		driver = new UpDriver("uos.DeviceDriver");

		driver.addService("listDrivers").addParameter(DRIVER_NAME_KEY, UpService.ParameterType.OPTIONAL);

		driver.addService("authenticate").addParameter(SECURITY_TYPE_KEY, UpService.ParameterType.MANDATORY);

		driver.addService("goodbye");

		driver.addService("handshake").addParameter(DEVICE_KEY, UpService.ParameterType.MANDATORY);

		driver.addService("tellEquivalentDriver").addParameter(DRIVER_NAME_KEY, UpService.ParameterType.MANDATORY);
		
		listeners = new HashMap<String, UpDevice>();
	}

	@Override
	public UpDriver getDriver() {
		return driver;
	}

	@Override
	public void init(Gateway gateway, InitialProperties properties, String instanceId) {
		this.gateway = gateway;
	}

	@Override
	public void destroy() {
	}

	@Override
	public List<UpDriver> getParent() {
		return null;
	}

	/**
	 * Service responsible for retrieving the list of Driver Instances present
	 * in the underlying device. This listing service can have its result
	 * filtered with the use of the parameters 'serviceName' or 'driverName'. It
	 * responds in a single responseMap within the parameter 'driverList'
	 */
	@SuppressWarnings("unchecked")
	public void listDrivers(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		logger.info("Handling DeviceDriverImpl#listDrivers service");

		List<DriverData> listDrivers = null;

		Map<String, Object> parameters = serviceCall.getParameters();

		// handle parameters to filter message
		DriverManager driverManager = ((SmartSpaceGateway) this.gateway).getDriverManager();
		if (parameters != null) {
			listDrivers = driverManager.listDrivers((String) parameters.get(DRIVER_NAME_KEY),
					this.gateway.getCurrentDevice().getName());
		} else {
			// In case no parameters informed, list all drivers
			listDrivers = driverManager.listDrivers(null, this.gateway.getCurrentDevice().getName());
		}

		// If the current device is doing proxy, filter the list of drivers
		if (((SmartSpaceGateway) this.gateway).getConnectivityManager().doProxying()) {
			((SmartSpaceGateway) this.gateway).getConnectivityManager().filterDriversList(listDrivers);
		}

		// Converts the list of DriverData into Parameters
		ObjectNode driversList = mapper.getNodeFactory().objectNode();
		if (listDrivers != null && !listDrivers.isEmpty()) {
			for (DriverData driverData : listDrivers)
				driversList.set(driverData.getInstanceID(), mapper.valueToTree(driverData.getDriver()));
		}
		@SuppressWarnings("rawtypes")
		Map responseData = new HashMap();

		responseData.put(DRIVER_LIST_KEY, driversList);

		serviceResponse.setResponseData(responseData);
	}

	/**
	 * Service responsible for authenticating a device. This Service can be
	 * called multiple times for a authentication process with multiple steps.
	 * The authentication algorithm is determined by the parameter
	 * 'securityType'.
	 */
	public void authenticate(Call serviceCall, Response serviceResponse, CallContext messageContext) {

		String securityType = (String) serviceCall.getParameters().get(SECURITY_TYPE_KEY);

		// find the Authentication handler responsible for the requested
		// securityType
		AuthenticationHandler ah = ((SmartSpaceGateway) this.gateway).getSecurityManager()
				.getAuthenticationHandler(securityType);

		// delegate the authentication process to the AuthenticationHandler
		// responsible
		if (ah != null) {
			ah.authenticate(serviceCall, serviceResponse, messageContext);
		} else {
			serviceResponse.setError("No AuthenticationHandler found for the security type : '" + securityType + "' .");
		}

	}

	/**
	 * This method is responsible for creating a mutual knowledge of two device
	 * about it's basic informations. This information must be informed in the
	 * parameter 'device'(<code>UpDevice</code>) by the caller device and will
	 * be returned in the same parameter with the information of the called
	 * device.
	 */
	@SuppressWarnings("unchecked")
	public void handshake(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		SmartSpaceGateway gtw = (SmartSpaceGateway) this.gateway;
		DeviceManager deviceManager = gtw.getDeviceManager();

		// Get and Convert the UpDevice Parameter
		String deviceParameter = (String) serviceCall.getParameterString(DEVICE_KEY);
		if (deviceParameter == null) {
			serviceResponse.setError("No 'device' parameter informed.");
			return;
		}
		try {
			UpDevice device = mapper.readValue(deviceParameter, UpDevice.class);
			// TODO : DeviceDriver : validate if the device doing the handshake
			// is the same that is in the parameter
			deviceManager.registerDevice(device);
			serviceResponse.addParameter(DEVICE_KEY, mapper.valueToTree(gateway.getCurrentDevice()));
			Response driversResponse = gateway.callService(device, new Call("uos.DeviceDriver", "listDrivers"));
			Object driverList = driversResponse.getResponseData("driverList");
			if (driverList != null) {
				Map<String, Object> driverMap;
				if(driverList instanceof Map){
					driverMap = (Map<String,Object>)driverList;
				}else{
					driverMap = mapper.readValue(driverList.toString(), Map.class);
				}
				// TODO: this is duplicated with
				// DeviceManager.registerRemoteDriverInstances
				for (String id : driverMap.keySet()) {
					UpDriver upDriver = mapper.readValue(mapper.writeValueAsString(driverMap.get(id)), UpDriver.class);
					DriverModel driverModel = new DriverModel(id, upDriver, device.getName());
					gtw.getDriverManager().insert(driverModel);
				}
			}
		} catch (Exception e) {
			serviceResponse.setError(e.getMessage());
			logger.log(Level.SEVERE, "Problems on handshake", e);
		}
	}

	/**
	 * This method is responsible for informing that the caller device is
	 * leaving the smart-space, so all its data must be removed.
	 */
	public void goodbye(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		((SmartSpaceGateway) gateway).getDeviceManager().deviceLeft(messageContext.getCallerNetworkDevice());
	}

	/**
	 * This method is responsible for informing the unknown equivalent driverss.
	 */
	public void tellEquivalentDrivers(Call serviceCall, Response serviceResponse, CallContext messageContext) {
		try {

			String equivalentDrivers = (String) serviceCall.getParameter(DRIVERS_NAME_KEY);
			ArrayNode equivalentDriversJson = (ArrayNode) mapper.readTree(equivalentDrivers);
			ArrayNode jsonList = mapper.getNodeFactory().arrayNode();
			Map<String, Object> responseData = new HashMap<String, Object>();
			for (int i = 0; i < equivalentDriversJson.size(); ++i) {
				String equivalentDriver = equivalentDriversJson.get(i).asText();
				UpDriver driver = ((SmartSpaceGateway) gateway).getDriverManager()
						.getDriverFromEquivalanceTree(equivalentDriver);
				if (driver != null) {
					addToEquivalanceList(jsonList, driver);
				}
			}
			responseData.put(INTERFACES_KEY, jsonList.toString());
			serviceResponse.setResponseData(responseData);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problems on equivalent drivers.", e);
		}
	}

	private void addToEquivalanceList(ArrayNode jsonList, UpDriver upDriver) {
		List<String> equivalentDrivers = upDriver.getEquivalentDrivers();
		if (equivalentDrivers != null) {
			for (String equivalentDriver : equivalentDrivers) {
				UpDriver driver = ((SmartSpaceGateway) gateway).getDriverManager()
						.getDriverFromEquivalanceTree(equivalentDriver);
				if (driver != null) {
					addToEquivalanceList(jsonList, driver);
				}
			}
		}
		jsonList.add(mapper.valueToTree(upDriver));
	}
	
	public void registerListener(Call call, Response response, CallContext context) {
		try {
			UpNetworkInterface uni = getNetworkInterface(context);

			String eventKey = call.getParameterString("eventKey");
			List<String> keys = new ArrayList<String>();
			if ((eventKey == null) || (eventKey.equalsIgnoreCase(DEVICE_ENTERED_EVENT_KEY)))
				keys.add(DEVICE_ENTERED_EVENT_KEY);
			if ((eventKey == null) || (eventKey.equalsIgnoreCase(DEVICE_LEFT_EVENT_KEY)))
				keys.add(DEVICE_LEFT_EVENT_KEY);

			if (keys.isEmpty())
				response.setError("no valid event key provided for registration");
			else {
				for (String key : keys) {
					String id = uni.toString() + "@" + key;
					if (!listeners.containsKey(id))
						listeners.put(id, context.getCallerDevice());
				}
			}
		} catch (Exception e) {
			response.setError(e.getMessage());
			logger.log(Level.SEVERE, "Problems while registering for DeviceDriver event", e);
		}
	}

	public void unregisterListener(Call call, Response response, CallContext context) {
		try {
			UpNetworkInterface uni = getNetworkInterface(context);

			String eventKey = call.getParameterString("eventKey");
			List<String> keys = new ArrayList<String>();
			if ((eventKey == null) || (eventKey.equalsIgnoreCase(DEVICE_ENTERED_EVENT_KEY)))
				keys.add(DEVICE_ENTERED_EVENT_KEY);
			if ((eventKey == null) || (eventKey.equalsIgnoreCase(DEVICE_LEFT_EVENT_KEY)))
				keys.add(DEVICE_LEFT_EVENT_KEY);

			if (keys.isEmpty())
				response.setError("no valid event key provided for deregistration");
			else {
				for (String key : keys) {
					String id = uni.toString() + "@" + key;
					listeners.remove(id);
				}
			}
		} catch (Exception e) {
			response.setError(e.getMessage());
			logger.log(Level.SEVERE, "Problems while unregistering for DeviceDriver event", e);
		}
	}

	private static UpNetworkInterface getNetworkInterface(CallContext context) {
		NetworkDevice networkDevice = context.getCallerNetworkDevice();
		String host = networkDevice.getNetworkDeviceName().split(":")[1];
		return new UpNetworkInterface(networkDevice.getNetworkDeviceType(), host);
	}

	private void doNotify(String eventKey, UpDevice device) throws NotifyException {
		Notify n = new Notify(eventKey, driver.getName(), null);
		n.addParameter(DEVICE_KEY, device.toJSON().toString());
		
		for (Entry<String, UpDevice> entry : listeners.entrySet())
			if (entry.getKey().endsWith(eventKey))
				gateway.notify(n, entry.getValue());
	}
}
