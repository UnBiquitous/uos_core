package org.unbiquitous.uos.core;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
	private RadarControlCenter radarControlCenter;
//	private UpDevice currentDevice;

	private ApplicationDeployer applicationDeployer;
	private DeviceManager deviceManager;

	private ReflectionServiceCaller serviceCaller;
	private EventManager eventManager;
    private Ontology ontology;
    private ResourceBundle properties;

	private ApplicationManager applicationManager;
        
	
	
	private UOSComponentFactory factory;
	private List<UOSComponent> components ;
	
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
	@SuppressWarnings("serial")
	public void init(ResourceBundle resourceBundle) throws ContextException {
		
		try {
			this.properties	= resourceBundle;
			this.factory		= new UOSComponentFactory(resourceBundle);
			this.components = new ArrayList<UOSComponent>(){
				{
					add(factory.get(ConnectionManagerControlCenter.class));
					add(factory.get(MessageEngine.class));
				}
			};
			/*---------------------------------------------------------------*/
			/* 							CREATE								 */
			/*---------------------------------------------------------------*/
			
			//TODO: hack to handle the "currentDevice" creation
			factory.get(UpDevice.class);
			
			for(UOSComponent component:components){
				component.create(properties);
			}
			
			/*---------------------------------------------------------------*/
			/* 							INIT								 */
			/*---------------------------------------------------------------*/
			for(UOSComponent component:components){
				component.init(factory);
			}
			
			/*---------------------------------------------------------------*/
			
			logger.debug("Initializing CurrentDevice");
			initCurrentDevice();
			
			/*---------------------------------------------------------------*/
			
			// Start Service Handler
			logger.debug("Initializing ServiceHandler");
			initAdaptabilityEngine();

			/*---------------------------------------------------------------*/
			
			factory.get(MessageEngine.class).setDeviceManager(deviceManager);

			/*---------------------------------------------------------------*/
			
            initOntology();
                        
            //FIXME: This is trash
            factory.get(SmartSpaceGateway.class)
            	.init(	factory.get(AdaptabilityEngine.class), 
            			factory.get(UpDevice.class), 
            			factory.get(SecurityManager.class),
            			factory.get(ConnectivityManager.class),
            			deviceManager, 
            			driverManager, 
            			applicationDeployer, ontology);

			/*---------------------------------------------------------------*/
			
			// Start Connectivity Manager
			logger.debug("Initializing ConnectivityManager");
			initConnectivityManager();

			// Start Radar Control Center
			logger.debug("Initializing RadarControlCenter");
			initRadarControlCenter();

			// Initialize the deployed Drivers
			driverManager.initDrivers(factory.get(SmartSpaceGateway.class));

			// Start The Applications within the middleware
			logger.debug("Initializing Applications");
			initApplications();
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

	private void initRadarControlCenter()
			throws NetworkException {
		radarControlCenter = new RadarControlCenter(deviceManager,
				properties, factory.get(ConnectionManagerControlCenter.class));
		radarControlCenter.startRadar();
	}

//	private void initMessageEngine() {
//		MessageHandler messageHandler = new MessageHandler(properties, 
//				factory.get(ConnectionManagerControlCenter.class),
//												factory.get(SecurityManager.class),
//												factory.get(ConnectivityManager.class)
//											);
//		factory.get(MessageEngine.class)
//			.init(	factory.get(AdaptabilityEngine.class), 
//					factory.get(AdaptabilityEngine.class),
//					factory.get(SecurityManager.class), 
//					factory.get(ConnectionManagerControlCenter.class), 
//					messageHandler);
//	}

	private void initAdaptabilityEngine() throws DriverManagerException, SecurityException {

		// Start Driver Manager
		logger.debug("Initializing DriverManager");
		driverManager = new DriverManager(	factory.get(UpDevice.class), 
											factory.get(DriverDao.class), 
											factory.get(DeviceDao.class), 
											getServiceCaller());

		// Deploy service-drivers
		DriverDeployer driverDeployer = new DriverDeployer(driverManager,properties);
		driverDeployer.deployDrivers();
		
		// Init Adaptability Engine
		factory.get(AdaptabilityEngine.class)
			.init(	factory.get(ConnectionManagerControlCenter.class), 
					driverManager,
					factory.get(UpDevice.class), this, 
					factory.get(MessageEngine.class), 
					factory.get(ConnectivityManager.class), getEventManager());
		
		// Start Device Manager
		logger.debug("Initializing DeviceManager");
		initDeviceManager();

	}

	private void initCurrentDevice() {

		// Collect device informed name
		UpDevice currentDevice = factory.get(UpDevice.class);
		if (properties.containsKey(DEVICE_NAME_KEY)){
			currentDevice.setName(properties.getString(DEVICE_NAME_KEY));
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
		List<NetworkDevice> networkDeviceList = factory.get(ConnectionManagerControlCenter.class).getNetworkDevices();
		List<UpNetworkInterface> networks = new ArrayList<UpNetworkInterface>();
		for (NetworkDevice nd : networkDeviceList) {
			UpNetworkInterface nInf = new UpNetworkInterface();
			nInf.setNetType(nd.getNetworkDeviceType());
			nInf.setNetworkAddress(factory.get(ConnectionManagerControlCenter.class).getHost(nd.getNetworkDeviceName()));
			networks.add(nInf);
			logger.info(nd.getNetworkDeviceType() + " > " + nd.getNetworkDeviceName());
		}

		currentDevice.setNetworks(networks);
	}

	private void initDeviceManager() throws SecurityException {
		deviceManager = new DeviceManager(factory.get(UpDevice.class), 
								factory.get(DeviceDao.class),  
								factory.get(DriverDao.class), 
								factory.get(ConnectionManagerControlCenter.class), 
								factory.get(ConnectivityManager.class), 
								factory.get(SmartSpaceGateway.class), getDriverManager());
	}

	private void initConnectivityManager() {
		//Read proxying attribute from the resource bundle
		boolean doProxying = false;

		try {
			if ((properties.getString("ubiquitos.connectivity.doProxying")).equalsIgnoreCase("yes")) {
				doProxying = true;
			}
		} catch (MissingResourceException e) {
			logger.info("No proxying attribute found in the properties. Proxying set as false.");
		}

		factory.get(ConnectivityManager.class)
			.init(	this, 
					factory.get(SmartSpaceGateway.class), doProxying);
	}

	private void initApplications()throws ContextException {
		applicationManager = new ApplicationManager(properties, 
				factory.get(SmartSpaceGateway.class));
		applicationDeployer = new ApplicationDeployer(properties,applicationManager);
		applicationDeployer.deployApplications();
		applicationManager.startApplications();
	}

	private void initOntology() {
		try {
			// TODO: check if this is right
			if (!properties.containsKey("ubiquitos.ontology.path"))
				return;
			ontology = new Ontology(properties);
			// ontology.setDriverManager(driverManager);
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
		factory.get(ConnectionManagerControlCenter.class).tearDown();

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
		return factory.get(SmartSpaceGateway.class);
	}

	/**
	 * @return The ApplicationDeployer used to deploy applications dynamically
	 *         into the middleware.
	 */
	public ApplicationManager getApplicationManager() {
		return applicationManager;
	}

	public RadarControlCenter getRadarControlCenter(){
		return radarControlCenter;
	}
	
	private ReflectionServiceCaller getServiceCaller(){
		if (serviceCaller == null) serviceCaller = new ReflectionServiceCaller(factory.get(ConnectionManagerControlCenter.class));
		return serviceCaller;
	}
	
	private EventManager getEventManager(){
		if (eventManager == null) eventManager = new EventManager(factory.get(MessageEngine.class));
		return eventManager;
	}

}
