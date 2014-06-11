package org.unbiquitous.uos.core.driverManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.unbiquitous.uos.core.ClassLoaderUtils;
import org.unbiquitous.uos.core.InitialProperties;
import org.unbiquitous.uos.core.UOSLogging;
import org.unbiquitous.uos.core.applicationManager.CallContext;
import org.unbiquitous.uos.core.messageEngine.dataType.UpService;
import org.unbiquitous.uos.core.messageEngine.messages.Call;
import org.unbiquitous.uos.core.messageEngine.messages.Response;

/**
 * Class responsible for loading the specified driver in the Driver Manager.
 * 
 * @author Fabricio Nogueira Buzeto
 *
 */
public class DriverDeployer {
	
	private static Logger logger = UOSLogging.getLogger();
	
    private static final String DRIVER_PATH_RESOURCE_KEY = "ubiquitos.driver.path";

    private static String DRIVER_PATH;

    private static String DEFAULT_DRIVER_PATH = "drivers/";
	
	private DriverManager driverManager;
	
	private InitialProperties properties;
	
	public DriverDeployer(DriverManager driverManager, InitialProperties properties) {
		this.driverManager = driverManager;
		this.properties = properties;
	}
	
	/**
	 * Method responsible to load the Driver from the property DRIVER_LIST_RESOURCE_KEY on the resoruceBundle.
	 * 
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	public void deployDrivers() throws DriverManagerException {
		logger.info("Deploying Drivers.");
		if (driverManager != null  && properties != null){
			try {
				List<InitialProperties.Tuple<Class<UosDriver>, String>> driverList = properties.getDrivers();
				if (driverList == null){
					logger.warning("No Driver defined. This implies on no drivers for this instance.");
					return;
				}
				for(InitialProperties.Tuple<Class<UosDriver>, String> t : driverList){
					deployDriver(t.x.getCanonicalName(), t.y);
				}
			} catch (Exception e) {
				throw new DriverManagerException(e);
			}
		}else{
			logger.fine("No parameters informed to Deployer.");
		}
	}


	/**
	 * 
	 * Method responsible for instantiating and deploying a specific driver into the middleware.
	 * 
	 * @param driverClass Name of the driver <code>Class</code> to be instantiated.
	 * @param instanceId Name of the InstanceID of the Driver do be Deployed (optional).
	 * @throws DriverManagerException
	 * @throws DriverNotFoundException 
	 */
	private void deployDriver(String driverClass, String instanceId)
			throws DriverManagerException, InterfaceValidationException {
		if(properties.containsKey(DRIVER_PATH_RESOURCE_KEY)) {
			DRIVER_PATH = properties.getString(DRIVER_PATH_RESOURCE_KEY);
		} else {
			DRIVER_PATH = DEFAULT_DRIVER_PATH;
		}
		
		try {
			ClassLoader classLoader = ClassLoaderUtils.builder
												.createClassLoader(DRIVER_PATH);
			Class<?> clazz = classLoader.loadClass(driverClass);
			UosDriver driver = (UosDriver) clazz.newInstance();
			try {
				driverManager.deployDriver(driver.getDriver(), driver, instanceId);
			} catch (DriverNotFoundException e) {
				try {
					driverManager.addToEquivalenceTree(driver.getParent());
					driverManager.deployDriver(driver.getDriver(), driver, instanceId);
				} catch (DriverNotFoundException ex) {
					logger.log(Level.SEVERE,"Problems ocurred in the registering of driver '"+driver.getDriver().getName()+
							"' and it will not be registered.", ex);
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE,"Problems Deploying driver",e);
			new DriverManagerException(e);
		} 
	}

	/**
	 * Method responsible for validating the interface of a driver's instance
	 * 
	 * @param driverInstance Driver Instance to be validated 
	 * @return <code>true</code> if the instance is valid. <code>false</code> otherwise.
	 */
	private boolean validateServiceInterface(UosDriver driverInstance) {
		if (driverInstance.getDriver() == null){
			String erroMessage = "DriverClass '"+driverInstance.getClass().getName()+"' does not inform a valid UPDriver instance.";
			logger.fine(erroMessage);
			return false;
		}
		if (driverInstance.getDriver().getServices() == null||
				driverInstance.getDriver().getServices().isEmpty()){
			String erroMessage = "DriverClass '"+driverInstance.getClass().getName()+"' does informs a empty or non-existent list of services.";
			logger.fine(erroMessage);
			return false;
		}
		for (UpService ups : driverInstance.getDriver().getServices()){
			try {
				driverInstance.getClass().getDeclaredMethod(ups.getName(), Call.class , Response.class, CallContext.class);
			} catch (SecurityException e) {
				String erroMessage = "Service '"+ups.getName()+"' on DriverClass '"+driverInstance.getClass().getName()+"' has security acces issues.";
				logger.log(Level.FINE,erroMessage,e);
				return false;
			} catch (NoSuchMethodException e) {
				String erroMessage = "Service '"+ups.getName()+"' on DriverClass '"+driverInstance.getClass().getName()+"' does not exist.";
				logger.log(Level.FINE,erroMessage,e);
				return false;
			}
		}
		return true;
	}
}
