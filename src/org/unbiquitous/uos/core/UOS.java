package org.unbiquitous.uos.core;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import org.unbiquitous.uos.core.adaptabitilyEngine.AdaptabilityEngine;
import org.unbiquitous.uos.core.adaptabitilyEngine.EventManager;
import org.unbiquitous.uos.core.adaptabitilyEngine.Gateway;
import org.unbiquitous.uos.core.adaptabitilyEngine.SmartSpaceGateway;
import org.unbiquitous.uos.core.applicationManager.ApplicationDeployer;
import org.unbiquitous.uos.core.applicationManager.ApplicationManager;
import org.unbiquitous.uos.core.connectivity.ConnectivityManager;
import org.unbiquitous.uos.core.deviceManager.DeviceDao;
import org.unbiquitous.uos.core.deviceManager.DeviceManager;
import org.unbiquitous.uos.core.driverManager.DriverDao;
import org.unbiquitous.uos.core.driverManager.DriverDeployer;
import org.unbiquitous.uos.core.driverManager.DriverManager;
import org.unbiquitous.uos.core.driverManager.DriverManagerException;
import org.unbiquitous.uos.core.driverManager.ReflectionServiceCaller;
import org.unbiquitous.uos.core.messageEngine.MessageEngine;
import org.unbiquitous.uos.core.messageEngine.MessageHandler;
import org.unbiquitous.uos.core.messageEngine.dataType.UpDevice;
import org.unbiquitous.uos.core.messageEngine.dataType.UpNetworkInterface;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerControlCenter;
import org.unbiquitous.uos.core.network.connectionManager.MessageListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.radar.RadarControlCenter;
import org.unbiquitous.uos.core.ontologyEngine.Ontology;
import org.unbiquitous.uos.core.ontologyEngine.exception.ReasonerNotDefinedException;

/**
 * 
 * This class centralizes the process of initialization e teardown of the
 * middleware and it's dependencies.
 * 
 * @author Fabricio Nogueira Buzeto
 * 
 */
public class UOS {

	private static final String DEVICE_NAME_KEY = "ubiquitos.uos.deviceName";

	private static final Logger logger = Logger
			.getLogger(UOS.class);

	private static String DEFAULT_UBIQUIT_BUNDLE_FILE = "ubiquitos";

	private DriverManager driverManager;
	private MessageEngine messageEngine;
	private ConnectionManagerControlCenter connectionManagerControlCenter;
	private RadarControlCenter radarControlCenter;
	private AdaptabilityEngine adaptabilityEngine;
	private SecurityManager securityManager;
	private UpDevice currentDevice;

	private ApplicationDeployer applicationDeployer;
	private DeviceManager deviceManager;
	private SmartSpaceGateway gateway;

	private DriverDao driverDao;
	private DeviceDao deviceDao;
	private ReflectionServiceCaller serviceCaller;
	private EventManager eventManager;
    private Ontology ontology;
    private ResourceBundle resourceBundle;

	private ApplicationManager applicationManager;
        
	
	public static void main(String[] args) throws Exception{
		new UOS().init();
	}
	
	/**
	 * Initializes the components of the uOS middleware using 'ubiquitos' as the
	 * name of the resouce bundle to be used.
	 * 
	 * @throws ContextException
	 */
	public void init() throws ContextException {
		init(DEFAULT_UBIQUIT_BUNDLE_FILE);
	}

	/**
	 * Initializes the components of the uOS middleware acording to the
	 * resourceBundle informed.
	 * 
	 * @param resourceBundleName
	 *            Name of the <code>ResourceBundle</code> to be used for finding
	 *            the properties of the uOS middleware.
	 * @throws ContextException
	 */
	public void init(ResourceBundle resourceBundle) throws ContextException {
		
		try {
			this.resourceBundle = resourceBundle;
			
			// Objects Instantiation
			adaptabilityEngine = new AdaptabilityEngine();
			messageEngine = new MessageEngine();
			
			// Start Security Manager
			logger.debug("Initializing SecurityManager");
			initSecurityManager();

			gateway = new SmartSpaceGateway();

			
			/*---------------------------------------------------------------*/
			
			// Start Connection Manager Control Center
			logger.debug("Initializing ConnectionManagerControlCenter");
			initConnectionManagerControlCenter( messageEngine);
			
			logger.debug("Initializing CurrentDevice");
			initCurrentDevice(connectionManagerControlCenter);
			
			/*---------------------------------------------------------------*/
			
			// Start The Message Listener
			logger.debug("Initializing MessageListener");
			initMessageEngine(adaptabilityEngine, securityManager,
					connectionManagerControlCenter);

			
			/*---------------------------------------------------------------*/
			
			// Start Driver Manager
			logger.debug("Initializing DriverManager");
			initDriverManager(currentDevice);
			
			// Start Service Handler
			logger.debug("Initializing ServiceHandler");
			initAdaptabilityEngine(connectionManagerControlCenter,
					driverManager, currentDevice, messageEngine);

			// Start Device Manager
			logger.debug("Initializing DeviceManager");
			initDeviceManager(currentDevice, adaptabilityEngine,
					connectionManagerControlCenter);
			
			/*---------------------------------------------------------------*/
			
			messageEngine.setDeviceManager(deviceManager);

			/*---------------------------------------------------------------*/
			
            initOntology(deviceManager);
                        
            //FIXME: This is trash
			gateway.init(adaptabilityEngine, currentDevice, securityManager,
					get(ConnectivityManager.class),
					deviceManager, driverManager, applicationDeployer, ontology);

			/*---------------------------------------------------------------*/
			
			// Start Connectivity Manager
			logger.debug("Initializing ConnectivityManager");
			initConnectivityManager(gateway);

			// Start Radar Control Center
			logger.debug("Initializing RadarControlCenter");
			initRadarControlCenter( deviceManager,
					connectionManagerControlCenter);

			// Initialize the deployed Drivers
			driverManager.initDrivers(gateway);

			// Start The Applications within the middleware
			logger.debug("Initializing Applications");
			initApplications(resourceBundle, gateway);
		} catch (DriverManagerException e) {
			logger.error(e);
			throw new ContextException(e);
		} catch (NetworkException e) {
			throw new ContextException(e);
		} catch (SecurityException e) {
			throw new ContextException(e);
		}
	}

	/**
	 * Initializes the components of the uOS middleware acording to the
	 * resourceBundle informed.
	 * 
	 * @param resourceBundleName
	 *            Name of the <code>ResourceBundle</code> to be used for finding
	 *            the properties of the uOS middleware.
	 * @throws ContextException
	 */
	public void init(String resourceBundleName) throws ContextException {
		// Log start Message
		logger.info("..::|| Starting uOS ||::..");

		// Get the resource Bundle
		logger.debug("Retrieving Resource Bundle Information");
		ResourceBundle resourceBundle = ResourceBundle
				.getBundle(resourceBundleName);

		init(resourceBundle);
	}

	private void initConnectionManagerControlCenter( MessageListener messageListener)
			throws NetworkException {
		connectionManagerControlCenter = null;
		try {
			connectionManagerControlCenter = new ConnectionManagerControlCenter(
					messageListener, resourceBundle);
		} catch (NetworkException ex) {
			logger.error(
					"[Starting] Error creating Connection Manager Control Center.",
					ex);
			throw ex;
		}
	}

	private void initRadarControlCenter(DeviceManager deviceManager,
			ConnectionManagerControlCenter connectionManagerControlCenter)
			throws NetworkException {
		radarControlCenter = new RadarControlCenter(deviceManager,
				resourceBundle, connectionManagerControlCenter);
		radarControlCenter.startRadar();
	}

	private void initMessageEngine(AdaptabilityEngine adaptabilityEngine,
			SecurityManager securityManager,
			ConnectionManagerControlCenter connectionManagerControlCenter) {
		MessageHandler messageHandler = new MessageHandler(resourceBundle, connectionManagerControlCenter,securityManager,get(ConnectivityManager.class));
		messageEngine.init(adaptabilityEngine, adaptabilityEngine,
				securityManager, connectionManagerControlCenter, 
				messageHandler);
	}

	private void initDriverManager(UpDevice device) throws DriverManagerException {
		driverManager = new DriverManager(device, getDriverDao(), getDeviceDao(), getServiceCaller());

		// Deploy service-drivers
		DriverDeployer driverDeployer = new DriverDeployer(driverManager,resourceBundle);
		driverDeployer.deployDrivers();

	}

	private void initAdaptabilityEngine(
			ConnectionManagerControlCenter connectionManagerControlCenter,
			DriverManager driverManager, UpDevice currentDevice,
			MessageEngine messageEngine) {

		adaptabilityEngine.init(connectionManagerControlCenter, driverManager,
				currentDevice, this, messageEngine, 
				get(ConnectivityManager.class), getEventManager());

	}

	private void initCurrentDevice(ConnectionManagerControlCenter connectionManagerControlCenter) {

		// Collect device informed name
		currentDevice = new UpDevice();
		if (resourceBundle.containsKey(DEVICE_NAME_KEY)){
			currentDevice.setName(resourceBundle.getString(DEVICE_NAME_KEY));
		}else{
			try {
				currentDevice.setName(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException e) {
				new ContextException(e);
			}
		}

		if (currentDevice.getName().equals("localhost")){
			UUID uuid = UUID.randomUUID();
			currentDevice.setName(uuid.toString());
		}
		
		//get metadata
		currentDevice.addProperty("platform",System.getProperty("java.vm.name"));
		
		// Collect network interface information
		List<NetworkDevice> networkDeviceList = connectionManagerControlCenter.getNetworkDevices();
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		for (NetworkDevice nd : networkDeviceList) {
			UpNetworkInterface nInf = new UpNetworkInterface();
			nInf.setNetType(nd.getNetworkDeviceType());
			nInf.setNetworkAddress(connectionManagerControlCenter.getHost(nd.getNetworkDeviceName()));
			networks.add(nInf);
			logger.info(nd.getNetworkDeviceType() + " > " + nd.getNetworkDeviceName());
		}

		currentDevice.setNetworks(networks);
	}

	private void initSecurityManager() throws SecurityException {
		securityManager = new SecurityManager(resourceBundle);
	}

	private void initDeviceManager(UpDevice currentDevice,
			AdaptabilityEngine adaptabilityEngine,
			ConnectionManagerControlCenter connectionManagerControlCenter) throws SecurityException {
		deviceManager = new DeviceManager(currentDevice, 
								getDeviceDao(),getDriverDao(), 
								getConnectionManagerControlCenter(), 
								get(ConnectivityManager.class), 
								gateway, getDriverManager());
	}

	private void initConnectivityManager(Gateway gateway) {
		//Read proxying attribute from the resource bundle
		boolean doProxying = false;

		try {
			if ((resourceBundle.getString("ubiquitos.connectivity.doProxying")).equalsIgnoreCase("yes")) {
				doProxying = true;
			}
		} catch (MissingResourceException e) {
			logger.info("No proxying attribute found in the properties. Proxying set as false.");
		}

		get(ConnectivityManager.class).init(this, gateway, doProxying);
	}

	private void initApplications(ResourceBundle resourceBundle, Gateway gateway)
			throws ContextException {
		applicationManager = new ApplicationManager(resourceBundle, gateway);
		applicationDeployer = new ApplicationDeployer(resourceBundle,applicationManager);
		applicationDeployer.deployApplications();
		applicationManager.startApplications();
	}

        private void initOntology(DeviceManager deviceManager) {
            try {
            	//TODO: check if this is right
            	if (!resourceBundle.containsKey("ubiquitos.ontology.path")) return;
                ontology = new Ontology(resourceBundle);     
                //ontology.setDriverManager(driverManager);
                ontology.initializeOntology();
            } catch (ReasonerNotDefinedException ex) {
                logger.info(ex);
            }
        }
        
	/**
	 * Shutdown the middleware infrastructure.
	 */
	public void tearDown() {

		// inform the applications about the teardown process
		try {
			applicationManager.tearDown();
		} catch (Exception e) {
			logger.error(e);
		}

		// inform the drivers about the teardown process
		driverManager.tearDown();

		// inform the network layer about the tear down process
		connectionManagerControlCenter.tearDown();

		// stopApplications all radars
		radarControlCenter.stopRadar();

	}

	/**
	 * @return Returns the Driver Manager of this Application Context.
	 */
	public DriverManager getDriverManager() {
		return driverManager;
	}

	/**
	 * @return Returns the ConnectionManagerControlCenter of this Application
	 *         Context.
	 */
	public ConnectionManagerControlCenter getConnectionManagerControlCenter() {
		return connectionManagerControlCenter;
	}

	/**
	 * @return the securityManager
	 */
	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	/**
	 * @return the deviceManager
	 */
	public DeviceManager getDeviceManager() {
		return deviceManager;
	}

	/**
	 * @return The Gateway used by Drivers and Applications to interact with the
	 *         Smart Space
	 */
	public Gateway getGateway() {
		return gateway;
	}

	/**
	 * @return The ApplicationDeployer used to deploy applications dynamically
	 *         into the middleware.
	 */
	public ApplicationManager getApplicationManager() {
		return applicationManager;
	}

	/**
	 * @return The DriverDao used to store all driver info
	 *         into the middleware.
	 */
	public DriverDao getDriverDao() {
		if (driverDao == null) driverDao = new DriverDao(resourceBundle);
		return driverDao;
	}
	
	public RadarControlCenter getRadarControlCenter(){
		return radarControlCenter;
	}
	
	/**
	 * @return The DeviceDao used to store all device info
	 *         into the middleware.
	 */
	public DeviceDao getDeviceDao() {
		if (deviceDao == null) deviceDao = new DeviceDao(resourceBundle);
		return deviceDao;
	}
	
	private ReflectionServiceCaller getServiceCaller(){
		if (serviceCaller == null) serviceCaller = new ReflectionServiceCaller(connectionManagerControlCenter);
		return serviceCaller;
	}
	
	private EventManager getEventManager(){
		if (eventManager == null) eventManager = new EventManager(messageEngine);
		return eventManager;
	}

	public UpDevice device() {
		return currentDevice;
	}
	
	
//	-----------------------------------------------------------------
	
	private Map<Class, Object> instances = new HashMap<Class, Object>();
	
	private <T> T get(Class<T> clazz){
		if (!instances.containsKey(clazz)){
			try {
				instances.put(clazz, clazz.newInstance());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return (T) instances.get(clazz);
	}
}
